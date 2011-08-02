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

package org.immopoly.android;

import org.immopoly.android.helper.Settings;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;


public class ExposeWebViewActivity extends Activity {

	private String exposeID;
	private String exposeName;
	private String exposeDescription;
	
	private String exposeURL;
	private Boolean mLoadTwice = false;
	private WebView webView;
	private boolean owned = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.expose_detail_web_view);
		Intent intent = getIntent();

		if (intent != null) {
			exposeID = intent.getExtras().getString("exposeID");
			if (intent.getExtras().getBoolean("exposeInPortfolio", false)) {
				((Button) findViewById(R.id.BackButton))
						.setText(getString(R.string.webview_back_button));
				owned = true;
			}
			exposeName = intent.getExtras().getString("exposeName");
			exposeDescription = intent.getExtras().getString(
					"exposeDescription");
			exposeURL = intent.getExtras().getString("exposeURL");
			String url = Settings.getFlatLink(exposeID,true);

			webView = (WebView) findViewById(R.id.exposeWevView);

			webView.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageStarted(WebView view, String url,
						Bitmap favicon) {
					super.onPageStarted(view, url, favicon);
					// check url
				}

				@Override
				public void onPageFinished(WebView view, String url) {
					super.onPageFinished(view, url);
					if (mLoadTwice) {
						webView.loadUrl(url);
						mLoadTwice = false;
					} else {
						findViewById(R.id.progress).setVisibility(View.GONE);
					}

				}

				@Override
				public void onLoadResource(WebView view, String url) {
					super.onLoadResource(view, url);
					findViewById(R.id.progress).setVisibility(View.VISIBLE);
				}

			});
			SharedPreferences shared = getSharedPreferences("exposeWebView", 0);
			String visited = shared.getString("visited", "");
			if (visited.length() == 0) {
				mLoadTwice = true;
				SharedPreferences.Editor editor = shared.edit();
				editor.putString("visited", "true");
				editor.commit();
			}
			webView.loadUrl(url);

		} else {

			finish();
		}
	}

	public void addCurrentExpose(View v) {
		Intent i = new Intent(this, PlacesMap.class);
		i.putExtra("addToPortifolio", true);
		i.putExtra("exposeID", exposeID);
		i.putExtra("exposeName", exposeName);
		i.putExtra("exposeDescription", exposeDescription);
		i.putExtra("exposeURL", exposeURL);
		if (!owned) {
			setResult(RESULT_OK, i);
		} else {
			setResult(RESULT_CANCELED, i);
		}
		finish();

	}

	public void shareExpose(View v){
		Settings.shareMessage(this, "Schau mal habe ich bei @immopoly gefunden", exposeName, Settings.getFlatLink(exposeID,false));
	}
}
