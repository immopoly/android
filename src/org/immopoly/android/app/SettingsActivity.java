package org.immopoly.android.app;

import org.immopoly.android.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);
		try {
			setTitle( "Immopoly " + getPackageManager().getPackageInfo( getPackageName(), 0 ).versionName 
					  + " (" + getPackageManager().getPackageInfo( getPackageName(), 0 ).versionCode + ")" );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
