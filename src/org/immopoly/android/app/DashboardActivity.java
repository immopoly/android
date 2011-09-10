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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import oauth.signpost.OAuth;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import org.immopoly.android.R;
import org.immopoly.android.adapter.FlatAdapter;
import org.immopoly.android.adapter.FlatsCursorAdapter;
import org.immopoly.android.adapter.HistoryAdapter;
import org.immopoly.android.adapter.QypePlacesAdapter;
import org.immopoly.android.api.ApiResultReciever.Receiver;
import org.immopoly.android.api.IS24ApiService;
import org.immopoly.android.api.ReceiverState;
import org.immopoly.android.helper.LocationHelper;
import org.immopoly.android.helper.MapLocationCallback;
import org.immopoly.android.helper.Settings;
import org.immopoly.android.helper.TrackingManager;
import org.immopoly.android.helper.WebHelper;
import org.immopoly.android.model.Flats;
import org.immopoly.android.model.ImmopolyUser;
import org.immopoly.android.model.OAuthData;
import org.immopoly.android.model.QypePlace;
import org.immopoly.android.provider.FlatsProvider;
import org.immopoly.android.provider.FlatsProvider.Flat;
import org.immopoly.android.tasks.GetUserInfoTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class DashboardActivity extends BaseListActivity implements Receiver,
		MapLocationCallback {

	private static final String HTTP_API_QYPE_COM_V1 = "http://api.qype.com/v1/";
	FlatAdapter adapter;
	ReceiverState mState;
	double lat = 52.548932;
	double lng = 13.416416;
	float radius = 100.0f;
	private AutoCompleteTextView locationView;
	private Flats mFlats;
	private int mNumberGeoCodeTry;
	private Button mRefreshButton;
	private AsyncTask<String, Void, ImmopolyUser> mGetUserInfoTask;

	private GoogleAnalyticsTracker tracker;

	@Override
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		tracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker in manual dispatch mode...
		tracker.startNewSession(TrackingManager.UA_ACCOUNT, Const.ANALYTICS_INTERVAL , getApplicationContext());
		

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dashboard);
		LocationHelper.callback = this;
		mState = (ReceiverState) getLastNonConfigurationInstance();
		mRefreshButton = (Button) findViewById(R.id.location_refresh);
		if (mState != null) {
			// Start listening for Service updates again
			mState.mReceiver.setReceiver(this);
		} else {
			mState = new ReceiverState();
			mState.mReceiver.setReceiver(this);
		}
		updateLocation();
		/*
		 * if (!OAuthData.signedIn) { signIn(); }
		 */

		Uri myUri = FlatsProvider.CONTENT_URI;
		// ContentResolver content = getContentResolver();
		// Cursor cursor = content.query(RegionProvider.CONTENT_URI,
		// RegionProvider.CURSOR_COLUMS, null, null, null);
		// Cursor regions = managedQuery(RegionProvider.CONTENT_URI, null, null,
		// null, null);
		// SimpleCursorAdapter adapter1 = new SimpleCursorAdapter(this,
		// android.R.layout.simple_dropdown_item_1line, cursor,
		// new String[] { SearchManager.SUGGEST_COLUMN_TEXT_1 },
		// new int[] { android.R.id.text1 });
		// adapter1.setFilterQueryProvider(new FilterQueryProvider() {
		//
		// @Override
		// public Cursor runQuery(CharSequence constraint) {
		// String[] selection = new String[1];
		// selection[0] = constraint.toString();
		// return getContentResolver().query(RegionProvider.CONTENT_URI,
		// RegionProvider.CURSOR_COLUMS, null, selection, null);
		// }
		// });
		// adapter1.setCursorToStringConverter(new CursorToStringConverter() {
		//
		// @Override
		// public CharSequence convertToString(Cursor cursor) {
		//
		// return cursor.getString(1);
		// }
		// });

		// locationView = (AutoCompleteTextView)
		// findViewById(R.id.regionsAutoComplete);

		// locationView.setThreshold(0);
		// locationView.setAdapter(adapter1);
		// final EditText eText = (EditText)findViewById(R.id.search);
		// setCityIfThere(getIntent());
		// Then query for this specific record:
		// THE DESIRED COLUMNS TO BE BOUND
		String[] columns = new String[] { Flat.FLAT_NAME };
		// THE XML DEFINED VIEWS WHICH THE DATA WILL BE BOUND TO
		int[] to = new int[] { R.id.name };

		Cursor cur = managedQuery(myUri, null, null, null, null);
		setListAdapter(new FlatsCursorAdapter(this, R.layout.flat_list_item,
				cur, columns, to));
		getListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (arg3 != -1) {
					Intent i = new Intent(DashboardActivity.this,
							ExposeWebViewActivity.class);
					i.putExtra("exposeID", String.valueOf(arg3));
					i.putExtra("exposeInPortfolio", true);
					startActivity(i);
				}
				/*
				 * startActivity(new Intent(DashboardActivity.this,
				 * FlatDetailActivity.class));
				 */

			}

		});
	}


	@Override
	protected void onResume() {
		ImmopolyUser.getInstance().readToken(DashboardActivity.this);
		if (ImmopolyUser.getInstance().getToken() != null) {
			initDashboard();
		}
		super.onResume();
	}

	public void getPlaces(View v) {
		if (locationView != null) {
			String location = locationView.getText().toString();
			StringBuilder sb = new StringBuilder(HTTP_API_QYPE_COM_V1);
			sb.append("places.json?")
					.append("consumer_key=PXAwcYL4ZOYVWYZ61GOZrw").append("&")
					.append("in=").append(Uri.encode(location))
					.append("&category=883");
			try {
				JSONObject data = WebHelper.getHttpData(new URL(sb.toString()),
						true, DashboardActivity.this);

				JSONArray results = data.getJSONArray("results");
				final int size = results.length();
				QypePlace qPlace;
				JSONObject place;

				ArrayList<QypePlace> arrayListPlaces = new ArrayList<QypePlace>();
				for (int i = 0; i < size; i++) {
					place = results.getJSONObject(i).getJSONObject("place");
					qPlace = new QypePlace();
					qPlace.title = place.getString("title");
					qPlace.rating = place.getDouble("average_rating");
					arrayListPlaces.add(qPlace);
				}
				QypePlacesAdapter a = new QypePlacesAdapter(this,
						arrayListPlaces);
				getListView().invalidate();
				setListAdapter(a);

				Toast.makeText(this, "DATA QYPE PLACES" + data.length(),
						Toast.LENGTH_SHORT).show();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void setCityIfThere(Intent intent) {
		if (intent.getExtras() != null) {
			String city = "";
			if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
				city = intent
						.getStringExtra(SearchManager.SUGGEST_COLUMN_TEXT_1);
				city = intent
						.getStringExtra(SearchManager.SUGGEST_COLUMN_INTENT_DATA);
			} else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
				city = intent.getDataString();
			}
			if (locationView != null) {
				locationView.setText(city);
			}
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setCityIfThere(intent);
		super.onNewIntent(intent);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		// Clear reference to receiver
		// we will re-attach in onCreate
		mState.mReceiver.clearReceiver();
		return mState;
	}

	@Override
	protected void onStart() {
		super.onStart();
		LocationHelper.callback = this;
		tracker.trackPageView(TrackingManager.VIEW_DASHBOARD);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mGetUserInfoTask != null) {
			mGetUserInfoTask.cancel(true);
		}
	}

	public void updateList() {
		if (mFlats != null) {
			adapter = new FlatAdapter(this, mFlats);
			setListAdapter(adapter);
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {
		switch (resultCode) {
		case IS24ApiService.STATUS_RUNNING:
			// show progress
			// Toast.makeText(this, "Running", Toast.LENGTH_SHORT).show();
			break;
		case IS24ApiService.STATUS_FINISHED:
			ArrayList<org.immopoly.android.model.Flat> a = resultData
					.getParcelableArrayList("flats");
			mFlats = (Flats) a;
			updateList();
			// do something interesting
			// hide progress
			break;
		case IS24ApiService.STATUS_ERROR:
			// Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
			// handle the error;
			break;
		}
	}

	public void signIn() {
		String authUrl = "";
		SharedPreferences shared = getSharedPreferences("oauth", 0);
		String accessToken = shared.getString("oauth_token", "");
		if (accessToken.length() > 0) {
			OAuthData.getInstance(this.getBaseContext()).signedIn = true;
			OAuthData.getInstance(this.getBaseContext()).accessToken = accessToken;

		} else {
			OAuthData.getInstance(this.getBaseContext()).signedIn = false;
			try {
				authUrl = OAuthData.getInstance(this.getBaseContext()).provider.retrieveRequestToken(
						OAuthData.getInstance(this.getBaseContext()).consumer, OAuth.OUT_OF_BAND);
				Log.d("OAUTH", authUrl);
			} catch (OAuthMessageSignerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OAuthNotAuthorizedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OAuthExpectationFailedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OAuthCommunicationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Login in web view
			Intent i = new Intent(this, WebViewActivity.class);
			i.putExtra("oauth_url", authUrl);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
		}

	}

	public void handleMapClick(View v) {
		finish();
	}

	public void headerClick(View v) {
		switch (v.getId()) {
		case R.id.location_refresh:
			updateLocation();
			break;
		case R.id.header_logo:
			finish();
			break;
		}

	}

	private void updateLocation() {
		LocationHelper.getLastLocation(this);
		mRefreshButton.startAnimation(AnimationUtils.loadAnimation(this,
				R.anim.locating_animation));
		if (mGetUserInfoTask != null
				&& mGetUserInfoTask.getStatus().equals(Status.RUNNING)) {

		} else if (ImmopolyUser.getInstance().isOld()) {
			mGetUserInfoTask = new GetUserInfoUpdateTask(this)
					.execute(ImmopolyUser.getInstance().getToken());
		}
	}

	private class GeoCodeLocationTask extends AsyncTask<Double, Void, String> {

		@Override
		protected String doInBackground(Double... params) {
			String result = null;
			if ((params[0] != null && params[1] != null)
					&& (params[0] != 0.0 || params[1] != 0.0)) {
				try {
					Geocoder geocoder = new Geocoder(DashboardActivity.this,
							Locale.getDefault());
					List<Address> addresses = geocoder.getFromLocation(
							params[0], params[1], 1);
					if (addresses != null && addresses.size() > 0) {
						Address address = addresses.get(0);
						// sending back first mAddress line and locality
						result = address.getAddressLine(0) + ", "
								+ address.getLocality();
						// ((TextView)findViewById(R.id.header_location)).setText(result);
						LocationHelper.mAddress = result;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				LocationHelper.mAddress = null;
				result = null;
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			setAddress(result);
		}
	}

	private void setAddress(String address) {
		if (address != null && address.length() > 0) {
			mNumberGeoCodeTry = 0;
			((TextView) findViewById(R.id.header_location)).setText(address);
		} else if (LocationHelper.sAccuracy >= 0) {
			mNumberGeoCodeTry++;
			if (mNumberGeoCodeTry < 3) {
				new GeoCodeLocationTask().execute(LocationHelper.sLat,
						LocationHelper.sLng);
			} else {
				NumberFormat nFormat = NumberFormat.getInstance(Locale.GERMANY);
				nFormat.setMinimumIntegerDigits(2);
				nFormat.setMaximumFractionDigits(2);
				((TextView) findViewById(R.id.header_location)).setText("lat:"
						+ nFormat.format(LocationHelper.sLat) + " - lng:"
						+ nFormat.format(LocationHelper.sLng) + " ~"
						+ nFormat.format(LocationHelper.sAccuracy));
			}
		} else {
			((TextView) findViewById(R.id.header_location))
					.setText("<Keine Lokation>");
		}
	}

	private class GetUserInfoUpdateTask extends GetUserInfoTask {

		public GetUserInfoUpdateTask(Context context) {
			super(context);
		}

		@Override
		protected void onPostExecute(ImmopolyUser result) {
			if (result != null) {
				initDashboard();
			} else if (Settings.isOnline(DashboardActivity.this)) {
				Intent intent = new Intent(DashboardActivity.this,
						UserSignupActivity.class);

				startActivity(intent);
			} else {
				Toast.makeText(DashboardActivity.this,
						R.string.no_internet_connection, Toast.LENGTH_LONG)
						.show();
			}
		}
	}

	private void initDashboard() {
		NumberFormat nFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY);
		nFormat.setMinimumIntegerDigits(1);
		nFormat.setMaximumFractionDigits(2);
		((TextView) findViewById(R.id.user_cash)).setText(nFormat
				.format(ImmopolyUser.getInstance().getBalance()));
		((TextView) findViewById(R.id.last_rent)).setText(nFormat
				.format(ImmopolyUser.getInstance().getLastRent()));
		((TextView) findViewById(R.id.last_provision)).setText(nFormat
				.format(ImmopolyUser.getInstance().getLastProvision()));
	}

	public void showHistory(View v) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.history_title);
		builder.setAdapter(new HistoryAdapter(this), new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

			}
		});
		builder.setCancelable(false).setPositiveButton(R.string.button_ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {

					}
				});
		if (ImmopolyUser.getInstance().mUserHistory.size() == 0) {
			builder.setTitle(R.string.no_history_entries);
		}
		AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	public void updateMapData(boolean newLoc) {
		mRefreshButton.setAnimation(null);
		if (newLoc || LocationHelper.mAddress == null) {
			new GeoCodeLocationTask().execute(LocationHelper.sLat,
					LocationHelper.sLng);
		} else {
			setAddress(LocationHelper.mAddress);
		}
	}

	public void showInfo(View v) {

		LayoutInflater inflater = LayoutInflater.from(DashboardActivity.this);

		View alertDialogView = inflater.inflate(R.layout.info_webview, null);

		WebView myWebView = (WebView) alertDialogView
				.findViewById(R.id.DialogWebView);
		myWebView.setWebViewClient(new WebViewClient());
		myWebView.getSettings().setSupportZoom(true);
		myWebView.getSettings().setUseWideViewPort(true);

		myWebView.loadUrl(WebHelper.SERVER_URL_PREFIX);
		AlertDialog.Builder builder = new AlertDialog.Builder(
				DashboardActivity.this);
		builder.setView(alertDialogView);
		builder.setPositiveButton(R.string.button_ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).show();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		tracker.stopSession();
	}
}
