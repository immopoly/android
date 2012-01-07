package org.immopoly.android.helper;

public interface OnTrackingEventListener {
	public void onTrackPageView(String page);
	public void onTrackEvent(String category, String action, String label, int i);
}
