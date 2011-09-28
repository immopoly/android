package org.immopoly.android.fragments;

import org.immopoly.android.R;
import org.immopoly.android.app.UserSignupActivity;
import org.immopoly.android.constants.Const;
import org.immopoly.android.helper.Settings;
import org.immopoly.android.helper.TrackingManager;
import org.immopoly.android.model.ImmopolyUser;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			loadPage(intent);
		}

	};

	private GoogleAnalyticsTracker tracker;

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
		// TODO Auto-generated method stub
		super.onAttach(arg0);
		IntentFilter filter = new IntentFilter();
		filter.addAction("expose_view");
		arg0.registerReceiver(mReceiver, filter);

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	public View onCreateView(LayoutInflater arg0, ViewGroup arg1, Bundle arg2) {
		mView = arg0.inflate(R.layout.expose_detail_web_view, arg1);
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
					// findViewById(R.id.progress).setVisibility(View.GONE);
				}
			}

			@Override
			public void onLoadResource(WebView view, String url) {
				super.onLoadResource(view, url);
				// findViewById(R.id.progress).setVisibility(View.VISIBLE);
			}

		});
		loadPage(getActivity().getIntent());
		return mView;
	}

	void loadPage(Intent intent) {
		FragmentManager fm = getFragmentManager();
		if (intent != null && intent.getExtras() != null
				&& intent.getExtras().containsKey(Const.EXPOSE_ID)) {
			fm.beginTransaction().show(this).commit();
			exposeID = intent.getExtras().getString(Const.EXPOSE_ID);

			tracker.setCustomVar(1, Const.SOURCE,
					intent.getExtras().getString(Const.SOURCE), 1);

			if (intent.getExtras()
					.getBoolean(Const.EXPOSE_IN_PORTOFOLIO, false)) {
				// ((Button) findViewById(R.id.BackButton))
				// .setText(getString(R.string.webview_back_button));
				owned = true;
			}
			exposeName = intent.getExtras().getString(Const.EXPOSE_NAME);
			exposeDescription = intent.getExtras().getString(Const.EXPOSE_DESC);
			exposeURL = intent.getExtras().getString(Const.EXPOSE_URL);
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
		} else {

			fm.beginTransaction().hide(this).commit();
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

			// finish();
		} else {
			intent = new Intent(getActivity(), UserSignupActivity.class);
		}
		getActivity().sendBroadcast(intent);
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
