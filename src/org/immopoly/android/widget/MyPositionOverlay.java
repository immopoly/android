/*
 * This is the Android component of Immopoly
 * http://immopoly.appspot.com
 * Copyright (C) 2011 Tobias Sasse
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */

package org.immopoly.android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;


public class MyPositionOverlay extends ItemizedOverlay<PlaceOverlayItem> {

	private PlaceOverlayItem overlayItem = null;

	public MyPositionOverlay(Drawable defaultMarker) {
		super(defaultMarker);
	}

	public MyPositionOverlay(Drawable defaultMarker, Context context,
			MapView map, LayoutInflater inflator) {
		super(defaultMarker);
	}

	@Override
	protected PlaceOverlayItem createItem(int i) {
		boundCenterBottom(overlayItem.getMarker(0));
		return overlayItem;
	}

	@Override
	public int size() {
		if (null == overlayItem)
			return 0;
		return 1;
	}

	public void setPlaceOverlayItem(PlaceOverlayItem overlayItem) {
		this.overlayItem = overlayItem;
		populate();
	}

	public void clear() {
		overlayItem = null;
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if ( ! shadow )
			super.draw(canvas, mapView, shadow);
	}
}
