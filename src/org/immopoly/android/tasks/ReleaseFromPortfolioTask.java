package org.immopoly.android.tasks;

import java.net.URL;

import org.immopoly.android.helper.TrackingManager;
import org.immopoly.android.helper.WebHelper;
import org.immopoly.android.model.Flat;
import org.immopoly.android.model.ImmopolyUser;
import org.json.JSONObject;

import android.app.Activity;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class ReleaseFromPortfolioTask extends AbstractExposeTask {

	public ReleaseFromPortfolioTask(Activity activity, GoogleAnalyticsTracker tracker ) {
		super(activity, tracker, TrackingManager.ACTION_RELEASED_EXPOSE);
	}

	@Override
	protected JSONObject request(Flat flat) throws Exception {
		return WebHelper.getHttpData(new URL(WebHelper.SERVER_URL_PREFIX + "/portfolio/remove?token="
				+ ImmopolyUser.getInstance().getToken() + "&expose=" + flat.uid), false, super.context);
	}


}