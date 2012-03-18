package org.immopoly.android.fragments;

import java.util.List;

import org.immopoly.android.adapter.HistoryAdapter;
import org.immopoly.android.app.UserDataListener;
import org.immopoly.android.app.UserDataManager;
import org.immopoly.android.model.Flat;
import org.immopoly.android.model.Flats;
import org.immopoly.android.model.ImmopolyHistory;
import org.immopoly.android.model.ImmopolyUser;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

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
		int userState = UserDataManager.instance.getState();
		if ( userState == UserDataManager.LOGGED_IN) {
			setListAdapter(new HistoryAdapter(getActivity()));
			setEmptyText("Noch keine Einträge in der History");
		} else if ( userState == UserDataManager.LOGIN_PENDING ) {
			setEmptyText("Anmeldung läuft...");
		} else {
			setEmptyText("Anmelden um hier History Einträge zu sehen");
		}
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		List<ImmopolyHistory> history = ImmopolyUser.getInstance().getHistory();
		ImmopolyHistory entry = history.get(position);
		
		long fid = entry.getExposeId();
		
		// get flat object from users portfolio or create empty Flat object with just an id
		Flats userFlats = ImmopolyUser.getInstance().getPortfolio();
		Flat flat = null;
		for ( Flat f : userFlats ) {
			if ( f.uid == fid ) {
				flat = f;
				break;
			}
		}
		if ( flat == null ) {
			flat = new Flat();
			flat.uid = (int) fid;
		}
		
		DialogFragment newFragment = ExposeFragment.newInstance(flat);
		newFragment.show(getActivity().getSupportFragmentManager(), "dialog");
	}
}