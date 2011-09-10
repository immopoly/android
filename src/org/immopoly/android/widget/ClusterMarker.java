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

import java.util.ArrayList;

import org.immopoly.android.model.Flat;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.DisplayMetrics;

/**
 *	Marker drawable for ImmoPlaceOverlayItem with >1 flats 
 */
public class ClusterMarker extends Drawable {

	private static final float TEXT_SIZE_DP = 18.0f;
	
	private static Paint textPaint = new TextPaint();
	private static Paint shadowPaint = new TextPaint();
	private static int textSize;
	private static int textY;

	private ArrayList<Flat> flats;
	private boolean		 	shadowMode;
	private Drawable		baseDrawable;
	

	public ClusterMarker(ArrayList<Flat> flats, Drawable baseDrawable ) {
		this.baseDrawable = baseDrawable;
		this.flats 		  = flats;
	}
	
	static void init(DisplayMetrics displayMetrics, int markerHeight) {
		textSize = (int) (TEXT_SIZE_DP * displayMetrics.density + 0.5f);
		textY = -(markerHeight - textSize) / 2;
		
		textPaint.setColor( 0xFFFFFFFF );
		textPaint.setTextSize( textSize );
		textPaint.setAntiAlias( true );
		textPaint.setTextAlign( Align.CENTER );
//		textPaint.setShadowLayer( 10, 10, 5, Color.BLACK ); // no go :(
		
		shadowPaint.setColor( 0xFF000000 );
		shadowPaint.setTextSize( textSize );
		shadowPaint.setAntiAlias( true );
		shadowPaint.setTextAlign( Align.CENTER );
		shadowPaint.setFlags( Paint.FAKE_BOLD_TEXT_FLAG );
	}

	@Override
	public void draw(Canvas canvas) {
		int numFlats = flats.size();
		baseDrawable.draw(canvas);
		if ( ! shadowMode ) {
			String text = numFlats < 10 ? Integer.toString( numFlats ) : "+";
			canvas.drawText( text,  1, textY+1, shadowPaint );
			canvas.drawText( text, -1, textY+1, shadowPaint );
			canvas.drawText( text,  1, textY-1, shadowPaint );
			canvas.drawText( text, -1, textY-1, shadowPaint );
			canvas.drawText( text,  0, textY  , textPaint );
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
