package org.immopoly.android.fragments;

import org.immopoly.android.R;
import org.immopoly.android.adapter.PortfolioFlatsAdapter;
import org.immopoly.android.app.ImmopolyActivity;
import org.immopoly.android.constants.Const;
import org.immopoly.android.model.Flat;
import org.immopoly.android.model.Flats;
import org.immopoly.android.model.ImmopolyUser;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class PortfolioListFragment extends Fragment implements OnItemClickListener {

	private Flats flats;

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
		View layout = getActivity().getLayoutInflater().inflate( R.layout.portfolio_list, null, false );
		ListView listView = (ListView) layout.findViewById(R.id.pf_list_view);
		
		ImmopolyUser user = ImmopolyUser.getInstance();
		flats = user.getPortfolio();	
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Flat flat = flats.get( position );
		((OnMapItemClickedListener) getActivity()).onMapOverlayClicked(flat.uid, flat.owned);
	}

}
