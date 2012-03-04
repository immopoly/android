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
import org.immopoly.android.model.ImmopolyException;
import org.immopoly.android.model.ImmopolyUser;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

public class UserRegisterActivity extends Activity {

	public static final int RESULT_LOGIN = 1234;
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
		tracker.trackPageView(TrackingManager.VIEW_REGISTER);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// init login
		toggleProgressHandler = new Handler();
		setContentView(R.layout.user_register_activity);

	}

	public void loginUser(View v) {
		setResult(RESULT_LOGIN);
		finish();
	}

	public void registerUser(View v) {

		EditText username = (EditText) findViewById(R.id.user_name);
		EditText userPassword = (EditText) findViewById(R.id.user_password);
		EditText userEmail = (EditText) findViewById(R.id.user_email);
		EditText userTwitter = (EditText) findViewById(R.id.user_twitter);

		// check if passwords are the same
		// if (userPassword.getText().toString()
		// .equals(userPasswordConfirm.getText().toString())) {
		if (userPassword.getText().length() >= MIN_PASSWORTH_LENGTH) {

			if (username.getText().toString().length() >= MIN_USERNAME_LENGTH) {
				// register
				if (userEmail.getText().length() == 0) {
					Toast.makeText(this, R.string.toast_email_verification,
							Toast.LENGTH_LONG).show();
				} else {
					new RegisterUserTask().execute(username.getText()
							.toString(), userPassword.getText().toString(),
							userEmail.getText().toString(), userTwitter
									.getText().toString());
				}
			} else {
				Toast.makeText(
						this,
						getString(R.string.toast_min_lenght_username,
								MIN_USERNAME_LENGTH), Toast.LENGTH_LONG).show();
			}
		} else {
			Toast.makeText(
					this,
					getString(R.string.toast_min_lenght_password,
							MIN_PASSWORTH_LENGTH), Toast.LENGTH_LONG).show();
		}
		// } else {
		// Toast.makeText(this, R.string.toast_passwords_must_match,
		// Toast.LENGTH_LONG).show();
		// }
	}

	public void recoverPassword(View v) {
		EditText username = (EditText) findViewById(R.id.user_name);
		EditText userEmail = (EditText) findViewById(R.id.user_email);

		if ( username.length() == 0 || userEmail.length() == 0 ) {
			// TODO strings
			Toast.makeText( this, "Bitte gib Name und Email-Adresse ein.", Toast.LENGTH_LONG ).show();
			return;
		}
		
		new RecoverPasswordTask().execute(username.getText().toString(), userEmail.getText().toString() );
	}
	
	private class RegisterUserTask extends AsyncTask<String, Void, ImmopolyUser> {

		@Override
		protected ImmopolyUser doInBackground(String... params) {
			String username = params[0];
			String password = params[1];
			String email = params[2];
			String twitter = params[3];
			if (twitter != null && !twitter.startsWith("@")) {
				twitter = "@" + twitter;
			}
			JSONObject obj = null;
			ImmopolyUser user;
			try {
				toggleProgress();
				StringBuilder sb = new StringBuilder(
						WebHelper.SERVER_HTTPS_URL_PREFIX + "/user/register?");
				sb.append("username=").append(URLEncoder.encode(username))
						.append("&password=")
						.append(URLEncoder.encode(password)).append("&email=")
						.append(URLEncoder.encode(email)).append("&twitter=")
						.append(URLEncoder.encode(twitter));
				obj = WebHelper.getHttpsData(new URL(sb.toString()), false,
						UserRegisterActivity.this);
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
				UserDataManager.setToken(UserRegisterActivity.this,
						user.getToken());
				C2DMessaging.register(UserRegisterActivity.this,
						Const.IMMOPOLY_EMAIL);
				// finish
				setResult(RESULT_OK);
				finish();

			} else if (Settings.isOnline(UserRegisterActivity.this)) {
				Toast.makeText(UserRegisterActivity.this,
						getString(R.string.username_already_used),
						Toast.LENGTH_LONG).show();
			} else {

				Toast.makeText(UserRegisterActivity.this,
						getString(R.string.no_internet_connection),
						Toast.LENGTH_LONG).show();
				// findViewById(R.id.loginview).setVisibility(View.VISIBLE);

			}
		}
	}

	private class RecoverPasswordTask extends AsyncTask<String, Void, JSONObject> {
		private String username;
		@Override
		protected JSONObject doInBackground(String... params) {
			username = params[0];
			String email = params[1];
			JSONObject obj = null;
			try {
				toggleProgress();
				StringBuilder sb = new StringBuilder(
						WebHelper.SERVER_HTTPS_URL_PREFIX + "/user/sendpasswordmail?");
				sb.append("username=").append(URLEncoder.encode(username))
						.append("&email=").append(URLEncoder.encode(email));
				obj = WebHelper.getHttpsData(new URL(sb.toString()), false,
						UserRegisterActivity.this);
				Log.i( Const.LOG_TAG, "GOT PWD JSON: " + obj.toString() );
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			toggleProgress();
			return obj;
		}
		
		@Override
		protected void onPostExecute(JSONObject obj) {
			if (obj == null ) {
				if ( ! Settings.isOnline(UserRegisterActivity.this)) {
					Toast.makeText(UserRegisterActivity.this, getString(R.string.no_internet_connection),
						Toast.LENGTH_LONG).show();
				} else {
					Log.e( Const.LOG_TAG, "ooops: " + obj );
				}
			} else if (obj.has("OK")) {
				Toast.makeText(UserRegisterActivity.this, getString(R.string.pwd_reset_mail_sent),
						Toast.LENGTH_LONG).show();
			} else if ( obj.has(Const.MESSAGE_IMMOPOLY_EXCEPTION) ) {
				ImmopolyException exc = new ImmopolyException(UserRegisterActivity.this, obj);
				if ( exc.getErrorCode() == ImmopolyException.USER_SEND_PASSWORDMAIL_NOEMAIL )
					startSocialPasswordReset();
				else
					Toast.makeText(UserRegisterActivity.this, exc.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
		
		private void startSocialPasswordReset() {
			AlertDialog.Builder dialog = new AlertDialog.Builder(UserRegisterActivity.this);
			dialog.setTitle(getString(R.string.social_pwd_reset_dlg_title)); 
			dialog.setMessage( getString(R.string.social_pwd_reset_dlg_text));
			dialog.setNegativeButton( getString(R.string.social_pwd_reset_dlg_cancel), null );
			dialog.setPositiveButton( getString(R.string.social_pwd_reset_dlg_ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(Intent.ACTION_SEND);
					intent.setType("message/rfc822");
					intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "immopolyteam@gmail.com" });
					intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.social_pwd_reset_mail_subject));
					intent.putExtra(Intent.EXTRA_TEXT, 
							getString(R.string.social_pwd_reset_mail_text1) + username + getString(R.string.social_pwd_reset_mail_text2) );
					startActivity(Intent.createChooser(intent, getString(R.string.social_pwd_reset_intent_title)));
				}
			} );
			dialog.show();
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
}
