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

import java.net.HttpURLConnection;
import java.net.URL;

import org.immopoly.android.constants.Const;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public abstract class CheckNewsTask extends AsyncTask<Void, Void, String> 
{
	private static final String BASE_URL = Const.IMMOPOLY_WEBSITE + "/frameless-news.html?lastvisit=";
	
	private final Context mContext;

	public CheckNewsTask(Context context) {
		mContext = context.getApplicationContext();
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
			int lastVisit = prefs.getInt( "NEWEST_NEWS_SEEN", 0 );
			// DBG:	prefs.edit().putInt( "NEWEST_NEWS_SEEN", 0 ).commit();
			
			Log.d( Const.LOG_TAG, "Checking for news. URL: " + BASE_URL + lastVisit );
			URL url = new URL( BASE_URL + lastVisit );
			HttpURLConnection request = (HttpURLConnection) url.openConnection();
			request.setInstanceFollowRedirects(false);
			
			int httpStatus = request.getResponseCode();
			
			if ( httpStatus == 304 ) {
				Log.d( Const.LOG_TAG, "No new news." );
				return null;
			}
			
			if ( httpStatus != 303 ) {
				Log.w( Const.LOG_TAG, "Unhandled HTTP response code: " + httpStatus + " Resonse Msg: " + request.getResponseMessage() );
				return null;
			}
				
			String redirectLocation = request.getHeaderField( "Location" );
			Log.d( Const.LOG_TAG, "Got redirect location: " + redirectLocation );
			int newestStart = redirectLocation.indexOf( "newest=" ) + "newest=".length();
			int newestEnd   = redirectLocation.indexOf( "&", newestStart ); // just in case
			if ( newestEnd == -1 )
				newestEnd = redirectLocation.length();

			String newestStr = redirectLocation.substring( newestStart, newestEnd );
			
			int newest = Integer.parseInt( newestStr );
			prefs.edit().putInt( "NEWEST_NEWS_SEEN", newest ).commit();
			
			return Const.IMMOPOLY_WEBSITE + redirectLocation;
		} catch ( Exception e ) {
			Log.e( Const.LOG_TAG, "Error checking for news: ", e );
			return null;
		}
	}
}
