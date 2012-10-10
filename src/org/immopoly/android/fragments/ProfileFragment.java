package org.immopoly.android.fragments;

import org.immopoly.android.R;
import org.immopoly.android.app.UserDataListener;
import org.immopoly.android.app.UserDataManager;
import org.immopoly.android.constants.Const;
import org.immopoly.android.helper.TrackingManager;
import org.immopoly.android.model.ImmopolyUser;
import org.immopoly.android.widget.BadgesView;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class ProfileFragment extends Fragment implements UserDataListener {

	private GoogleAnalyticsTracker mTracker;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mTracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker in manual dispatch mode...
		mTracker.startNewSession(TrackingManager.UA_ACCOUNT,
				Const.ANALYTICS_INTERVAL, getActivity().getApplicationContext());

		mTracker.trackPageView(TrackingManager.VIEW_PROFILE);

		View layout = inflater.inflate(R.layout.fragment_profile, container,
				false);
		UserDataManager.instance.addUserDataListener(this);
		updateVisibility(layout);

		return layout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		((BadgesView)(getView().findViewById(R.id.gridview))).initWithBadges(ImmopolyUser.getInstance().getBadges(), mTracker);

		((TextView) getView().findViewById(R.id.username)).setText(ImmopolyUser
				.getInstance().getUserName());
	}

	private void updateVisibility(View v) {
		if (null == v)
			return;
		if (UserDataManager.instance.getState() == UserDataManager.LOGGED_IN) {
			// helptext deaktiveren
			v.findViewById(R.id.profile_notloggedin).setVisibility(View.GONE);
		} else {
			v.findViewById(R.id.profile_notloggedin)
					.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onDestroyView() {
		mTracker.stopSession();
		UserDataManager.instance.removeUserDataListener(this);
		super.onDestroyView();
	}

	@Override
	public void onUserDataUpdated() {
		updateVisibility(getView());
	}
}
