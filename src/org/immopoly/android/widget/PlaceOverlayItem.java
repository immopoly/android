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

import org.immopoly.android.model.Flat;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;



public class PlaceOverlayItem extends OverlayItem {

	public Flat flat;


	public PlaceOverlayItem(GeoPoint point, Flat f) {
		super(point, f.name, f.locationNote);
		flat = f;
	}

	public PlaceOverlayItem(GeoPoint point, String string, String string2) {
		super(point, string, string2);
	}

	
}
