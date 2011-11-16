package org.immopoly.android.fragments;

import org.immopoly.android.R;
import org.immopoly.android.app.UserSignupActivity;
import org.immopoly.android.constants.Const;
import org.immopoly.android.helper.Settings;
import org.immopoly.android.helper.TrackingManager;
import org.immopoly.android.model.ImmopolyUser;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class ExposeFragment extends DialogFragment {
	private String mExposeId;
	private String mExposeName;
	private String mExposeDescription;

	private String mExposeUrl;
	private Boolean mLoadTwice = false;
	private WebView mWebView;
	private boolean mOwned = false;

	public interface OnExposeClickedListener {
		public void onExposeTakeOver(String exposeID);

		public void onExposeRelease(String exposeID);

		public void onShareClick(int exposeID, boolean isInPortfolio);

	}

	private GoogleAnalyticsTracker tracker;
	private OnExposeClickedListener mOnExposeClickedListener;

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
	public static ExposeFragment newInstance(int exposeID, boolean isInPortfolio) {
		ExposeFragment f = new ExposeFragment();
		Bundle b = new Bundle();
		b.putString(Const.EXPOSE_ID, String.valueOf(exposeID));
		b.putBoolean(Const.EXPOSE_IN_PORTOFOLIO, isInPortfolio);
		f.setArguments(b);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// fullscreen dialog with no title
		setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme);

		try {
			mOnExposeClickedListener = (OnExposeClickedListener) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(getActivity().toString() + " must implement OnMapItemClickedListener");
		}
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
		
		if ( mOwned ) {
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
				if (mOwned)
					takeOrReleaseButton.setText(getString(R.string.release_expose));
				else
					takeOrReleaseButton.setText(getString(R.string.try_takeover));
			}
		};
		buttonDelayFinishedHandler.sendMessageDelayed(new Message(), 10000);
	}

	void loadPage(Bundle intent) {
		if (intent != null && intent.containsKey(Const.EXPOSE_ID)) {
			mExposeId = intent.getString(Const.EXPOSE_ID);

			tracker.setCustomVar(1, Const.SOURCE, intent.getString(Const.SOURCE), 1);

			if (intent.getBoolean(Const.EXPOSE_IN_PORTOFOLIO, false)) {
//				((Button) getView().findViewById(R.id.BackButton)).setText(getString(R.string.webview_back_button));
				mOwned = true;
			}
			mExposeName = intent.getString(Const.EXPOSE_NAME);
			mExposeDescription = intent.getString(Const.EXPOSE_DESC);
			mExposeUrl = intent.getString(Const.EXPOSE_URL);
			String url = Settings.getFlatLink(mExposeId, true);
			mWebView.loadUrl(url);
		}
	}

	public void addCurrentExpose(View v) {
		Intent intent;
		if (ImmopolyUser.getInstance().readToken(getActivity()).length() > 0) {
			intent = new Intent();
			intent.putExtra(Const.EXPOSE_ADD_PORTIFOLIO, true);
			intent.putExtra(Const.EXPOSE_ID, mExposeId);
			intent.putExtra(Const.EXPOSE_NAME, mExposeName);
			intent.putExtra(Const.EXPOSE_DESC, mExposeDescription);
			intent.putExtra(Const.EXPOSE_URL, mExposeUrl);
			intent.setAction("add_expose");
			if (!mOwned) {
				// setResult(Activity.RESULT_OK, i);
				tracker.trackEvent(TrackingManager.CATEGORY_CLICKS, TrackingManager.ACTION_EXPOSE,
						TrackingManager.LABEL_TRY, 0);
			} else {
				// setResult(RESULT_CANCELED, i);
			}
			mOnExposeClickedListener.onExposeTakeOver(mExposeId);
			dismiss();
			// finish();
		} else {
			Intent intent2 = new Intent(getActivity(),
					UserSignupActivity.class);
			startActivityForResult(intent2,Const.USER_SIGNUP);
		}
	}

	public void releaseCurrentExpose(View v) {
		Intent intent;
		if (ImmopolyUser.getInstance().readToken(getActivity()).length() > 0) {
			intent = new Intent();
			intent.putExtra(Const.EXPOSE_RELEASE_PORTIFOLIO, true);
			intent.putExtra(Const.EXPOSE_ID, mExposeId);
			intent.putExtra(Const.EXPOSE_NAME, mExposeName);
			intent.putExtra(Const.EXPOSE_DESC, mExposeDescription);
			intent.putExtra(Const.EXPOSE_URL, mExposeUrl);
			intent.setAction("add_expose");
			if (mOwned) {
				// setResult(Activity.RESULT_OK, i);
				tracker.trackEvent(TrackingManager.CATEGORY_CLICKS, TrackingManager.ACTION_EXPOSE, TrackingManager.LABEL_RELEASE, 0);
			} else {
				// setResult(RESULT_CANCELED, i);
			}
			mOnExposeClickedListener.onExposeRelease(mExposeId);
			dismiss();
			// finish();
		} else {
			Intent intent2 = new Intent(getActivity(),
					UserSignupActivity.class);
			startActivityForResult(intent2,Const.USER_SIGNUP);
		}
	}

	public void shareExpose(View v) {
		Settings.shareMessage(getActivity(), getString(R.string.link_share_flat), mExposeName,
				Settings.getFlatLink(mExposeId, false));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mWebView.destroy();
		mWebView = null;
		tracker.stopSession();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode,
            Intent data) {
		if(requestCode == Const.USER_SIGNUP && resultCode == Activity.RESULT_OK){
			addCurrentExpose(null);
		}
	}
}
