/**
 * 
 */
package org.immopoly.android.app;

import org.immopoly.android.R;
import org.immopoly.android.constants.Const;
import org.immopoly.android.fragments.ExposeFragment;
import org.immopoly.android.fragments.MapFragment;
import org.immopoly.android.fragments.MapFragment.OnMapItemClickedListener;
import org.immopoly.android.fragments.callbacks.HudCallbacks;

import android.content.Intent;
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
public class ImmopolyActivity extends FragmentActivity implements OnMapItemClickedListener, HudCallbacks{

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

	/**
	 * Init the game
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.immopoly_activity);
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

	void showFragment(int which, Bundle bundle) {
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

}
