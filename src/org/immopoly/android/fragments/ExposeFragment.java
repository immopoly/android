package org.immopoly.android.fragments;

import org.immopoly.android.R;
import org.immopoly.android.app.UserDataManager;
import org.immopoly.android.constants.Const;
import org.immopoly.android.helper.Settings;
import org.immopoly.android.helper.TrackingManager;
import org.immopoly.android.model.Flat;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class ExposeFragment extends DialogFragment {

	private WebView mWebView;

	private GoogleAnalyticsTracker mTracker;
	private Flat flat = null;

	private final static String sInjectJString;
	private Button takeOrReleaseButton;
	static {
		StringBuilder jsInjectString;
		jsInjectString = new StringBuilder(
				"var headID = document.getElementsByTagName('head')[0];");
		jsInjectString.append("var cssNode = document.createElement('style');")
				.append("cssNode.innerHTML = '#layer{display:none;}div.header_blue{display:none;}';")
				.append("headID.appendChild(cssNode);");
		sInjectJString = "javascript:" + jsInjectString.toString() + ";";
	}

	/**
	 * Create a new instance of MyFragment that will be initialized with the
	 * given arguments.
	 */
	public static ExposeFragment newInstance(Flat flat) {
		ExposeFragment f = new ExposeFragment();
		Bundle b = new Bundle();
		b.putParcelable( "flat", flat );
		f.setArguments( b );
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		flat = getArguments().getParcelable( "flat" );
		// fullscreen dialog with no title
		setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme);
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.expose_detail_web_view,
				container, false);
		mTracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker in manual dispatch mode...
		mTracker.startNewSession(TrackingManager.UA_ACCOUNT,
				Const.ANALYTICS_INTERVAL, getActivity().getApplicationContext());

		mTracker.trackPageView(TrackingManager.VIEW_EXPOSE);

		final ImageButton shareButton = (ImageButton) view
				.findViewById(R.id.shareExpose);
		takeOrReleaseButton = (Button) view
				.findViewById(R.id.TakeOrReleaseButton);
		takeOrReleaseButton.setEnabled(false);
		takeOrReleaseButton.setText(R.string.still_loading);
		
		mWebView = (WebView) view.findViewById(R.id.exposeWevView);
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setJavaScriptEnabled(true);
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				// String urlstring = view.getUrl();
				if (url.matches(".+?\\/suche\\/$")) {
					mTracker.trackEvent(
							TrackingManager.CATEGORY_CLICKS,
							TrackingManager.ACTION_EXPOSE,
							TrackingManager.LABEL_BACK_INSIDE_IMMOSCOUT_WEBSITE,
							0);
				}
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				// inject css from url
				view.loadUrl(sInjectJString);
				if (url.matches(".+?\\/bilder\\.htm$")) {
					// match image details
					mTracker.trackPageView(TrackingManager.ACTION_EXPOSE + "/"
							+ TrackingManager.LABEL_IMAGES);
				}
				if (url.matches(".+?\\/bilder\\.htm#bigpicture$")) {
					// navigate in details images
					mTracker.trackEvent(TrackingManager.CATEGORY_CLICKS,
							TrackingManager.ACTION_VIEW,
							TrackingManager.LABEL_IMAGES_DETAILS, 0);
				}
				
				
				getView().findViewById(R.id.progress).setVisibility(
						View.GONE);
				takeOrReleaseButton.setEnabled(true);
				if (null != flat && flat.owned) {
					takeOrReleaseButton.setText(getString(R.string.release_expose));
				} else {
					takeOrReleaseButton.setText(getString(R.string.try_takeover));
				}
				
			}

			@Override
			public void onLoadResource(WebView view, String url) {
				super.onLoadResource(view, url);
				getView().findViewById(R.id.progress).setVisibility(
						View.VISIBLE);
			}

		});
		loadPage(getArguments());

		if (null != shareButton) {
			shareButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					Settings.getFlatLink(Integer.toString(flat.uid), false);
					Settings.shareMessage(getActivity(), getActivity()
							.getString(R.string.share), flat.name, Settings
							.getFlatLink(Integer.toString(flat.uid), false) /* LINk */);
					mTracker.trackEvent(TrackingManager.CATEGORY_SHAREBUTTON,
							TrackingManager.ACTION_SHARE,
							TrackingManager.LABEL_POSITIVE, 0);
				}
			});
		}

		if (null != flat && flat.owned) {
			takeOrReleaseButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					releaseCurrentExpose(v);
				}
			});
		} else {
			takeOrReleaseButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					addCurrentExpose(v);
				}
			});
		}

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		
	}

	void loadPage(Bundle intent) {
		if (null == flat)
			return;
		String url = Settings.getFlatLink(String.valueOf(flat.uid), true);
		mWebView.loadUrl(url);
	}

	public void addCurrentExpose(View v) {
		if (null == flat)
			return;
		if (!flat.owned) {
			mTracker.trackEvent(TrackingManager.CATEGORY_CLICKS,
					TrackingManager.ACTION_EXPOSE, TrackingManager.LABEL_TRY, 0);
		}
		UserDataManager.instance.addToPortfolio(flat);
		dismiss();
	}

	public void releaseCurrentExpose(View v) {
		if (null == flat)
			return;
		// assuming user is logged in - there shouldn't be any "release flat"
		// button otherwise
		if (flat.owned) { // TODO if that isn't true, why are we here anyway
			mTracker.trackEvent(TrackingManager.CATEGORY_CLICKS,
					TrackingManager.ACTION_EXPOSE,
					TrackingManager.LABEL_RELEASE, 0);
		}
		UserDataManager.instance.releaseFromPortfolio(flat);
		dismiss();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mTracker.stopSession();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mWebView.destroy();
		mWebView = null;
	}
}
