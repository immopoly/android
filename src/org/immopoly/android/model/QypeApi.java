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

package org.immopoly.android.model;

import java.net.MalformedURLException;
import java.net.URL;

import org.immopoly.android.helper.WebHelper;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

public class QypeApi {

	private static final String CONSUMER_KEY = "PXAwcYL4ZOYVWYZ61GOZrw";
	private static final String CONSUMER_KEY_PAIR = "consumer_key="
			+ CONSUMER_KEY;
	private static final String domain = "URLhttp://api.qype.com/v1/";

	private static final String places = domain + "places?" + CONSUMER_KEY_PAIR;

	// http://api.qype.com/v1/places?&consumer_key=PXAwcYL4ZOYVWYZ61GOZrw

	public static JSONObject getPlacesInCity(String search, String city,
			Context context) {
		if (search.length() > 0 && city.length() > 0) {
			try {
				return WebHelper.getHttpData(new URL(places + "&city=" + city
						+ "&search=" + search), false, context);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
}
