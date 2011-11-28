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

import org.immopoly.android.R;
import org.immopoly.android.constants.Const;
import org.immopoly.android.model.Flat;
import org.immopoly.android.model.Flats;
import org.immopoly.android.widget.EllipsizingTextView;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class PortfolioFlatsAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	
	private Flats mFlats;
	private Flats clusterFlats;
	private Flat  selectedFlat;

	public PortfolioFlatsAdapter(Activity context, Flats flats) {
		mFlats = flats;
		inflater = context.getLayoutInflater();
	}

	public int getCount() {
		return mFlats.size();
	}

	public void setFlats(Flats mFlats) {
		this.mFlats = mFlats;
		notifyDataSetChanged();
	}
	
	public Flat getItem(int pos) {
		return mFlats.get(pos);
	}

	public long getItemId(int pos) {
		return pos;
	}

	public View getView( int position, View convertView, ViewGroup parent ) {
		final Flat flat = mFlats.get( position );
		int selectIdx = -1;
		if ( clusterFlats != null )
			selectIdx = clusterFlats.indexOf( flat );
		else if ( flat == selectedFlat ) 
			selectIdx = 0;
			
		if ( convertView == null )
			convertView = inflater.inflate( R.layout.portfolio_list_item, parent, false);
		
		ImageView iconView = (ImageView) convertView.findViewById( R.id.teaser_icon );
		if ( flat.owned )
			iconView.setImageResource( 
				selectIdx > -1 ? R.drawable.owned_property_big: R.drawable.map_marker_property_icon );
		else if ( flat.age == Flat.AGE_OLD )
			iconView.setImageResource( R.drawable.house_old );
		else if ( flat.age == Flat.AGE_NEW )
			iconView.setImageResource( R.drawable.house_new );
		((EllipsizingTextView) convertView.findViewById( R.id.flat_desc_text )).setText( flat.name );
		((TextView) convertView.findViewById( R.id.rooms_text )).setText( flat.numRooms );
		((TextView) convertView.findViewById( R.id.qm_text )).setText( flat.livingSpace );
		((TextView) convertView.findViewById( R.id.price_text )).setText( flat.priceValue + "€" ); // TODO kommt im IS24 JSON immer EUR/MONTH ? 
		
		if ( flat.owned && flat.takeoverTries > 0 ) {
			((EllipsizingTextView) convertView.findViewById( R.id.flat_desc_text )).setMaxLines( 2 );
			((LinearLayout) convertView.findViewById( R.id.takeOverContainer )).setVisibility( View.VISIBLE ) ;
			((TextView) convertView.findViewById( R.id.takeovers_text )).setText( "" + flat.takeoverTries );
		} else {
			((EllipsizingTextView) convertView.findViewById( R.id.flat_desc_text )).setMaxLines( 3 );
			Log.i( Const.LOG_TAG, "Elippsized lines: " +((EllipsizingTextView) convertView.findViewById( R.id.flat_desc_text )).getLineCount() );
			((LinearLayout) convertView.findViewById( R.id.takeOverContainer )).setVisibility( View.GONE ) ;
		}
		
//		if ( selectIdx > -1 ) { // TODO this was used when testing list & map side-by-side in a tablet layout - unused for now
//			if ( flat == selectedFlat )
//				convertView.setBackgroundColor( 0xFFAAAAAA ); // TODO colors
//			else
//				convertView.setBackgroundColor( 0xFFCCCCCC ); 
//			convertView.findViewById( R.id.swipe_indicator ).setVisibility( View.VISIBLE );
//			convertView.findViewById( R.id.swipe_indicator ).setVisibility( View.GONE );
//			convertView.findViewById( R.id.swipe_finger ).setVisibility( View.GONE );
//			convertView.findViewById( R.id.swipe_left_img ).setVisibility( View.GONE );
//			convertView.findViewById( R.id.swipe_right_img ).setVisibility( View.GONE );
//			convertView.findViewById( R.id.swipe_counter ).setVisibility( View.VISIBLE );
//			((TextView) convertView.findViewById( R.id.swipe_counter )).setText(
//					(selectIdx+1)+"/"+ (clusterFlats == null ? 1 : clusterFlats.size()) );
//		} else {
//			convertView.setBackgroundColor( 0xFFFFFFFF );
//			convertView.findViewById( R.id.swipe_indicator ).setVisibility( View.GONE );
//		}
		return convertView;
	}

}
