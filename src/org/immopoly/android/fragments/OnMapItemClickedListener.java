package org.immopoly.android.fragments;

import org.immopoly.android.model.Flat;

public interface OnMapItemClickedListener {
	
	public void onFlatClicked( Flat flat );
	
//	public void onMapItemClicked(int exposeID, boolean isInPortfolio);
//	public void onMapOverlayClicked(int exposeID, boolean isInPortfolio);
}