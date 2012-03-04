/**
 * 
 */
package org.immopoly.android.app;

import org.immopoly.android.R;
import org.immopoly.android.constants.Const;
import org.immopoly.android.fragments.ExposeFragment;
import org.immopoly.android.fragments.HistoryFragment;
import org.immopoly.android.fragments.MapFragment;
import org.immopoly.android.fragments.OnMapItemClickedListener;
import org.immopoly.android.fragments.PortfolioListFragment;
import org.immopoly.android.fragments.PortfolioMapFragment;
import org.immopoly.android.fragments.ProfileFragment;
import org.immopoly.android.helper.TrackingManager;
import org.immopoly.android.model.Flat;
import org.immopoly.android.model.ImmopolyUser;
import org.immopoly.android.widget.TabManager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.android.maps.MapView;

/**
 * @author tosa,sebastia,björn Example implementation of fragments communication
 */

public class ImmopolyActivity extends FragmentActivity implements OnMapItemClickedListener {

	private MapView mMapView;
	private Fragment mMapViewHolder;

	TabHost mTabHost;
	TabManager mTabManager;
	public GoogleAnalyticsTracker tracker;

	/**
	 * Init the game
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker in manual dispatch mode...

		tracker.startNewSession(TrackingManager.UA_ACCOUNT, Const.ANALYTICS_INTERVAL, getApplicationContext());

		UserDataManager.instance.setActivity(this);
		setContentView(R.layout.immopoly_activity);

		// mFragmentContainer = (FrameLayout)
		// findViewById(R.id.fragment_container);
		if (savedInstanceState == null) {
			parseData();
		}

		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();

		mTabManager = new TabManager(this, mTabHost, R.id.fragment_container);

		// TODO cleanup fragment management for fragments with an without tabs
		// (currently in widget.TabManager)
		addTab(R.drawable.btn_map, "map", MapFragment.class, false);
		addTab(R.drawable.btn_portfolio, "portfolio", PortfolioListFragment.class, false);
		addTab(R.drawable.btn_portfolio, "portfolio_map", PortfolioMapFragment.class, true);
		addTab(R.drawable.btn_profile, "profile", ProfileFragment.class, false);
		addTab(R.drawable.btn_history, "history", HistoryFragment.class, false);

		if (savedInstanceState != null) {
			mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
		}

	}

	private void addTab(int imageId, String name, Class<?> clss, boolean tabless) {
		TabSpec tabSpec = mTabHost.newTabSpec(name);
		if (!tabless) {
			ImageButton tab = (ImageButton) LayoutInflater.from(this).inflate(R.layout.tab_map, null);
			tab.setImageResource(imageId);
			tabSpec.setIndicator(tab);
		}
		mTabManager.addTab(tabSpec, clss, null, tabless);
	}

	public TabManager getTabManager() {
		return mTabManager;
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

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("tab", mTabHost.getCurrentTabTag());
	}

	/*
	 * parse intent and do action
	 */
	private void parseData() {
		// TODO implement parsing and actions
		getIntent();

		// showFragment(1, null);
	}

	@Override
	public void onFlatClicked(Flat flat) {
		// Toast.makeText(this, "onFlatClicked", Toast.LENGTH_LONG).show();
		DialogFragment newFragment = ExposeFragment.newInstance(flat);
		// newFragment.setArguments(tmp);
		newFragment.show(getSupportFragmentManager(), "dialog");
		tracker.trackEvent(TrackingManager.CATEGORY_CLICKS, TrackingManager.ACTION_EXPOSE,
				TrackingManager.LABEL_EXPOSE_MAP, 0);

	}

	/**
	 * Used by a Fragment to gain ownership of the one and only MapView
	 * 
	 * @param mapViewHolder
	 *            The Fragment that wants to use the MapView
	 * @return our MapView
	 */
	public MapView acquireMapView(Fragment mapViewHolder) {
		if (this.mMapViewHolder != null) {
			throw new IllegalStateException("The one and only MapView was not released by "
					+ mMapViewHolder.getClass().getName());
		}
		this.mMapViewHolder = mapViewHolder;
		if (mMapView == null) {
			mMapView = new MapView(this,
					getString(R.string.google_maps_key));
			mMapView.setClickable(true);
			mMapView.setTag("map_view");
		}
		return mMapView;
	}

	/**
	 * Used by a Fragment to release ownership of the one and only MapView
	 * 
	 * @param mapViewHolder
	 *            The Fragment that wants to release the MapView
	 */
	public void releaseMapView(Fragment mapViewHolder) {
		if (mapViewHolder != mMapViewHolder) {
			throw new IllegalStateException("Wrong Fragment tried to release the one and only MapView " + " Holder: "
					+ this.mMapViewHolder.getClass().getName() + " Releaser: " + mapViewHolder.getClass().getName());
		}
		if (mMapView.getParent() != null && mMapView.getParent() instanceof ViewGroup)
			((ViewGroup) mMapView.getParent()).removeView(mMapView);
		mMapView.getOverlays().clear();
		mMapView.removeAllViews();
		mMapViewHolder = null;
	}

	// @Override
	// public void onShareClick(int exposeID, boolean isInPortfolio) {
	// Log.i(Const.LOG_TAG, "https://github.com/immopoly/android/issues/15");
	// }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		UserDataManager.instance.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (UserDataManager.instance.getState() == UserDataManager.USER_UNKNOWN) {
			menu.findItem(R.id.menu_logout).setTitle("Anmelden");
		} else {
			menu.findItem(R.id.menu_logout).setTitle("Abmelden");
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		int itemId = item.getItemId();
		switch (itemId) {
		case R.id.menu_settings:
			intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			break;
		case R.id.menu_website:
			intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("http://immopoly.appspot.com"));
			startActivity(intent);
			break;
		case R.id.menu_contact:
			intent = new Intent(Intent.ACTION_SEND);
			intent.setType("message/rfc822");
			intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "immopolyteam@gmail.com" });
			intent.putExtra(Intent.EXTRA_SUBJECT, "Immopoly Feedback");
			startActivity(Intent.createChooser(intent, "Feedback:"));
			break;
		case R.id.menu_logout:
			if (UserDataManager.instance.getState() == UserDataManager.LOGGED_IN) {
				UserDataManager.instance.logout();
			} else {
				UserDataManager.instance.login();
			}
			break;
		default:
			break;
		}

		return true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		tracker.stopSession();
	}

}
