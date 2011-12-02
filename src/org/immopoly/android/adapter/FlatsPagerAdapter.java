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

package org.immopoly.android.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.immopoly.android.R;
import org.immopoly.android.app.ImmopolyActivity;
import org.immopoly.android.constants.Const;
import org.immopoly.android.fragments.MapFragment;
import org.immopoly.android.fragments.OnMapItemClickedListener;
import org.immopoly.android.helper.ActivityHelper;
import org.immopoly.android.helper.ImageListDownloader;
import org.immopoly.android.helper.Settings;
import org.immopoly.android.model.Flat;
import org.immopoly.android.widget.EllipsizingTextView;

import android.content.Context;
import android.content.Intent;
import android.opengl.Visibility;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.maps.MapActivity;

/**
 * Implementation of android.support.v4.view.PagerAdapter.
 * 
 * Provides views inflated from bubble_content.xml on
 * an android.support.v4.view.ViewPager's demand.
 * 
 */
public class FlatsPagerAdapter extends PagerAdapter {

	private ArrayList<Flat>  flats;	// list of flats presented in the ViewPager
	private View[] 			 views;	// storing views for each flat for use in destroyItem() & isViewFromObject()
	private Fragment		 mContext;
	private ImageListDownloader imageDownloader;

	private static final SimpleDateFormat dateSDF = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);

	public FlatsPagerAdapter(ArrayList<Flat> flats, Fragment context) {
		this.mContext = context;
		this.flats = flats;
		this.views = new View[flats.size()];
		this.imageDownloader = Settings.getExposeImageDownloader(context.getActivity());
	}

	@Override
	public int getCount() {
		return flats.size();
	}

	@Override
	public Object instantiateItem(View parent, int idx) {
		final Flat flat = flats.get(idx);
		View flatView = views[idx] != null ? views[idx]	: getFlatView(flat, idx);
		((ViewPager) parent).addView(flatView, 0);
		views[idx] = flatView;
		return flat;
	}

	// create a view inflated from bubble_content.xml for the given Flat
	private View getFlatView(final Flat flat, final int idx) {
		LayoutInflater inflater = LayoutInflater.from( mContext.getActivity() );
		View markerView = inflater.inflate( R.layout.bubble_content, null, false);

//		ImageView iconView = (ImageView) markerView.findViewById( R.id.teaser_icon );
//		if (flat.owned)
//			iconView.setImageResource( R.drawable.map_marker_property_icon );
//		else if (flat.age == Flat.AGE_OLD)
//			iconView.setImageResource( R.drawable.house_old );
//		else if (flat.age == Flat.AGE_NEW)
//			iconView.setImageResource( R.drawable.house_new );
		if ( flats.size() == 1 )
			markerView.findViewById( R.id.swipe_indicator ).setVisibility( View.GONE );
		else {
			if ( idx < flats.size()-1)
				((ImageView) markerView.findViewById( R.id.swipe_left_img )).setImageResource( R.drawable.swipe_left_1 );
			if ( idx > 0 )
				((ImageView) markerView.findViewById( R.id.swipe_right_img )).setImageResource( R.drawable.swipe_right_1 );
			((TextView) markerView.findViewById( R.id.swipe_counter )).setText( (idx+1)+"/"+flats.size() );
		}
		((EllipsizingTextView) markerView.findViewById( R.id.flat_desc_text )).setText( flat.name );
		((TextView) markerView.findViewById( R.id.rooms_text )).setText( flat.numRooms > 0 ? Integer.toString(flat.numRooms) : "?" );
		((TextView) markerView.findViewById( R.id.qm_text )).setText( flat.livingSpace > 0 ? 
				Integer.toString( (int) Math.round(flat.livingSpace) ) : "?" );
		((TextView) markerView.findViewById( R.id.price_text )).setText( flat.priceValue + "€" ); // TODO kommt im IS24 JSON immer EUR/MONTH ? 

		if ( flat.owned ) {
			((LinearLayout) markerView.findViewById( R.id.takeover_daterow )).setVisibility( View.VISIBLE ) ;
			String takeoverDate = flat.takeoverDate > 0 ? dateSDF.format( new Date(flat.takeoverDate) ) : "?";
			((EllipsizingTextView) markerView.findViewById( R.id.flat_desc_text )).setMaxLines( 3 );
			((TextView) markerView.findViewById( R.id.takeover_date )).setText( takeoverDate ); 
			if ( flat.owned && flat.takeoverTries > 0 ) {
				((LinearLayout) markerView.findViewById( R.id.takeover_numrow )).setVisibility( View.VISIBLE ) ;
				((EllipsizingTextView) markerView.findViewById( R.id.flat_desc_text )).setMaxLines( 2 );
				((TextView) markerView.findViewById( R.id.takeovers_text )).setText( "" + flat.takeoverTries );
			} else {
				((LinearLayout) markerView.findViewById( R.id.takeover_numrow )).setVisibility( View.GONE ) ;
			}
		} else {
			((EllipsizingTextView) markerView.findViewById( R.id.flat_desc_text )).setMaxLines( 4 );
			((LinearLayout) markerView.findViewById( R.id.takeover_daterow )).setVisibility( View.GONE ) ;
			((LinearLayout) markerView.findViewById( R.id.takeover_numrow )).setVisibility( View.GONE ) ;
		}

		markerView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((OnMapItemClickedListener) mContext.getActivity()).onFlatClicked( flat );
			}
		});

		ImageView iconView = (ImageView) markerView.findViewById( R.id.teaser_icon );
		if ( flat.titlePictureSmall.trim().length() > 0) {
			imageDownloader.download(flat.titlePictureSmall, iconView );
		} else {
			iconView.clearAnimation();
			iconView.setAnimation( null );
			iconView.setImageDrawable( inflater.getContext().getResources().getDrawable( R.drawable.portfolio_fallback));
		}
		
		return markerView;
	}

	@Override
	public void destroyItem(View collection, int idx, Object view) {
		if (idx < views.length) {
			((ViewPager) collection).removeView(views[idx]);
			views[idx] = null;
		}
	}

	@Override
	public boolean isViewFromObject(View view, Object obj) {
		int idx = flats.indexOf(obj);
		if (idx == -1)
			return false;
		return view == views[idx];
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void finishUpdate(View arg0) {
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {
	}

	@Override
	public void startUpdate(View arg0) {
	}
}
