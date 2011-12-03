package org.immopoly.android.fragments;

import org.immopoly.android.R;
import org.immopoly.android.app.ImmopolyActivity;
import org.immopoly.android.app.UserDataListener;
import org.immopoly.android.app.UserDataManager;
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

public class PortfolioMapFragment extends Fragment implements UserDataListener {

	private MapView mMapView;
	private ImmoscoutPlacesOverlay flatsOverlay;
	private Flats mFlats;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.i(Const.LOG_TAG, "PortfolioMapFragment.onCreateView");
		View layout = inflater.inflate(R.layout.portfolio_map, null, false);

		ImmopolyUser user = ImmopolyUser.getInstance();
		mFlats = user.getPortfolio();

		mMapView = ((ImmopolyActivity) getActivity()).acquireMapView(this);
		flatsOverlay = new ImmoscoutPlacesOverlay(this, mMapView, inflater);
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

		return layout;
	}

	@Override
	public void onDestroyView() {
		((ImmopolyActivity) getActivity()).releaseMapView(this);
		super.onDestroyView();
	}

	@Override
	public void onUserDataUpdated() {
		// TODO check my vilibility
		mFlats = ImmopolyUser.getInstance().getPortfolio();
		flatsOverlay.setFlats(mFlats);
		mMapView.invalidate();
	}

}
