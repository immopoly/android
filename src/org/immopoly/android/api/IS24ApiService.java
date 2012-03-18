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

package org.immopoly.android.api;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import org.immopoly.android.constants.Const;
import org.immopoly.android.helper.WebHelper;
import org.immopoly.android.model.Flats;
import org.immopoly.android.model.OAuthData;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class IS24ApiService extends IntentService {

	public static final String API_RECEIVER = "api_receiver";
	public static final String LAT = "lat";
	public static final String LNG = "lng";
	public static final String NO_FLATS = "no_flats";

	public static final int STATUS_RUNNING = 1;
	public static final int STATUS_FINISHED = 2;
	public static final int STATUS_ERROR = 3;
	
	public IS24ApiService() {
		super("");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		final ResultReceiver receiver = intent.getParcelableExtra(API_RECEIVER);
		double lat = intent.getDoubleExtra(LAT, 0.0);
		double lon = intent.getDoubleExtra(LNG, 0.0);
		Bundle b = new Bundle();
		Log.d( Const.LOG_TAG, "IS24 search started" ); 
		try {
			receiver.send(STATUS_RUNNING, Bundle.EMPTY);
			final float[] radii = Const.SEARCH_RADII;
			// search for flats in radii[i]. finish if enough flats found (1 is enough in the last run)
			for ( int i = 0; i < radii.length; i++ ) {
				int min = i == radii.length-1 ? 1 : Const.SEARCH_MIN_RESULTS; 
				Flats flats = loadFlats(lat, lon, radii[i], min, Const.SEARCH_MAX_RESULTS );
				if ( flats != null ) {
					Log.d( Const.LOG_TAG, "IS24 search finished. #flats: " + flats.size() ); 
					b.putParcelableArrayList("flats", flats);
					receiver.send(STATUS_FINISHED, b);
					this.stopSelf();
					return;
				}
			}
			// no flats found
			Log.d( Const.LOG_TAG, "IS24 search finished with no flats: " ); 
			b.putString(Intent.EXTRA_TEXT, NO_FLATS);
		} catch (Exception e) {
			Log.e( Const.LOG_TAG, "IS24 search caught Exception: ", e );
		}
		receiver.send(STATUS_ERROR, b); 
		this.stopSelf();
	}

	/**
	 * Runs an IS2 search with the given lat,lon,r
	 * Returns at most 'max' Flats or null if there are less than 'min' flats.   
	 * @param lat Latitude
	 * @param lon Longitude
	 * @param r Radius
	 * @param min minimum nuber of flats
	 * @param max maximum nuber of flats
	 * @return Flats or null
	 * @throws JSONException. MalformedURLException, NullPointerException 
	 */
	private Flats loadFlats(double lat, double lon, float r, int min, int max ) throws JSONException, MalformedURLException 
	{
		Log.d( Const.LOG_TAG, "IS24 search: Lat: " + lat + " Lon: " + lon + " R: " + r + " min: " + min + " max: " + max ); 
		// get the first result page and extract paging info
		JSONObject json = loadPage(lat, lon, r, 1 );  // IS24 page nr starts at 1
		JSONObject resultList = json.getJSONObject("resultlist.resultlist");
		JSONObject pagingInfo = resultList.getJSONObject("paging");
		int numPages = pagingInfo.getInt( "numberOfPages" );
		int results  = pagingInfo.getInt( "numberOfHits" );
		int pageSize = pagingInfo.getInt( "pageSize" );

		Log.d( Const.LOG_TAG, "IS24 search got first page, numPages: " + numPages + " results: " + results +  " pageSize: " + pageSize   ); 
		// return if there aren't enough results
		if ( results < min || numPages*pageSize < min || results <= 0 )
			return null;
		
		// parse flats from 1st result page
		Flats flats = new Flats( max );
		flats.parse( json );
		
		// calc pages to get
		int pages = max / pageSize;
		if ( pages >= 0 && max % pageSize > 0 )
			pages++;
		if ( pages > numPages ) // if this happens theres something wrong here or in the json
			pages = numPages;
		
		// evtly get more pages
		for ( int i = 2; i <= pages; i++ ) {
			json = loadPage(lat, lon, r, i);
			flats.parse( json );
			Log.d( Const.LOG_TAG, "IS24 search got page " + i + "/" + pages + " #flats: " + flats.size() ); 
		}

		// restrict number of results
		if ( flats.size() > max ) {
			Flats lessFlats = new Flats( max );
			lessFlats.addAll( flats.subList(0, max) );
			flats = lessFlats;
		}
		return flats;
	}

	/**
	 * Gets result page number 'page' from IS24 for the given lat,lon,r
	 * @param lat
	 * @param lon
	 * @param r
	 * @param page
	 * @return
	 * @throws MalformedURLException
	 * @throws JSONException
	 */
	private JSONObject loadPage( double lat, double lon, float r, int page ) throws MalformedURLException, JSONException {
		URL url = new URL( OAuthData.SERVER + OAuthData.SEARCH_PREFIX
				+ "search/radius.json?realEstateType=apartmentrent&pagenumber="
				+ page + "&geocoordinates=" + lat + ";" + lon + ";" + r );
		JSONObject obj = WebHelper.getHttpData(url, true, this);
		if (obj == null) { // does this ever happen?
			throw new JSONException( "Got (JSONObject) null for search result. Lat: " + lat + "Lon: " 
									+ lon + " R: " + r + " pageNr: " + page );
		}
		return obj;
	}
}
