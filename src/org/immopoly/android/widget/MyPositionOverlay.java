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
import org.immopoly.android.constants.Const;
import org.immopoly.android.helper.ImageListDownloader;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;


public class MyPositionOverlay extends ItemizedOverlay<PlaceOverlayItem> {

	private final ArrayList<PlaceOverlayItem> mOverlayItems = new ArrayList<PlaceOverlayItem>();
	private MapView mMapView;
	private Context mContext;
	private LayoutInflater mLayoutInflater;
	private View mMarkerView;
	private static ImageListDownloader imageDownloader = new ImageListDownloader();

	public MyPositionOverlay(Drawable defaultMarker) {
		super(defaultMarker);
	}

	public MyPositionOverlay(Drawable defaultMarker, Context context,
			MapView map, LayoutInflater inflator) {
		super(defaultMarker);
		mMapView = map;
		mContext = context;
		mLayoutInflater = inflator;
	}

	@Override
	protected PlaceOverlayItem createItem(int i) {
		PlaceOverlayItem item = mOverlayItems.get(i);
		boundCenterBottom(item.getMarker(0));
		return item;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return mOverlayItems.size();
	}

	public void addOverlay(PlaceOverlayItem overlay) {
		mOverlayItems.add(overlay);
		populate();
	}

	@Override
	protected boolean onTap(int index) {
		final PlaceOverlayItem item = mOverlayItems.get(index);
		if (mMarkerView == null) {
			mMarkerView = mLayoutInflater.inflate(R.layout.map_marker_popup,
					mMapView, false);
		}
		mMapView.removeAllViews();

		((TextView) mMarkerView.findViewById(R.id.titleMarkerText))
				.setText(item.getTitle());
		// ((TextView) mMarkerView.findViewById(R.id.snippetMarkerText))
		// .setText(item.getSnippet());
		if (item.flat != null) {
			if (item.flat.titlePictureSmall.trim().length() > 0) {
				((ImageView) mMarkerView.findViewById(R.id.imagePreview))
						.startAnimation(AnimationUtils.loadAnimation(mContext,
                                R.anim.loading_animation));
				imageDownloader
						.download(item.flat.titlePictureSmall,
                                (ImageView) mMarkerView
                                        .findViewById(R.id.imagePreview));
			} else {
				((ImageView) mMarkerView.findViewById(R.id.imagePreview))
						.setImageDrawable(mContext.getResources().getDrawable(
						R.drawable.mylocation));
			}
			if (item.flat.priceValue.length() > 0) {
				((TextView) mMarkerView.findViewById(R.id.priceInfo))
						.setText(item.flat.priceValue + " "
                                + item.flat.currency + " / "
                                + item.flat.priceIntervaleType);
			}

			((Button) mMarkerView.findViewById(R.id.btnOpenExpose))
					.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                        	Intent i = new Intent();
                    		i.putExtra(Const.EXPOSE_ID, String.valueOf(item.flat.uid));
                    		i.putExtra(Const.EXPOSE_NAME, String.valueOf(item.flat.name));
                    		i.putExtra(Const.EXPOSE_DESC, String.valueOf(item.flat.description));
                    		i.putExtra(Const.EXPOSE_PICTURE_SMALL,
                    				String.valueOf(item.flat.titlePictureSmall));
                    		i.putExtra(Const.EXPOSE_IN_PORTOFOLIO, item.flat.owned);
                    		i.putExtra(Const.SOURCE, MapActivity.class.getSimpleName());
                    		i.setAction("expose_view");
                    		v.getContext().sendBroadcast(i);
                        }
                    });
		}

		mMarkerView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View v) {
				// mContext.startActivity(intent);
			}
		});

		mMapView.addView(mMarkerView);
		final int height = mMarkerView.getMeasuredHeight() == 0 ? 100
				: mMarkerView.getMeasuredHeight();
		final MapView.LayoutParams mapParams = new MapView.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT, 0, mMapView
						.getMeasuredHeight()
						- height, MapView.LayoutParams.TOP);

		mMarkerView.setLayoutParams(mapParams);
		mMarkerView.startAnimation(AnimationUtils.loadAnimation(mContext,
				R.anim.left_to_right));
		mMapView.getController().animateTo(item.getPoint());
		return true;
	}

	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		if (!super.onTap(p, mapView)) {
			mMapView.removeAllViews();
		}
		return true;
	}

	public void startExposeWebView(Intent i) {
		mContext.startActivity(i);
	}

	public void clear() {
		mOverlayItems.clear();
	}
}
