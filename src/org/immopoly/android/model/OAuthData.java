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

	public static final String CONSUMER_KEY = ""; // e. g. a system
	// identifier
	public static final String CONSUMER_SECRET = "";

	// technical and business context of the search webservice
	public static final String SEARCH_PREFIX = "/restapi/api" + "/search/v1.0/";

	public static final OAuthConsumer consumer = new DefaultOAuthConsumer(
			CONSUMER_KEY, CONSUMER_SECRET);

	public static final OAuthProvider provider = new DefaultOAuthProvider(
			REQUEST_TOKEN_ENDPOINT, ACCESS_TOKEN_ENDPOINT,
			AUTHORIZATION_ENDPOINT);

	// public static String accessToken =
	// "3cf72426-1154-47d1-b602-0c4bf4734655";
	public static String accessToken = "";
	public static boolean signedIn = false;

	public static String sExposeUrl = "http://mobil.immobilienscout24.de"
			+ "/expose/";
	public static String sExposeUrlWeb = "http://immobilienscout24.de"
		+ "/expose/";
}
