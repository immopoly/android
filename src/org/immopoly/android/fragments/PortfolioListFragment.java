package org.immopoly.android.fragments;

import org.immopoly.android.R;
import org.immopoly.android.adapter.PortfolioFlatsAdapter;
import org.immopoly.android.app.ImmopolyActivity;
import org.immopoly.android.app.UserDataListener;
import org.immopoly.android.app.UserDataManager;
import org.immopoly.android.constants.Const;
import org.immopoly.android.model.Flat;
import org.immopoly.android.model.Flats;
import org.immopoly.android.model.ImmopolyUser;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;

public class PortfolioListFragment extends ListFragment implements UserDataListener, OnClickListener {

	private Flats mFlats;

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		UserDataManager.instance.addUserDataListener(this);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		updateList();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onDestroyView() {
		UserDataManager.instance.removeUserDataListener(this);
		super.onDestroyView();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Flat flat = mFlats.get(position);
		DialogFragment newFragment = ExposeFragment.newInstance(flat);
		newFragment.show(getActivity().getSupportFragmentManager(), "dialog");
	}

	@Override
	public void onUserDataUpdated() {
		Log.i(Const.LOG_TAG, "PortfolioMapFragment.onUserDataUpdated!!");
		updateList();
	}

	private void updateList() {

		if (UserDataManager.instance.getState() == UserDataManager.LOGGED_IN) {
			ImmopolyUser user = ImmopolyUser.getInstance();
			mFlats = user.getPortfolio();

			if (mFlats != null && mFlats.size() > 0) {
				setListAdapter(new PortfolioFlatsAdapter(getActivity(), mFlats));
				setListShown(true);
				addMapButton();
			} else {
				setEmptyText("Keine Wohnungen");
				setListAdapter(null);
			}
		} else {
			setEmptyText("Nicht angemeldet");
			setListAdapter(null);
		}
	}

	private void addMapButton() {
		if (getView().findViewById(R.id.portfolio_btn_map) == null) {
			LayoutInflater inflater = LayoutInflater.from(getActivity());
			ViewGroup viewGroup = (ViewGroup) getView();
			inflater.inflate(R.layout.image_btn_map, viewGroup);
			viewGroup.findViewById(R.id.portfolio_btn_map).setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View v) {
		Log.i(Const.LOG_TAG, "PortfolioMapFragment show listg");
		((ImmopolyActivity) getActivity()).getTabManager().onTabChanged("portfolio_map");
	}
}