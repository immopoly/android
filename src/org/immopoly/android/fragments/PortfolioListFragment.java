package org.immopoly.android.fragments;

import org.immopoly.android.R;
import org.immopoly.android.adapter.PortfolioFlatsAdapter;
import org.immopoly.android.app.ImmopolyActivity;
import org.immopoly.android.constants.Const;
import org.immopoly.android.model.Flat;
import org.immopoly.android.model.Flats;
import org.immopoly.android.provider.FlatsProvider;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class PortfolioListFragment extends Fragment implements OnItemClickListener{

	private Flats flats;

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
		flats = queryFlats();
		View layout = getActivity().getLayoutInflater().inflate( R.layout.portfolio_list, null, false );
		ListView listView = (ListView) layout.findViewById(R.id.pf_list_view);
		if ( flats != null )
			listView.setAdapter( new PortfolioFlatsAdapter( getActivity(), flats ) );
	
		listView.setOnItemClickListener( this );
		
		layout.findViewById( R.id.pf_map_btn ).setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				Log.i(Const.LOG_TAG, "PortfolioMapFragment show listg");
				((ImmopolyActivity) getActivity()).showFragment( ImmopolyActivity.PORTFOLIO_MAP_FRAGMENT, null );
			}
		});
		
		return layout;
	}
	
	
//	@Override
//	public void onListItemClick( ListView listView, View itemView, int idx, long id ) {
//		if ( flats == null || flats.isEmpty() )
//			return;
//		FlatSelectListener listener = (FlatSelectListener) getActivity();
//		listener.flatSelected( flats.get(idx) );
//		adapter.setSelection( selectedFlats );
//	}

	// TODO flats vom server nehmen
    private Flats queryFlats() {
        Cursor cur = getActivity().getContentResolver().query(FlatsProvider.CONTENT_URI, null, null, null, null);
        Flats flats = new Flats();
        if ( cur.moveToFirst() )
            do {
            	Flat flat         = new Flat();
            	flat.uid          = cur.getInt( cur.getColumnIndex( FlatsProvider.Flat.FLAT_ID ) );
            	flat.name         = cur.getString( cur.getColumnIndex( FlatsProvider.Flat.FLAT_NAME ) );
            	flat.description  = cur.getString( cur.getColumnIndex( FlatsProvider.Flat.FLAT_DESCRIPTION ) );
            	// TODO lat & lng are swapped in the DB !?
            	flat.lng          = cur.getDouble( cur.getColumnIndex( FlatsProvider.Flat.FLAT_LATITUDE ) );
            	flat.lat          = cur.getDouble( cur.getColumnIndex( FlatsProvider.Flat.FLAT_LONGITUDE ) );
            	flat.creationDate = cur.getInt( cur.getColumnIndex( FlatsProvider.Flat.FLAT_CREATIONDATE ) ) * 1000;
            	flat.owned        = true;
            	flats.add( flat );
            } while ( cur.moveToNext() );
        cur.close();
        return flats;
    }


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Flat flat = flats.get( position );
		((OnMapItemClickedListener) getActivity()).onMapOverlayClicked(flat.uid, flat.owned);
	}

}
