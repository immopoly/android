package org.immopoly.android.fragments;

import org.immopoly.android.adapter.HistoryAdapter;
import org.immopoly.android.constants.Const;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;

public class HistoryFragment extends ListFragment {
	@Override
	public void onActivityCreated(Bundle arg0) {
		super.onActivityCreated(arg0);
		setListAdapter(new HistoryAdapter(getActivity()));
	}

	@Override
	public void onResume() {
		super.onResume();
		// wir werden angezeigt
		Log.i(Const.LOG_TAG, "zeich dem user die history");
	}

}
