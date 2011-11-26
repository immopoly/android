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
import java.util.ArrayList;

import org.immopoly.android.app.UserDataManager;
import org.immopoly.android.helper.WebHelper;
import org.immopoly.android.model.Flat;
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
			// fill user objects from server's UserInfo data
			ImmopolyUser.getInstance().fromJSON(obj);
			user = ImmopolyUser.getInstance();

			// synchronize local flats DB
			ArrayList<Flat>    toBeAdded   = new ArrayList<Flat>( user.flats );
			ArrayList<Integer> toBeDeleted = new ArrayList<Integer>();
			Cursor cur = mContext.getContentResolver().query( 
					FlatsProvider.CONTENT_URI, null, null, null, null);
			if (cur.getCount() > 0) {
				boolean isIn;
				cur.moveToFirst();
				do {
					isIn = false;
					int id = cur.getInt(cur.getColumnIndex(FlatsProvider.FLAT_ID));
					for (int i = 0; i < user.flats.size(); i++) {
						if (user.flats.get(i).uid == id) {
							isIn = true;
							toBeAdded.remove( user.flats.get(i) );
							break;
						}
					}
					if ( ! isIn ) {
						toBeDeleted.add( id );
					}
				} while (cur.moveToNext());
			}
			for ( Integer id : toBeDeleted ) {
				deleteFlat( id );
			}
			for ( Flat f : toBeAdded ) {
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
				FlatsProvider.FLAT_ID + "=" + id, null);
	}

	private void addFlat(Flat f) {
		ContentValues values;
		values = new ContentValues();
		values.put(FlatsProvider.FLAT_ID, f.uid);
		values.put(FlatsProvider.FLAT_NAME, f.name);
		values.put(FlatsProvider.FLAT_DESCRIPTION, "-");
		values.put(FlatsProvider.FLAT_LATITUDE, f.lat);
		values.put(FlatsProvider.FLAT_LONGITUDE, f.lng);
		values.put(FlatsProvider.FLAT_CREATIONDATE, f.creationDate);
		mContext.getContentResolver().insert(FlatsProvider.CONTENT_URI, values);
	}
}
