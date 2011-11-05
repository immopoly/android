package org.immopoly.android.fragments;

import java.text.NumberFormat;
import java.util.Locale;

import org.immopoly.android.R;
import org.immopoly.android.model.ImmopolyUser;
import org.immopoly.android.provider.UserProvider;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class HudFragment extends Fragment implements OnClickListener, LoaderCallbacks<Cursor> {

	public interface OnHudEventListener {
		public void updateHud(Intent data, int element);
		public void onHudAction(View view);
	}

	private IntentFilter mHudFilter;

	class UserObserver extends ContentObserver {

		public UserObserver(Handler handler) {
			super(handler);
			// updateHud(null, 1);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
		}
	}

	private Handler mHandler = new Handler();
	private UserObserver mUserObserver;

	@Override
	public void onAttach(Activity arg0) {
		super.onAttach(arg0);
		// mHudFilter = new IntentFilter();
		// mHudFilter.
		// arg0.registerReceiver(mBroadcastReceiver, )
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.hud, container);
		((Button) view.findViewById(R.id.hud_text)).setOnClickListener(this);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerObserver();
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		unregisterObserver();
	}

	private void registerObserver() {
		ContentResolver cr = getActivity().getContentResolver();
		mUserObserver = new UserObserver(mHandler);
		cr.registerContentObserver(UserProvider.CONTENT_URI_USER, true, mUserObserver);
	}

	private void unregisterObserver() {
		ContentResolver cr = getActivity().getContentResolver();
		if (mUserObserver != null) {
			cr.unregisterContentObserver(mUserObserver);
			mUserObserver = null;
		}
	}

	public void updateHud(Intent data, int element) {
		Button hudButton = (Button) getView().findViewById(R.id.hud_text);
		if (hudButton != null) {
			NumberFormat nFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY);
			nFormat.setMinimumIntegerDigits(1);
			nFormat.setMaximumFractionDigits(2);
			hudButton.setText(nFormat.format(ImmopolyUser.getInstance().getBalance()));
		}
	}

	@Override
	public void onClick(View arg0) {

	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(), UserProvider.CONTENT_URI_USER, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		updateHud(null, 1);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}
}
