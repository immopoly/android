package org.immopoly.android.fragments;

public interface OnMapItemClickedListener {
	public void onMapItemClicked(int exposeID, boolean isInPortfolio);
	public void onMapOverlayClicked(int exposeID, boolean isInPortfolio);
}