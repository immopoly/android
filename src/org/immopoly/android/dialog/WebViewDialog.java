package org.immopoly.android.dialog;

import org.immopoly.android.R;
import org.immopoly.android.constants.Const;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

/**
 * Dialog for displaying small pages in a WebView.
 * 
 * Uses webview_dialog.xml as its layout.
 * 
 * Subclasses may provide a different layout by overriding getLayout().
 * Custom layouts must have a WebView with id = R.id.webView and should have a
 * so called ProgressBar with id=R.id.progress
 * 
 * @author bjoern
 */
public class WebViewDialog extends WebViewClient 
{
	protected Activity 	activity;
	private AlertDialog alert;
	private WebView webView;
	private ProgressBar progressView;
	private String url;
	private String title;
	
	public WebViewDialog( Activity activity, String title, String url ) {
		this.activity = activity;
		this.url = url;
		this.title    = title;
	}

	public void show() {
		View layout = getLayout();

		webView = (WebView) layout.findViewById( R.id.webView );
		if ( webView == null ) {
			Log.e( Const.LOG_TAG, "You must supply a WebView with id 'webView' in your dialog layout" );
		}
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setBackgroundColor( 0 );
		webView.setMinimumHeight( 300 );
		webView.setWebViewClient( this );
		
		progressView = (ProgressBar) layout.findViewById( R.id.progress );

		AlertDialog.Builder builder = new AlertDialog.Builder( activity );
		builder.setTitle( title );
		builder.setCancelable(false)
			   .setPositiveButton("Schließen",
					new DialogInterface.OnClickListener() {
						public void onClick( DialogInterface dialog, int id) {
							onClose();
							dialog.dismiss();
						}
				});
		builder.setView( layout );
		alert = builder.create();
		alert.show();
		loadURL(url);
	}

	/**
	 * override this method to roll & return your layout
	 * @return
	 */
	protected View getLayout() {
		return activity.getLayoutInflater().inflate( R.layout.webview_dialog, null );
	}
	
	protected void onClose() {
	}
	
	public void loadURL( String url ) {
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl( url );
		webView.getSettings().setJavaScriptEnabled(true);
		if ( progressView != null )
			progressView.setVisibility( View.VISIBLE );
	}

	@Override
	public void onPageFinished(WebView view, String url) {
		if ( progressView != null )
			progressView.setVisibility( View.GONE );
	}

//	@Override
//	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//		super.onReceivedError(view, errorCode, description, failingUrl);
//		Toast.makeText(activity, "WebError: " + description, Toast.LENGTH_LONG ).show();
//	}
}
