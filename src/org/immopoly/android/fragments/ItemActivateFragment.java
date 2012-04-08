package org.immopoly.android.fragments;

import org.immopoly.android.app.ImmopolyActivity;
import org.immopoly.android.constants.Const;
import org.immopoly.android.helper.TrackingManager;
import org.immopoly.android.model.ImmopolyActionItem;
import org.immopoly.android.model.ImmopolyUser;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

public class ItemActivateFragment extends DialogFragment {

	private int mItemId;
	private GoogleAnalyticsTracker mTracker;

	public static ItemActivateFragment newInstance(int item) {
		ItemActivateFragment f = new ItemActivateFragment();

		Bundle bundle = new Bundle();
		bundle.putInt("item", item);
		f.setArguments(bundle);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mItemId = getArguments().getInt("item");
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mTracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker in manual dispatch mode...
		mTracker.startNewSession(TrackingManager.UA_ACCOUNT,
				Const.ANALYTICS_INTERVAL, getActivity().getApplicationContext());

		mTracker.trackPageView(TrackingManager.VIEW_ACTION_ITEM_SPY);
		
		ImmopolyActionItem item = ImmopolyUser.getInstance().getActionItems().get(mItemId);

		return new AlertDialog.Builder(getActivity()).setTitle(item.getText()).setMessage(item.getDescription())
				.setPositiveButton("Aktivieren", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Fragment fragment = getFragmentManager().findFragmentByTag(ImmopolyActivity.FRAGMENT_MAP);
						if (fragment instanceof MapFragment) {
							((MapFragment) fragment).filterFreeFlats();
//							dismiss();
						}
					}
				}).setNegativeButton("Abbrechen", null).create();
	}
	
	@Override
	public void onDestroyView() {
		mTracker.stopSession();
		super.onDestroyView();
	}
}
