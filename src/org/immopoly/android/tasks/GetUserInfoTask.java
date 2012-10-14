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
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
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
			obj = WebHelper.getHttpObjectData(new URL(
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
		}
		return user;
	}

	@Override
	protected void onPostExecute(ImmopolyUser user) {
	}
}
