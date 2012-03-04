/**
 * 
 */
package org.immopoly.android.app;

import oauth.signpost.OAuth;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import org.immopoly.android.R;
import org.immopoly.android.constants.Const;
import org.immopoly.android.fragments.ExposeFragment;
import org.immopoly.android.fragments.HistoryFragment;
import org.immopoly.android.fragments.MapFragment;
import org.immopoly.android.fragments.OnMapItemClickedListener;
import org.immopoly.android.fragments.PortfolioListFragment;
import org.immopoly.android.fragments.PortfolioMapFragment;
import org.immopoly.android.fragments.ProfileFragment;
import org.immopoly.android.helper.OnTrackingEventListener;
import org.immopoly.android.helper.TrackingManager;
import org.immopoly.android.model.Flat;
import org.immopoly.android.model.OAuthData;
import org.immopoly.android.widget.TabManager;

import android.content.Intent;
import android.content.SharedPreferences;
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

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.android.maps.MapView;

/**
 * @author tosa,sebastia,björn Example implementation of fragments communication
 */

public class ImmopolyActivity extends FragmentActivity implements
		OnMapItemClickedListener, OnTrackingEventListener {

	public static final String C2DM_START = "c2dm_start";
	public static final int START_HISTORY = 0x1;
	private MapView mMapView;
	private Fragment mMapViewHolder;

	private TabHost mTabHost;
	private TabManager mTabManager;
	private GoogleAnalyticsTracker mTracker;

	/**
	 * Init the game
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker in manual dispatch mode...

		mTracker.startNewSession(TrackingManager.UA_ACCOUNT,
				Const.ANALYTICS_INTERVAL, getApplicationContext());

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
		addTab(R.drawable.btn_portfolio, "portfolio",
				PortfolioListFragment.class, false);
		addTab(R.drawable.btn_portfolio, "portfolio_map",
				PortfolioMapFragment.class, true);
		addTab(R.drawable.btn_profile, "profile", ProfileFragment.class, false);
		addTab(R.drawable.btn_history, "history", HistoryFragment.class, false);

		if (savedInstanceState != null) {
			mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
		}
		// for generating oauth token
		// signIn();
	}

	private void addTab(int imageId, String name, Class<?> clss, boolean tabless) {
		TabSpec tabSpec = mTabHost.newTabSpec(name);
		if (!tabless) {
			ImageButton tab = (ImageButton) LayoutInflater.from(this).inflate(
					R.layout.tab_map, null);
			tab.setImageResource(imageId);
			tabSpec.setIndicator(tab);
		}
		mTabManager.addTab(tabSpec, clss, null, tabless);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	public TabManager getTabManager() {
		return mTabManager;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		parseData();
	}

	/**
	 * n Activity is visible but someone called for a action
	 */
	@Override
	public void onNewIntent(Intent newIntent) {
		// update original intent
		setIntent(newIntent);	
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
		// Start with specific fragment
		Intent i = getIntent();
		if (mTabHost != null && i.hasExtra(ImmopolyActivity.C2DM_START)) {
			UserDataManager.instance.getUserInfo();
			switch (i.getIntExtra(ImmopolyActivity.C2DM_START,
					ImmopolyActivity.START_HISTORY)) {
			case ImmopolyActivity.START_HISTORY:
				mTabHost.setCurrentTabByTag("history");
				break;

			default:
				mTabHost.setCurrentTabByTag("history");
				break;
			}
			i.removeExtra(ImmopolyActivity.C2DM_START);
		}
	}

	@Override
	public void onFlatClicked(Flat flat) {
		// Toast.makeText(this, "onFlatClicked", Toast.LENGTH_LONG).show();
		DialogFragment newFragment = ExposeFragment.newInstance(flat);
		// newFragment.setArguments(tmp);
		newFragment.show(getSupportFragmentManager(), "dialog");
		mTracker.trackEvent(TrackingManager.CATEGORY_CLICKS,
				TrackingManager.ACTION_EXPOSE,
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
			throw new IllegalStateException(
					"The one and only MapView was not released by "
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
			throw new IllegalStateException(
					"Wrong Fragment tried to release the one and only MapView "
							+ " Holder: "
							+ this.mMapViewHolder.getClass().getName()
							+ " Releaser: "
							+ mapViewHolder.getClass().getName());
		}
		if (mMapView.getParent() != null
				&& mMapView.getParent() instanceof ViewGroup)
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
		UserDataManager.instance
				.onActivityResult(requestCode, resultCode, data);
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
			intent.setData(Uri.parse("http://immopoly.org"));
			startActivity(intent);
			break;
		case R.id.menu_highscore:
			intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("http://immopoly.org/livestats.html?c=android"));
			startActivity(intent);
			break;
		case R.id.menu_help:
			intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("http://immopoly.org/helpandroid.html"));
			startActivity(intent);
			break;
		// case R.id.menu_recommend:
		// intent = new Intent(Intent.ACTION_SEND);
		// intent.setType("text/plain");
		// intent.putExtra(Intent.EXTRA_TEXT,
		// "Immopoly is geil, lade es dir hier runter http://immopoly.org/download.html");
		// startActivity(Intent.createChooser(intent, "Sahre"));
		// break;
		case R.id.menu_contact:
			intent = new Intent(Intent.ACTION_SEND);
			intent.setType("message/rfc822");
			intent.putExtra(Intent.EXTRA_EMAIL,
					new String[] { "immopolyteam@gmail.com" });
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
		mTracker.stopSession();
	}

	@Override
	public void onTrackPageView(String page) {
		mTracker.trackPageView(page);
	}

	@Override
	public void onTrackEvent(String category, String action, String label, int i) {
		mTracker.trackEvent(category, action, label, i);
	}

	public void signIn() {
		String authUrl = "";
		SharedPreferences shared = getSharedPreferences("oauth", 0);
		String accessToken = shared.getString("oauth_token", "");
		if (accessToken.length() > 0) {
			OAuthData.getInstance(this).signedIn = true;
			OAuthData.getInstance(this).accessToken = accessToken;

		} else {
			OAuthData.getInstance(this).signedIn = false;
			try {
				authUrl = OAuthData.getInstance(this).provider
						.retrieveRequestToken(
								OAuthData.getInstance(this).consumer,
								OAuth.OUT_OF_BAND);
			} catch (OAuthMessageSignerException e) {
				e.printStackTrace();
			} catch (OAuthNotAuthorizedException e) {
				e.printStackTrace();
			} catch (OAuthExpectationFailedException e) {
				e.printStackTrace();
			} catch (OAuthCommunicationException e) {
				e.printStackTrace();
			}

			// Login in web view
			Intent i = new Intent(this, OauthLoginActivity.class);
			i.putExtra("oauth_url", authUrl);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
		}

	}

}
