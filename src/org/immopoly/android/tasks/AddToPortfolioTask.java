package org.immopoly.android.tasks;

import java.net.MalformedURLException;
import java.net.URL;

import org.immopoly.android.helper.TrackingManager;
import org.immopoly.android.helper.WebHelper;
import org.immopoly.android.model.Flat;
import org.immopoly.android.model.ImmopolyHistory;
import org.immopoly.android.model.ImmopolyUser;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class AddToPortfolioTask extends AbstractExposeTask {


	public AddToPortfolioTask(Context context, GoogleAnalyticsTracker tracker) {
		super(context, tracker, TrackingManager.ACTION_TOOK_EXPOSE);
	}

	protected JSONObject request(Flat flat) throws MalformedURLException, JSONException {
		return WebHelper.getHttpObjectData(new URL(WebHelper.SERVER_URL_PREFIX + "/portfolio/add?token=" + ImmopolyUser.getInstance().getToken()
				+ "&expose=" + flat.uid), false, super.context);
	}

	@Override
	protected boolean getSuccess(ImmopolyHistory history) {
		return history.getType() == 1;
	}
	
}