package org.immopoly.android.dialog;

import org.immopoly.android.R;
import org.immopoly.android.app.ImmopolyActivity;
import org.immopoly.android.constants.Const;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.opengl.Visibility;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

public class FirstAidDialog extends WebViewDialog
{
	public static String URL = "http://immopoly.org/frameless-firstaid.html";
	private CheckBox cbox;
	
	public FirstAidDialog( Activity activity ) {
		super( activity, "Erste Hilfe", URL );
	}

	@Override
	protected View getLayout() {
		View layout = activity.getLayoutInflater().inflate( R.layout.first_aid_dialog, null );
		cbox = (CheckBox) layout.findViewById( R.id.never_again_cb );
		return layout;
	}
	
	@Override
	protected void onClose() {
		SharedPreferences prefs = activity.getSharedPreferences( "FirstAid", Context.MODE_PRIVATE);  // TODO const
		Editor editor = prefs.edit();
		editor.putBoolean( "showFirstAid", cbox.isChecked() );
		editor.commit();
	}
}
