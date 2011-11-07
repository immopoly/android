package org.immopoly.android.fragments;

import org.immopoly.android.R;
import org.immopoly.android.app.ImmopolyActivity;
import org.immopoly.android.constants.Const;
import org.immopoly.android.model.Flats;
import org.immopoly.android.model.ImmopolyUser;
import org.immopoly.android.widget.ImmoscoutPlacesOverlay;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.maps.MapView;

public class PortfolioMapFragment extends Fragment {

	private MapView mMapView;
	private ImmoscoutPlacesOverlay flatsOverlay;
	private Flats mFlats;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.i( Const.LOG_TAG, "PortfolioMapFragment.onCreateView" );
		View layout = getActivity().getLayoutInflater().inflate( R.layout.portfolio_map, null, false );
		
		ImmopolyUser user = ImmopolyUser.getInstance();
		mFlats = user.getPortfolio();
		
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

}
