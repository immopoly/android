package org.immopoly.android.app;

import org.immopoly.android.R;
import org.immopoly.android.fragments.HudFragment;
import org.immopoly.android.fragments.MapFragment;
import org.immopoly.android.fragments.callbacks.HudCallbacks;
import org.immopoly.android.helper.HudPopupHelper;
import org.immopoly.android.helper.LocationHelper;
import org.immopoly.android.helper.TrackingManager;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.Window;



public class MainActitivy extends FragmentActivity implements HudCallbacks {
	
	private HudPopupHelper mHudPopup;
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		mHudPopup = new HudPopupHelper(this, HudPopupHelper.TYPE_FINANCE_POPUP);
		//mMap = new MapFragment();
		//mHud = new HudFragment();
		//FragmentManager fm = getSupportFragmentManager();
		//fm.beginTransaction().add(mHud,"hud").commit();
		//fm.beginTransaction().add(R.id.root, mMap).commit();
	}
	@Override
	public void updateHud(Intent data, int element) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (mHudPopup != null) {
			mHudPopup.dismiss();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}
	@Override
	public void onHudAction(View view) {
		switch (view.getId()) {
		case R.id.hud_map:
			// hud map
			// already there
			LocationHelper.getLastLocation(this);
			break;
		case R.id.hud_portfolio:
			startActivity(new Intent(this, DashboardActivity.class));
			break;
		case R.id.hud_profile:
			startActivity(new Intent(this, DashboardActivity.class));
			break;
		case R.id.hud_text:
			// Toast.makeText(this, ImmopolyUser.getInstance().flats.toString(),
			// Toast.LENGTH_LONG);
			if (mHudPopup != null) {
				mHudPopup.show(findViewById(R.id.hud_text), -200, -60);
			}
			break;
		default:
			break;
		}
	}
}
