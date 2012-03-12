package org.immopoly.android.app;

import java.util.Vector;

import org.immopoly.android.R;
import org.immopoly.android.constants.Const;
import org.immopoly.android.helper.Settings;
import org.immopoly.android.helper.TrackingManager;
import org.immopoly.android.model.Flat;
import org.immopoly.android.model.ImmopolyHistory;
import org.immopoly.android.model.ImmopolyUser;
import org.immopoly.android.tasks.AddToPortfolioTask;
import org.immopoly.android.tasks.GetUserInfoTask;
import org.immopoly.android.tasks.ReleaseFromPortfolioTask;
import org.immopoly.android.tasks.Result;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

/**
 * contains methods that interact with the immopoly server. - login/signup
 * (using UserSignupActivity) - getUserInfo (using GetUserInfoTask) -
 * addToPortfolio (using AddToPortfolioTask) - releaseFromPortfolio (using
 * ReleaseFromPortfolioTask)
 * 
 * @author bjoern TODO re-enable addToPortfolio after signup
 * 
 */
public class UserDataManager {
	public static final int USER_UNKNOWN = 0;
	public static final int LOGIN_PENDING = 1; // TODO unused (yet)
	public static final int LOGGED_IN = 2;

//	private boolean actionPending;

	private int state = USER_UNKNOWN;
	private Activity activity;
	private GoogleAnalyticsTracker mTracker;

	private Vector<UserDataListener> listeners = new Vector<UserDataListener>();
	private Flat flatToAddAfterLogin;

	// the singleton instance
	public static final UserDataManager instance = new UserDataManager();

	private UserDataManager() {
	}

	public void setActivity(Activity activity) {
		this.activity = activity;

		// try to get user info on first activity start if we have a token
		String token = ImmopolyUser.getInstance().readToken(activity);
		if (state == USER_UNKNOWN && token != null && token.length() > 0) {
			getUserInfo();
		}

		if (mTracker == null) {
			mTracker = GoogleAnalyticsTracker.getInstance();
			// Start the tracker in manual dispatch mode...
			mTracker.startNewSession(TrackingManager.UA_ACCOUNT,
					Const.ANALYTICS_INTERVAL, activity.getApplicationContext());
		}
	}

	public void addUserDataListener(UserDataListener udl) {
		listeners.add(udl);
	}

	public void removeUserDataListener(UserDataListener udl) {
		listeners.remove(udl);
	}

	public int getState() {
		return state;
	}

	/**
	 * summons UserSignupActivity to login or register informs listeners on
	 * successful operation
	 */
	public void login() {
		Log.i(Const.LOG_TAG, "UserDataManager.login() state = " + state);
//		if (actionPending) {
//			Log.w(Const.LOG_TAG,
//					"Refusing to log in while another server request is running");
//			return;
//		}
		if (state != USER_UNKNOWN) {
			Log.e(Const.LOG_TAG, "Already logged in. Log out first.");
			return;
		}

//		actionPending = true;
		state = LOGIN_PENDING;
		Intent intent = new Intent(activity, UserSignupActivity.class);
		activity.startActivityForResult(intent, Const.USER_SIGNUP);
	}

	public boolean logout() {
		Log.i(Const.LOG_TAG, "UserDataManager.logout() state = " + state);
//		if (actionPending) {
//			Log.w(Const.LOG_TAG,
//					"Refusing to log out while another server request is running");
//			return false;
//		}
		if (state != LOGGED_IN) {
			Log.e(Const.LOG_TAG, "Already logged in. Log out first.");
			return false;
		}
		state = USER_UNKNOWN;
		ImmopolyUser.resetInstance();
		if (null != this.activity)
			setToken(activity, null);
		fireUsedDataChanged();
		return true;
	}

	/**
	 * Receives results from Actitvies (UserSignupActivity for now)
	 * 
	 * informs listeners if signup succeeded
	 * 
	 * ! Must be invoked from ImmopolyActivity.onActivityResult
	 * 
	 */
	void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(Const.LOG_TAG, "UserDataManager.onActivityResult() state = "
				+ state);
//		actionPending = false;
		if (requestCode == Const.USER_SIGNUP) {
			if (resultCode == Activity.RESULT_OK) {
				state = LOGGED_IN;
				Log.i(Const.LOG_TAG,
						"UserDataManager.onActivityResult() state2  = " + state);
				fireUsedDataChanged();
				if (flatToAddAfterLogin != null)
					addToPortfolio(flatToAddAfterLogin);
			} else {
				Log.i(Const.LOG_TAG,
						"UserDataManager.onActivityResult() state3  = " + state);
				state = USER_UNKNOWN;
				fireUsedDataChanged();
			}
		}
		Log.i(Const.LOG_TAG, "UserDataManager.onActivityResult() state4  = "
				+ state);
	}

	/**
	 * retrieves UserInfo data from server and fills ImmopolyUser.instance
	 * 
	 * informs listeners on successful operation
	 */
	public void getUserInfo() {
		Log.i(Const.LOG_TAG, "UserDataManager.getUserInfo() state = " + state);
//		if (actionPending) {
//			Log.w(Const.LOG_TAG,
//					"Refusing to get user info while another server request is running");
//			return;
//		}
		Log.i(Const.LOG_TAG, "UserDataManager.getUserInfo() state2 = " + state);
//		actionPending = true;
		state = LOGIN_PENDING;
		Log.i(Const.LOG_TAG, "UserDataManager.getUserInfo() state3 = " + state);
		GetUserInfoTask task = new GetUserInfoTask(activity) {
			@Override
			protected void onPostExecute(ImmopolyUser user) {
//				actionPending = false;
				if (user != null) {
					state = LOGGED_IN;
				} else {
					state = USER_UNKNOWN;
				}
				Log.i(Const.LOG_TAG, "UserDataManager.getUserInfo() state4 = "
						+ state);
				fireUsedDataChanged();
			}
		};
		task.execute(ImmopolyUser.getInstance().getToken());
	}

	/**
	 * Tries to add a flat to the users portfolio using AddToPortfolioTask.
	 * Informs listeners on successful operation
	 * 
	 * @param flat
	 *            the flat to add
	 */
	public void addToPortfolio(final Flat flat) {
		Log.i(Const.LOG_TAG, "UserDataManager.addToPortfolio " + flat.uid);
		// login first if not yet
		if (state == USER_UNKNOWN) {
			flatToAddAfterLogin = flat;
			login();
			return;
		}

		if (!checkState())
			return;
//		actionPending = true;
		new AddToPortfolioTask(activity, mTracker) {
			protected void onPostExecute(final Result result) {
				super.onPostExecute(result);
				
				//TODO schtief der Rotz hier kommt nach super da gibs jetzt nen UserDataManager.update 
				if (result.success) {
					flat.owned = true;
					flat.takeoverDate = System.currentTimeMillis();
					ImmopolyUser.getInstance().getPortfolio().add(flat);
				}
				/**
				 * show the feedback in a dialog, from there the user can either
				 * share the result or
				 */
				int title = result.success ? R.string.take_over_success_dlg_title : R.string.take_over_fail_dlg_title;
				showExposeDialog(flat, result, activity.getString(title));
				//TODO schtief wird schon in super gemacht
				fireUsedDataChanged();
//				actionPending = false;
			}
		}.execute(flat);
	}

	protected void showExposeDialog(final Flat flat, final Result result, final String title ) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle( title );
		final String text = null != result.history ? result.history.getText() : (null != result.exception ? result.exception.getMessage(): "Häää?");

		builder.setMessage(text);
		// builder.setContentView(R.layout.maindialog);
		builder.setCancelable(true).setNegativeButton(activity.getString(R.string.share_item), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				Settings.getFlatLink( Integer.toString(flat.uid), false);
				Settings.shareMessage(activity, title, text, Settings.getFlatLink(
						Integer.toString(flat.uid), false) /* LINk */);
				mTracker.trackEvent(TrackingManager.CATEGORY_ALERT, TrackingManager.ACTION_SHARE, TrackingManager.LABEL_POSITIVE, 0);
			}
		});
		builder.setPositiveButton(result.success ? R.string.button_ok : R.string.button_mist, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				mTracker.trackEvent(TrackingManager.CATEGORY_ALERT, TrackingManager.ACTION_SHARE, TrackingManager.LABEL_NEGATIVE, 0);
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	/**
	 * Tries to release a flat from the users portfolio using
	 * RemoveFromPortfolioTask. Informs listeners on successful operation
	 * 
	 * @param flat
	 *            the flat to add
	 */
	public void releaseFromPortfolio(final Flat flat) {
		Log.i(Const.LOG_TAG, "UserDataManager.releaseFromPortfolio " + flat.uid);
		if (!checkState())
			return;
//		actionPending = true;
		new ReleaseFromPortfolioTask(activity, mTracker) {
			@Override
			protected void onPostExecute(Result result) {
				super.onPostExecute(result);
				if (result.success) {
					// remove the flat from users portfolio list
					flat.owned = false;
					Flat toBeRemoved = null;
					for (Flat f : ImmopolyUser.getInstance().getPortfolio())
						if (f.uid == flat.uid)
							toBeRemoved = f;
					if (toBeRemoved != null) {
						Log.i(Const.LOG_TAG,
								"UserDataManager.releaseFromPortfolio() Flat removed: "
										+ flat.name);
						ImmopolyUser.getInstance().getPortfolio()
								.remove(toBeRemoved);
						toBeRemoved.owned = false;
					} else {
						Log.w(Const.LOG_TAG,
								"UserDataManager.releaseFromPortfolio() Flat NOT removed: "
										+ flat.name);
					}
					
					/**
					 * show the feedback in a dialog, from there the user can either
					 * share the result or
					 */
					showExposeDialog(flat, result, activity.getString(R.string.flat_released_dlg_title));
					
					fireUsedDataChanged();
//					actionPending = false;
				}
			};
		}.execute(flat);
	}

	private void fireUsedDataChanged() {
		Log.i(Const.LOG_TAG, "UserDataManager.fireUsedDataChanged()");
		for (UserDataListener listener : listeners)
			listener.onUserDataUpdated();
	}

	private boolean checkState() {
//		if (actionPending) {
//			Log.w(Const.LOG_TAG,
//					"Refusing to send request while another server request is running");
//			return false;
//		}
		if (state != LOGGED_IN) {
			Log.e(Const.LOG_TAG, "Not logged in.");
			return false;
		}
		return true;
	}

	// handle the token only here set null to remove
	public static void setToken(ContextWrapper context, String token) {
		SharedPreferences shared = context.getSharedPreferences(
				ImmopolyUser.sPREF_USER, 0);
		SharedPreferences.Editor editor = shared.edit();
		if (token == null)
			editor.remove(ImmopolyUser.sPREF_TOKEN);
		else
			editor.putString(ImmopolyUser.sPREF_TOKEN, token);
		editor.commit();
	}

	public void update(ImmopolyHistory history) {
		ImmopolyUser u =ImmopolyUser.getInstance();
		if(null==u)
			return;
		
		u.getHistory().add(0, history);
		
		if(null!=history.getAmount())
			u.setBalance(u.getBalance()+history.getAmount());

		fireUsedDataChanged();
	}
}
