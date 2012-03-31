package org.immopoly.android.fragments;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.immopoly.android.R;
import org.immopoly.android.api.ApiResultReciever.Receiver;
import org.immopoly.android.api.IS24ApiService;
import org.immopoly.android.api.ReceiverState;
import org.immopoly.android.app.ImmopolyActivity;
import org.immopoly.android.app.UserDataListener;
import org.immopoly.android.app.UserDataManager;
import org.immopoly.android.constants.Const;
import org.immopoly.android.helper.HudPopupHelper;
import org.immopoly.android.helper.LocationHelper;
import org.immopoly.android.helper.OnTrackingEventListener;
import org.immopoly.android.helper.TrackingManager;
import org.immopoly.android.model.Flat;
import org.immopoly.android.model.Flats;
import org.immopoly.android.model.ImmopolyUser;
import org.immopoly.android.tasks.FreeFlatsTask;
import org.immopoly.android.widget.ImmoscoutPlacesOverlay;
import org.immopoly.android.widget.MyPositionOverlay;
import org.immopoly.android.widget.PlaceOverlayItem;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class MapFragment extends Fragment implements Receiver, OnMapItemClickedListener, UserDataListener {

	public static final String TAG = "Immopoly";

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
	private HudPopupHelper mHudPopup;

	private Button hudText;
	private ImageButton mapButton;

	private OnMapItemClickedListener mOnMapItemClickedListener;
	private OnTrackingEventListener mEventListener;

	private ProgressBar mProgressIndicator;
	private ImageView mCompassButton;

	private View mSplashscreen;

	private View mActionItemFreeFlats;

	private ImageButton itemsButton;

	public OnMapItemClickedListener getOnMapItemClickedListener() {
		return mOnMapItemClickedListener;
	}

	// @Override
	// public void onHudAction(View view) {
	// switch (view.getId()) {
	// case R.id.hud_map:
	// // hud map
	// // already there
	// LocationHelper.getLastLocation(getActivity());
	// break;
	// case R.id.hud_portfolio:
	// startActivity(new Intent(getActivity(), DashboardActivity.class));
	// break;
	// case R.id.hud_profile:
	// startActivity(new Intent(getActivity(), DashboardActivity.class));
	// break;
	// case R.id.hud_text:
	// // Toast.makeText(this, ImmopolyUser.getInstance().flats.toString(),
	// // Toast.LENGTH_LONG);
	// if (mHudPopup != null) {
	// mHudPopup.show(getActivity().findViewById(R.id.hud_text), -200, -60);
	// }
	// break;
	// default:
	// break;
	// }
	// }

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mOnMapItemClickedListener = (OnMapItemClickedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnMapItemClickedListener");
		}
		try {
			mEventListener = (OnTrackingEventListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnTrackingEventListener");
		}
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mMapView = ((ImmopolyActivity) getActivity()).acquireMapView(this);
		mMapView.setBuiltInZoomControls(true);
		mMapController = mMapView.getController();
		
		// mState = (ReceiverState) getActivity()
		// .getLastNonConfigurationInstance();
		if (mState != null) {
			// Start listening for Service updates again
			mState.mReceiver.setReceiver(this);
		} else {
			mState = new ReceiverState();
			mState.mReceiver.setReceiver(this);
		}

		UserDataManager.instance.addUserDataListener(this);

		// wrap map in relative layout for windrose icon rechts oben
		// https://github.com/immopoly/android/issues/12
		View layout = inflater.inflate(R.layout.map_fragment, null, false);
		RelativeLayout relativeLayout = (RelativeLayout) layout.findViewById(R.id.map_relative_layout);
		relativeLayout.setGravity(Gravity.RIGHT);
		relativeLayout.addView(mMapView, 0);
		// ImageView compass = new ImageView(getActivity());
		// compass.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_compass));
		// relativeLayout.addView(compass,1);
		mProgressIndicator = (ProgressBar) layout.findViewById(R.id.map_progress);
		mCompassButton = (ImageView) layout.findViewById(R.id.map_reload);
		mSplashscreen = layout.findViewById(R.id.splashscreen);
		itemsButton = (ImageButton) layout.findViewById(R.id.items_button);
		itemsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new ItemsFragment().show(getFragmentManager(), "itemsDialog");
			}
		});
		updateActionItem();

		if (mMapOverlays == null) {
			showSplashScreen();
		}
		mCompassButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showProgress(true);
				LocationHelper.getLastLocation(getActivity(), new MapLocationCallback());
			}

		});
		return layout;
	}

	private void showProgress(boolean showProgress) {
		if (null != mProgressIndicator)
			mProgressIndicator.setVisibility(showProgress ? View.VISIBLE : View.GONE);
		if (null != mCompassButton)
			mCompassButton.setVisibility(showProgress ? View.GONE : View.VISIBLE);
	}

	private void showSplashScreen() {
		mSplashscreen.setVisibility(View.VISIBLE);
		PackageInfo packInfo;
		String version = "";
		try {
			packInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
			version = "Version " + packInfo.versionName + " (" + packInfo.versionCode + ")";
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		((TextView) mSplashscreen.findViewById(R.id.splash_version)).setText(version);
		new Handler().postDelayed(new Runnable() {
			public void run() {
				hideSplashScreen();
			}
		}, 10000);
	}

	private void hideSplashScreen() {
		if (!isAdded() || !mSplashscreen.isShown())
			return;
		Animation fadeOutAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
		fadeOutAnim.setAnimationListener(new Animation.AnimationListener() {
			public void onAnimationStart(Animation animation) {
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationEnd(Animation animation) {
				if (isAdded() && mSplashscreen.isShown())
					mSplashscreen.setVisibility(View.GONE);
			}
		});
		mSplashscreen.startAnimation(fadeOutAnim);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		GeoPoint point = new GeoPoint((int) (LocationHelper.sLat * 1E6), (int) (LocationHelper.sLng * 1E6));

		mMapOverlays = mMapView.getOverlays();

		// this is the bounding box container

		myLocationOverlayItem = new PlaceOverlayItem(point, "my city", "This is wher you are");
		overlays = new ImmoscoutPlacesOverlay(this, mMapView, getActivity().getLayoutInflater(), false);
		myLocationOverlays = new MyPositionOverlay(getResources().getDrawable(R.drawable.mylocation), getActivity(),
				mMapView, getActivity().getLayoutInflater());
		myLocationOverlayItem.setMarker(this.getResources().getDrawable(R.drawable.mylocation));

		myLocationOverlays.setPlaceOverlayItem(myLocationOverlayItem);
		mMapOverlays.add(myLocationOverlays);
		// setMapViewWithZoom(R.id.mapview, R.id.map_zoom_controls);
		mMapView.setBuiltInZoomControls(true);
		mMapView.invalidate();

		if (mFlats == null) {
			LocationHelper.getLastLocation(getActivity(), new MapLocationCallback());
		} else {
			syncFlats(); // flats may have been released in portfolio fragments
			updateMap(true);
		}
	}

	class MapLocationCallback implements LocationHelper.LocationCallback {

		@Override
		public void onLocationChanged(boolean center) {
			if (getActivity() != null) {
				if (mMapView != null)
					mMapView.getController().setCenter(
							new GeoPoint((int) (LocationHelper.sLat * 1E6), (int) (LocationHelper.sLng * 1E6)));
				requestFlatUpdate(center);
			}
		}

		@Override
		public void failed() {
			showProgress(true);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		mEventListener.onTrackPageView(TrackingManager.VIEW_MAP);
		updateHud(null, 0);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mHudPopup != null) {
			mHudPopup.dismiss();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	public void onDestroyView() {
		UserDataManager.instance.removeUserDataListener(this);
		((ImmopolyActivity) getActivity()).releaseMapView(this);
		Log.i("IMPO", "MapFragment.onDestroyView");
		mMapView = null;
		super.onDestroyView();
	}

	public MapView getMapView() {
		return mMapView;
	}

	public ViewGroup getContentView() {
		return mMapView;
	}

	public void setMapViewWithZoom(int mapLayoutId, int zoomControlsLayoutId) {

		ZoomControls zoomControls = (ZoomControls) mMapView.findViewById(zoomControlsLayoutId);
		zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getMapView().getController().zoomInFixing(getMapView().getWidth() / 2, getMapView().getHeight() / 2);
			}
		});
		zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getMapView().getController().zoomOut();
			}
		});
	}

	private void requestFlatUpdate(boolean newLoc) {
		// TODO schtief we dont need this right now
		// if (newLoc || LocationHelper.mAddress == null) {
		// new GeoCodeLocationTask().execute(LocationHelper.sLat,
		// LocationHelper.sLng);
		// } else {
		// setAddress(LocationHelper.mAddress);
		// }
		Intent i = new Intent(getActivity(), IS24ApiService.class);
		i.putExtra(IS24ApiService.LAT, LocationHelper.sLat);
		i.putExtra(IS24ApiService.LNG, LocationHelper.sLng);
		i.putExtra(IS24ApiService.API_RECEIVER, mState.mReceiver);
		getActivity().startService(i);
		updateHud(null, 0);
	}

	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {
		Log.i(Const.LOG_TAG, "onReceiveResult " + resultCode);
		switch (resultCode) {
		case IS24ApiService.STATUS_RUNNING:
			// show progress
			// Toast.makeText(this, "Running", Toast.LENGTH_SHORT).show();
			showProgress(true);
			break;
		case IS24ApiService.STATUS_FINISHED:
			// Toast.makeText(this, "Finished", Toast.LENGTH_SHORT).show();
			ArrayList<Flat> a = resultData.getParcelableArrayList("flats");
			mFlats = (Flats) a;
			syncFlats();
			updateMap(true);
			// do something interesting
			// hide progress
			showProgress(false);
			hideSplashScreen();
			break;
		case IS24ApiService.STATUS_ERROR:
			// handle the error;
			showProgress(false);
			hideSplashScreen();
			if (IS24ApiService.NO_FLATS.equals(resultData.getString(Intent.EXTRA_TEXT)))
				Toast.makeText(getActivity(), R.string.sorry_no_flats, Toast.LENGTH_LONG).show();
			// else there was an exception (probably logged already). what TODO?
			break;
		}
	}

	// sets immopoly specific attributes on owned Flats that were loaded from
	// IS24 json
	private void syncFlats() {
		if (mFlats == null)
			return;
		for (Flat iscoutFlat : mFlats) {
			int uid = iscoutFlat.uid;
			iscoutFlat.owned = false;
			if (UserDataManager.instance.getState() == UserDataManager.LOGGED_IN)
				for (Flat userFlat : ImmopolyUser.getInstance().getPortfolio()) {
					if (userFlat.uid == uid) {
						iscoutFlat.takeoverDate = userFlat.takeoverDate;
						iscoutFlat.takeoverTries = userFlat.takeoverTries;
						iscoutFlat.owned = true;
						break;
					}
				}
		}
	}

	public void updateMap(boolean centerMap) {
		if (mMapView == null) // currently not attached
			return;
		int count = 0;
		double minX = 999, minY = 999, maxX = -999, maxY = -999;
		myLocationOverlays.clear();
		mMapOverlays.clear();

		GeoPoint point = new GeoPoint((int) (LocationHelper.sLat * 1E6), (int) (LocationHelper.sLng * 1E6));

		myLocationOverlayItem = new PlaceOverlayItem(point, "my city", "THis is wher you are");
		myLocationOverlayItem.setMarker(this.getResources().getDrawable(R.drawable.mylocation));

		myLocationOverlays.setPlaceOverlayItem(myLocationOverlayItem);
		mMapOverlays.add(myLocationOverlays);

		if (mMapView != null && mFlats != null) {
			for (Flat f : mFlats) {
				if (f.visible) {
					if (f.lat != 0.0 || f.lng != 0.0) {
						if (f.lng < minX) {
							minX = f.lng;
						}
						if (f.lng > maxX) {
							maxX = f.lng;
						}

						if (f.lat < minY)
							minY = f.lat;
						if (f.lat > maxY)
							maxY = f.lat;

						count++;
					}
				}
			}

			overlays.setFlats(mFlats);

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
				int spanLon = (int) Math.round(((maxX - minX) / 1.0) * 1000000);
				int spanLat = (int) Math.round(((maxY - minY) / 1.0) * 1000000);
				mMapController.zoomToSpan(spanLat, spanLon);
			}
			if (count > 0) {
				mMapOverlays.add(overlays);
			}
			mMapView.invalidate();
		}

	}

	private class GeoCodeLocationTask extends AsyncTask<Double, Void, String> {

		@Override
		protected String doInBackground(Double... params) {
			String result = null;
			LocationHelper.mAddress = null;
			if ((params[0] != null && params[1] != null) && (params[0] != 0.0 && params[1] != 0.0)) {
				try {
					Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
					List<Address> addresses = geocoder.getFromLocation(params[0], params[1], 1);
					if (addresses != null && addresses.size() > 0) {
						Address address = addresses.get(0);
						// sending back first mAddress line and locality
						result = address.getAddressLine(0) + ", " + address.getLocality();
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
			// TODO schtief we dont need geocoding right now
			// setAddress(result);
		}
	}

	// TODO schtief we dont need geocoding right now
	// private void setAddress(String address) {
	// if (address != null && address.length() > 0) {
	// mNumberGeoCodeTry = 0;
	// // ((TextView) findViewById(R.id.header_location)).setText(address);
	// } else if (LocationHelper.sAccuracy >= 0) {
	// mNumberGeoCodeTry++;
	// if (mNumberGeoCodeTry < 3) {
	// new GeoCodeLocationTask().execute(LocationHelper.sLat,
	// LocationHelper.sLng);
	// } else {
	// NumberFormat nFormat = NumberFormat.getInstance(Locale.GERMANY);
	// nFormat.setMinimumIntegerDigits(2);
	// nFormat.setMaximumFractionDigits(2);
	// /*
	// * ((TextView)
	// * findViewById(R.id.header_location)).setText("lat:" +
	// * nFormat.format(LocationHelper.sLat) + " - lng:" +
	// * nFormat.format(LocationHelper.sLng) + " ~" +
	// * nFormat.format(LocationHelper.sAccuracy));
	// */
	// }
	// } else {
	// // ((TextView) findViewById(R.id.header_location))
	// // .setText(R.string.no_location_value);
	// }
	// }

	public void updateHud(Intent data, int element) {
		if (mapButton != null) {
			mapButton.setSelected(true);
		}
		if (hudText != null) {
			NumberFormat nFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY);
			nFormat.setMinimumIntegerDigits(1);
			nFormat.setMaximumFractionDigits(2);
			hudText.setText(nFormat.format(ImmopolyUser.getInstance().getBalance()));
		}
	}

	@Override
	public void startActivity(Intent arg0) {
		getActivity().sendBroadcast(arg0);
	}

	@Override
	public void onFlatClicked(Flat flat) {
		mOnMapItemClickedListener.onFlatClicked(flat);
	}

	@Override
	public void onUserDataUpdated() {
		syncFlats();
		updateMap(false);
		overlays.updateBubble();
		updateActionItem();
	}

	private void updateActionItem() {
		// check if there are actionItems with amount > 0
		if (ImmopolyUser.getInstance().hasActionItemWithAmount())
			itemsButton.setVisibility(View.VISIBLE);
		else
			itemsButton.setVisibility(View.GONE);

	}

	public void hideCompass() {
		mCompassButton.setVisibility(View.GONE);
	}

	public void showCompass() {
		if (mProgressIndicator.getVisibility() != View.VISIBLE)
			mCompassButton.setVisibility(View.VISIBLE);
	}

	public void filterFreeFlats() {
		Log.d(TAG, "filter flats");
		new FilterFreeExposesTask().execute(mFlats);
	}

	private class FilterFreeExposesTask extends FreeFlatsTask {

		@Override
		protected void onPostExecute(Flats result) {
			if (result != null) {
				mFlats = result;
				updateMap(true);
			}
			super.onPostExecute(result);
		}
	}
}