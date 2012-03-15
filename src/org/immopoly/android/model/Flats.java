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

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Flats extends ArrayList<Flat> {

	/**
	 * Make sure when adding fields to add them in write to Parcel as well
	 */
	private static final long serialVersionUID = 4642208262846389104L;

	public Flats() {}

	public Flats( int capacity ) {
		super( capacity );
	}
	
	public void parse(JSONObject obj) {
		try {
			Flat item;
			JSONObject resultList = obj.getJSONObject("resultlist.resultlist");
			JSONArray resultEntries = resultList
					.optJSONArray("resultlistEntries");
			if (resultEntries != null && resultEntries.length() > 0) {
				JSONObject flatsObj = resultEntries.getJSONObject(0);
				JSONArray resultEntry = flatsObj.optJSONArray("resultlistEntry");
				if(resultEntry == null){
					JSONObject flatObj = flatsObj.optJSONObject("resultlistEntry");
					if ( flatObj != null) {
						item = new Flat(flatObj);
						add(item);
					}
				} else {
					if (resultEntries != null && resultEntry != null) {
						for (int i = 0; i < resultEntry.length(); i++) {
							item = new Flat(resultEntry.getJSONObject(i));
							add(item);
						}
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
