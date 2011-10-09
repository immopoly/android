/**
 * 
 */
package org.immopoly.android.app;

import java.net.MalformedURLException;
import java.net.URL;

import org.immopoly.android.R;
import org.immopoly.android.app.MainActivity.AddToPortifolioTask;
import org.immopoly.android.constants.Const;
import org.immopoly.android.fragments.ExposeFragment;
import org.immopoly.android.fragments.ExposeFragment.OnExposeClickedListener;
import org.immopoly.android.fragments.MapFragment;
import org.immopoly.android.fragments.MapFragment.OnMapItemClickedListener;
import org.immopoly.android.fragments.callbacks.HudCallbacks;
import org.immopoly.android.helper.Settings;
import org.immopoly.android.helper.TrackingManager;
import org.immopoly.android.helper.WebHelper;
import org.immopoly.android.model.ImmopolyHistory;
import org.immopoly.android.model.ImmopolyUser;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * @author tosa,sebastia,björn Example implemetation of fragments communication
 */
public class ImmopolyActivity extends FragmentActivity implements OnMapItemClickedListener, OnExposeClickedListener, HudCallbacks{

	private static final String PROFILE_FRAGMENT_TAG = "PROFILE_FRAGMENT";
	private static final String PORTFOLIO_FRAGMENT_TAG = "PORTFOLIO_FRAGMENT";
	private static final String MAP_FRAGMENT_TAG = "MAP_FRAGMENT";
	private static final String EXPOSE_FRAGMENT_TAG = "EXPOSE_FRAGMENT";
	
	private static final int MAP_FRAGMENT = 1;
	private static final int PROFILE_FRAGMENT = 2;
	private static final int PORTFOLIO_FRAGMENT = 3;
	private static final int EXPOSE_FRAGMENT = 4;

	private FrameLayout mFragmentContainer;

	// Fragments
	private MapFragment mMapFragment;
	private Fragment mLastFragment;
	private boolean mIsVeryFirstFragment = true;
	private GoogleAnalyticsTracker tracker;
	/**
	 * Init the game
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		setContentView(R.layout.immopoly_activity);
		tracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker in manual dispatch mode...
		tracker.startNewSession(TrackingManager.UA_ACCOUNT,
				Const.ANALYTICS_INTERVAL,
				ImmopolyActivity.this.getApplicationContext());
		
		mFragmentContainer = (FrameLayout) findViewById(R.id.fragment_container);
		if (savedInstanceState == null) {
			 parseData();
		}
	}

	/**
	 * Activity is visible but someone called for a action
	 */
	@Override
	public void onNewIntent(Intent newIntent) {
		super.onNewIntent(newIntent);
		// update original intent
		setIntent(newIntent);
		parseData();
	}

	/*
	 * parse intent and do action
	 */
	private void parseData() {
		// TODO implement parsing and actions
		getIntent();

		showFragment(1,null);
	}

	public void showFragment(int which, Bundle bundle) {
		FragmentManager fm = getSupportFragmentManager();
		Fragment newFragment = null;
		String newTag = "";
		switch (which) {
		case MAP_FRAGMENT:
			newTag = MAP_FRAGMENT_TAG;
			newFragment = fm.findFragmentByTag(MAP_FRAGMENT_TAG);
			if (newFragment == null){
				newFragment = new MapFragment();
				
			}
			break;
		case PORTFOLIO_FRAGMENT:
			newTag = PORTFOLIO_FRAGMENT_TAG;
			newFragment = fm.findFragmentByTag(PORTFOLIO_FRAGMENT_TAG);
			if (newFragment == null){
				newFragment = new MapFragment();
			}
			break;
		case PROFILE_FRAGMENT:
			newTag = PROFILE_FRAGMENT_TAG;
			newFragment = fm.findFragmentByTag(PROFILE_FRAGMENT_TAG);
			if (newFragment == null){
				newFragment = new MapFragment();
			}
			break;
		case EXPOSE_FRAGMENT:
			newTag = EXPOSE_FRAGMENT_TAG;
			newFragment = fm.findFragmentByTag(EXPOSE_FRAGMENT_TAG);
			if (newFragment == null){
				newFragment = new ExposeFragment();
			}
			break;
		}
		
		if (newFragment != null) {
			newFragment.setArguments(bundle);
			FragmentTransaction transaction = fm.beginTransaction();
			transaction = transaction.replace(R.id.fragment_container, newFragment,newTag);
			if(!mIsVeryFirstFragment ){
				transaction = transaction.addToBackStack(null);
			} else {
				mIsVeryFirstFragment = false;
			}
			transaction.commit();
		}
		
	}

	@Override
	public void onMapItemClicked(int exposeID, boolean isInPortfolio) {
		Toast.makeText(this, "onMapItemClicked", Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onMapOverlayClicked(int exposeID, boolean isInPortfolio) {
		Toast.makeText(this, "onMapOverlayClicked", Toast.LENGTH_LONG).show();
		Bundle tmp = new Bundle();
		tmp.putString(Const.EXPOSE_ID, String.valueOf(exposeID));
		tmp.putBoolean(Const.EXPOSE_OWNED, isInPortfolio);
		showFragment(EXPOSE_FRAGMENT,tmp);
	}

	@Override
	public void updateHud(Intent data, int element) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onHudAction(View view) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onExposeClick(String exposeID) {
		getSupportFragmentManager().popBackStack();
		if(exposeID != null){
			new AddToPortifolioTask().execute(exposeID);
		}
	}

	@Override
	public void onShareClick(int exposeID, boolean isInPortfolio) {
		// TODO Auto-generated method stub
		
	}
	
	
	class AddToPortifolioTask extends AsyncTask<String, Void, ImmopolyHistory> {

		@Override
		protected ImmopolyHistory doInBackground(String... params) {
			JSONObject obj = null;
			ImmopolyHistory history = null;
			try {
				ImmopolyUser.getInstance().readToken(ImmopolyActivity.this);
				obj = WebHelper.getHttpData(new URL(WebHelper.SERVER_URL_PREFIX
						+ "/portfolio/add?token="
						+ ImmopolyUser.getInstance().getToken() + "&expose="
						+ params[0]), false, ImmopolyActivity.this);
				if (obj != null
						&& !obj.has("org.immopoly.common.ImmopolyException")) {
					history = new ImmopolyHistory();
					history.fromJSON(obj);
					tracker.trackEvent(TrackingManager.CATEGORY_ALERT,
							TrackingManager.ACTION_EXPOSE,
							TrackingManager.LABEL_TRY, 0);
				} else if (obj != null) {
					history = new ImmopolyHistory();
					switch (obj.getJSONObject(
							"org.immopoly.common.ImmopolyException").getInt(
							"errorCode")) {
					case 201:
						history.mText = getString(R.string.flat_already_in_portifolio);
						break;
					case 301:
						history.mText = getString(R.string.flat_does_not_exist_anymore);

						break;
					case 302:
						history.mText = getString(R.string.flat_has_no_raw_rent);
						break;
					case 441:
						history.mText = getString(R.string.expose_location_spoofing);
					}
					tracker.trackEvent(TrackingManager.CATEGORY_ALERT,
							TrackingManager.ACTION_EXPOSE,
							TrackingManager.LABEL_NEGATIVE, 0);
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return history;
		}

		@Override
		protected void onPostExecute(ImmopolyHistory result) {
			final ImmopolyHistory res = result;
			if (result != null && result.mText != null
					&& result.mText.length() > 0) {

				Toast.makeText(ImmopolyActivity.this, res.mText, Toast.LENGTH_LONG)
						.show();
				// new GetUserInfoUpdateTask(PlacesMapActivity.this)
				// .execute(ImmopolyUser.getInstance().getToken());
			} else if (Settings.isOnline(ImmopolyActivity.this)) {
				Toast.makeText(ImmopolyActivity.this, R.string.expose_couldnt_add,
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(ImmopolyActivity.this,
						R.string.no_internet_connection, Toast.LENGTH_LONG)
						.show();
			}
			super.onPostExecute(result);
		}

	}
}
