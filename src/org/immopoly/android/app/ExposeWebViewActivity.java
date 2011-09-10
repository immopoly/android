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
import org.immopoly.android.constants.Const;
import org.immopoly.android.helper.Settings;
import org.immopoly.android.helper.TrackingManager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class ExposeWebViewActivity extends Activity {

	private String exposeID;
	private String exposeName;
	private String exposeDescription;

	private String exposeURL;
	private Boolean mLoadTwice = false;
	private WebView webView;
	private boolean owned = false;

	private GoogleAnalyticsTracker tracker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		tracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker in manual dispatch mode...
		tracker.startNewSession(TrackingManager.UA_ACCOUNT, this);
		

		setContentView(R.layout.expose_detail_web_view);
		Intent intent = getIntent();

		if (intent != null) {
			
			exposeID = intent.getExtras().getString(Const.EXPOSE_ID);
			
			tracker.setCustomVar(1, Const.SOURCE, intent.getExtras().getString(Const.SOURCE), 1);
			
			if (intent.getExtras().getBoolean(Const.EXPOSE_IN_PORTOFOLIO, false)) {
				((Button) findViewById(R.id.BackButton))
						.setText(getString(R.string.webview_back_button));
				owned = true;
			}
			exposeName = intent.getExtras().getString(Const.EXPOSE_NAME);
			exposeDescription = intent.getExtras().getString(Const.EXPOSE_DESC);
			exposeURL = intent.getExtras().getString(Const.EXPOSE_URL);
			String url = Settings.getFlatLink(exposeID, true);

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
					if (url.matches(".+?\\/bilder\\.htm$")) {
						// match image details
						tracker.trackEvent(TrackingManager.CATEGORY_CLICKS,
								TrackingManager.ACTION_VIEW_FOTOS,
								TrackingManager.LABEL_IMAGES, 0);
					}
					if (url.matches(".+?\\/bilder\\.htm#bigpicture$")) {
						// navigate in details images
						tracker.trackEvent(TrackingManager.CATEGORY_CLICKS,
								TrackingManager.ACTION_VIEW_FOTOS,
								TrackingManager.LABEL_IMAGES_DETAILS, 0);
					}
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
			SharedPreferences shared = getSharedPreferences(Const.SHARED_PREF_EXPOSE_WEBVIEW, 0);
			String visited = shared.getString(Const.KEY_VISITED, "");
			if (visited.length() == 0) {
				mLoadTwice = true;
				SharedPreferences.Editor editor = shared.edit();
				editor.putString(Const.KEY_VISITED, "true");
				editor.commit();
			}
			webView.loadUrl(url);

		} else {

			finish();
		}
		
		tracker.trackPageView(TrackingManager.VIEW_EXPOSE);
	}

	public void addCurrentExpose(View v) {
		Intent i = new Intent(this, PlacesMapActivity.class);
		i.putExtra(Const.EXPOSE_ADD_PORTIFOLIO, true);
		i.putExtra(Const.EXPOSE_ID, exposeID);
		i.putExtra(Const.EXPOSE_NAME, exposeName);
		i.putExtra(Const.EXPOSE_DESC, exposeDescription);
		i.putExtra(Const.EXPOSE_URL, exposeURL);
		if (!owned) {
			setResult(RESULT_OK, i);
			tracker.trackEvent(TrackingManager.CATEGORY_CLICKS,
					TrackingManager.ACTION_TAKE_OVER,
					TrackingManager.LABEL_REQUEST, 0);
		} else {
			setResult(RESULT_CANCELED, i);
		}
		finish();

	}

	public void shareExpose(View v) {
		Settings.shareMessage(this,
				getString(R.string.link_share_flat), exposeName,
				Settings.getFlatLink(exposeID, false));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		tracker.stopSession();
	}
}
