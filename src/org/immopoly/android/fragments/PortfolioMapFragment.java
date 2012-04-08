package org.immopoly.android.fragments;

import org.immopoly.android.R;
import org.immopoly.android.app.ImmopolyActivity;
import org.immopoly.android.app.UserDataListener;
import org.immopoly.android.app.UserDataManager;
import org.immopoly.android.constants.Const;
import org.immopoly.android.helper.TrackingManager;
import org.immopoly.android.model.Flat;
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

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

public class PortfolioMapFragment extends Fragment implements UserDataListener {

	private MapView mMapView;
	private ImmoscoutPlacesOverlay flatsOverlay;
	private Flats mFlats;
	private GoogleAnalyticsTracker mTracker;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mTracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker in manual dispatch mode...
		mTracker.startNewSession(TrackingManager.UA_ACCOUNT,
				Const.ANALYTICS_INTERVAL, getActivity().getApplicationContext());

		mTracker.trackPageView(TrackingManager.VIEW_PORTFOLIO_MAP);
		Log.i(Const.LOG_TAG, "PortfolioMapFragment.onCreateView");
		View layout = inflater.inflate(R.layout.portfolio_map, null, false);

		ImmopolyUser user = ImmopolyUser.getInstance();
		mFlats = user.getPortfolio();

		mMapView = ((ImmopolyActivity) getActivity()).acquireMapView(this);
		flatsOverlay = new ImmoscoutPlacesOverlay(this, mMapView, inflater, true);
		flatsOverlay.setFlats(mFlats);
		mMapView.getOverlays().add(flatsOverlay);

		FrameLayout mapFrame = (FrameLayout) layout.findViewById(R.id.pf_map_frame);
		mapFrame.addView(mMapView, 0);

		layout.findViewById(R.id.portfolio_btn_list).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.i("IMPO", "PortfolioMapFragment show listg");
				((ImmopolyActivity) getActivity()).getTabManager().onTabChanged("portfolio");
			}
		});

		UserDataManager.instance.addUserDataListener(this);

		// set map to show all flats
		double minLat = Double.MAX_VALUE, maxLat = Double.MIN_VALUE, minLon = Double.MAX_VALUE, maxLon = Double.MIN_VALUE;
		for (Flat f : mFlats) {
			if ( f.lat < minLat ) minLat = f.lat;
			if ( f.lat > maxLat ) maxLat = f.lat;
			if ( f.lng < minLon ) minLon = f.lng;
			if ( f.lng > maxLon ) maxLon = f.lng;
		}
		Log.i( Const.LOG_TAG, "LAT MIN: " + minLat + "  MAX: " + maxLat );
		Log.i( Const.LOG_TAG, "LON MIN: " + minLon + "  MAX: " + maxLon );
		mMapView.getController().setCenter( new GeoPoint( (int) ((minLat+maxLat)/2 * 1E6), (int) ((minLon+maxLon)/2*1E6) ));
		mMapView.getController().zoomToSpan( (int) ((maxLat-minLat) * 1E6 * 1.1), (int) ((maxLon-minLon) * 1E6 * 1.1));
		
		return layout;
	}

	@Override
	public void onDestroyView() {
		mTracker.stopSession();
		((ImmopolyActivity) getActivity()).releaseMapView(this);
		super.onDestroyView();
	}

	@Override
	public void onUserDataUpdated() {
		// TODO check my vilibility
		mFlats = ImmopolyUser.getInstance().getPortfolio();
		flatsOverlay.hideBubble();
		flatsOverlay.setFlats(mFlats);
		mMapView.invalidate();
	}

}
