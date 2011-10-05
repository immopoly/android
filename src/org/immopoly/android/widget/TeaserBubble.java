package org.immopoly.android.widget;

import java.util.ArrayList;

import org.immopoly.android.R;
import org.immopoly.android.adapter.FlatsPagerAdapter;
import org.immopoly.android.model.Flat;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

public class TeaserBubble extends FrameLayout {

	// finals
	private static final float MIN_WIDTH_DP 		 = 260;
	private static final float MAX_WIDTH_DP 		 = 320;
	private static final float HEIGHT_DP		 	 = 160;
	private static final float SIDE_PADDING_DP 	 	 =  30;
	private static final float TOP_PADDING_DP 	 	 =   6;
	private static final float RECT_ROUNDNESS_DP 	 =   6;
	private static final float SHADOW_OFFSET_DP		 =   6;
	private static final float NOSE_SIZE_DP		 	 =  15;
	private static final float NOSE_WIDTH_DP		 =  17;

	// statics 
	private static float   screenDesity = 1.0f; // like on a G1!!11!

	private ArrayList<Flat> flats;
	private MapView mapView;
	private int sidePadding;
	private int topPadding;
	public boolean animationDone; // TODO public
	public GeoPoint mapIconGeoPoint; // geopoint of noseTipScreenPos at creation time // TODO public
	Point mapIconPos;

	private Paint shadowPaint;
	private Paint nosePaint;
	private RectF bubbleRect;
	private RectF noseRootRect;
	private Path nosePath;
	
	public TeaserBubble( Activity mapActivity, MapView mapView, ArrayList<Flat> flats) {
		super( mapActivity );
		this.mapView 	 = mapView;
		this.flats 		 = flats;

		DisplayMetrics metrics = new DisplayMetrics();
		mapActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		screenDesity = metrics.density;

		shadowPaint = new Paint();
		shadowPaint.setColor( 0x88000000 );
		shadowPaint.setAntiAlias( true );
		nosePaint = new Paint();
		nosePaint.setStrokeWidth( 2 );
		nosePaint.setAntiAlias( true );
		nosePath = new Path();
		
		setBackgroundDrawable( new ColorDrawable( 0x00000000 ) );
		setup();

		ViewPager flatsPager = new ViewPager( mapActivity );
		final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams( 
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT );
		flatsPager.setLayoutParams(layoutParams);
		FlatsPagerAdapter pagerAdapter = new FlatsPagerAdapter( flats, mapActivity );
		flatsPager.setAdapter( pagerAdapter );
		addView( flatsPager );
		
		mapView.addView( this );
		Animation animation = AnimationUtils.loadAnimation( mapActivity, R.anim.bubble_animation );
		animation.setAnimationListener( new Animation.AnimationListener() {
			public void onAnimationStart(Animation animation) {}
			public void onAnimationRepeat(Animation animation) {}
			public void onAnimationEnd(Animation animation) {
				animationDone = true;
			}
		} );
		startAnimation( animation );
	}

	private void setup() {  // TODO cleanup
		float mapWidthDp = mapView.getWidth() / screenDesity;
		float widthDp = mapWidthDp;  						// whole width for now
		float sidePaddingDp = 0;
		if ( mapWidthDp > MIN_WIDTH_DP ) {
			widthDp = MIN_WIDTH_DP;							// MIN_WIDTH + evtly sidePadding
			sidePaddingDp = (mapWidthDp - MIN_WIDTH_DP) / 2;
			if ( sidePaddingDp > SIDE_PADDING_DP ) {
				sidePaddingDp = SIDE_PADDING_DP;			// perfect SIDE_PADDING + whole width left
				widthDp = mapWidthDp - 2*SIDE_PADDING_DP;
				if ( widthDp > MAX_WIDTH_DP )				// perfect SIDE_PADDING_DP + perfect width
					widthDp = MAX_WIDTH_DP;
			}
		}
		int width  		  = (int) (widthDp		  * screenDesity);
		int height 		  = (int) (HEIGHT_DP 	  * screenDesity);
		sidePadding   	  = (int) (sidePaddingDp	  * screenDesity);
		topPadding	  	  = (int) (TOP_PADDING_DP * screenDesity);
		int bottomPadding = (int) (NOSE_SIZE_DP   * screenDesity);
		int x 			  = (mapView.getWidth()  - (width+sidePadding*2)) / 2;
		int y 			  = 0;

		final int morePadding = (int) (6*screenDesity);
		
		setPadding( sidePadding+morePadding, topPadding+morePadding, sidePadding+morePadding, bottomPadding+morePadding );
		setLayoutParams( new MapView.LayoutParams( width+2*sidePadding, height+topPadding+bottomPadding, x, y, MapView.LayoutParams.TOP_LEFT ) );
		
		final int noseSizePx  = (int) (NOSE_SIZE_DP  * screenDesity);
		final int noseWidthPx = (int) (NOSE_WIDTH_DP * screenDesity / 2.0f);

		bubbleRect = new RectF( sidePadding+1, topPadding+1, sidePadding+width-1, topPadding+height-1 );
		noseRootRect = new RectF( ((sidePadding*2+width)+noseWidthPx+3)/2, topPadding+height-4,
				 				  ((sidePadding*2+width)-noseWidthPx-3)/2, topPadding+height+1 );
		int pathX = sidePadding + width/2 - noseWidthPx;
		int pathY = topPadding + height;
		nosePath.reset();
		nosePath.moveTo( pathX, pathY );
		nosePath.lineTo( pathX, pathY );
		pathX += noseWidthPx; pathY += noseSizePx;
		nosePath.lineTo( pathX, pathY);
		pathX += noseWidthPx; pathY -= noseSizePx;
		nosePath.lineTo( pathX, pathY );
		
		mapIconGeoPoint  = new GeoPoint( (int) (flats.get(0).lat * 1E6), (int) (flats.get(0).lng * 1E6) );
		mapIconPos =  new Point( mapView.getWidth() / 2, y + height+topPadding+bottomPadding + ImmoscoutPlacesOverlay.markerBounds.height()*3/4 );
		Point currentIconPos = mapView.getProjection().toPixels( mapIconGeoPoint, null );
		int moveX = currentIconPos.x - mapView.getWidth()/2;
		int moveY = currentIconPos.y - (y + height + topPadding + bottomPadding + ImmoscoutPlacesOverlay.markerBounds.height()*3/4);
		Point moveToPoint = new Point( mapView.getWidth() / 2 + moveX, mapView.getHeight()/2 + moveY );
		mapView.getController().animateTo( mapView.getProjection().fromPixels( moveToPoint.x, moveToPoint.y ) );
	}
	
	@Override
	public void onDraw( Canvas canvas ) {
		final int noseSizePx  = (int) (NOSE_SIZE_DP  * screenDesity);
		final int width  = getWidth();
		final int height = getHeight();
		
		// draw shadow
		final float shadowHeight = getHeight()*0.5f;
		final float skew = 0.3f;
		final float roundness = RECT_ROUNDNESS_DP * screenDesity;
		RectF r = new RectF( sidePadding, shadowHeight, width-sidePadding, height-noseSizePx+SHADOW_OFFSET_DP*screenDesity );
		canvas.save();
		canvas.translate( shadowHeight*skew*1.96f, 0 );
		canvas.skew( -skew, 0.0f );
		canvas.drawRoundRect( r, roundness, roundness, shadowPaint );
		canvas.restore();

		// draw nose
		nosePaint.setColor( 0xFFFFFFFF );
		nosePaint.setStyle( Style.FILL );
		canvas.drawPath( nosePath, nosePaint );
		nosePaint.setColor( 0xFF000000 );
		nosePaint.setStyle( Style.STROKE );
		canvas.drawPath( nosePath, nosePaint );
		
		// draw bubble
		nosePaint.setColor( Color.WHITE );
		nosePaint.setStyle( Style.FILL );
		canvas.drawRoundRect( bubbleRect, roundness, roundness, nosePaint);
		nosePaint.setColor( Color.BLACK );
		nosePaint.setAntiAlias( true );
		nosePaint.setStrokeWidth( 3 );
		nosePaint.setStyle( Style.STROKE );
		canvas.drawRoundRect( bubbleRect, roundness, roundness, nosePaint);
		nosePaint.setColor( Color.WHITE );
		nosePaint.setStyle( Style.FILL );
		canvas.drawRect( noseRootRect, nosePaint);
	}

	public void detach() {
		this.mapView.removeView( this );
	}
}
