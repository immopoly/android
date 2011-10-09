package org.immopoly.android.fragments;

import org.immopoly.android.R;
import org.immopoly.android.app.ImmopolyActivity;
import org.immopoly.android.app.UserSignupActivity;
import org.immopoly.android.constants.Const;
import org.immopoly.android.helper.Settings;
import org.immopoly.android.helper.TrackingManager;
import org.immopoly.android.model.ImmopolyUser;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class ExposeFragment extends Fragment {
	private String exposeID;
	private String exposeName;
	private String exposeDescription;

	private String exposeURL;
	private Boolean mLoadTwice = false;
	private WebView webView;
	private boolean owned = false;
	private View mView;

	public interface OnExposeClickedListener {
		public void onExposeClick(String exposeID);

		public void onShareClick(int exposeID, boolean isInPortfolio);

	}

	private GoogleAnalyticsTracker tracker;
	private OnExposeClickedListener mOnExposeClickedListener;

	private final static String sInjectJString;
	static {
		StringBuilder jsInjectString;
		jsInjectString = new StringBuilder(
				"var headID = document.getElementsByTagName('head')[0];");
		jsInjectString.append("var cssNode = document.createElement('style');")
				.append("cssNode.innerHTML = '.layer{display:none;}';")
				.append("headID.appendChild(cssNode);");
		sInjectJString = "javascript:" + jsInjectString.toString() + ";";
	}

	@Override
	public void onAttach(Activity arg0) {
		super.onAttach(arg0);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			mOnExposeClickedListener = (OnExposeClickedListener) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(getActivity().toString()
					+ " must implement OnMapItemClickedListener");
		}
	}

	public View onCreateView(LayoutInflater arg0, ViewGroup arg1, Bundle arg2) {
		mView = arg0.inflate(R.layout.expose_detail_web_view, arg1, false);
		tracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker in manual dispatch mode...
		tracker.startNewSession(TrackingManager.UA_ACCOUNT,
				Const.ANALYTICS_INTERVAL, getActivity().getApplicationContext());

		tracker.trackPageView(TrackingManager.VIEW_EXPOSE);

		Button addExpose = (Button) mView.findViewById(R.id.BackButton);
		addExpose.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				addCurrentExpose(v);
			}
		});
		webView = (WebView) mView.findViewById(R.id.exposeWevView);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewClient() {
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
					tracker.trackPageView(TrackingManager.ACTION_EXPOSE + "/"
							+ TrackingManager.LABEL_IMAGES);
				}
				if (url.matches(".+?\\/bilder\\.htm#bigpicture$")) {
					// navigate in details images
					tracker.trackEvent(TrackingManager.CATEGORY_CLICKS,
							TrackingManager.ACTION_VIEW,
							TrackingManager.LABEL_IMAGES_DETAILS, 0);
				}
				if (mLoadTwice) {
					webView.loadUrl(url);
					mLoadTwice = false;
				} else {
					mView.findViewById(R.id.progress).setVisibility(View.GONE);
				}
			}

			@Override
			public void onLoadResource(WebView view, String url) {
				super.onLoadResource(view, url);
				mView.findViewById(R.id.progress).setVisibility(View.VISIBLE);
			}

		});
		loadPage(getArguments());
		return mView;
	}

	void loadPage(Bundle intent) {
		if (intent != null && intent.containsKey(Const.EXPOSE_ID)) {
			exposeID = intent.getString(Const.EXPOSE_ID);

			tracker.setCustomVar(1, Const.SOURCE,
					intent.getString(Const.SOURCE), 1);

			if (intent.getBoolean(Const.EXPOSE_IN_PORTOFOLIO, false)) {
				((Button) mView.findViewById(R.id.BackButton))
						.setText(getString(R.string.webview_back_button));
				owned = true;
			}
			exposeName = intent.getString(Const.EXPOSE_NAME);
			exposeDescription = intent.getString(Const.EXPOSE_DESC);
			exposeURL = intent.getString(Const.EXPOSE_URL);
			String url = Settings.getFlatLink(exposeID, true);

			SharedPreferences shared = getActivity().getSharedPreferences(
					Const.SHARED_PREF_EXPOSE_WEBVIEW, 0);
			String visited = shared.getString(Const.KEY_VISITED, "");
			if (visited.length() == 0) {
				mLoadTwice = true;
				SharedPreferences.Editor editor = shared.edit();
				editor.putString(Const.KEY_VISITED, "true");
				editor.commit();
			}
			webView.loadUrl(url);
		}
	}

	public void addCurrentExpose(View v) {
		Intent intent;
		if (ImmopolyUser.getInstance().readToken(getActivity()).length() > 0) {
			intent = new Intent();
			intent.putExtra(Const.EXPOSE_ADD_PORTIFOLIO, true);
			intent.putExtra(Const.EXPOSE_ID, exposeID);
			intent.putExtra(Const.EXPOSE_NAME, exposeName);
			intent.putExtra(Const.EXPOSE_DESC, exposeDescription);
			intent.putExtra(Const.EXPOSE_URL, exposeURL);
			intent.setAction("add_expose");
			if (!owned) {
				// setResult(Activity.RESULT_OK, i);
				tracker.trackEvent(TrackingManager.CATEGORY_CLICKS,
						TrackingManager.ACTION_EXPOSE,
						TrackingManager.LABEL_TRY, 0);
			} else {
				// setResult(RESULT_CANCELED, i);
			}
			mOnExposeClickedListener.onExposeClick(exposeID);
			// finish();
		} else {
			mOnExposeClickedListener.onExposeClick(null);
			intent = new Intent(getActivity(), UserSignupActivity.class);
		}

	}

	public void shareExpose(View v) {
		Settings.shareMessage(getActivity(),
				getString(R.string.link_share_flat), exposeName,
				Settings.getFlatLink(exposeID, false));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		tracker.stopSession();
	}
}
