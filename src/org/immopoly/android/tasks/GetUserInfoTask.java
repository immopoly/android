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

package org.immopoly.android.tasks;

import java.net.MalformedURLException;
import java.net.URL;

import org.immopoly.android.helper.WebHelper;
import org.immopoly.android.model.ImmopolyUser;
import org.immopoly.android.provider.FlatsProvider;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

public abstract class GetUserInfoTask extends
		AsyncTask<String, Void, ImmopolyUser> {

	private final Context mContext;

	public GetUserInfoTask(Context context) {
		mContext = context;
	}

	@Override
	protected ImmopolyUser doInBackground(String... params) {
		String token = params[0];
		JSONObject obj = null;
		ImmopolyUser user = null;
		try {
			obj = WebHelper.getHttpData(new URL(
					WebHelper.SERVER_URL_PREFIX + "/user/info?token=" + token),
					false, mContext);

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (obj == null || obj.has("org.immopoly.common.ImmopolyException")) {
			user = null;
		} else {
			ImmopolyUser.getInstance().fromJSON(obj);
			user = ImmopolyUser.getInstance();

			// getContentResolver().delete(
			// FlatsProvider.CONTENT_URI,
			// org.immopoly.android.provider.FlatsProvider.Flat.FLAT_ID
			// + " > 0", null);
			Cursor cur = mContext.getContentResolver().query(
					FlatsProvider.CONTENT_URI, null, null, null, null);
			if (cur.getCount() > 0) {
				boolean isIn;
				int current;
				cur.moveToFirst();
				do {
					isIn = false;
					current = -1;
					for (int i = 0; i < ImmopolyUser.getInstance().flats.size(); i++) {
						if (cur.getInt(cur
								.getColumnIndex(FlatsProvider.Flat.FLAT_ID)) == ImmopolyUser
								.getInstance().flats.get(i).uid) {
							isIn = true;
							current = i;
							break;
						}
					}
					if (isIn == false) {
						// delete
						deleteFlat(cur.getInt(cur
								.getColumnIndex(FlatsProvider.Flat.FLAT_ID)));
					} else if (current != -1) {
						ImmopolyUser.getInstance().flats.remove(current);
					}
				} while (cur.moveToNext());
			}
			for (org.immopoly.android.model.Flat f : ImmopolyUser.getInstance().flats) {
				addFlat(f);
			}
		}
		return user;
	}

	@Override
	protected void onPostExecute(ImmopolyUser user) {

	}

	private void deleteFlat(int id) {
		mContext.getContentResolver().delete(FlatsProvider.CONTENT_URI,
				FlatsProvider.Flat.FLAT_ID + "=" + id, null);
	}

	private void addFlat(org.immopoly.android.model.Flat f) {
		ContentValues values;
		values = new ContentValues();
		values.put(org.immopoly.android.provider.FlatsProvider.Flat.FLAT_ID,f.uid);
		values.put(org.immopoly.android.provider.FlatsProvider.Flat.FLAT_NAME,f.name);
		values.put(org.immopoly.android.provider.FlatsProvider.Flat.FLAT_DESCRIPTION,"-");
		values.put(org.immopoly.android.provider.FlatsProvider.Flat.FLAT_LATITUDE, f.lat);
		values.put(org.immopoly.android.provider.FlatsProvider.Flat.FLAT_LONGITUDE, f.lng);
		values.put(org.immopoly.android.provider.FlatsProvider.Flat.FLAT_CREATIONDATE, f.creationDate);
		mContext.getContentResolver().insert(FlatsProvider.CONTENT_URI, values);
	}
}
