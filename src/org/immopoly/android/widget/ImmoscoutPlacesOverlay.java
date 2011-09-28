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

import org.immopoly.android.R;
import org.immopoly.android.adapter.FlatsPagerAdapter;
import org.immopoly.android.model.Flat;
import org.immopoly.android.model.Flats;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;



public class ImmoscoutPlacesOverlay extends ItemizedOverlay<OverlayItem> {

	private static final float MIN_INTERSECTION_AMOUNT = 0.25f;
	private static int 		   MIN_INTERSECTION_AREA;
	
	private final ArrayList<ClusterItem> tmpItems = new ArrayList<ClusterItem>();
	private MapView mMapView;
	private Rect markerBounds;
	private Flats mFlats;
	private int prevLatProjection = -1;
	private ViewPager flatsPager; 			// TODO move to PlacesMap
	
	static Drawable mapMarkerIcon;
	static Drawable mapMarkerIcon_old;
	static Drawable mapMarkerIcon_new;
	static Drawable mapMarkerIcon_owned;
	static Drawable mapMarkerIcon_cluster;
	
	public ImmoscoutPlacesOverlay(MapView map, LayoutInflater inflator) {
		super( boundCenterBottom( map.getResources().getDrawable( R.drawable.map_marker_icon)) );
		mMapView = map;
		
		Resources resources = map.getResources();
		mapMarkerIcon         = boundCenterBottom( resources.getDrawable( R.drawable.map_marker_icon));
		mapMarkerIcon_new     = boundCenterBottom( resources.getDrawable( R.drawable.map_marker_icon_new ));
		mapMarkerIcon_old     = boundCenterBottom( resources.getDrawable( R.drawable.map_marker_icon_old ));
		mapMarkerIcon_owned   = boundCenterBottom( resources.getDrawable( R.drawable.map_marker_property_icon ));
		mapMarkerIcon_cluster = boundCenterBottom( resources.getDrawable( R.drawable.map_marker_icon_cluster ));
		
		markerBounds = mapMarkerIcon.getBounds();
		
		MIN_INTERSECTION_AREA = (int) (markerBounds.width() * markerBounds.height() * MIN_INTERSECTION_AMOUNT);
		
		ClusterMarker.init( resources.getDisplayMetrics(), mapMarkerIcon.getIntrinsicHeight() );
		
	}

	public void setFlats(Flats mFlats) {
		this.mFlats = mFlats;
		clusterize();
	}
	
	@Override
	protected OverlayItem createItem(int i) {
		return tmpItems.get(i).getOverlayItem();
	}

	@Override
	public int size() {
		return tmpItems.size(); 
	}
	
	@Override
	protected boolean onTap(int index) {
		if ( tmpItems != null && index < tmpItems.size()) {
			final ClusterItem item = tmpItems.get(index);

			if ( flatsPager != null )
				mMapView.removeView( flatsPager );

			flatsPager = new ViewPager( mMapView.getContext() ) {
		    	public boolean onTouchEvent(MotionEvent arg0) {
		    		super.onTouchEvent(arg0);
		    		return true;  // to avoid map panning when the touch obvioulsy happens on the ViewPager
		    	}
		    };

			FlatsPagerAdapter pagerAdapter = new FlatsPagerAdapter( item.flats, mMapView.getContext() );
		    flatsPager.setAdapter( pagerAdapter );

		    
			final int standardHeight = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 70,
						mMapView.getResources().getDisplayMetrics());
			final int height = flatsPager.getMeasuredHeight() == 0 ? standardHeight
							 : flatsPager.getMeasuredHeight();
			final RelativeLayout.LayoutParams relLayoutParams = new RelativeLayout.LayoutParams( 
						LayoutParams.FILL_PARENT, height );
			relLayoutParams.addRule( RelativeLayout.ALIGN_PARENT_BOTTOM );
			
			flatsPager.setLayoutParams( relLayoutParams );
			mMapView.addView( flatsPager );

		    mMapView.setBuiltInZoomControls( false );
		    mMapView.getZoomButtonsController().setVisible( false );
			flatsPager.startAnimation(AnimationUtils.loadAnimation( mMapView.getContext(), R.anim.left_to_right));
			mMapView.getController().animateTo(item.point);
		}
		return true;
	}

	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		if (!super.onTap(p, mapView)) {
			mMapView.removeView( flatsPager );
		    mMapView.setBuiltInZoomControls( true );
		    mMapView.getZoomButtonsController().setVisible( true );
		}
		return true;
	}

	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		// any better place to detect zoom change? 
		int latProjection = (mapView.getProjection().fromPixels( 200, 200 ).getLatitudeE6()
						   - mapView.getProjection().fromPixels( 0, 0 ).getLatitudeE6() );
		if ( Math.abs( latProjection - prevLatProjection ) > 10 ) {
			prevLatProjection = latProjection;
			clusterize();
		}
		super.draw(canvas, mapView, shadow);
	}
	
	/**
	 * clusterize flats based on their supposed marker position
	 */
	public void clusterize() {
		if ( mFlats == null )
			return;
		final Flats flats = mFlats;
		final ArrayList<ClusterItem> items = tmpItems;
		final Projection projection = mMapView.getProjection();
		final Point screenPos = new Point();
		items.clear();

		// create tmp items with flat, geopoint & screenBounds 
		for ( int i = 0; i < flats.size(); i++ ) {
			final Flat flat = flats.get(i);
			if ( flat.lat == 0 && flat.lng == 0 )
				continue;
			Rect itemBounds = new Rect( markerBounds );
			GeoPoint point = new GeoPoint( (int) (flat.lat * 1E6), (int) (flat.lng * 1E6) );
			projection.toPixels( point, screenPos );
			itemBounds.offset( screenPos.x, screenPos.y );
			items.add( new ClusterItem(flat, point, itemBounds ) );
		}

		// join ClusterItems if their markers would intersect 
		ArrayList<ClusterItem> newItems = new ArrayList<ClusterItem>();
		final Rect intersection = new Rect();
		final int size = items.size();
		for ( int i = 0; i < size; i++ ) {
			final ClusterItem item = (ClusterItem) items.get(i);
			if ( item.consumed )
				continue;
			for ( int j = i+1; j < size; j++ ) {
				final ClusterItem otherItem = (ClusterItem) items.get( j );
				if ( ! otherItem.consumed 
						&& intersection.setIntersect( item.screenBounds, otherItem.screenBounds )
						&& intersection.width()*intersection.height() >= MIN_INTERSECTION_AREA ) 
					item.add( otherItem );
			}
			newItems.add( item );
		}
		tmpItems.clear();
		tmpItems.addAll( newItems );

		// see http://groups.google.com/group/android-developers/browse_thread/thread/38b11314e34714c3
		setLastFocusedIndex(-1);
		populate();
	}


	/*
	 * Class used while clustering.
	 */
	private final class ClusterItem {
		public boolean consumed;
		long latSum, lonSum;
		Rect screenBounds;
		ArrayList<Flat> flats = new ArrayList<Flat>( 8 );
		GeoPoint point;

		public ClusterItem( Flat flat, GeoPoint point, Rect screenBounds ) {
			this.screenBounds = screenBounds;
			latSum = point.getLatitudeE6();
			lonSum = point.getLongitudeE6();
			flats.add( flat );
		}
		
		public void add( ClusterItem item ) {
			flats.addAll( item.flats );
			latSum  += item.latSum;
			lonSum  += item.lonSum;
			item.consumed = true;
		}

		OverlayItem getOverlayItem() {
			int size = flats.size();
			
			point = new GeoPoint( (int) (latSum / size), (int) (lonSum / size) );

			ImmoPlaceOverlayItem item = new ImmoPlaceOverlayItem( point, flats );
			if ( size == 1 ) {
				Flat f = flats.get(0);
				if ( f.owned ) 
					item.setMarker( mapMarkerIcon_owned );
				else if ( f.age == Flat.AGE_NEW ) 
					item.setMarker( mapMarkerIcon_new );
				else if ( f.age == Flat.AGE_OLD ) 
					item.setMarker( mapMarkerIcon_old );
				else
					item.setMarker( mapMarkerIcon );
			} else
				item.setMarker( boundCenterBottom( new ClusterMarker( flats, mapMarkerIcon_cluster ) ) );
			return item;
		}
		
	}
	
}
