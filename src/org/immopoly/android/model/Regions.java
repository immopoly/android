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

public class Regions {

	private static ArrayList<Region> data = new ArrayList<Region>();

	public static void parseJSON(JSONObject obj) {

		if (obj.has("region.regions")) {
			Region tmp;
			data.clear();
			try {
				JSONArray objRegions = obj.getJSONArray("region.regions");
				JSONArray data = objRegions.getJSONObject(0).getJSONArray(
						"region");
				for (int i = 0; i < data.length(); i++) {
					tmp = new Region();
					tmp.name = data.getJSONObject(i).optString("name");
					tmp.geoCode = data.getJSONObject(i).optInt("geoCodeId", 0);
					tmp.amount = data.getJSONObject(i).optInt("amount", 0);
					Regions.data.add(tmp);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static Region getRegion(int position) {
		return position < Regions.data.size() ? Regions.data.get(position)
				: null;
	}

	public static ArrayList<Region> getData() {
		return data;
	}

	public static int size() {
		return Regions.data.size();

	}

}
