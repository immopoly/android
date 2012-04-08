package org.immopoly.android.app;

import org.immopoly.android.R;
import org.immopoly.android.constants.Const;
import org.immopoly.android.helper.TrackingManager;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {

	private GoogleAnalyticsTracker mTracker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker in manual dispatch mode...
		mTracker.startNewSession(TrackingManager.UA_ACCOUNT,
				Const.ANALYTICS_INTERVAL, this);
		mTracker.trackPageView(TrackingManager.VIEW_SETTINGS);
		
		addPreferencesFromResource(R.xml.preferences);
		try {
			setTitle( "Immopoly " + getPackageManager().getPackageInfo( getPackageName(), 0 ).versionName 
					  + " (" + getPackageManager().getPackageInfo( getPackageName(), 0 ).versionCode + ")" );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onDestroy() {
		mTracker.stopSession();
		super.onDestroy();
	}
}
