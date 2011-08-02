package org.immopoly.android.helper;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.immopoly.android.model.OAuthData;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;

public class WebHelper {

	public static final int SOCKET_TIMEOUT = 30000;

	public static JSONObject getHttpsData(URL url, boolean signed,
			Context context) throws JSONException {
		JSONObject obj = null;
		if (Settings.isOnline(context)) {
			HttpsURLConnection request;
			try {
				request = (HttpsURLConnection) url.openConnection();

				if (signed)
					OAuthData.consumer.sign(request);

				request.setConnectTimeout(SOCKET_TIMEOUT);
				request.connect();

				InputStream in = new BufferedInputStream(request
						.getInputStream());
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

	public static JSONObject getHttpData(URL url, boolean signed,
			Context context) throws JSONException {
		JSONObject obj = null;
		if (Settings.isOnline(context)) {
			HttpURLConnection request;
			try {

				request = (HttpURLConnection) url.openConnection();

				request.addRequestProperty("Accept-Encoding", "gzip");
				if (signed)
					OAuthData.consumer.sign(request);
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

	public static String readInputStream(InputStream in) throws IOException {
		StringBuffer stream = new StringBuffer();
		byte[] b = new byte[4096];
		for (int n; (n = in.read(b)) != -1;) {
			stream.append(new String(b, 0, n));
		}
		return stream.toString();
	}
}
