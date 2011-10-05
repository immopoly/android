package org.immopoly.android.fragments;

import org.immopoly.android.adapter.PortfolioFlatsAdapter;
import org.immopoly.android.model.Flat;
import org.immopoly.android.model.Flats;
import org.immopoly.android.widget.FlatSelectListener;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class PortfolioListFragment extends ListFragment implements FlatSelectListener {

	private Flats flats;
	private Flats selectedFlats;
	private PortfolioFlatsAdapter adapter;

	
	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
		if ( flats != null )
			setListAdapter( new PortfolioFlatsAdapter( getActivity(), flats ) );
		return super.onCreateView( inflater, container, savedInstanceState );
	}

	
	public void setFlats( Flats flats ) {
		this.flats = flats;
		if ( isAdded() ) {
			adapter = new PortfolioFlatsAdapter( getActivity(), flats );
			setListAdapter( adapter );
		}
	}
	
	@Override
	public void onListItemClick( ListView listView, View itemView, int idx, long id ) {
		if ( flats == null || flats.isEmpty() )
			return;
		FlatSelectListener listener = (FlatSelectListener) getActivity();
		listener.flatSelected( flats.get(idx) );
		Log.w("IMPO", "PortfolioListFragment.onListItemClick: " + idx );
		//		adapter.setSelection( selectedFlats );
	}

//	public void selectFlats( Flats selectedFlats ) {
//		if ( this.selectedFlats == selectedFlats )
//			return;
//		adapter.setSelection( selectedFlats );
//		if ( selectedFlats == null || selectedFlats.isEmpty() )
//			Log.w( "IMPO", "PortfolioListFragment created." );
//		getListView().setSelection( flats.indexOf( selectedFlats.get(0) ) );
//	}


	@Override
	public void flatSelected(Flat flat) {
		Log.w("IMPO", "PortfolioListFragment.flatSelected" );
//		if ( this.selectedFlats == flat )
//			return;
		adapter.flatSelected(flat);
	}


	@Override
	public void flatClusterSelected(Flats flats) {
		Log.w("IMPO", "PortfolioListFragment.flatClusterSelected" );
		adapter.flatClusterSelected(flats);
		
	}
}
