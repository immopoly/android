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
import org.immopoly.android.model.Flat;
import org.immopoly.android.tasks.ReleaseFromPortfolioTask;
import org.immopoly.android.widget.TabManager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TabHost;
import android.widget.Toast;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.android.maps.MapView;

/**
 * @author tosa,sebastia,björn Example implementation of fragments communication
 */
public class ImmopolyActivity extends FragmentActivity implements OnMapItemClickedListener  {

	private static final String PROFILE_FRAGMENT_TAG = "PROFILE_FRAGMENT";
	private static final String PORTFOLIO_MAP_FRAGMENT_TAG = "PORTFOLIO_MAP_FRAGMENT";
	private static final String PORTFOLIO_LIST_FRAGMENT_TAG = "PORTFOLIO_LIST_FRAGMENT";
	private static final String MAP_FRAGMENT_TAG = "MAP_FRAGMENT";
	private static final String EXPOSE_FRAGMENT_TAG = "EXPOSE_FRAGMENT";

	public static final int MAP_FRAGMENT = 1;
	public static final int PROFILE_FRAGMENT = 2;
	public static final int PORTFOLIO_MAP_FRAGMENT = 3;
	public static final int PORTFOLIO_LIST_FRAGMENT = 4;
	public static final int EXPOSE_FRAGMENT = 5;


	// Fragments
	private Fragment mLastFragment;
	private boolean mIsVeryFirstFragment = true;
	private MapView mMapView;
	private Fragment mMapViewHolder;
	private int mCurrentFragment;

	TabHost mTabHost;
	TabManager mTabManager;

	/**
	 * Init the game
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		UserDataManager.instance.setActivity( this );
		setContentView(R.layout.immopoly_activity);

		// mFragmentContainer = (FrameLayout)
		// findViewById(R.id.fragment_container);
		if (savedInstanceState == null) {
			parseData();
		}

		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();

		mTabManager = new TabManager(this, mTabHost, R.id.fragment_container);

		addTab(R.drawable.btn_map, "map", MapFragment.class);
		addTab(R.drawable.btn_portfolio, "portofolio", PortfolioListFragment.class);
		addTab(R.drawable.btn_profile, "profile", ProfileFragment.class);
		addTab(R.drawable.btn_notify, "history", HistoryFragment.class);


		if (savedInstanceState != null) {
			mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
		}

	}

	private void addTab(int imageId, String name, Class<?> clss) {
		ImageButton tab = (ImageButton) LayoutInflater.from(this).inflate(R.layout.tab_map, null);
		tab.setImageResource(imageId);
		mTabManager.addTab(mTabHost.newTabSpec(name).setIndicator(tab), clss, null);
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

		// showFragment(1, null);
	}

	public void showFragment(int which, Bundle bundle) {
		if (mCurrentFragment == which)
			return; // avoid "fragment already active" exception
		mCurrentFragment = which;
		FragmentManager fm = getSupportFragmentManager();
		Fragment newFragment = null;
		String newTag = "";
		switch (which) {
		case MAP_FRAGMENT:
			newTag = MAP_FRAGMENT_TAG;
			newFragment = fm.findFragmentByTag(MAP_FRAGMENT_TAG);
			if (newFragment == null) {
				newFragment = new MapFragment();
			}
			break;
		case PORTFOLIO_MAP_FRAGMENT:
			newTag = PORTFOLIO_MAP_FRAGMENT_TAG;
			newFragment = fm.findFragmentByTag(PORTFOLIO_MAP_FRAGMENT_TAG);
			if (newFragment == null) {
				newFragment = new PortfolioMapFragment();
			}
			break;
		case PORTFOLIO_LIST_FRAGMENT:
			newTag = PORTFOLIO_LIST_FRAGMENT_TAG;
			newFragment = fm.findFragmentByTag(PORTFOLIO_LIST_FRAGMENT_TAG);
			if (newFragment == null) {
				newFragment = new PortfolioListFragment();
			}
			break;
		case PROFILE_FRAGMENT:
			newTag = PROFILE_FRAGMENT_TAG;
			newFragment = fm.findFragmentByTag(PROFILE_FRAGMENT_TAG);
			if (newFragment == null) {
				newFragment = new MapFragment();
			}
			break;
		case EXPOSE_FRAGMENT:
			newTag = EXPOSE_FRAGMENT_TAG;
			newFragment = fm.findFragmentByTag(EXPOSE_FRAGMENT_TAG);
			if (newFragment == null) {
				newFragment = new ExposeFragment();
			}
			break;
		}

		if (newFragment != null) {
			if (bundle != null)
				newFragment.setArguments(bundle);
			FragmentTransaction transaction = fm.beginTransaction();
			if (mLastFragment != null)
				transaction.remove(mLastFragment);
			transaction.add(R.id.fragment_container, newFragment, newTag);
			if (!mIsVeryFirstFragment) {
				transaction = transaction.addToBackStack(null);
			} else {
				mIsVeryFirstFragment = false;
			}
			transaction.commit();
			mLastFragment = newFragment;
		}

	}

	@Override
	public void onFlatClicked( Flat flat ) {
		Toast.makeText(this, "onFlatClicked", Toast.LENGTH_LONG).show();
		DialogFragment newFragment = ExposeFragment.newInstance(flat);
		// newFragment.setArguments(tmp);
		newFragment.show(getSupportFragmentManager(), "dialog");
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
			mMapView = new MapView(this, getString(R.string.google_maps_key_debug));
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


//	@Override
//	public void onShareClick(int exposeID, boolean isInPortfolio) {
//		Log.i(Const.LOG_TAG, "https://github.com/immopoly/android/issues/15");
//	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		UserDataManager.instance.onActivityResult(requestCode, resultCode, data);
	}
	
}
