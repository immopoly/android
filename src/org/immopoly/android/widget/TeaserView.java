package org.immopoly.android.widget;

import java.util.ArrayList;

import org.immopoly.android.R;
import org.immopoly.android.adapter.FlatsPagerAdapter;
import org.immopoly.android.constants.Const;
import org.immopoly.android.helper.Settings;
import org.immopoly.android.model.Flat;

import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

public class TeaserView extends RelativeLayout {

	// statics 
	private static float   screenDesity = 1.0f; // like on a G1!!11!

	private ArrayList<Flat> flats;
	private MapView mapView;
	public boolean animationDone; // TODO public
	public GeoPoint mapIconGeoPoint; // TODO public
	Point mapIconPos;

	private ViewPager flatsPager;
	private Fragment fragment;
	private View noseView;

	private boolean isPortfolio;
	
	public TeaserView( Fragment fragment, MapView mapView, ArrayList<Flat> flats, boolean isPortfolio ) {
		super( fragment.getActivity() );
		this.isPortfolio = isPortfolio;
		this.fragment	 = fragment;
		this.mapView 	 = mapView;
		this.flats 		 = flats;

		screenDesity = Settings.getScreenDensity( fragment.getActivity() );
		
		setBackgroundDrawable( new ColorDrawable( 0x00000000 ) );

		int frameHeight = 122;
		
		FrameLayout frame = new FrameLayout( fragment.getActivity() );
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				(int) (330*screenDesity), (int) (frameHeight*screenDesity) );
		layoutParams.alignWithParent = true;
		layoutParams.addRule( RelativeLayout.CENTER_HORIZONTAL );
		layoutParams.addRule( RelativeLayout.ALIGN_PARENT_TOP );
		frame.setLayoutParams(layoutParams);
		frame.setBackgroundResource(R.drawable.teaser_background);
		frame.setId( 1 );		
		
		flatsPager = new ViewPager( fragment.getActivity() );
		FlatsPagerAdapter pagerAdapter = new FlatsPagerAdapter( flats, fragment, isPortfolio );
		flatsPager.setAdapter( pagerAdapter );
		flatsPager.setPageMargin( (int) (3*screenDesity) );
		if ( flats.size() > 1 )
			flatsPager.setForcedChildWidth( (int) (260 * screenDesity) );
		frame.addView( flatsPager );
		addView( frame );
		
		noseView = new View( fragment.getActivity() );
		noseView.setBackgroundResource(R.drawable.teaser_nose);
		layoutParams = new RelativeLayout.LayoutParams(
				(int) (24*screenDesity), (int) (140*screenDesity) );
		layoutParams.addRule( RelativeLayout.CENTER_HORIZONTAL );
		layoutParams.addRule( RelativeLayout.BELOW, frame.getId() );
		layoutParams.setMargins(0, (int) (screenDesity*-13), 0, 0);
		noseView.setLayoutParams(layoutParams);
		addView( noseView );

		setLayoutParams( new MapView.LayoutParams( (int)(330*screenDesity), mapView.getHeight()/2, mapView.getWidth()/2, 0, MapView.LayoutParams.TOP | MapView.LayoutParams.CENTER_HORIZONTAL | MapView.LayoutParams.MODE_VIEW ) );
		mapView.addView( this );
		
		Animation animation = AnimationUtils.loadAnimation( fragment.getActivity(), R.anim.bubble_animation );
		animation.setAnimationListener( new Animation.AnimationListener() {
			public void onAnimationStart(Animation animation) {}
			public void onAnimationRepeat(Animation animation) {}
			public void onAnimationEnd(Animation animation) {
				animationDone = true;
			}
		} );
		startAnimation( animation );

		mapIconGeoPoint  = new GeoPoint( (int) (flats.get(0).lat * 1E6), (int) (flats.get(0).lng * 1E6) );
		mapIconPos =  new Point( mapView.getWidth() / 2, mapView.getHeight() / 2 + ImmoscoutPlacesOverlay.markerBounds.height()*2/4 );
		Point currentIconPos = mapView.getProjection().toPixels( mapIconGeoPoint, null );
		int moveX = currentIconPos.x - mapView.getWidth()/2;
		if ( flats.size() > 1 )
			moveX -= (int) (3*screenDesity);
		int moveY = currentIconPos.y - (mapView.getHeight() / 2 + ImmoscoutPlacesOverlay.markerBounds.height()*2/4);
		Point moveToPoint = new Point( mapView.getWidth() / 2 + moveX, mapView.getHeight()/2 + moveY );
		mapView.getController().animateTo( mapView.getProjection().fromPixels( moveToPoint.x, moveToPoint.y ) );
	}

	public void detach() {
		this.mapView.removeView( this );
	}

	public void refreshContent() {
		Log.i( Const.LOG_TAG, "TeaserBubble.refresh" );
		int idx = flatsPager.getCurrentItem();
		FlatsPagerAdapter pagerAdapter = new FlatsPagerAdapter( flats, fragment, isPortfolio );
		flatsPager.setAdapter( pagerAdapter );
		flatsPager.setCurrentItem( idx );
	}
}
