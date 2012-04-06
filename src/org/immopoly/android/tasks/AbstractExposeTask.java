package org.immopoly.android.tasks;

import org.immopoly.android.R;
import org.immopoly.android.app.UserDataManager;
import org.immopoly.android.helper.Settings;
import org.immopoly.android.helper.TrackingManager;
import org.immopoly.android.model.Flat;
import org.immopoly.android.model.ImmopolyException;
import org.immopoly.android.model.ImmopolyHistory;
import org.immopoly.android.model.ImmopolyUser;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public abstract class AbstractExposeTask extends AsyncTask<Flat, Void, Result> {

	protected Context context;
	private GoogleAnalyticsTracker tracker;
	private String trackerLabel;

	public AbstractExposeTask(Context context, GoogleAnalyticsTracker tracker, String trackerLabel) {
		this.context = context;
		this.tracker = tracker;
		this.trackerLabel = trackerLabel;
	}

	@Override
	protected Result doInBackground(Flat... params) {
		Flat flat = params[0];
		
		Result result = new Result();
		try {
			ImmopolyUser.getInstance().readToken(this.context);
	
			JSONObject obj = request(flat);
	
			if (obj != null && obj.has("org.immopoly.common.History")) {
				result.history = new ImmopolyHistory(obj);
				result.success = getSuccess(result.history);
	
				tracker.trackEvent(TrackingManager.CATEGORY_ALERT_EXPOSE_TAKEN, this.trackerLabel,
						TrackingManager.LABEL_TRY, 0);
			} else if (obj != null && obj.has("org.immopoly.common.ImmopolyException")) {
				result.exception = new ImmopolyException(context, obj);
	
				tracker.trackEvent(TrackingManager.CATEGORY_ALERT_EXPOSE_TAKEN, this.trackerLabel,
						TrackingManager.LABEL_NEGATIVE, 0);
			}
		} catch (Exception e) {
			result.exception = new ImmopolyException(e);
		}
		return result;
	}

	protected boolean getSuccess(ImmopolyHistory history) {
		return true;
	}

	protected abstract JSONObject request(Flat flat) throws Exception;

	@Override
	protected void onPostExecute(Result result) {
		if (result.history != null)
		{
			UserDataManager.instance.update(result.history);
		} else if (Settings.isOnline(this.context)) {
			if (null != result.exception) {
				// exception wird über dialog ausgegegeben
				// Toast.makeText(this.context, result.exception.getMessage(),
				// Toast.LENGTH_LONG).show();
			} else
				Toast.makeText(this.context, "Error", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this.context, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
		}
	}

}