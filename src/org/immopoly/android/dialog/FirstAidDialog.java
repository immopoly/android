package org.immopoly.android.dialog;

import org.immopoly.android.R;
import org.immopoly.android.app.ImmopolyActivity;
import org.immopoly.android.constants.Const;
import org.immopoly.android.helper.TrackingManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.opengl.Visibility;
import android.preference.PreferenceManager;
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
	public static String URL = "http://immopoly.org/frameless-description.html";
	private CheckBox cbox;
	
	public FirstAidDialog( Activity activity ) {
		super( activity, activity.getString(R.string.first_aid_dlg_title), URL );
	}

	@Override
	protected View getLayout() {
		View layout = activity.getLayoutInflater().inflate( R.layout.first_aid_dialog, null );
		cbox = (CheckBox) layout.findViewById( R.id.show_again_cb );
		return layout;
	}
	
	@Override
	protected void onClose() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
		Editor editor = prefs.edit();
		editor.putBoolean( "showFirstAid", cbox.isChecked() );
		if (!cbox.isChecked()) {
			mTracker.trackEvent(TrackingManager.CATEGORY_CLICKS, TrackingManager.ACTION_FIRST_AID, TrackingManager.LABEL_DISABLED, 0);
		} else {
			mTracker.trackEvent(TrackingManager.CATEGORY_CLICKS, TrackingManager.ACTION_FIRST_AID, TrackingManager.LABEL_ENABLED, 0);
		}
		editor.commit();
		super.onClose();
	}
}
