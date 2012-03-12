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

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;

/**
 *	Marker drawable for ImmoPlaceOverlayItem with =1 flats 
 *  prevents shadow drawing
 */
public class SingleFlatMarker extends Drawable 
{
	private boolean		 	shadowMode;
	private Drawable		baseDrawable;
	

	public SingleFlatMarker( Drawable baseDrawable ) {
		this.baseDrawable = baseDrawable;
	}
	
	@Override
	public void draw(Canvas canvas) {
		if ( ! shadowMode ) {
			baseDrawable.draw(canvas);
		}
	}

	@Override
	public boolean setState(int[] stateSet) { 
		return baseDrawable.setState( stateSet );
	}
	
	@Override
	public int getOpacity() {
		return baseDrawable.getOpacity();
	}

	@Override
	public void setAlpha(int alpha) {
		baseDrawable.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		baseDrawable.setColorFilter( cf );
		this.shadowMode = cf != null;
	}

}
