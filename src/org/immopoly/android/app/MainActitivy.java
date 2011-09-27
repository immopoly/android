package org.immopoly.android.app;

import org.immopoly.android.R;
import org.immopoly.android.fragments.MapFragment;


import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;



public class MainActitivy extends FragmentActivity {
	
	MapFragment mMap;
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.main);
		mMap = new MapFragment();
		FragmentManager fm = getSupportFragmentManager();
		
		fm.beginTransaction().add(R.id.root, mMap).commit();
	}
}
