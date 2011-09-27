package org.immopoly.android.fragments;

import java.text.NumberFormat;
import java.util.Locale;

import org.immopoly.android.R;
import org.immopoly.android.fragments.callbacks.HudCallbacks;
import org.immopoly.android.model.ImmopolyUser;
import org.immopoly.android.provider.AUserObserver;
import org.immopoly.android.provider.UserProvider;

import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class HudFragment extends Fragment implements HudCallbacks {

	class UserObserver extends AUserObserver {

		public UserObserver(Handler handler) {
			super(handler);
			updateHud(null, 1);
		}

	}

	private Handler mHandler = new Handler();
	private UserObserver mUserObserver;
	private Button mHudText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CursorLoader cursorLoader = new CursorLoader(getActivity(),
				UserProvider.CONTENT_URI_USER, null, null, null, null);
		cursorLoader.loadInBackground();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.hud, container);
		mHudText = (Button) view.findViewById(R.id.hud_text);
		registerObserver();
		return view;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		unregisterObserver();
	}
	
	private void registerObserver() {
		ContentResolver cr = getActivity().getContentResolver();
		mUserObserver = new UserObserver(mHandler);
		cr.registerContentObserver(UserProvider.CONTENT_URI_USER, true,
				mUserObserver);
	}

	private void unregisterObserver() {
		ContentResolver cr = getActivity().getContentResolver();
		if (mUserObserver != null) {
			cr.unregisterContentObserver(mUserObserver);
			mUserObserver = null;
		}
	}

	@Override
	public void updateHud(Intent data, int element) {
		if (mHudText != null) {
			NumberFormat nFormat = NumberFormat
					.getCurrencyInstance(Locale.GERMANY);
			nFormat.setMinimumIntegerDigits(1);
			nFormat.setMaximumFractionDigits(2);
			mHudText.setText(nFormat.format(ImmopolyUser.getInstance()
					.getBalance()));
		}
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
