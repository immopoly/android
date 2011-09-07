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

package org.immopoly.android;

import java.util.ArrayList;

import org.immopoly.android.R;
import org.immopoly.android.constants.Const;
import org.immopoly.android.helper.ImageListDownloader;
import org.immopoly.android.model.Flat;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Implementation of android.support.v4.view.PagerAdapter. 
 * 
 * Provides views inflated from map_marker_popup.xml on
 * an android.support.v4.view.ViewPager's demand.
 * 
 */
public class FlatsPagerAdapter extends PagerAdapter {

	private static ImageListDownloader imageDownloader = new ImageListDownloader();
	
	private ArrayList<Flat>    flats;	// list of flats presented in the ViewPager
	private View[] 			   views;	// storing views for each flat for use in destroyItem() & isViewFromObject()
	private PlacesMap		   context; 

	public FlatsPagerAdapter( ArrayList<Flat> flats, PlacesMap context ) {
		this.context = context;
		this.flats   = flats;
		this.views   = new View[flats.size()];
	}

	@Override
	public int getCount() {
		return flats.size();
	}

	@Override
	public Object instantiateItem( View parent, int idx ) {
		final Flat flat = flats.get(idx);
		View flatView = views[idx] != null ? views[idx] : getFlatView( flat, idx );
		((ViewPager) parent).addView( flatView, 0 );
		views[idx] = flatView;
		return flat;
	}
	
	// create a view inflated from map_marker_popup.xml for the given Flat
	private View getFlatView( final Flat flat, final int idx ) {
		LayoutInflater inflater = context.getLayoutInflater();
		View markerView = inflater.inflate( R.layout.map_marker_popup, null, false);
		if ( flat.owned )
			markerView.setBackgroundColor( Const.OWNED_FLAT_BACKGROUND_COLOR );
		else if ( flat.age == Flat.AGE_OLD )
			markerView.setBackgroundColor( Const.OLD_FLAT_BACKGROUND_COLOR );
		else if ( flat.age == Flat.AGE_NEW )
			markerView.setBackgroundColor( Const.NEW_FLAT_BACKGROUND_COLOR );
		else
			markerView.setBackgroundColor( Const.NORMAL_FLAT_BACKGROUND_COLOR );

		((TextView) markerView.findViewById(R.id.titleMarkerText)).setText( flat.name );
		if ( flats.size() > 1 )
			((TextView) markerView.findViewById(R.id.pagerPages)).setText( (idx+1) + "/" + flats.size() );
		else
			((TextView) markerView.findViewById(R.id.pagerPages)).setVisibility( View.GONE );
		if ( flat.titlePictureSmall.trim().length() > 0 ) {
			((ImageView) markerView.findViewById(R.id.imagePreview))
					.startAnimation(AnimationUtils.loadAnimation( context, R.anim.loading_animation ) );
			imageDownloader.download( 
					flat.titlePictureSmall, (ImageView) markerView.findViewById(R.id.imagePreview));
		} else {
			((ImageView) markerView.findViewById(R.id.imagePreview))
				.setImageDrawable( context.getResources()
						.getDrawable(R.drawable.house_drawn));
		}
		if ( flat.priceValue.length() > 0) {
			((TextView) markerView.findViewById(R.id.priceInfo))
				.setText( flat.priceValue + " "
						+ flat.currency + " / "
						+ flat.priceIntervaleType);
		}
		((Button) markerView.findViewById(R.id.btnOpenExpose) ).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.callbackCall( flat );
                }
        });

		return markerView;
	}
	
	
	@Override
	public void destroyItem( View collection, int idx, Object view ) {
		if ( idx < views.length ) {
			((ViewPager) collection).removeView( views[idx] );
			views[idx] = null;
		}
	}

	@Override
	public boolean isViewFromObject(View view, Object obj) {
		int idx = flats.indexOf( obj );
		if ( idx == -1 )
			return false;
		return view == views[idx];
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void finishUpdate(View arg0) {}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {}

	@Override
	public void startUpdate(View arg0) {}
}
