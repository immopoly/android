package org.immopoly.android.fragments;

import org.immopoly.android.R;
import org.immopoly.android.adapter.PortfolioFlatsAdapter;
import org.immopoly.android.app.ImmopolyActivity;
import org.immopoly.android.app.UserDataListener;
import org.immopoly.android.app.UserDataManager;
import org.immopoly.android.constants.Const;
import org.immopoly.android.helper.OnTrackingEventListener;
import org.immopoly.android.helper.TrackingManager;
import org.immopoly.android.model.Flat;
import org.immopoly.android.model.Flats;
import org.immopoly.android.model.ImmopolyUser;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

public class PortfolioListFragment extends ListFragment implements UserDataListener, OnClickListener {
	private Flats mFlats;
	private OnTrackingEventListener mEventListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mEventListener = (OnTrackingEventListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnTrackingEventListener");
		}
	}

	// @Override
	// public View onCreateView(LayoutInflater inflater, ViewGroup container,
	// Bundle savedInstanceState) {
	// return inflater.inflate(R.layout.fragment_portfolio_list, null);
	// }

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		UserDataManager.instance.addUserDataListener(this);
		getListView().setDividerHeight(0);
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

		mEventListener.onTrackEvent(TrackingManager.CATEGORY_CLICKS, TrackingManager.ACTION_EXPOSE,
				TrackingManager.LABEL_EXPOSE_PORTFOLIO, 0);

	}

	@Override
	public void onUserDataUpdated() {
		Log.i(Const.LOG_TAG, "PortfolioMapFragment.onUserDataUpdated!!");
		updateList();
	}

	private void updateList() {
		setListAdapter(null);
		if (UserDataManager.instance.getState() == UserDataManager.LOGGED_IN) {
			ImmopolyUser user = ImmopolyUser.getInstance();
			mFlats = user.getPortfolio();

			if (mFlats != null && mFlats.size() > 0) {
				setListAdapter(new PortfolioFlatsAdapter(getActivity(), mFlats));

				LayoutInflater inflater = LayoutInflater.from(getActivity());
				addMapButton(inflater);
			} else {
				setEmptyText("Noch keine Wohnungen im Portfolio");
			}

			// if (mFlats != null && mFlats.size() > 0) {
			// TextView view;
			// LayoutInflater inflater = LayoutInflater.from(getActivity());
			// if (getListView().getHeaderViewsCount() == 0) {
			// view = (TextView)
			// inflater.inflate(R.layout.portfolio_list_header, null);
			// getListView().addHeaderView(view);
			// } else {
			// view = (TextView)
			// getView().findViewById(R.id.portfolio_header_text);
			// }
			// view.setText(getResources().getString(R.string.flats_number,
			// mFlats.size(), MAX_NUMBER_FLATS));
			//
			// setListAdapter(new PortfolioFlatsAdapter(getActivity(), mFlats));
			// addMapButton(inflater);
			// } else {
			// ImageView view = (ImageView) getListView().getEmptyView();
			// view.setImageResource(R.drawable.portfolio_fallback);
			// }

		} else {
			setEmptyText("Anmelden um dein Portfolio zu sehen");
		}
	}

	private void addMapButton(LayoutInflater inflater) {
		if (getView().findViewById(R.id.portfolio_btn_map) == null) {
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