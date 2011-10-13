package org.immopoly.android.fragments;

import org.immopoly.android.R;
import org.immopoly.android.app.ImmopolyActivity;
import org.immopoly.android.model.Flat;
import org.immopoly.android.model.Flats;
import org.immopoly.android.provider.FlatsProvider;
import org.immopoly.android.widget.ImmoscoutPlacesOverlay;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.google.android.maps.MapView;

public class PortfolioMapFragment extends Fragment {

	private MapView mMapView;
	private ImmoscoutPlacesOverlay flatsOverlay;
	private Flats mFlats;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.i( "IMPO", "PortfolioMapFragment.onCreateView" );
		mFlats = queryFlats();
		View layout = getActivity().getLayoutInflater().inflate( R.layout.portfolio_map, null, false );
		
		mMapView = ((ImmopolyActivity) getActivity()).acquireMapView( this );
		flatsOverlay = new ImmoscoutPlacesOverlay( this, mMapView, getActivity().getLayoutInflater() );
		flatsOverlay.setFlats( mFlats );
		mMapView.getOverlays().add( flatsOverlay );

		FrameLayout mapFrame = (FrameLayout) layout.findViewById( R.id.pf_map_frame );
		mapFrame.addView( mMapView );

		layout.findViewById( R.id.pf_list_btn ).setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				Log.i( "IMPO", "PortfolioMapFragment show listg" );
				((ImmopolyActivity) getActivity()).showFragment( ImmopolyActivity.PORTFOLIO_LIST_FRAGMENT, null );
			}
		});
		
		return layout;
	}

	@Override
	public void onDestroyView() {
		((ImmopolyActivity) getActivity()).releaseMapView( this );
		super.onDestroyView();
	}
	
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
	
}
