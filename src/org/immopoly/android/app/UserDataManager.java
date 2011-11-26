package org.immopoly.android.app;

import java.util.Vector;

import org.immopoly.android.constants.Const;
import org.immopoly.android.helper.TrackingManager;
import org.immopoly.android.model.Flat;
import org.immopoly.android.model.ImmopolyUser;
import org.immopoly.android.tasks.AddToPortfolioTask;
import org.immopoly.android.tasks.GetUserInfoTask;
import org.immopoly.android.tasks.ReleaseFromPortfolioTask;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

/**
 * contains methods that interact with the immopoly server.
 *  - login/signup (using UserSignupActivity)
 *  - getUserInfo (using GetUserInfoTask)
 *  - addToPortfolio (using AddToPortfolioTask)
 *  - releaseFromPortfolio (using ReleaseFromPortfolioTask)
 * 
 * @author bjoern
 * TODO re-enable  addToPortfolio after signup
 *
 */
public class UserDataManager
{
	public static final int USER_UNKNOWN  = 0;
	public static final int LOGIN_PENDING = 1; // TODO unused (yet)
	public static final int LOGGED_IN     = 2;

	private boolean actionPending;
	
	private int state = USER_UNKNOWN;
	private Activity mActivity;
	private GoogleAnalyticsTracker mTracker;

	private Vector<UserDataListener> listeners = new Vector<UserDataListener>();
	private Flat flatToAddAfterLogin;

	// the singleton instance
	public static final UserDataManager instance = new UserDataManager();

	private UserDataManager() {
	}

	public void setActivity( Activity activity ) {
		this.mActivity = activity;

		// try to get user info on first activity start if we have a token
		if ( state == USER_UNKNOWN && ImmopolyUser.getInstance().readToken( mActivity ) != null ) {
			getUserInfo();
		}
		
		if ( mTracker == null ) {
			mTracker = GoogleAnalyticsTracker.getInstance();
			// Start the tracker in manual dispatch mode...
			mTracker.startNewSession(TrackingManager.UA_ACCOUNT, Const.ANALYTICS_INTERVAL, activity.getApplicationContext() );
		}
	}
	
	public void addUserDataListener( UserDataListener udl ) {
		listeners.add(udl);
	}

	public void removeUserDataListener( UserDataListener udl ) {
		listeners.remove(udl);
	}

	public int getState() {
		return state;
	}
	
	/**
	 * summons UserSignupActivity to login or register
	 * informs listeners on successful operation
	 */
	public void login() {
		Log.i( Const.LOG_TAG, "UserDataManager.login()" );
		if ( actionPending ) {
			Log.w( Const.LOG_TAG, "Refusing to log in while another server request is running" );
			return;
		}
		if ( state != USER_UNKNOWN ) {
			Log.e( Const.LOG_TAG, "Already logged in. Log out first." );
			return;
		}
		
		actionPending = true;
		state = LOGIN_PENDING;
		Intent intent = new Intent( mActivity, UserSignupActivity.class );
		mActivity.startActivityForResult( intent, Const.USER_SIGNUP );
	}


	/**
	 * Receives results from Actitvies (UserSignupActivity for now)
	 * 
	 * informs listeners if signup succeeded
	 * 
	 * ! Must be invoked from ImmopolyActivity.onActivityResult
	 * 
	 */
	void onActivityResult( int requestCode, int resultCode, Intent data ) {
		Log.i( Const.LOG_TAG, "UserDataManager.onActivityResult()" );
		if(requestCode == Const.USER_SIGNUP ) {
			if ( resultCode == Activity.RESULT_OK ) {
				state = LOGGED_IN;
				fireUsedDataChanged();
				if ( flatToAddAfterLogin != null )
					addToPortfolio( flatToAddAfterLogin );
			} else {
				state = USER_UNKNOWN;
				fireUsedDataChanged();
			}
		}
		actionPending = false;
	}
	
	/**
	 * retrieves UserInfo data from server and fills ImmopolyUser.instance
	 * 
	 * informs listeners on successful operation
	 */
	public void getUserInfo() {
		Log.i( Const.LOG_TAG, "UserDataManager.getUserInfo()" );
		if ( actionPending ) {
			Log.w( Const.LOG_TAG, "Refusing to get user info while another server request is running" );
			return;
		}
		actionPending = true;
		state = LOGIN_PENDING;
		GetUserInfoTask task = new GetUserInfoTask( mActivity ) {
			@Override
			protected void onPostExecute(ImmopolyUser user) {
				actionPending = false;
				if ( user != null ) {
					state = LOGGED_IN;
				} else {
					state = USER_UNKNOWN;
				}
				fireUsedDataChanged();
			}
		};
		task.execute( ImmopolyUser.getInstance().getToken() );
	}

	/**
	 * Tries to add a flat to the users portfolio using AddToPortfolioTask.
	 * Informs listeners on successful operation
	 * 
	 * @param flat the flat to add
	 */
	public void addToPortfolio( final Flat flat ) {
		Log.i( Const.LOG_TAG, "UserDataManager.addToPortfolio() tracker: " +mTracker );
		// login first if not yet		
		if ( state == USER_UNKNOWN ) {
			flatToAddAfterLogin = flat;
			login();
			return;
		}
			
		if ( ! checkState() )
			return;
		actionPending = true;
		new AddToPortfolioTask( mActivity, mTracker ) {
			protected void onPostExecute( AddToPortfolioTask.Result result) {
				super.onPostExecute( result );
				if ( result.success ) {
					flat.owned = true;
					ImmopolyUser.getInstance().getPortfolio().add( flat );
				}
					
				fireUsedDataChanged();
				actionPending = false;
			};
		}.execute( flat );
	}

	/**
	 * Tries to release a flat from the users portfolio using RemoveFromPortfolioTask.
	 * Informs listeners on successful operation
	 * 
	 * @param flat the flat to add
	 */
	public void releaseFromPortfolio( final Flat flat ) {
		Log.i( Const.LOG_TAG, "UserDataManager.releaseFromPortfolio()" );
		if ( ! checkState() )
			return;
		actionPending = true;
		new ReleaseFromPortfolioTask( mActivity, mTracker ) {
			@Override
			protected void onPostExecute( ReleaseFromPortfolioTask.Result result) {
				super.onPostExecute( result );
				if ( result.success ) {  // remove the flat from users portfolio list
					flat.owned = false;
					Flat toBeRemoved = null; // flat in users list, which was eventually created from DB,
											 // while the parameter flat was probably created from is24 data
					for ( Flat f : ImmopolyUser.getInstance().getPortfolio() )
						if ( f.uid == flat.uid )
							toBeRemoved = f;
					if ( toBeRemoved != null ) {
						Log.i( Const.LOG_TAG, "UserDataManager.releaseFromPortfolio() Flat removed: " + flat.name );
						ImmopolyUser.getInstance().getPortfolio().remove( toBeRemoved );
						toBeRemoved.owned = false;
					} else {
						Log.w( Const.LOG_TAG, "UserDataManager.releaseFromPortfolio() Flat NOT removed: " + flat.name );
					}
					fireUsedDataChanged();
					actionPending = false;
				}
			};
		}.execute( String.valueOf(flat.uid)  );
	}

	private void fireUsedDataChanged() {
		Log.i( Const.LOG_TAG, "UserDataManager.fireUsedDataChanged()" );
		for ( UserDataListener listener : listeners ) 
			listener.onUserDataUpdated();
	}
	
	private boolean checkState() {
		if ( actionPending ) {
			Log.w( Const.LOG_TAG, "Refusing to send request while another server request is running" );
			return false;
		}
		if ( state != LOGGED_IN ) {
			Log.e( Const.LOG_TAG, "Not logged in." );
			return false;
		}
		return true;
	}

}

