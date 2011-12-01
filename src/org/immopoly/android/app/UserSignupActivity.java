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

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.immopoly.android.R;
import org.immopoly.android.c2dm.C2DMessaging;
import org.immopoly.android.constants.Const;
import org.immopoly.android.helper.LocationHelper;
import org.immopoly.android.helper.Settings;
import org.immopoly.android.helper.TrackingManager;
import org.immopoly.android.helper.WebHelper;
import org.immopoly.android.model.ImmopolyUser;
import org.immopoly.android.tasks.GetUserInfoTask;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class UserSignupActivity extends Activity {

	public static final int MIN_PASSWORTH_LENGTH = 4;
	private static final int MIN_USERNAME_LENGTH = 1;

	private GoogleAnalyticsTracker tracker;
	Handler toggleProgressHandler = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		tracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker in manual dispatch mode...

		tracker.startNewSession(TrackingManager.UA_ACCOUNT, Const.ANALYTICS_INTERVAL, getApplicationContext());
		tracker.trackPageView(TrackingManager.VIEW_LOGIN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		
		// init login
		toggleProgressHandler=new Handler();
		setContentView(R.layout.user_signup_activity);
		// check if user as token and is already logedin
		ImmopolyUser.getInstance().readToken(this);
//		LocationHelper.getLastLocation(this);
		if (LocationHelper.getBestProvider(getApplicationContext()) == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.allow_localization_title);
			builder.setMessage(R.string.allow_localization_message);
			builder.setCancelable(true).setNegativeButton(
					R.string.button_cancel,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							init();
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
		} else {
			init();
		}
		
	}

	@Override
	protected void onStart() {
		super.onStart();
		tracker.trackPageView(TrackingManager.VIEW_LOGIN);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		init();
//		LocationHelper.getLastLocation(this);
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void init() {
		if (ImmopolyUser.getInstance().getToken().length() > 0) {
			findViewById(R.id.loginview).setVisibility(View.GONE);
			new GetUserInfoSingUpTask(this).execute(ImmopolyUser.getInstance()
					.getToken());
		} else {
			findViewById(R.id.loginview).setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Called by res/layout/user_signup_activity.xml loginbutton onclick
	 */
	public void registerUser(View v) {
		EditText userPasswordConfirm = (EditText) findViewById(R.id.user_password_confirm);
		if (userPasswordConfirm.getVisibility() != View.VISIBLE) {
			userPasswordConfirm.setVisibility(View.VISIBLE);
			userPasswordConfirm.requestFocus();
		} else {
			EditText userPassword = (EditText) findViewById(R.id.user_password);
			// check if passwords are the same
			if (userPassword.getText().toString()
					.equals(userPasswordConfirm.getText().toString())) {
				if (userPassword.getText().length() >= MIN_PASSWORTH_LENGTH) {
					EditText username = (EditText) findViewById(R.id.user_name);
					if (username.getText().toString().length() >= MIN_USERNAME_LENGTH) {
						// register
						new RegisterUserTask().execute(username.getText()
								.toString(), userPassword.getText().toString());
					} else {
						Toast.makeText(
								this,
								getString(R.string.toast_min_lenght_username,
										MIN_USERNAME_LENGTH), Toast.LENGTH_LONG)
								.show();
					}
				} else {
					Toast.makeText(
							this,
							getString(R.string.toast_min_lenght_password,
									MIN_PASSWORTH_LENGTH), Toast.LENGTH_LONG)
							.show();
				}
			} else {
				Toast.makeText(this, R.string.toast_passwords_must_match,
						Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * Called by res/layout/user_signup_activity.xml loginbutton onclick
	 */
	public void loginUser(View v) {
		EditText username = (EditText) findViewById(R.id.user_name);
		if (username.getText().toString().length() >= MIN_USERNAME_LENGTH) {
			EditText userPassword = (EditText) findViewById(R.id.user_password);
			if (userPassword.getText().length() >= MIN_PASSWORTH_LENGTH) {
				// register
				new LoginUserTask().execute(username.getText().toString(),
						userPassword.getText().toString());
			} else {
				Toast.makeText(
						this,
						getString(R.string.password_min_lenght,
								MIN_PASSWORTH_LENGTH), Toast.LENGTH_LONG)
						.show();
			}
		} else {
			Toast.makeText(
					this,
					getString(R.string.username_min_lenght, MIN_USERNAME_LENGTH),
					Toast.LENGTH_LONG).show();
		}
	}

	private class RegisterUserTask extends
			AsyncTask<String, Void, ImmopolyUser> {

		@Override
		protected ImmopolyUser doInBackground(String... params) {
			String username = params[0];
			String password = params[1];
			JSONObject obj = null;
			ImmopolyUser user;
			try {
				toggleProgress();
				obj = WebHelper.getHttpsData(
						new URL(WebHelper.SERVER_HTTPS_URL_PREFIX
								+ "/user/register?username="
								+ URLEncoder.encode(username) + "&password="
								+ URLEncoder.encode(password)), false,
						UserSignupActivity.this);

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (obj == null || obj.has(Const.MESSAGE_IMMOPOLY_EXCEPTION)) {
				user = null;

			} else {
				user = ImmopolyUser.getInstance();
				user.fromJSON(obj);
			}
			toggleProgress();
			return user;
		}

		@Override
		protected void onPostExecute(ImmopolyUser user) {
			if (user != null) {
				UserDataManager.setToken(UserSignupActivity.this,user.getToken());
				C2DMessaging.register(UserSignupActivity.this, Const.IMMOPOLY_EMAIL);
				startGame();
			} else if (Settings.isOnline(UserSignupActivity.this)) {
				Toast.makeText(UserSignupActivity.this,
						getString(R.string.username_already_used),
						Toast.LENGTH_LONG).show();
			} else {

				Toast.makeText(UserSignupActivity.this,
						getString(R.string.no_internet_connection),
						Toast.LENGTH_LONG).show();
				findViewById(R.id.loginview).setVisibility(View.VISIBLE);

			}
		}
	}
	
	private void toggleProgress(){
		toggleProgressHandler.post(new Runnable() {  
			public void run() {
				View progress = findViewById(R.id.login_register_progress);
				if(progress.getVisibility()==View.GONE)
				{
					progress.setVisibility(View.VISIBLE);
					findViewById(R.id.login).setVisibility(View.GONE);
					findViewById(R.id.register).setVisibility(View.GONE);
				}else{
					progress.setVisibility(View.GONE);
					findViewById(R.id.login).setVisibility(View.VISIBLE);
					findViewById(R.id.register).setVisibility(View.VISIBLE);
				}
			}
		});			
	}

	private class LoginUserTask extends AsyncTask<String, Void, ImmopolyUser> {

		@Override
		protected ImmopolyUser doInBackground(String... params) {
			String username = params[0];
			String password = params[1];
			JSONObject obj = null;
			ImmopolyUser user;
			try {
				toggleProgress();
				obj = WebHelper.getHttpsData(
						new URL(WebHelper.SERVER_HTTPS_URL_PREFIX
								+ "/user/login?username="
								+ URLEncoder.encode(username) + "&password="
								+ URLEncoder.encode(password)), false,
						UserSignupActivity.this);

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (obj == null || obj.has(Const.MESSAGE_IMMOPOLY_EXCEPTION)) {
				user = null;
			} else {
				user = ImmopolyUser.getInstance();
				user.fromJSON(obj);
			}
			toggleProgress();
			return user;
		}

		@Override
		protected void onPostExecute(ImmopolyUser user) {
			if (user != null && user.getToken().length() > 0) {
				UserDataManager.setToken(UserSignupActivity.this,user.getToken());
				C2DMessaging.register(UserSignupActivity.this, Const.IMMOPOLY_EMAIL);
				startGame();
			} else if (Settings.isOnline(UserSignupActivity.this)) {
				Toast.makeText(UserSignupActivity.this,
						R.string.toast_wrong_username_or_pasword,
						Toast.LENGTH_LONG).show();
			} else {

				Toast.makeText(UserSignupActivity.this,
						R.string.toast_no_connection, Toast.LENGTH_LONG).show();
				findViewById(R.id.loginview).setVisibility(View.VISIBLE);

			}
		}
	}

	private class GetUserInfoSingUpTask extends GetUserInfoTask {

		public GetUserInfoSingUpTask(Context context) {
			super(context);
		}

		@Override
		protected void onPostExecute(ImmopolyUser user) {
			super.onPostExecute(user);
			if (user != null) {
				startGame();
			} else if (Settings.isOnline(UserSignupActivity.this)) {
				Toast.makeText(UserSignupActivity.this,
						R.string.toast_session_timeout, Toast.LENGTH_LONG)
						.show();
				findViewById(R.id.loginview).setVisibility(View.VISIBLE);

			} else {
				Toast.makeText(UserSignupActivity.this,
						R.string.toast_no_connection, Toast.LENGTH_LONG).show();
				findViewById(R.id.loginview).setVisibility(View.VISIBLE);

			}

		}

	}

	public void cheat(View v) {
		startGame();
	}

	public void startGame() {
		// start game
		//Intent i = new Intent(this, PlacesMapActivity.class);
		//startActivity(i);
		// login success, trigger action requested before login
		setResult(Activity.RESULT_OK);
		finish();
	}

//	public void showInfo(View v) {
//
//		LayoutInflater inflater = LayoutInflater.from(UserSignupActivity.this);
//
//		View alertDialogView = inflater.inflate(R.layout.info_webview, null);
//
//		WebView myWebView = (WebView) alertDialogView
//				.findViewById(R.id.DialogWebView);
//		myWebView.setWebViewClient(new WebViewClient());
//		myWebView.getSettings().setSupportZoom(true);
//		myWebView.getSettings().setUseWideViewPort(true);
//
//		myWebView.loadUrl(WebHelper.SERVER_URL_PREFIX);
//		AlertDialog.Builder builder = new AlertDialog.Builder(
//				UserSignupActivity.this);
//		builder.setView(alertDialogView);
//		builder.setPositiveButton(R.string.button_ok,
//				new DialogInterface.OnClickListener() {
//
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						dialog.cancel();
//					}
//				}).show();
//
//	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		tracker.stopSession();
	}
	
	public void closeLogin(View v){
		finish();
	}
	

}
