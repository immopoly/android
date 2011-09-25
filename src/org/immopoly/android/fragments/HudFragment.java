package org.immopoly.android.fragments;

import org.immopoly.android.R;
import org.immopoly.android.fragments.callbacks.HudCallbacks;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HudFragment extends Fragment implements HudCallbacks {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.hud, container);
	}

	@Override
	public void updateHud(Intent data, int element) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onHudAction(View view) {
		switch (view.getId()) {
		case R.id.hud_map:
			// hud map
			break;
		case R.id.hud_portfolio:
			break;
		case R.id.hud_profile:
			break;
		case R.id.hud_text:
			break;
		default:
			break;
		}
	}

}
