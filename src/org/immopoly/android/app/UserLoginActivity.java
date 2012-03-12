package org.immopoly.android.app;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.immopoly.android.R;
import org.immopoly.android.c2dm.C2DMessaging;
import org.immopoly.android.constants.Const;
import org.immopoly.android.helper.Settings;
import org.immopoly.android.helper.TrackingManager;
import org.immopoly.android.helper.WebHelper;
import org.immopoly.android.model.ImmopolyUser;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

public class UserLoginActivity extends Activity {

	public static final int RESULT_REGISTER = 4321;
	private GoogleAnalyticsTracker tracker;
	private Handler toggleProgressHandler;
	public static final int MIN_PASSWORTH_LENGTH = 4;
	private static final int MIN_USERNAME_LENGTH = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker in manual dispatch mode...

		tracker.startNewSession(TrackingManager.UA_ACCOUNT,
				Const.ANALYTICS_INTERVAL, getApplicationContext());
		tracker.trackPageView(TrackingManager.VIEW_LOGIN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// init login
		toggleProgressHandler = new Handler();
		setContentView(R.layout.user_login_activity);

	}

	public void registerUser(View v) {
		setResult(RESULT_REGISTER);
		finish();
	}

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

	private void toggleProgress() {
		toggleProgressHandler.post(new Runnable() {
			public void run() {
				View progress = findViewById(R.id.login_progress);
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
						UserLoginActivity.this);

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
				UserDataManager.setToken(UserLoginActivity.this,
						user.getToken());
				C2DMessaging.register(UserLoginActivity.this,
						Const.IMMOPOLY_EMAIL);
				setResult(RESULT_OK);
				finish();
			} else if (Settings.isOnline(UserLoginActivity.this)) {
				Toast.makeText(UserLoginActivity.this,
						R.string.toast_wrong_username_or_pasword,
						Toast.LENGTH_LONG).show();
			} else {

				Toast.makeText(UserLoginActivity.this,
						R.string.toast_no_connection, Toast.LENGTH_LONG).show();
				// findViewById(R.id.loginview).setVisibility(View.VISIBLE);

			}
		}
	}
}
