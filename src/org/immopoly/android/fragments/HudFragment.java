package org.immopoly.android.fragments;

import java.text.NumberFormat;
import java.util.Locale;

import org.immopoly.android.R;
import org.immopoly.android.app.UserSignupActivity;
import org.immopoly.android.constants.Const;
import org.immopoly.android.helper.HudPopupHelper;
import org.immopoly.android.model.ImmopolyUser;
import org.immopoly.android.provider.UserProvider;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
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

public class HudFragment extends Fragment implements OnClickListener,
		LoaderCallbacks<Cursor> {

	private HudPopupHelper mHudPopup;

	public interface OnHudEventListener {
		public void updateHud(Intent data, int element);

		public void onHudAction(View view);
	}

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.hud, container);
		((Button) view.findViewById(R.id.hud_text)).setOnClickListener(this);
		mHudPopup = new HudPopupHelper(getActivity(),
				HudPopupHelper.TYPE_FINANCE_POPUP);
		return view;
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (mHudPopup != null) {
			mHudPopup.dismiss();
		}
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

	public void updateHud(Intent data, int element) {
		Button hudButton = (Button) getView().findViewById(R.id.hud_text);
		if (hudButton != null) {
			if (ImmopolyUser.getInstance().getToken().length() > 0) {
				NumberFormat nFormat = NumberFormat
						.getCurrencyInstance(Locale.GERMANY);
				nFormat.setMinimumIntegerDigits(1);
				nFormat.setMaximumFractionDigits(0);
				hudButton.setText(nFormat.format(ImmopolyUser.getInstance()
						.getBalance()));
			} else {
				hudButton.setText(R.string.login_button);
			}
		}
	}

	@Override
	public void onClick(View arg0) {
		if (mHudPopup != null) {
			if (ImmopolyUser.getInstance().getToken().length() > 0) {
				mHudPopup.show(getView().findViewById(R.id.hud_text), -200, -60);
			} else {
				Intent intent2 = new Intent(getActivity(),
						UserSignupActivity.class);
				startActivityForResult(intent2,Const.USER_SIGNUP);
			}
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode,
            Intent data) {
		if(requestCode == Const.USER_SIGNUP && resultCode == Activity.RESULT_OK){
			updateHud(null, 1);
			onClick(null);
		}
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(), UserProvider.CONTENT_URI_USER,
				null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		updateHud(null, 1);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}
	
	
}
