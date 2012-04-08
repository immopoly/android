package org.immopoly.android.dialog;

import org.immopoly.android.R;
import org.immopoly.android.constants.Const;
import org.immopoly.android.helper.TrackingManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

/**
 * Dialog for displaying small pages in a WebView. (it's actually not a dialog,
 * but creates one in show())
 * 
 * Uses webview_dialog.xml as its default layout.
 * 
 * Subclasses may provide a different layout by overriding getLayout(). Custom
 * layouts _must_ have a WebView with id=R.id.webView and should have a so
 * called ProgressBar with id=R.id.progress
 * 
 * @author bjoern
 */
public class WebViewDialog extends WebViewClient {
	protected Activity activity;
	private AlertDialog alert;
	private WebView webView;
	private ProgressBar progressView;
	private String url;
	private String title;
	public GoogleAnalyticsTracker mTracker;

	public WebViewDialog(Activity activity, String title, String url) {
		this.activity = activity;
		this.url = url;
		this.title = title;
	}

	public void show() {

		View layout = getLayout();

		webView = (WebView) layout.findViewById(R.id.webView);
		if (webView == null) {
			Log.e(Const.LOG_TAG,
					"You must supply a WebView with id 'webView' in your dialog layout");
			return;
		}
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setVerticalScrollbarOverlay(true);
		webView.setHorizontalScrollbarOverlay(true);
		webView.setBackgroundColor(0);
		webView.setMinimumHeight(300);
		webView.setWebViewClient(this);

		progressView = (ProgressBar) layout.findViewById(R.id.progress);

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(title);
		builder.setCancelable(false).setPositiveButton(
				activity.getString(R.string.close),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						onClose();
						mTracker.stopSession();
						dialog.dismiss();
					}
				});
		builder.setView(layout);
		alert = builder.create();
		alert.show();
		loadURL(url);
	}

	/**
	 * override this method to roll & return your layout
	 * 
	 * @return top-level view of the layout
	 */
	protected View getLayout() {
		return activity.getLayoutInflater().inflate(R.layout.webview_dialog,
				null);
	}

	/**
	 * this method is called before the dialog is closed
	 */
	protected void onClose() {
	}

	public void loadURL(String url) {
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl(url);
		webView.getSettings().setJavaScriptEnabled(true);
		if (progressView != null)
			progressView.setVisibility(View.VISIBLE);
		mTracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker in manual dispatch mode...
		mTracker.startNewSession(TrackingManager.UA_ACCOUNT,
				Const.ANALYTICS_INTERVAL, activity);
		if (url == "http://immopoly.org/frameless-helpandroid.html") {
			mTracker.trackPageView(TrackingManager.VIEW_HELP);
		} else if (url == "http://immopoly.org/frameless-description.html") {
			mTracker.trackPageView(TrackingManager.VIEW_FIRST_AID);
		} else if (url == "http://immopoly.org/frameless-topx_balance.html") {
			mTracker.trackPageView(TrackingManager.VIEW_HIGHSCORE_OVERALL);
		} else if (url == "http://immopoly.org/frameless-topx_balancereleasebadge.html") {
			mTracker.trackPageView(TrackingManager.VIEW_HIGHSCORE_RELEAS_TESTER);
		} else if (url == "http://immopoly.org/frameless-topx_balancemonth.html") {
			mTracker.trackPageView(TrackingManager.VIEW_HIGHSCORE_MONTH);
		} else {
			mTracker.trackPageView(TrackingManager.VIEW_WEB_VIEW_DIALOG);
		}
	}

	@Override
	public void onPageFinished(WebView view, String url) {
		if (progressView != null)
			progressView.setVisibility(View.GONE);
	}

	// @Override
	// public void onReceivedError(WebView view, int errorCode, String
	// description, String failingUrl) {
	// super.onReceivedError(view, errorCode, description, failingUrl);
	// Toast.makeText(activity, "WebError: " + description, Toast.LENGTH_LONG
	// ).show();
	// }
}
