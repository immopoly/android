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
import org.immopoly.android.constants.Const;
import org.immopoly.android.fragments.MapFragment;
import org.immopoly.android.model.Flat;
import org.immopoly.android.model.Flats;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

public class ImmoscoutPlacesOverlay extends ItemizedOverlay<OverlayItem> {

	private static final float MIN_INTERSECTION_AMOUNT = 0.25f;
	private static int MIN_INTERSECTION_AREA;

	private final ArrayList<ClusterItem> tmpItems = new ArrayList<ClusterItem>();
	private MapView mMapView;
	private Fragment mMapFragment;

	public static Rect markerBounds;	// TODO public static
	private Flats mFlats;
	private int prevLatProjection = -1;
	private TeaserBubble bubble;

	static Drawable mapMarkerIcon;
	static Drawable mapMarkerIcon_old;
	static Drawable mapMarkerIcon_new;
	static Drawable mapMarkerIcon_owned;
	static Drawable mapMarkerIcon_cluster;

	public ImmoscoutPlacesOverlay(Fragment fragment, MapView mapView, LayoutInflater inflator) {
		super(boundCenterBottom(fragment.getResources().getDrawable(R.drawable.map_marker_icon)));
		mMapView = mapView;
		mMapFragment = fragment;
		Resources resources = fragment.getResources();
		mapMarkerIcon = boundCenterBottom(resources.getDrawable(R.drawable.map_marker_icon));
		mapMarkerIcon_new = boundCenterBottom(resources.getDrawable(R.drawable.map_marker_icon_new));
		mapMarkerIcon_old = boundCenterBottom(resources.getDrawable(R.drawable.map_marker_icon_old));
		mapMarkerIcon_owned = boundCenterBottom(resources.getDrawable(R.drawable.map_marker_property_icon));
		mapMarkerIcon_cluster = boundCenterBottom(resources.getDrawable(R.drawable.map_marker_icon_cluster));

		markerBounds = mapMarkerIcon.getBounds();

		MIN_INTERSECTION_AREA = (int) (markerBounds.width() * markerBounds.height() * MIN_INTERSECTION_AMOUNT);

		ClusterMarker.init(resources.getDisplayMetrics(), mapMarkerIcon.getIntrinsicHeight());
	}

	public void setFlats(Flats mFlats) {
		this.mFlats = mFlats;
		clusterize();
	}

	public Rect getMarkerBounds() {
		return markerBounds;
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
		if ( bubble != null )
			bubble.detach();
		if ( tmpItems == null || index >= tmpItems.size()) {
			if (mMapFragment instanceof MapFragment) // show wind rose (hack)
				((MapFragment) mMapFragment).showCompass();
			return false;
		}
		final ClusterItem item = tmpItems.get(index);
		final RelativeLayout.LayoutParams relLayoutParams = new RelativeLayout.LayoutParams(
				LayoutParams.FILL_PARENT, 210 );
		relLayoutParams.addRule( RelativeLayout.BELOW, R.id.header );
		bubble = new TeaserBubble( mMapFragment, mMapView, item.flats );
		if (mMapFragment instanceof MapFragment) // hide wind rose (hack)
			((MapFragment) mMapFragment).hideCompass();
		return true;
	}

	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		if (!super.onTap(p, mapView)) {
			hideBubble();
			mMapView.setBuiltInZoomControls(true);
			mMapView.getZoomButtonsController().setVisible(true);
		}
		return true;
	}

	// TODO we're doing a lot of strange things here and draw() doesnt feel comfortable with that off-purpose stuff 
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		// test for map movement. Remove Balloon evtly. 
		if ( bubble != null && bubble.animationDone ) {
			Point ntp = bubble.mapIconPos;	// screen pos of selected icon as the bubble prefers 
			Point p   = new Point();		// current screen pos of selected icon 
			mMapView.getProjection().toPixels( bubble.mapIconGeoPoint, p );
			if ( Math.abs( ntp.x - p.x) > 20 || Math.abs( ntp.y - p.y) > 20 ) {
				hideBubble();
			}
		}
		// test for map zoom. Evtly do clusterizing 
		int latProjection = (mapView.getProjection().fromPixels( 200, 200 ).getLatitudeE6()
						   - mapView.getProjection().fromPixels( 0, 0 ).getLatitudeE6() );
		if (Math.abs(latProjection - prevLatProjection) > 10) {
			prevLatProjection = latProjection;
			clusterize();
		}
		super.draw(canvas, mapView, shadow);
	}

	/**
	 * clusterize flats based on their supposed marker position
	 */
	public void clusterize() {
		if (mFlats == null)
			return;
		final Flats flats = mFlats;
		final ArrayList<ClusterItem> items = tmpItems;
		final Projection projection = mMapView.getProjection();
		final Point screenPos = new Point();
		items.clear();

		// create tmp items with flat, geopoint & screenBounds
		for (int i = 0; i < flats.size(); i++) {
			final Flat flat = flats.get(i);
			if (flat.lat == 0 && flat.lng == 0)
				continue;
			Rect itemBounds = new Rect(markerBounds);
			GeoPoint point = new GeoPoint((int) (flat.lat * 1E6), (int) (flat.lng * 1E6));
			projection.toPixels(point, screenPos);
			itemBounds.offset(screenPos.x, screenPos.y);
			items.add(new ClusterItem(flat, point, itemBounds));
		}

		// join ClusterItems if their markers would intersect
		ArrayList<ClusterItem> newItems = new ArrayList<ClusterItem>();
		final Rect intersection = new Rect();
		final int size = items.size();
		for (int i = 0; i < size; i++) {
			final ClusterItem item = (ClusterItem) items.get(i);
			if (item.consumed)
				continue;
			for (int j = i + 1; j < size; j++) {
				final ClusterItem otherItem = (ClusterItem) items.get(j);
				if (!otherItem.consumed
						&& intersection.setIntersect(item.screenBounds, otherItem.screenBounds)
						&& intersection.width() * intersection.height() >= MIN_INTERSECTION_AREA)
					item.add(otherItem);
			}
			newItems.add(item);
		}
		tmpItems.clear();
		tmpItems.addAll(newItems);

		// see http://groups.google.com/group/android-developers/browse_thread/thread/38b11314e34714c3
		setLastFocusedIndex(-1);
		populate();
	}
	
	public void hideBubble() {
		if ( bubble != null ) {
			bubble.detach();
			bubble = null;
			if (mMapFragment instanceof MapFragment) // show wind rose (hack)
				((MapFragment) mMapFragment).showCompass();
		}
	}

	public void updateBubble() {
		if ( bubble != null )
			bubble.refreshContent();
	}

	/*
	 * Class used while clustering.
	 */
	private final class ClusterItem { // TODO join with ImmoPlaceOverlayItem or something
		public boolean consumed;
		long latSum, lonSum;
		Rect screenBounds;
		Flats flats = new Flats();
		GeoPoint point;
		OverlayItem overlayItem;

		public ClusterItem(Flat flat, GeoPoint point, Rect screenBounds) {
			this.screenBounds = screenBounds;
			latSum = point.getLatitudeE6();
			lonSum = point.getLongitudeE6();
			flats.add(flat);
		}

		public void add(ClusterItem item) {
			flats.addAll(item.flats);
			latSum += item.latSum;
			lonSum += item.lonSum;
			item.consumed = true;
		}

		OverlayItem getOverlayItem() {
			if ( overlayItem != null )
				return overlayItem;

			int size = flats.size();

//			point = new GeoPoint( (int) (latSum / size), (int) (lonSum / size) );
			point  = new GeoPoint( (int) (flats.get(0).lat * 1E6), (int) (flats.get(0).lng * 1E6) );

			overlayItem = new ImmoPlaceOverlayItem( point, flats );
			if (size == 1) {
				Flat f = flats.get(0);
				if (f.owned)
					overlayItem.setMarker( mapMarkerIcon_owned );
				else if (f.age == Flat.AGE_NEW)
					overlayItem.setMarker( mapMarkerIcon_new );
				else if (f.age == Flat.AGE_OLD)
					overlayItem.setMarker( mapMarkerIcon_old );
				else
					overlayItem.setMarker( mapMarkerIcon );
			} else
				overlayItem.setMarker( boundCenterBottom( new ClusterMarker( flats, mapMarkerIcon_cluster ) ) );
			return overlayItem;
		}
	}

}
