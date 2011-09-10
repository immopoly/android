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

package org.immopoly.android.app;

import org.immopoly.android.R;
import org.immopoly.android.model.OAuthData;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class WebViewActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);
		Intent intent = getIntent();
		if (intent != null) {
			String url = intent.getExtras().getString("oauth_url");

			WebView webView = (WebView) findViewById(R.id.webview);

			webView.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageStarted(WebView view, String url,
						Bitmap favicon) {
					super.onPageStarted(view, url, favicon);
					// check url
					if (url.matches(".+?state=authorized")) {
						SharedPreferences shared = getSharedPreferences(
								"oauth", 0);
						SharedPreferences.Editor editor = shared.edit();
						Uri uri = Uri.parse(url);
						String access_token = uri
								.getQueryParameter("oauth_token");
						editor.putString("oauth_token", access_token);
						editor.commit();
						OAuthData.accessToken = access_token;
						Intent i = new Intent(WebViewActivity.this,
								PlacesMapActivity.class);
						startActivity(i);
						finish();
					}
				}
			});

			webView.loadUrl(url);
		}
	}
}
