package org.immopoly.android.app;

import java.net.MalformedURLException;
import java.net.URL;

import org.immopoly.android.R;
import org.immopoly.android.constants.Const;
import org.immopoly.android.fragments.callbacks.HudCallbacks;
import org.immopoly.android.helper.HudPopupHelper;
import org.immopoly.android.helper.LocationHelper;
import org.immopoly.android.helper.Settings;
import org.immopoly.android.helper.TrackingManager;
import org.immopoly.android.helper.WebHelper;
import org.immopoly.android.model.ImmopolyHistory;
import org.immopoly.android.model.ImmopolyUser;
import org.immopoly.android.provider.UserProvider;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.CursorLoader;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class MainActivity extends FragmentActivity implements HudCallbacks {

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null && intent.getExtras() != null
					&& intent.getExtras().containsKey(Const.EXPOSE_ID)) {
				String exposeID = intent.getExtras().getString(Const.EXPOSE_ID);
				new AddToPortifolioTask().execute(exposeID);
			}
			Fragment f = getSupportFragmentManager().findFragmentById(
					R.id.expose_fragment);
			if (f != null) {
				getSupportFragmentManager().beginTransaction().hide(f).commit();
			}
		}

	};
	private HudPopupHelper mHudPopup;
	private GoogleAnalyticsTracker tracker;

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		
	}

	@Override
	public void updateHud(Intent data, int element) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (mHudPopup != null) {
			mHudPopup.dismiss();
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {

		super.onPostCreate(savedInstanceState);
		mHudPopup = new HudPopupHelper(this, HudPopupHelper.TYPE_FINANCE_POPUP);
		// new InfoUpdateTask(PlacesMapActivity.this)
		// .execute(ImmopolyUser.getInstance().readToken(this));
		mHudPopup = new HudPopupHelper(this, HudPopupHelper.TYPE_FINANCE_POPUP);
		CursorLoader cursorLoader = new CursorLoader(this,
				UserProvider.CONTENT_URI_USER, null, null, null, null);
		cursorLoader.loadInBackground();
		tracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker in manual dispatch mode...
		tracker.startNewSession(TrackingManager.UA_ACCOUNT,
				Const.ANALYTICS_INTERVAL,
				MainActivity.this.getApplicationContext());
		IntentFilter filter = new IntentFilter();
		filter.addAction("add_expose");
		registerReceiver(mReceiver, filter);
	}

	@Override
	public void onHudAction(View view) {
		switch (view.getId()) {
		case R.id.hud_map:
			// hud map
			// already there
			LocationHelper.getLastLocation(this);
			break;
		case R.id.hud_portfolio:
			startActivity(new Intent(this, PortfolioActivity.class));
			break;
		case R.id.hud_profile:
			startActivity(new Intent(this, DashboardActivity.class));
			break;
		case R.id.hud_text:
			// Toast.makeText(this, ImmopolyUser.getInstance().flats.toString(),
			// Toast.LENGTH_LONG);
			if (mHudPopup != null) {
				mHudPopup.show(findViewById(R.id.hud_text), -200, -60);
			}
			break;
		default:
			break;
		}
	}

	class AddToPortifolioTask extends AsyncTask<String, Void, ImmopolyHistory> {

		@Override
		protected ImmopolyHistory doInBackground(String... params) {
			JSONObject obj = null;
			ImmopolyHistory history = null;
			try {
				ImmopolyUser.getInstance().readToken(MainActivity.this);
				obj = WebHelper.getHttpData(new URL(WebHelper.SERVER_URL_PREFIX
						+ "/portfolio/add?token="
						+ ImmopolyUser.getInstance().getToken() + "&expose="
						+ params[0]), false, MainActivity.this);
				if (obj != null
						&& !obj.has("org.immopoly.common.ImmopolyException")) {
					history = new ImmopolyHistory();
					history.fromJSON(obj);
					tracker.trackEvent(TrackingManager.CATEGORY_ALERT,
							TrackingManager.ACTION_EXPOSE,
							TrackingManager.LABEL_TRY, 0);
				} else if (obj != null) {
					history = new ImmopolyHistory();
					switch (obj.getJSONObject(
							"org.immopoly.common.ImmopolyException").getInt(
							"errorCode")) {
					case 201:
						history.mText = getString(R.string.flat_already_in_portifolio);
						break;
					case 301:
						history.mText = getString(R.string.flat_does_not_exist_anymore);

						break;
					case 302:
						history.mText = getString(R.string.flat_has_no_raw_rent);
						break;
					case 441:
						history.mText = getString(R.string.expose_location_spoofing);
					}
					tracker.trackEvent(TrackingManager.CATEGORY_ALERT,
							TrackingManager.ACTION_EXPOSE,
							TrackingManager.LABEL_NEGATIVE, 0);
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return history;
		}

		@Override
		protected void onPostExecute(ImmopolyHistory result) {
			final ImmopolyHistory res = result;
			if (result != null && result.mText != null
					&& result.mText.length() > 0) {

				Toast.makeText(MainActivity.this, res.mText, Toast.LENGTH_LONG)
						.show();
				// new GetUserInfoUpdateTask(PlacesMapActivity.this)
				// .execute(ImmopolyUser.getInstance().getToken());
			} else if (Settings.isOnline(MainActivity.this)) {
				Toast.makeText(MainActivity.this, R.string.expose_couldnt_add,
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(MainActivity.this,
						R.string.no_internet_connection, Toast.LENGTH_LONG)
						.show();
			}
			super.onPostExecute(result);
		}

	}
}
