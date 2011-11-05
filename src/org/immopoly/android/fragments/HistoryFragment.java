package org.immopoly.android.fragments;

import org.immopoly.android.adapter.HistoryAdapter;

import android.os.Bundle;
import android.support.v4.app.ListFragment;

public class HistoryFragment extends ListFragment {
	@Override
	public void onActivityCreated(Bundle arg0) {
		super.onActivityCreated(arg0);
		setListAdapter(new HistoryAdapter(getActivity()));
	}
}
