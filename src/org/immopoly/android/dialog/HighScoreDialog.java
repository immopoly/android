package org.immopoly.android.dialog;

import org.immopoly.android.R;
import org.immopoly.android.app.ImmopolyActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.opengl.Visibility;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * loads highscore html from website
 *
 * @author bjoern
 */
public class HighScoreDialog extends WebViewDialog implements AdapterView.OnItemSelectedListener
{
	private int selection = 0;
	
	private static String[] URLS = {
		"http://immopoly.org/frameless-topx_balance.html",
		"http://immopoly.org/frameless-topx_balancemonth.html",
		"http://immopoly.org/frameless-topx_balancereleasebadge.html"
	};

	public HighScoreDialog( Activity activity ) {
		super( activity, "Hisghscore", URLS[0] );
	}

	protected View getLayout() {
		View layout = activity.getLayoutInflater().inflate( R.layout.highscore_webview, null );
		Spinner dropDown = (Spinner) layout.findViewById( R.id.hscore_dropdown );
		dropDown.setOnItemSelectedListener( this );
		return layout;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id ) {
		if ( position == selection )
			return;
		loadURL( URLS[position] );
		selection = position;
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}
}	
