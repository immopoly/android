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
import org.immopoly.android.tasks.Result;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
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
					findViewById(R.id.forgot_password).setVisibility(View.GONE);
				} else {
					progress.setVisibility(View.GONE);
					findViewById(R.id.login).setVisibility(View.VISIBLE);
					findViewById(R.id.register).setVisibility(View.VISIBLE);
					findViewById(R.id.forgot_password).setVisibility(View.VISIBLE);
				}
			}
		});
	}

	private class LoginUserTask extends AsyncTask<String, Void, ImmopolyUser> {
		Result result = new Result();
		@Override
		protected ImmopolyUser doInBackground(String... params) {
			String username = params[0];
			String password = params[1];
			JSONObject obj = null;
			ImmopolyUser user=null;

			try {
				toggleProgress();
				obj = WebHelper.getHttpsData(
						new URL(WebHelper.SERVER_HTTPS_URL_PREFIX
								+ "/user/login?username="
								+ URLEncoder.encode(username) + "&password="
								+ URLEncoder.encode(password)), false,
						UserLoginActivity.this);
				if (obj != null && obj.has(Const.MESSAGE_IMMOPOLY_EXCEPTION)) {
					result.exception = new ImmopolyException(UserLoginActivity.this, obj);
				} else {
					user = ImmopolyUser.getInstance();
					user.fromJSON(obj);
					if(user != null && user.getToken().length() > 0)
						result.success=true;
				}
			} catch (ImmopolyException e) {
				e.printStackTrace();
				result.exception = e;
			} catch (MalformedURLException e) {
				e.printStackTrace();
				result.exception = new ImmopolyException(e);
			}
			
			toggleProgress();
			return user;
		}

		@Override
		protected void onPostExecute(ImmopolyUser user) {
			if (result.success) {
				UserDataManager.setToken(UserLoginActivity.this,user.getToken());

				// c2dm only if wanted
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(UserLoginActivity.this);
				if(sharedPreferences.getBoolean("notification", true))
					C2DMessaging.register(UserLoginActivity.this,Const.IMMOPOLY_EMAIL);
				setResult(RESULT_OK);
				finish();
			} else if (Settings.isOnline(UserLoginActivity.this)) {
				if(result.exception != null )
					showLoginFailedDialog(result.exception.getMessage());
				else
					showLoginFailedDialog("merkwürdiger Fehler, bitte versuche es nochmal!");
//				Toast.makeText(UserLoginActivity.this,R.string.toast_wrong_username_or_pasword,Toast.LENGTH_LONG).show();
			} else {
				showLoginFailedDialog(getString(R.string.toast_no_connection));
//				Toast.makeText(UserLoginActivity.this,, Toast.LENGTH_LONG).show();
			}
		}
		
		private void showLoginFailedDialog(String message) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(UserLoginActivity.this);
			dialog.setTitle(getString(R.string.login_failed)); 
			dialog.setMessage(message);
			dialog.setPositiveButton(getString(R.string.button_mist), null );
			dialog.show();
		}
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
						UserLoginActivity.this);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (ImmopolyException e) {
				e.printStackTrace();
			}
			toggleProgress();
			return obj;
		}
		
		@Override
		protected void onPostExecute(JSONObject obj) {
			if (obj == null ) {
				if ( ! Settings.isOnline(UserLoginActivity.this)) {
					Toast.makeText(UserLoginActivity.this, getString(R.string.no_internet_connection),
						Toast.LENGTH_LONG).show();
				} else {
					Log.e( Const.LOG_TAG, "ooops: " + obj );
				}
			} else if (obj.has("OK")) {
				Toast.makeText(UserLoginActivity.this, getString(R.string.pwd_reset_mail_sent),
						Toast.LENGTH_LONG).show();
			} else if ( obj.has(Const.MESSAGE_IMMOPOLY_EXCEPTION) ) {
				ImmopolyException exc = new ImmopolyException(UserLoginActivity.this, obj);
				if ( exc.getErrorCode() == ImmopolyException.USER_SEND_PASSWORDMAIL_NOEMAIL )
					startSocialPasswordReset();
				else
					Toast.makeText(UserLoginActivity.this, exc.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
		
		private void startSocialPasswordReset() {
			AlertDialog.Builder dialog = new AlertDialog.Builder(UserLoginActivity.this);
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
}
