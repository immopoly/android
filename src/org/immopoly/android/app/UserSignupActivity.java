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
import org.immopoly.android.helper.LocationHelper;
import org.immopoly.android.helper.TrackingManager;
import org.immopoly.android.model.ImmopolyUser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class UserSignupActivity extends Activity {

	public static final int MIN_PASSWORTH_LENGTH = 4;
	private static final int MIN_USERNAME_LENGTH = 1;
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
		// check if user as token and is already logedin
		ImmopolyUser.getInstance().readToken(this);
		// LocationHelper.getLastLocation(this);
		if (LocationHelper.getBestProvider(getApplicationContext()) == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.allow_localization_title);
			builder.setMessage(R.string.allow_localization_message);
			builder.setCancelable(true).setNegativeButton(
					R.string.button_cancel,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
						}
					});
			builder.setPositiveButton("Einstellungen",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {

							final ComponentName toLaunch = new ComponentName(
									"com.android.settings",
									"com.android.settings.SecuritySettings");
							final Intent intent = new Intent(
									android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							intent.addCategory(Intent.CATEGORY_LAUNCHER);
							intent.setComponent(toLaunch);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivityForResult(intent, 0);
						}
					});
			AlertDialog alert = builder.create();
			alert.show();
		}

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
		//
	}

	private void toggleProgress() {
		toggleProgressHandler.post(new Runnable() {
			public void run() {
				View progress = findViewById(R.id.login_register_progress);
				if (progress.getVisibility() == View.GONE) {
					progress.setVisibility(View.VISIBLE);
					findViewById(R.id.login).setVisibility(View.GONE);
					findViewById(R.id.register).setVisibility(View.GONE);
				} else {
					progress.setVisibility(View.GONE);
					findViewById(R.id.login).setVisibility(View.VISIBLE);
					findViewById(R.id.register).setVisibility(View.VISIBLE);
				}
			}
		});
	}

	public void startGame() {
		// start game
		// Intent i = new Intent(this, PlacesMapActivity.class);
		// startActivity(i);
		// login success, trigger action requested before login
		setResult(Activity.RESULT_OK);
		finish();
	}

	// public void showInfo(View v) {
	//
	// LayoutInflater inflater = LayoutInflater.from(UserSignupActivity.this);
	//
	// View alertDialogView = inflater.inflate(R.layout.info_webview, null);
	//
	// WebView myWebView = (WebView) alertDialogView
	// .findViewById(R.id.DialogWebView);
	// myWebView.setWebViewClient(new WebViewClient());
	// myWebView.getSettings().setSupportZoom(true);
	// myWebView.getSettings().setUseWideViewPort(true);
	//
	// myWebView.loadUrl(WebHelper.SERVER_URL_PREFIX);
	// AlertDialog.Builder builder = new AlertDialog.Builder(
	// UserSignupActivity.this);
	// builder.setView(alertDialogView);
	// builder.setPositiveButton(R.string.button_ok,
	// new DialogInterface.OnClickListener() {
	//
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// dialog.cancel();
	// }
	// }).show();
	//
	// }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		tracker.stopSession();
	}

	public void closeLogin(View v) {
		finish();
	}

}
