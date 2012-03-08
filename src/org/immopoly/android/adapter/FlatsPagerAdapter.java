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

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.immopoly.android.R;
import org.immopoly.android.fragments.OnMapItemClickedListener;
import org.immopoly.android.helper.ImageListDownloader;
import org.immopoly.android.helper.Settings;
import org.immopoly.android.model.Flat;
import org.immopoly.android.widget.EllipsizingTextView;
import org.immopoly.android.widget.PagerAdapter;
import org.immopoly.android.widget.ViewPager;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
	private boolean inPortfolio;

	private static final SimpleDateFormat dateSDF = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
	private static final DecimalFormat    numFmt = new DecimalFormat( "#" );
	
	public FlatsPagerAdapter(ArrayList<Flat> flats, Fragment context, boolean inPortfolio ) {
		this.mContext = context;
		this.flats = flats;
		this.views = new View[flats.size()];
		this.inPortfolio = inPortfolio;
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

		int layout = inPortfolio ? R.layout.teaser_content_portfolio : R.layout.teaser_content;
		View teaserView = inflater.inflate( layout, null, false);

		ImageView stateSymbol = (ImageView) teaserView.findViewById( R.id.stateSymbol );
		if ( ! flat.owned ) {
			if (flat.age == Flat.AGE_OLD)
				stateSymbol.setImageResource( R.drawable.house_old );
			else if (flat.age == Flat.AGE_NEW)
				stateSymbol.setImageResource( R.drawable.house_new );
			else
				stateSymbol.setImageResource( R.drawable.house );
		}		

		((EllipsizingTextView) teaserView.findViewById( R.id.flat_desc_text )).setText( flat.name );
		((TextView) teaserView.findViewById( R.id.rooms_text )).setText( flat.numRooms > 0 ? Integer.toString(flat.numRooms) : "?" );
		((TextView) teaserView.findViewById( R.id.qm_text )).setText( flat.livingSpace > 0 ? 
				numFmt.format(flat.livingSpace) : "?" );
		((TextView) teaserView.findViewById( R.id.price_text )).setText(flat.priceValue + " €"); // TODO kommt im IS24 JSON immer EUR/MONTH ? 

		if ( inPortfolio ) {
			((LinearLayout) teaserView.findViewById( R.id.takeover_daterow )).setVisibility( View.VISIBLE ) ;
			String takeoverDate = flat.takeoverDate > 0 ? dateSDF.format( new Date(flat.takeoverDate) ) : "?";
			((EllipsizingTextView) teaserView.findViewById( R.id.flat_desc_text )).setMaxLines( 3 );
			((TextView) teaserView.findViewById( R.id.takeover_date )).setText( takeoverDate ); 
			if ( flat.owned && flat.takeoverTries > 0 ) {
				((LinearLayout) teaserView.findViewById( R.id.takeover_numrow )).setVisibility( View.VISIBLE ) ;
				((EllipsizingTextView) teaserView.findViewById( R.id.flat_desc_text )).setMaxLines( 2 );
				((TextView) teaserView.findViewById( R.id.takeovers_text )).setText( "" + flat.takeoverTries );
			} else {
				((LinearLayout) teaserView.findViewById( R.id.takeover_numrow )).setVisibility( View.GONE ) ;
			}
		}
		((EllipsizingTextView) teaserView.findViewById( R.id.flat_desc_text )).setMaxLines( 2 );

		teaserView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((OnMapItemClickedListener) mContext.getActivity()).onFlatClicked( flat );
			}
		});

		ImageView iconView = (ImageView) teaserView.findViewById( R.id.teaser_icon );
		if ( flat.titlePictureSmall.trim().length() > 0) {
			imageDownloader.download(flat.titlePictureSmall, iconView );
		} else {
			iconView.clearAnimation();
			iconView.setAnimation( null );
			iconView.setImageDrawable( inflater.getContext().getResources().getDrawable( R.drawable.portfolio_fallback));
		}
		
		teaserView.setLayoutParams( new LayoutParams( 270, 120 ) );
		return teaserView;
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
