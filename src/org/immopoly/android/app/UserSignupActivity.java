/*
 * This is the Android component of Immopoly
 * http://immopoly.appspot.com
 * Copyright (C) 2011 Tobias Sasse
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */

package org.immopoly.android.app;

import org.immopoly.android.R;
import org.immopoly.android.constants.Const;
import org.immopoly.android.helper.TrackingManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class UserSignupActivity extends Activity {

	
	private static final int REGISTER_REQUEST = 123;
	private static final int LOGIN_REQUEST = 124;

	private GoogleAnalyticsTracker tracker;
	Handler toggleProgressHandler = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		tracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker in manual dispatch mode...

		tracker.startNewSession(TrackingManager.UA_ACCOUNT,
				Const.ANALYTICS_INTERVAL, getApplicationContext());
		tracker.trackPageView(TrackingManager.VIEW_SIGNUP);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// init login
		toggleProgressHandler = new Handler();
		setContentView(R.layout.user_signup_activity);

	}

	@Override
	protected void onStart() {
		super.onStart();
		tracker.trackPageView(TrackingManager.VIEW_LOGIN);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REGISTER_REQUEST
				&& resultCode == UserRegisterActivity.RESULT_LOGIN) {
			tracker.trackEvent(TrackingManager.CATEGORY_CLICKS,
					TrackingManager.ACTION_VIEW,
					TrackingManager.LABEL_CHANGE_TO_LOGIN_FROM_REGISTER, 0);
			Intent login = new Intent(this, UserLoginActivity.class);
			startActivityForResult(login, LOGIN_REQUEST);
		} else if (requestCode == LOGIN_REQUEST
				&& resultCode == UserLoginActivity.RESULT_REGISTER) {
			tracker.trackEvent(TrackingManager.CATEGORY_CLICKS,
					TrackingManager.ACTION_VIEW,
					TrackingManager.LABEL_CHANGE_TO_REGISTER_FROM_LOGIN, 0);
			Intent login = new Intent(this, UserRegisterActivity.class);
			startActivityForResult(login, REGISTER_REQUEST);
		} else if (RESULT_OK == resultCode) {
			startGame();
		}
	}

	/**
	 * Called by res/layout/user_signup_activity.xml loginbutton onclick
	 */
	public void registerUser(View v) {
		Intent register = new Intent(this, UserRegisterActivity.class);
		startActivityForResult(register, REGISTER_REQUEST);

	}

	/**
	 * Called by res/layout/user_signup_activity.xml loginbutton onclick
	 */
	public void loginUser(View v) {
		Intent login = new Intent(this, UserLoginActivity.class);
		startActivityForResult(login, LOGIN_REQUEST);
	}

	public void startGame() {
		setResult(Activity.RESULT_OK);
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		tracker.stopSession();
	}

	public void closeLogin(View v) {
		finish();
	}

}
