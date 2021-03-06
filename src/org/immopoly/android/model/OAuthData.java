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

import android.content.Context;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;

public class OAuthData {

	public static final String SERVER = "http://rest.immobilienscout24.de";
	public static final String OAUTH_SECURITY_PREFIX = "/restapi/security/oauth/";

	public static final String AUTHORIZATION_ENDPOINT = SERVER
			+ OAUTH_SECURITY_PREFIX + "confirm_access";
	public static final String ACCESS_TOKEN_ENDPOINT = SERVER
			+ OAUTH_SECURITY_PREFIX + "access_token";
	public static final String REQUEST_TOKEN_ENDPOINT = SERVER
			+ OAUTH_SECURITY_PREFIX + "request_token";

	public static String CONSUMER_KEY = ""; // e. g. a system
	// identifier
	public static String CONSUMER_SECRET = "";

	// technical and business context of the search webservice
	public static final String SEARCH_PREFIX = "/restapi/api" + "/search/v1.0/";

	public OAuthConsumer consumer = new DefaultOAuthConsumer(CONSUMER_KEY,
			CONSUMER_SECRET);

	public OAuthProvider provider = new DefaultOAuthProvider(
			REQUEST_TOKEN_ENDPOINT, ACCESS_TOKEN_ENDPOINT,
			AUTHORIZATION_ENDPOINT);

	public String accessToken = "";
	public boolean signedIn = false;

	public static String sExposeUrl = "http://mobil.immobilienscout24.de"
			+ "/expose/";
	public static String sExposeUrlWeb = "http://immobilienscout24.de"
			+ "/expose/";
	private static OAuthData mInstance = null;

	private OAuthData() {
	}

	private OAuthData(Context context) {
		/**
		 * AN ERROR HERE IS NORMAL, we don't commit these properties, just
		 * create them in you project as resources in the string.xml or create a
		 * new file for that
		 */
		CONSUMER_KEY = context
				.getString(org.immopoly.android.R.string.consumer_key);
		CONSUMER_SECRET = context
				.getString(org.immopoly.android.R.string.consumer_secret);
		accessToken = context
				.getString(org.immopoly.android.R.string.is24_access_token);
		consumer = new DefaultOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
		provider = new DefaultOAuthProvider(REQUEST_TOKEN_ENDPOINT,
				ACCESS_TOKEN_ENDPOINT, AUTHORIZATION_ENDPOINT);
	}

	public static OAuthData getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new OAuthData(context);
		}
		return mInstance;
	}
}
