package org.immopoly.android.fragments;

import java.text.NumberFormat;
import java.util.Locale;

import org.immopoly.android.R;
import org.immopoly.android.app.UserDataListener;
import org.immopoly.android.app.UserDataManager;
import org.immopoly.android.constants.Const;
import org.immopoly.android.helper.HudPopupHelper;
import org.immopoly.android.model.ImmopolyUser;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;

public class HudFragment extends Fragment implements OnClickListener, UserDataListener
{
	private HudPopupHelper mHudPopup;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.hud, container);
		((Button) view.findViewById(R.id.hud_text)).setOnClickListener(this);
		mHudPopup = new HudPopupHelper(getActivity(),
				HudPopupHelper.TYPE_FINANCE_POPUP);
		UserDataManager.instance.addUserDataListener( this );
		// update HUD asap. but not now, because there is no view yet
		Runnable updateRunnable = new Runnable() {  
			public void run() {
				if ( getView() != null )
					updateHud();
				else  // this sometimes happens, try again
					new Handler().postDelayed( this, 100 );
			}
		};
		new Handler().post( updateRunnable );
		return view;
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mHudPopup != null) {
			mHudPopup.dismiss();
		}
	}

	@Override
	public void onDestroyView() {
		UserDataManager.instance.removeUserDataListener( this );
		super.onDestroyView();
	}

	public void updateHud() {
		// Log.i(Const.LOG_TAG, "updateHud " +
		// UserDataManager.instance.getState());
		Button hudButton = (Button) getView().findViewById(R.id.hud_text);
		View spacer = (View) getView().findViewById(R.id.hud_progress_spacer);
		View progress = (View) getView().findViewById(R.id.hud_progress);
		if (hudButton != null) {
			if ( UserDataManager.instance.getState() == UserDataManager.LOGGED_IN ) {
				progress.setVisibility(View.GONE);
				spacer.setVisibility(View.GONE);
				hudButton.setVisibility(View.VISIBLE);
				NumberFormat nFormat = NumberFormat
						.getCurrencyInstance(Locale.GERMANY);
				nFormat.setMinimumIntegerDigits(1);
				nFormat.setMaximumFractionDigits(0);
				hudButton.setText(nFormat.format(ImmopolyUser.getInstance()
						.getBalance()));
			} else if (UserDataManager.instance.getState() == UserDataManager.USER_UNKNOWN) {
				progress.setVisibility(View.GONE);
				spacer.setVisibility(View.GONE);
				hudButton.setVisibility(View.VISIBLE);
				hudButton.setText(R.string.login_button);
				// progress.setVisibility(View.VISIBLE);
				// spacer.setVisibility(View.VISIBLE);
				// hudButton.setVisibility(View.GONE);
			} else if (UserDataManager.instance.getState() == UserDataManager.LOGIN_PENDING) {
				progress.setVisibility(View.VISIBLE);
				spacer.setVisibility(View.VISIBLE);
				hudButton.setVisibility(View.GONE);
			} else {
				Log.i(Const.LOG_TAG, "unknown state " + UserDataManager.instance.getState());
			}
		}
	}

	@Override
	public void onClick(View arg0) {
		if (mHudPopup != null) {
			if (UserDataManager.instance.getState() == UserDataManager.LOGGED_IN && !mHudPopup.isShowing()) {
				mHudPopup.show(getView().findViewById(R.id.hud_text), -200, -60);
			} else {
				UserDataManager.instance.login();
			}
		}
	}

	@Override
	public void onUserDataUpdated() {
		updateHud();
	}
	
}
