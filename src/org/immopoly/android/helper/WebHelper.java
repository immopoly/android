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

package org.immopoly.android.helper;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.immopoly.android.model.ImmopolyException;
import org.immopoly.android.model.OAuthData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;

public class WebHelper {

// appengine on immopoly.appspot.com
	public static final String SERVER_URL_PREFIX = "http://immopoly.appspot.com";
	public static final String SERVER_HTTPS_URL_PREFIX = "https://immopoly.appspot.com";
// appengine on emulator host (http only)
//	public static final String SERVER_URL_PREFIX       = "http://10.0.2.2:8888";
//	public static final String SERVER_HTTPS_URL_PREFIX = "http://10.0.2.2:8888";
	
	public static final int SOCKET_TIMEOUT = 30000;

		
	

	
	public static JSONObject getHttpsData(URL url, boolean signed,
			Context context) throws ImmopolyException {
		JSONObject obj = null;
		if (Settings.isOnline(context)) {
			HttpURLConnection request;
			try {
				request = (HttpURLConnection) url.openConnection();

				if (signed)
					OAuthData.getInstance(context).consumer.sign(request);

				request.setConnectTimeout(SOCKET_TIMEOUT);
				request.connect();

				InputStream in = new BufferedInputStream(request
						.getInputStream());
				String s = readInputStream(in);
				JSONTokener tokener = new JSONTokener(s);
				obj = new JSONObject(tokener);

			} catch (JSONException e) {
				throw new ImmopolyException("Kommunikationsproblem (beim lesen der Antwort)",e);
			} catch (MalformedURLException e) {
				throw new ImmopolyException("Kommunikationsproblem (fehlerhafte URL)",e);
			} catch (OAuthMessageSignerException e) {
				throw new ImmopolyException("Kommunikationsproblem (Signierung)",e);
			} catch (OAuthExpectationFailedException e) {
				throw new ImmopolyException("Kommunikationsproblem (Sicherherit)",e);
			} catch (OAuthCommunicationException e) {
				throw new ImmopolyException("Kommunikationsproblem (Sicherherit)",e);
			} catch (IOException e) {
				throw new ImmopolyException("Kommunikationsproblem",e);
			}
		}
		return obj;
	}

	public static JSONObject getHttpData(URL url, boolean signed,
			Context context) throws JSONException {
		JSONObject obj = null;
		if (Settings.isOnline(context)) {
			HttpURLConnection request;
			try {

				request = (HttpURLConnection) url.openConnection();

				request.addRequestProperty("Accept-Encoding", "gzip");
				if (signed)
					OAuthData.getInstance(context).consumer.sign(request);
				request.setConnectTimeout(SOCKET_TIMEOUT);

				request.connect();
				String encoding = request.getContentEncoding();

				InputStream in;
				if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
					in = new GZIPInputStream(request.getInputStream());
				} else {
					in = new BufferedInputStream(request.getInputStream());
				}
				String s = readInputStream(in);
				JSONTokener tokener = new JSONTokener(s);
				obj = new JSONObject(tokener);

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OAuthMessageSignerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OAuthExpectationFailedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OAuthCommunicationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return obj;
	}
	
	private static HttpResponse postHttp(String url, JSONObject jsonObject)
			throws ClientProtocolException, IOException {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);

		httppost.setHeader("User-Agent", "immopoly android client");
		httppost.setHeader("Accept-Encoding", "gzip");
		
		HttpEntity entity;
		entity = new StringEntity(jsonObject.toString());
		// sets the post request as the resulting string
		httppost.setEntity(entity);
		// Pass local context as a parameter
		return httpclient.execute(httppost);
	}
	
	public static JSONArray postFlatIdsHttpData(String url, JSONObject jsonObject) throws JSONException {
		try {
			InputStream in;
			HttpResponse response = postHttp(url, jsonObject);
			Header contentEncoding = response.getFirstHeader("Content-Encoding");
			if (response != null && contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
				in = new GZIPInputStream(response.getEntity().getContent());
			} else {
				in = new BufferedInputStream(response.getEntity().getContent());
			}
			String s = readInputStream(in);
			return new JSONArray(s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public static String readInputStream(InputStream in) throws IOException {
		StringBuffer stream = new StringBuffer();
		byte[] b = new byte[4096];
		for (int n; (n = in.read(b)) != -1;) {
			stream.append(new String(b, 0, n));
		}
		return stream.toString();
	}
}
