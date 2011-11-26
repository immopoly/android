package org.immopoly.android.tasks;

import java.net.MalformedURLException;
import java.net.URL;

import org.immopoly.android.R;
import org.immopoly.android.constants.Const;
import org.immopoly.android.helper.Settings;
import org.immopoly.android.helper.TrackingManager;
import org.immopoly.android.helper.WebHelper;
import org.immopoly.android.model.ImmopolyHistory;
import org.immopoly.android.model.ImmopolyUser;
import org.immopoly.android.provider.FlatsProvider;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class ReleaseFromPortfolioTask extends AsyncTask<String, Void, ReleaseFromPortfolioTask.Result> {

	private final Activity mActivity;
	private GoogleAnalyticsTracker mTracker;

	public static class Result {
		public boolean 		   success;
		public ImmopolyHistory historyEvent;
	}
	
	/**
	 * @param activity
	 */
	public ReleaseFromPortfolioTask(Activity activity, GoogleAnalyticsTracker tracker ) {
		this.mActivity = activity;
		this.mTracker = tracker;
	}

	@Override
	protected Result doInBackground(String... params) {
		JSONObject obj = null;
		ImmopolyHistory history = null;
		Result result = new Result();
		try {
			ImmopolyUser.getInstance().readToken(mActivity);
			obj = WebHelper.getHttpData(new URL(WebHelper.SERVER_URL_PREFIX + "/portfolio/remove?token="
					+ ImmopolyUser.getInstance().getToken() + "&expose=" + params[0]), false, mActivity);
			if (obj != null && !obj.has("org.immopoly.common.ImmopolyException")) {
				history = new ImmopolyHistory();
				history.fromJSON(obj);
				mTracker.trackEvent(TrackingManager.CATEGORY_ALERT, TrackingManager.ACTION_RELEASED_EXPOSE, 
									TrackingManager.LABEL_TRY, 0);
				deleteFlatFromDB( Integer.parseInt(params[0]) );
				result.success = true;
			} else if (obj != null) {
				history = new ImmopolyHistory();
				switch (obj.getJSONObject("org.immopoly.common.ImmopolyException").getInt("errorCode")) {
				case 201:
					history.mText = mActivity.getString(R.string.flat_already_in_portifolio);
					break;
				case 301:
					history.mText = mActivity.getString(R.string.flat_does_not_exist_anymore);

					break;
				case 302:
					history.mText = mActivity.getString(R.string.flat_has_no_raw_rent);
					break;
				case 441:
					history.mText = this.mActivity.getString(R.string.expose_location_spoofing);
				}
				mTracker.trackEvent(TrackingManager.CATEGORY_ALERT, TrackingManager.ACTION_RELEASED_EXPOSE,
						TrackingManager.LABEL_NEGATIVE, 0);
			}
		} catch (MalformedURLException e) {
			Log.e(Const.LOG_TAG, "release error", e);
		} catch (JSONException e) {
			Log.e(Const.LOG_TAG, "release error", e);
		}
		result.historyEvent = history;
		return result;
	}

	@Override
	protected void onPostExecute(Result result) {
		if (result.historyEvent != null && result.historyEvent.mText != null && result.historyEvent.mText.length() > 0) {
			Toast.makeText(mActivity, result.historyEvent.mText, Toast.LENGTH_LONG).show();
			// add history entry to users list
			ImmopolyUser.getInstance().getHistory().add(0, result.historyEvent);
		} else if (Settings.isOnline(mActivity)) {
			Toast.makeText(mActivity, R.string.expose_couldnt_release, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(mActivity, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
		}
		
		super.onPostExecute(result);
	}
	
	private void deleteFlatFromDB(int id) {
		mActivity.getContentResolver().delete(FlatsProvider.CONTENT_URI,
				FlatsProvider.FLAT_ID + "=" + id, null);
	}
}