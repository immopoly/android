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

import org.immopoly.android.helper.WebHelper;
import org.immopoly.android.model.Flats;
import org.immopoly.android.model.OAuthData;
import org.immopoly.android.model.Regions;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class IS24ApiService extends IntentService {

	private static final String SEARCH_VALUE = "search";
	private static final String RADIUS = "radius";
	public static final String COMMAND = "command";
	public static final String API_RECEIVER = "api_receiver";
	public static final String LAT = "lat";
	public static final String LNG = "lng";

	public IS24ApiService() {
		super("");
	}

	public static final int CMD_REGION = 0x10;
	public static final int CMD_SEARCH = 0x11;

	public static final int STATUS_RUNNING = 1;
	public static final int STATUS_FINISHED = 2;
	public static final int STATUS_ERROR = 3;

	@Override
	protected void onHandleIntent(Intent intent) {
		final ResultReceiver receiver = intent.getParcelableExtra(API_RECEIVER);
		final int cmd = intent.getIntExtra(COMMAND, -1);
		Bundle b = new Bundle();

		receiver.send(STATUS_RUNNING, Bundle.EMPTY);
		double lat = 0.0d;
		double lng = 0.0d;
		float r = 0.0f;
		URL url;
		JSONObject obj;
		switch (cmd) {
		case CMD_REGION:
			lat = intent.getDoubleExtra(LAT, 0.0);
			lng = intent.getDoubleExtra(LNG, 0.0);
			r = intent.getFloatExtra(RADIUS, 1.0f);
			String searchValue = intent.getStringExtra(SEARCH_VALUE);
			try {
				url = new URL(OAuthData.SERVER + OAuthData.SEARCH_PREFIX
						+ "region.json?q=" + searchValue + "&geocoordinates="
						+ lat + ";" + lng + ";" + r);
				obj = WebHelper.getHttpData(url, true, this);
				Regions.parseJSON(obj);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			receiver.send(STATUS_FINISHED, b);
			break;
		case CMD_SEARCH:
			Flats flats = new Flats();

			lat = intent.getDoubleExtra(LAT, 0.0);
			lng = intent.getDoubleExtra(LNG, 0.0);
			r = intent.getFloatExtra(RADIUS, 3.0f);

			final int size = 5;
			flats = getFlats(lat, lng, r, flats, size);
			// second try with bigger radius if first 5 requests did not return enough flats
			if (flats.size() < 30) {
				r = 6.0f;
				flats.clear();
				flats = getFlats(lat, lng, r, flats, size);
			}
			if (flats.size() > 0) {
				b.putParcelableArrayList("flats", flats);
				receiver.send(STATUS_FINISHED, b);
			} else {
				b.putString(Intent.EXTRA_TEXT, "no flats");
				receiver.send(STATUS_ERROR, b);
			}
			break;

		default:
			break;
		}

		this.stopSelf();

	}

	private Flats getFlats(double lat, double lng, float r, Flats flats,
			final int size) {
		URL url;
		JSONObject obj;
		int items = 0;
		for (int i = 1; i < size; i++) {
			
			try {
				url = new URL(
						OAuthData.SERVER
								+ OAuthData.SEARCH_PREFIX
								+ "search/radius.json?realEstateType=apartmentrent&pagenumber="
								+ i + "&geocoordinates=" + lat + ";" + lng
								+ ";" + r);

				obj = WebHelper.getHttpData(url, true, this);
				Log.d("IS24ApiService", "expose api request loop :" + i);

				if (obj == null) {
					break;
				}
				flats.parse(obj);
				
				// if difference between two request is smaller then one, 
				// then there is no more data for this radius
				// means increase radius or be happy with how much we got :)
				
				/**
				 * TODO user proper paging of the api
				 * 
				 *  "paging": {
				 *     "previous": {
				 *       "@xlink.href": "http:\/\/r ..."
				 *     },
				 *     "pageNumber": 2,
				 *     "pageSize": 1,
				 *     "numberOfPages": 2,
				 *     "numberOfHits": 21
				 *   },
				 */
				if ((flats.size() - items) < 20 ) {
					break;
				}
				items = flats.size();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return flats;
	}

}
