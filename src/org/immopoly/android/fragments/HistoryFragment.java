package org.immopoly.android.fragments;

import org.immopoly.android.adapter.HistoryAdapter;
import org.immopoly.android.app.UserDataListener;
import org.immopoly.android.app.UserDataManager;
import org.immopoly.android.constants.Const;

import android.os.Bundle;
import android.os.Debug;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;

public class HistoryFragment extends ListFragment implements UserDataListener{
	@Override
	public void onActivityCreated(Bundle arg0) {
		super.onActivityCreated(arg0);
		getListView().setDivider(null);
	}

	@Override
	public void onResume() {
		super.onResume();
		onUserDataUpdated();
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		UserDataManager.instance.addUserDataListener(this);
	}

	@Override
	public void onDestroyView() {
		UserDataManager.instance.removeUserDataListener(this);
		super.onDestroyView();
	}

	@Override
	public void onUserDataUpdated() {
		setListAdapter(null);
		if (UserDataManager.instance.getState() == UserDataManager.LOGGED_IN) {
			setListAdapter(new HistoryAdapter(getActivity()));
			setEmptyText("Noch keine Einträge in der History");
		} else {
			setEmptyText("Anmelden um hier History Einträge zu sehen");
		}
	}
}