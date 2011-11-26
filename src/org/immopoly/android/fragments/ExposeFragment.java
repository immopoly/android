package org.immopoly.android.fragments;

import org.immopoly.android.R;
import org.immopoly.android.app.UserDataManager;
import org.immopoly.android.constants.Const;
import org.immopoly.android.helper.Settings;
import org.immopoly.android.helper.TrackingManager;
import org.immopoly.android.model.Flat;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class ExposeFragment extends DialogFragment {

	private Boolean mLoadTwice = false;
	private WebView mWebView;

	private GoogleAnalyticsTracker tracker;
	private Flat flat;

	private final static String sInjectJString;
	
	static {
		StringBuilder jsInjectString;
		jsInjectString = new StringBuilder("var headID = document.getElementsByTagName('head')[0];");
		jsInjectString.append("var cssNode = document.createElement('style');")
				.append("cssNode.innerHTML = '#layer{display:none;}';").append("headID.appendChild(cssNode);");
		sInjectJString = "javascript:" + jsInjectString.toString() + ";";
	}

	/**
	 * Create a new instance of MyFragment that will be initialized with the
	 * given arguments.
	 */
	public static ExposeFragment newInstance( Flat flat ) {
		ExposeFragment f = new ExposeFragment();
		f.setFlat( flat );
		return f;
	}

	/**
	 * sets the Flat to show the exposee for
	 * @param flat the flat
	 */
	public void setFlat( Flat flat ) {
		this.flat = flat;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// fullscreen dialog with no title
		setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.expose_detail_web_view, container, false);
		tracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker in manual dispatch mode...
		tracker.startNewSession(TrackingManager.UA_ACCOUNT, Const.ANALYTICS_INTERVAL, getActivity()
				.getApplicationContext());

		tracker.trackPageView(TrackingManager.VIEW_EXPOSE);

		final Button takeOrReleaseButton = (Button) view.findViewById(R.id.TakeOrReleaseButton);
		// wait for activating the button
		buttonWait(takeOrReleaseButton);

		mWebView = (WebView) view.findViewById(R.id.exposeWevView);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				// check url

			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				// inject css from url
				view.loadUrl(sInjectJString);
				if (url.matches(".+?\\/bilder\\.htm$")) {
					// match image details
					tracker.trackPageView(TrackingManager.ACTION_EXPOSE + "/" + TrackingManager.LABEL_IMAGES);
				}
				if (url.matches(".+?\\/bilder\\.htm#bigpicture$")) {
					// navigate in details images
					tracker.trackEvent(TrackingManager.CATEGORY_CLICKS, TrackingManager.ACTION_VIEW,
							TrackingManager.LABEL_IMAGES_DETAILS, 0);
				}
				if (mLoadTwice) {
					mWebView.loadUrl(url);
					mLoadTwice = false;
				} else {
					getView().findViewById(R.id.progress).setVisibility(View.GONE);
				}
			}

			@Override
			public void onLoadResource(WebView view, String url) {
				super.onLoadResource(view, url);
				getView().findViewById(R.id.progress).setVisibility(View.VISIBLE);
			}

		});
		loadPage(getArguments());
		
		if ( flat.owned ) {
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

	private void buttonWait(final Button takeOrReleaseButton) {
		Handler buttonDelayFinishedHandler = new Handler() {
			public void handleMessage(Message msg) {
				takeOrReleaseButton.setEnabled(true);
				if (flat.owned)
					takeOrReleaseButton.setText(getString(R.string.release_expose));
				else
					takeOrReleaseButton.setText(getString(R.string.try_takeover));
			}
		};
		buttonDelayFinishedHandler.sendMessageDelayed(new Message(), 10000);
	}

	void loadPage(Bundle intent) {
		String url = Settings.getFlatLink( String.valueOf( flat.uid ), true);
		mWebView.loadUrl(url);
	}

	public void addCurrentExpose(View v) {
		if (! flat.owned) {
			tracker.trackEvent(TrackingManager.CATEGORY_CLICKS, TrackingManager.ACTION_EXPOSE,
					TrackingManager.LABEL_TRY, 0);
		}
		UserDataManager.instance.addToPortfolio( flat );
		dismiss();
	}

	public void releaseCurrentExpose(View v) {
		// assuming user is logged in - there shouldn't be any "release flat" button otherwise  
		if (flat.owned) {  // TODO if that isn't true, why are we here anyway
			tracker.trackEvent(TrackingManager.CATEGORY_CLICKS, TrackingManager.ACTION_EXPOSE, TrackingManager.LABEL_RELEASE, 0);
		}
		UserDataManager.instance.releaseFromPortfolio( flat );
		dismiss();
	}

	public void shareExpose(View v) {
		Settings.shareMessage(getActivity(), getString(R.string.link_share_flat), flat.name,
				Settings.getFlatLink( String.valueOf( flat.uid ), false));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mWebView.destroy();
		mWebView = null;
		tracker.stopSession();
	}
}

