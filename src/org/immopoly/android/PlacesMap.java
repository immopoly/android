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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import oauth.signpost.OAuth;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import org.immopoly.android.api.ApiResultReciever.Receiver;
import org.immopoly.android.api.IS24ApiService;
import org.immopoly.android.api.ReceiverState;
import org.immopoly.android.constants.Const;
import org.immopoly.android.helper.LocationHelper;
import org.immopoly.android.helper.MapLocationCallback;
import org.immopoly.android.helper.MapMarkerCallback;
import org.immopoly.android.helper.Settings;
import org.immopoly.android.helper.TrackingManager;
import org.immopoly.android.helper.WebHelper;
import org.immopoly.android.model.Flat;
import org.immopoly.android.model.Flats;
import org.immopoly.android.model.ImmopolyHistory;
import org.immopoly.android.model.ImmopolyUser;
import org.immopoly.android.model.OAuthData;
import org.immopoly.android.provider.FlatsProvider;
import org.immopoly.android.tasks.GetUserInfoTask;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class PlacesMap extends MapActivity implements Receiver,
		MapMarkerCallback, MapLocationCallback {
	public static final String TAG = "Immopoly";
	private static long CURRENTTIME=System.currentTimeMillis();
	
	private MapController mMapController;
	private List<Overlay> mMapOverlays;
	private PlaceOverlayItem myLocationOverlayItem;
	private MapView mMapView;
	private ImmoscoutPlacesOverlay overlays;
	private Flats mFlats;

	public static final int RELOAD_DELAY_MILLIS = 800;
	ReceiverState mState;

	private MyPositionOverlay myLocationOverlays;
	private int mNumberGeoCodeTry;
	private Button mRefreshButton;
	private Flat mCurrentFlat;

	private GoogleAnalyticsTracker tracker;
	private RelativeLayout contentView;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		tracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker in manual dispatch mode...
		tracker.startNewSession(TrackingManager.UA_ACCOUNT, this);
		tracker.trackPageView(TrackingManager.VIEW_MAP);

		mState = (ReceiverState) getLastNonConfigurationInstance();
		if (mState != null) {
			// Start listening for Service updates again
			mState.mReceiver.setReceiver(this);
		} else {
			mState = new ReceiverState();
			mState.mReceiver.setReceiver(this);
		}

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.contentView = (RelativeLayout) getLayoutInflater().inflate( R.layout.places_map, null );
		setContentView( this.contentView );

		mMapView = (MapView) findViewById(R.id.mapview);

		mMapController = mMapView.getController();

		mRefreshButton = (Button) findViewById(R.id.location_refresh);
		mRefreshButton.startAnimation(AnimationUtils.loadAnimation(this,
				R.anim.locating_animation));

		GeoPoint point = new GeoPoint((int) (LocationHelper.sLat * 1E6),
				(int) (LocationHelper.sLng * 1E6));

		// //---Add a city marker---
		// Overlay mapOverlay = new Overlay();
		// List<Overlay> listOfOverlays = mapView.getOverlays();
		// listOfOverlays.clear();
		// listOfOverlays.add(mapOverlay);
		mMapOverlays = mMapView.getOverlays();
		LocationHelper.callback = this;
		LocationHelper.getLastLocation(this);
		// this is the bounding box container
		
		myLocationOverlayItem = new PlaceOverlayItem(point, "my city",
				"This is wher you are");
		overlays = new ImmoscoutPlacesOverlay( this, mMapView,
				getLayoutInflater());
		myLocationOverlays = new MyPositionOverlay( this.getResources().getDrawable(R.drawable.house_icon), this,
				mMapView, getLayoutInflater());
		myLocationOverlayItem.setMarker(this.getResources().getDrawable( R.drawable.house_icon) );

		myLocationOverlays.addOverlay(myLocationOverlayItem);
		mMapOverlays.add(myLocationOverlays);
		// setMapViewWithZoom(R.id.mapview, R.id.map_zoom_controls);
		mMapView.setBuiltInZoomControls( true );
		mMapView.invalidate();

		// maybe do this in your init or something
		final GestureDetector gDetector = new GestureDetector(new MyDetector());
		mMapView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				return gDetector.onTouchEvent(event);

			}

		});

		// signIn();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		LocationHelper.callback = this;
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	public MapView getMapView() {
		return mMapView;
	}
	
	public RelativeLayout getContentView() {
		return contentView;
	}
	
	public void setMapViewWithZoom(int mapLayoutId, int zoomControlsLayoutId) {
		mMapView = (MapView) findViewById(mapLayoutId);

		ZoomControls zoomControls = (ZoomControls) findViewById(zoomControlsLayoutId);
		zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getMapView().getController().zoomInFixing(
						getMapView().getWidth() / 2,
						getMapView().getHeight() / 2);
			}
		});
		zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getMapView().getController().zoomOut();
			}
		});
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		// Clear reference to receiver
		// we will re-attach in onCreate
		mState.mReceiver.clearReceiver();
		return mState;
	}

	@Override
	public void onNewIntent(Intent newIntent) {
		super.onNewIntent(newIntent);
		processData(newIntent);
	}

	private void processData(Intent newIntent) {
		if (newIntent.hasExtra("addToPortifolio")) {
			Toast.makeText(this, "ADD ADD ADD", Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {
		switch (resultCode) {
		case IS24ApiService.STATUS_RUNNING:
			// show progress
			// Toast.makeText(this, "Running", Toast.LENGTH_SHORT).show();
			break;
		case IS24ApiService.STATUS_FINISHED:
			// Toast.makeText(this, "Finished", Toast.LENGTH_SHORT).show();
			ArrayList<Flat> a = resultData.getParcelableArrayList("flats");
			mFlats = (Flats) a;
			updateMap(true);
			// do something interesting
			// hide progress
			break;
		case IS24ApiService.STATUS_ERROR:
			// Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
			// handle the error;
			break;
		}
	}

	public void updateMap(boolean centerMap) {
		if (mFlats != null) {
			int count = 0;
			double minX = 999, minY = 999, maxX = -999, maxY = -999;
			myLocationOverlays.clear();
			mMapOverlays.clear();

			GeoPoint point = new GeoPoint((int) (LocationHelper.sLat * 1E6),
					(int) (LocationHelper.sLng * 1E6));

			myLocationOverlayItem = new PlaceOverlayItem(point, "my city",
					"THis is wher you are");
			myLocationOverlayItem.setMarker(this.getResources().getDrawable(
					R.drawable.house_icon) );

			myLocationOverlays.addOverlay(myLocationOverlayItem);

			mMapOverlays.add(myLocationOverlays);

			Cursor cur;
			Log.d(this.getClass().getName(), "Flats :" + mFlats.size());
			for (Flat f : mFlats) {
				if (f.lat != 0.0 || f.lng != 0.0) {
					cur = getContentResolver().query(FlatsProvider.CONTENT_URI,
							null, FlatsProvider.Flat.FLAT_ID + "=" + f.uid,
							null, null);
					f.owned = cur.getCount() == 1;
					cur.close();

					if (f.lng < minX)
						minX = f.lng;
					if (f.lng > maxX)
						maxX = f.lng;

					if (f.lat < minY)
						minY = f.lat;
					if (f.lat > maxY)
						maxY = f.lat;

					count++;
				}
			}
			overlays.setFlats( mFlats );
			
			if (LocationHelper.sLng < minX)
				minX = LocationHelper.sLng;
			if (LocationHelper.sLng > maxX)
				maxX = LocationHelper.sLng;

			if (LocationHelper.sLat < minY)
				minY = LocationHelper.sLat;
			if (LocationHelper.sLat > maxY)
				maxY = LocationHelper.sLat;

			int mLon = (int) Math.round((minX + (maxX - minX) / 2.0) * 1000000);
			int mLat = (int) Math.round((minY + (maxY - minY) / 2.0) * 1000000);
			if (centerMap) {
				mMapController.setCenter(new GeoPoint(mLat, mLon));
			}
			if (count > 0) {
				mMapOverlays.add(overlays);
				// TODO etwas weiter rauszoomen
				int spanLon = (int) Math.round(((maxX - minX) / 2.0) * 1000000);
				int spanLat = (int) Math.round(((maxY - minY) / 2.0) * 1000000);
				mMapController.zoomToSpan(spanLat, spanLon);

			}

			mMapView.invalidate();
		}

	}

	public void signIn() {
		String authUrl = "";
		SharedPreferences shared = getSharedPreferences("oauth", 0);
		String accessToken = shared.getString("oauth_token", "");
		if (accessToken.length() != accessToken.length()) {
			OAuthData.signedIn = true;
			OAuthData.accessToken = accessToken;

		} else {
			OAuthData.signedIn = false;
			try {
				authUrl = OAuthData.provider.retrieveRequestToken(
						OAuthData.consumer, OAuth.OUT_OF_BAND);
				Log.d("OAUTH", authUrl);
			} catch (OAuthMessageSignerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OAuthNotAuthorizedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OAuthExpectationFailedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OAuthCommunicationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Login in web view
			Intent i = new Intent(this, WebViewActivity.class);
			i.putExtra(Const.AUTH_URL, authUrl);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1 && resultCode == RESULT_OK) {
			String exposeID = data.getExtras().getString(Const.EXPOSE_ID);
			new AddToPortifolioTask().execute(exposeID);
		}
	}

	@Override
	public void callbackCall(Flat f) {
		mCurrentFlat = f;
		Intent i = new Intent(this, ExposeWebViewActivity.class);
		i.putExtra(Const.EXPOSE_ID, String.valueOf(f.uid));
		i.putExtra(Const.EXPOSE_NAME, String.valueOf(f.name));
		i.putExtra(Const.EXPOSE_DESC, String.valueOf(f.description));
		i.putExtra(Const.EXPOSE_PICTURE_SMALL,
				String.valueOf(f.titlePictureSmall));
		i.putExtra(Const.EXPOSE_IN_PORTOFOLIO, f.owned);
		i.putExtra(Const.SOURCE, MapActivity.class.getSimpleName());
		startActivityForResult(i, 1);

	}

	public void headerClick(View v) {
		switch (v.getId()) {
		case R.id.location_refresh:
			LocationHelper.getLastLocation(this);
			mRefreshButton.startAnimation(AnimationUtils.loadAnimation(this,
					R.anim.locating_animation));
			break;
		case R.id.header_logo:
			startActivity(new Intent(this, DashboardActivity.class));
			break;
		}

	}

	private class GeoCodeLocationTask extends AsyncTask<Double, Void, String> {

		@Override
		protected String doInBackground(Double... params) {
			String result = null;
			LocationHelper.mAddress = null;
			if ((params[0] != null && params[1] != null)
					&& (params[0] != 0.0 && params[1] != 0.0)) {
				try {
					Geocoder geocoder = new Geocoder(PlacesMap.this,
							Locale.getDefault());
					List<Address> addresses = geocoder.getFromLocation(
							params[0], params[1], 1);
					if (addresses != null && addresses.size() > 0) {
						Address address = addresses.get(0);
						// sending back first mAddress line and locality
						result = address.getAddressLine(0) + ", "
								+ address.getLocality();
						LocationHelper.mAddress = result;
						// ((TextView)findViewById(R.id.header_location)).setText(result);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {

				result = null;
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			setAddress(result);
		}
	}

	private void setAddress(String address) {
		if (address != null && address.length() > 0) {
			mNumberGeoCodeTry = 0;
			((TextView) findViewById(R.id.header_location)).setText(address);
		} else if (LocationHelper.sAccuracy >= 0) {
			mNumberGeoCodeTry++;
			if (mNumberGeoCodeTry < 3) {
				new GeoCodeLocationTask().execute(LocationHelper.sLat,
						LocationHelper.sLng);
			} else {
				NumberFormat nFormat = NumberFormat.getInstance(Locale.GERMANY);
				nFormat.setMinimumIntegerDigits(2);
				nFormat.setMaximumFractionDigits(2);
				((TextView) findViewById(R.id.header_location)).setText("lat:"
						+ nFormat.format(LocationHelper.sLat) + " - lng:"
						+ nFormat.format(LocationHelper.sLng) + " ~"
						+ nFormat.format(LocationHelper.sAccuracy));
			}
		} else {
			((TextView) findViewById(R.id.header_location))
					.setText(R.string.no_location_value);
		}
	}

	class MyDetector extends SimpleOnGestureListener {
		@Override
		public boolean onDoubleTap(MotionEvent event) {
			mMapView.getController().zoomInFixing((int) event.getX(),
					(int) event.getY());
			return super.onDoubleTap(event);
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			return super.onSingleTapConfirmed(e);
		}
	}

	class AddToPortifolioTask extends AsyncTask<String, Void, ImmopolyHistory> {

		@Override
		protected ImmopolyHistory doInBackground(String... params) {
			JSONObject obj = null;
			ImmopolyHistory history = null;
			try {
				ImmopolyUser.getInstance().readToken(PlacesMap.this);
				obj = WebHelper.getHttpData(new URL(WebHelper.SERVER_URL_PREFIX
						+ "/portfolio/add?token="
						+ ImmopolyUser.getInstance().getToken() + "&expose="
						+ params[0]), false, PlacesMap.this);
				if (obj != null
						&& !obj.has("org.immopoly.common.ImmopolyException")) {
					history = new ImmopolyHistory();
					history.fromJSON(obj);
					tracker.trackEvent(TrackingManager.CATEGORY_ALERT,
							TrackingManager.ACTION_TAKE_OVER,
							TrackingManager.LABEL_RESPONSE_OK, 0);
				} else if (obj != null) {
					history = new ImmopolyHistory();
					switch (obj.getJSONObject(
							"org.immopoly.common.ImmopolyException").getInt(
							"errorCode")) {
					case 201:
						history.mText = getString(R.string.flat_already_in_portifolio);
						break;
					case 301:
						history.mText = getString(R.string.flat_does_not_exist_anymore);

						break;
					case 302:
						history.mText = getString(R.string.flat_has_no_raw_rent);
						break;
					}
					tracker.trackEvent(TrackingManager.CATEGORY_ALERT,
							TrackingManager.ACTION_TAKE_OVER,
							TrackingManager.LABEL_RESPONSE_FAIL, 0);
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return history;
		}

		@Override
		protected void onPostExecute(ImmopolyHistory result) {
			final ImmopolyHistory res = result;
			if (result != null && result.mText != null
					&& result.mText.length() > 0) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						PlacesMap.this);
				builder.setTitle(getString(R.string.take_over_try));
				builder.setMessage(result.mText);
				builder.setCancelable(true).setNegativeButton(
						getString(R.string.share_item),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								Settings.getFlatLink(
										mCurrentFlat.uid.toString(), false);
								Settings.shareMessage(PlacesMap.this,
										getString(R.string.take_over_try),
										res.mText, Settings.getFlatLink(
												mCurrentFlat.uid.toString(),
												false) /* LINk */);
								tracker.trackEvent(
										TrackingManager.CATEGORY_ALERT,
										TrackingManager.ACTION_TAKE_OVER_SHARE,
										TrackingManager.LABEL_RESPONSE_OK, 0);
							}

						});
				builder.setPositiveButton(R.string.button_ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								tracker.trackEvent(
										TrackingManager.CATEGORY_ALERT,
										TrackingManager.ACTION_TAKE_OVER_SHARE,
										TrackingManager.LABEL_RESPONSE_FAIL, 0);
							}
						});
				AlertDialog alert = builder.create();
				alert.show();
				// Toast.makeText(PlacesMap.this, res.mText, Toast.LENGTH_LONG)
				// .show();
				new GetUserInfoUpdateTask(PlacesMap.this).execute(ImmopolyUser
						.getInstance().getToken());
			} else if (Settings.isOnline(PlacesMap.this)) {
				Toast.makeText(PlacesMap.this, R.string.expose_couldnt_add,
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(PlacesMap.this, R.string.no_internet_connection,
						Toast.LENGTH_LONG).show();
			}
			super.onPostExecute(result);
		}

	}

	private class GetUserInfoUpdateTask extends GetUserInfoTask {

		public GetUserInfoUpdateTask(Context context) {
			super(context);
		}

		@Override
		protected void onPostExecute(ImmopolyUser result) {
			if (result != null && ImmopolyUser.getInstance().flats != null) {
				updateMap(false);
			} else if (Settings.isOnline(PlacesMap.this)) {
				Intent intent = new Intent(PlacesMap.this,
						UserSignupActivity.class);
				startActivity(intent);
			} else {
				Toast.makeText(PlacesMap.this, R.string.no_internet_connection,
						Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public void updateMapData(boolean newLoc) {
		mRefreshButton.clearAnimation();
		if (newLoc || LocationHelper.mAddress == null) {
			new GeoCodeLocationTask().execute(LocationHelper.sLat,
					LocationHelper.sLng);
		} else {
			setAddress(LocationHelper.mAddress);
		}
		Intent i = new Intent(this, IS24ApiService.class);
		i.putExtra(IS24ApiService.COMMAND, IS24ApiService.CMD_SEARCH);
		i.putExtra(IS24ApiService.LAT, LocationHelper.sLat);
		i.putExtra(IS24ApiService.LNG, LocationHelper.sLng);
		i.putExtra(IS24ApiService.API_RECEIVER, mState.mReceiver);
		startService(i);
	}

	public void showInfo(View v) {

		LayoutInflater inflater = LayoutInflater.from(PlacesMap.this);

		View alertDialogView = inflater.inflate(R.layout.info_webview, null);

		WebView myWebView = (WebView) alertDialogView
				.findViewById(R.id.DialogWebView);
		myWebView.setWebViewClient(new WebViewClient());
		myWebView.getSettings().setSupportZoom(true);
		myWebView.getSettings().setUseWideViewPort(true);

		myWebView.loadUrl(WebHelper.SERVER_URL_PREFIX);
		AlertDialog.Builder builder = new AlertDialog.Builder(PlacesMap.this);
		builder.setView(alertDialogView);
		builder.setPositiveButton(R.string.button_ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		tracker.stopSession();
	}

}
