package org.immopoly.android.tasks;

import java.net.MalformedURLException;
import java.net.URL;

import org.immopoly.android.R;
import org.immopoly.android.helper.Settings;
import org.immopoly.android.helper.TrackingManager;
import org.immopoly.android.helper.WebHelper;
import org.immopoly.android.model.Flat;
import org.immopoly.android.model.ImmopolyHistory;
import org.immopoly.android.model.ImmopolyUser;
import org.immopoly.android.provider.FlatsProvider;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class AddToPortfolioTask extends AsyncTask<Flat, Void, AddToPortfolioTask.Result> {


	private final Activity mActivity;
	private GoogleAnalyticsTracker mTracker;

	public static class Result {
		public boolean 		   success;
		public ImmopolyHistory historyEvent;
	}
	
	/**
	 * @param activity Activity to get sringns from
	 * @param tracker GoogleAnalyticsTracker to track events
	 */
	public AddToPortfolioTask(Activity activity, GoogleAnalyticsTracker tracker ) {
		this.mActivity = activity;
		this.mTracker = tracker;
	}

	@Override
	protected Result doInBackground(Flat... params) {
		Flat flat = params[0];
		JSONObject obj = null;
		ImmopolyHistory history = null;
		Result result = new Result(); 
		try {
			ImmopolyUser.getInstance().readToken(this.mActivity);
			obj = WebHelper
					.getHttpData(new URL(WebHelper.SERVER_URL_PREFIX + "/portfolio/add?token="
							+ ImmopolyUser.getInstance().getToken() + "&expose=" + flat.uid), false,
							this.mActivity);
			if (obj != null && !obj.has("org.immopoly.common.ImmopolyException")) {
				history = new ImmopolyHistory();
				history.fromJSON(obj);
				mTracker.trackEvent(TrackingManager.CATEGORY_ALERT, TrackingManager.ACTION_TOOK_EXPOSE,
						TrackingManager.LABEL_TRY, 0);
				if (history.getType() == 1) {
					addFlatToDB(flat);
					result.success = true;
				} else {
					result.success = false;
				}
			} else if (obj != null) {
				history = new ImmopolyHistory();
				switch (obj.getJSONObject("org.immopoly.common.ImmopolyException").getInt("errorCode")) {
				case 201:
					history.mText = this.mActivity.getString(R.string.flat_already_in_portifolio);
					break;
				case 301:
					history.mText = this.mActivity.getString(R.string.flat_does_not_exist_anymore);
					break;
				case 302:
					history.mText = this.mActivity.getString(R.string.flat_has_no_raw_rent);
					break;
				case 441:
					history.mText = this.mActivity.getString(R.string.expose_location_spoofing);
				default:
					history.mText = obj.getJSONObject("org.immopoly.common.ImmopolyException").getString("message");
				}
				mTracker.trackEvent(TrackingManager.CATEGORY_ALERT, TrackingManager.ACTION_TOOK_EXPOSE,
						TrackingManager.LABEL_NEGATIVE, 0);
				result.success = false;
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		result.historyEvent = history;
		return result;
	}

	@Override
	protected void onPostExecute(Result result) {
		if (result.historyEvent != null && result.historyEvent.mText != null 
				&& result.historyEvent.mText.length() > 0) 
		{
			Toast.makeText(this.mActivity, result.historyEvent.mText, Toast.LENGTH_LONG).show();
			// add history entry to users list
			ImmopolyUser.getInstance().getHistory().add(0, result.historyEvent);
		} else if (Settings.isOnline(this.mActivity)) {
			Toast.makeText(this.mActivity, R.string.expose_couldnt_add, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this.mActivity, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
		}
	}

	private void addFlatToDB( Flat f ) {
		ContentValues values;
		values = new ContentValues();
		values.put(org.immopoly.android.provider.FlatsProvider.Flat.FLAT_ID,f.uid);
		values.put(org.immopoly.android.provider.FlatsProvider.Flat.FLAT_NAME,f.name);
		values.put(org.immopoly.android.provider.FlatsProvider.Flat.FLAT_DESCRIPTION,"-");
		values.put(org.immopoly.android.provider.FlatsProvider.Flat.FLAT_LATITUDE, f.lat);
		values.put(org.immopoly.android.provider.FlatsProvider.Flat.FLAT_LONGITUDE, f.lng);
		values.put(org.immopoly.android.provider.FlatsProvider.Flat.FLAT_CREATIONDATE, f.creationDate);
		mActivity.getContentResolver().insert(FlatsProvider.CONTENT_URI, values);
	}
	
}