package org.immopoly.android.fragments;

import org.immopoly.android.R;
import org.immopoly.android.model.Flat;
import org.immopoly.android.model.Flats;
import org.immopoly.android.widget.FlatSelectListener;
import org.immopoly.android.widget.ImmoscoutPlacesOverlay;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.google.android.maps.MapView;

public class PortfolioMapFragment extends Fragment implements FlatSelectListener {

	private MapView mMapView;
	private ImmoscoutPlacesOverlay flatsOverlay;
	private boolean dualPane;

	public PortfolioMapFragment() {
		Log.w( "IMPO", "PortfolioMapFragment created." );
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mMapView = new MapView( getActivity(), getString( R.string.google_maps_key_debug ) );
		mMapView.setLayoutParams( new LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT ) );
		mMapView.setBuiltInZoomControls(true);
		mMapView.setClickable( true );
		
		flatsOverlay = new ImmoscoutPlacesOverlay( 
				getActivity(), mMapView, getActivity().getLayoutInflater() );
		mMapView.getOverlays().add( flatsOverlay );
		return mMapView;
	}

	@Override
	public void onActivityCreated(Bundle arg0) {
        dualPane = getActivity().findViewById(R.id.pf_switch_frame) == null;
		flatsOverlay.setUseBubble( ! dualPane );
		super.onActivityCreated(arg0);
	}

	@Override
	public void onDestroyView() {
		mMapView = null;
		super.onDestroyView();
	}

	public void setFlats(Flats flats) {
		flatsOverlay.setFlats( flats );	
	}

	@Override
	public void flatSelected(Flat flat) {
		Log.w("IMPO", "PortfolioMapFragment.flatSelected" );
		flatsOverlay.selectFlat( flat );
	}

	@Override
	public void flatClusterSelected(Flats flats) {}
}
