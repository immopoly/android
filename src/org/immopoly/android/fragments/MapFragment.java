package org.immopoly.android.fragments;

import org.immopoly.android.R;

import com.google.android.maps.MapView;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MapFragment extends Fragment {

	@Override
	public void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.map_simple, container, false);
		MapView mapView = (MapView) view.findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		return view;
	}
}
