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

package org.immopoly.android.helper;

import org.immopoly.android.R;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

public class LocationHelper {

	public interface LocationCallback {
		void onLocationChanged(boolean center);

		void failed();
	}

	// teshhold when location is too old
	// private static final int TRESH = 150000;

	public static double sLat = 52.548932;
	public static double sLng = 13.416416;
	public static float sAccuracy = Float.MAX_VALUE;;
	public static long sTime = 0;
	public static String mAddress = "";
	private static LocationListener mlocListener;
	private static LocationManager mLocationManager;
	private static Criteria mCriteria = null;

	// user can force location update
	// private static final int LOCATION_REFRESH_TRESH = 1;
	private static int sCountRequest = 0;

	public static void requestLocationSettings(final Context context) {
		// LocationHelper.getLastLocation(this);
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.allow_localization_title);
		builder.setMessage(R.string.allow_localization_message);
		builder.setCancelable(true).setNegativeButton(R.string.button_cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
					}
				});
		final Intent locationSettingsIntent = new Intent(
				android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		final ComponentName toLaunch = new ComponentName(
				"com.android.settings", "com.android.settings.SecuritySettings");

		locationSettingsIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		locationSettingsIntent.setComponent(toLaunch);
		locationSettingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		final Intent generellSettingsIntent = new Intent(
				Settings.ACTION_SETTINGS);
		builder.setPositiveButton("Einstellungen",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {

						if (context.getPackageManager().resolveActivity(
								locationSettingsIntent,
								PackageManager.MATCH_DEFAULT_ONLY) != null) {
							context.startActivity(locationSettingsIntent);
						} else if (context.getPackageManager().resolveActivity(
								generellSettingsIntent,
								PackageManager.MATCH_DEFAULT_ONLY) != null) {
							context.startActivity(generellSettingsIntent);
						} else {
							Toast.makeText(
									context,
									context.getString(R.string.allow_localization_message_manuel),
									Toast.LENGTH_LONG).show();
						}
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	// The default search radius when searching for places nearby.
	// public static int DEFAULT_RADIUS = 150;
	// The maximum distance the user should travel between location updates.
	// public static int MAX_DISTANCE = DEFAULT_RADIUS / 2;
	// The maximum time that should pass before the user gets a location update.
	// public static long MAX_TIME = AlarmManager.INTERVAL_FIFTEEN_MINUTES;

	public static void getLastLocation(final Context context,
			final LocationCallback callback) {
		// double tmpLat = sLat;
		// double tmpLng = sLng;
		sCountRequest++;

		getCriteria(context);
		if (mLocationManager == null) {
			mLocationManager = (LocationManager) context
					.getApplicationContext().getSystemService(
							Context.LOCATION_SERVICE);
		}
		// if (sTime < MAX_TIME || sAccuracy > MAX_DISTANCE || sCountRequest >
		// LOCATION_REFRESH_TRESH) {
		sCountRequest = 0;
		if (mlocListener == null) {
			String bestProvider = getBestProvider(context);
			if (bestProvider != null) {
				mlocListener = new LocationListener() {

					@Override
					public void onLocationChanged(Location loc) {
						boolean newLoc = false;
						// if (sAccuracy >= loc.getAccuracy()) {
						sLat = loc.getLatitude();

						sLng = loc.getLongitude();

						sAccuracy = loc.getAccuracy();
						sTime = loc.getTime();
						newLoc = true;
						// }
						if (callback != null) {
							callback.onLocationChanged(newLoc);
						}
						if (mlocListener != null)
							mLocationManager.removeUpdates(mlocListener);

						mlocListener = null;
					}

					@Override
					public void onProviderDisabled(String provider) {
						if (mlocListener != null) {
							mLocationManager.removeUpdates(mlocListener);
						}
						if (callback != null) {
							callback.failed();
						}
						mlocListener = null;
						requestLocationSettings(context);
					}

					@Override
					public void onProviderEnabled(String provider) {

					}

					@Override
					public void onStatusChanged(String arg0, int arg1,
							Bundle arg2) {
						if (mlocListener != null)
							mLocationManager.removeUpdates(mlocListener);
						mlocListener = null;
						requestLocationSettings(context);
					}
				};

				mLocationManager.requestLocationUpdates(bestProvider, 0, 0,
						mlocListener);
			} else {
				requestLocationSettings(context);
			}
		}

	}

	private static void getCriteria(final Context context) {
		if (mCriteria == null) {
			mCriteria = new Criteria();
			mCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
			mCriteria.setAltitudeRequired(false);
			mCriteria.setBearingRequired(false);
			mCriteria.setCostAllowed(true);
			mCriteria.setPowerRequirement(Criteria.POWER_LOW);
		}
	}

	public static void getLastLocationFromProvider(Context context) {

		Location lastLocation = null;
		try {
			getCriteria(context);
			lastLocation = mLocationManager
					.getLastKnownLocation(mLocationManager.getBestProvider(
							mCriteria, true));
			if (lastLocation != null) {
				sLat = lastLocation.getLatitude();
				sLng = lastLocation.getLongitude();
				sAccuracy = lastLocation.getAccuracy();
				sTime = lastLocation.getTime();
			}
		} catch (IllegalArgumentException e) {
			// do nothing at the moment, ideas ?
			requestLocationSettings(context);
		}

	}

	public static String getBestProvider(Context context) {
		getCriteria(context);
		if (mLocationManager == null) {
			mLocationManager = (LocationManager) context
					.getApplicationContext().getSystemService(
							Context.LOCATION_SERVICE);
		}
		return mLocationManager.getBestProvider(mCriteria, true);
	}

}
