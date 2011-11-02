package org.immopoly.android.c2dm;

import java.net.URL;

import org.immopoly.android.constants.Const;
import org.immopoly.android.helper.WebHelper;
import org.immopoly.android.model.ImmopolyUser;
import org.immopoly.android.notification.UserNotification;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.media.audiofx.BassBoost.Settings;
import android.os.Bundle;
import android.util.Log;

public class C2DMReceiver extends C2DMBaseReceiver {
	public C2DMReceiver() {
		// Email address currently not used by the C2DM Messaging framework
		super(Const.IMMOPOLY_EMAIL);
	}

	@Override
	public void onRegistered(Context context, String registrationId)
			throws java.io.IOException {
		// The registrationId should be send to your applicatioin server.
		// We just log it to the LogCat view
		// We will copy it from there
		Log.e("C2DM", "Registration ID arrived: Fantastic!!!");
		Log.e("C2DM", registrationId);
		String registerC2DM = WebHelper.SERVER_URL_PREFIX + "/user/C2DMregister?token="+ ImmopolyUser.getInstance().readToken(context	) + "&c2dmregistrationid=" + registrationId;
		try {
			JSONObject obj = WebHelper.getHttpData(new URL(registerC2DM), false, context);
			Log.e("C2DM", "registration with immopoly server YES");
			Log.e("C2DM", "request->" + registerC2DM);
			Log.e("C2DM", "DATA " + obj.toString());
		} catch (JSONException e) {
			Log.e("C2DM", "registration with immopoly NOOOOOOOOOOOOOOO");
			e.printStackTrace();
		}
	};

	@Override
	public void onUnregistered(Context context) {
		super.onUnregistered(context);
		Log.e("C2DM", "Sucessfully unregistered");
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.e("C2DM", "Message: Fantastic!!!");
		// Extract the payload from the message
		Bundle extras = intent.getExtras();
		if (extras != null) {
			for(String s : extras.keySet()){
				Log.e("C2DM", s);
			}
			System.out.println(extras.get("message"));
			UserNotification.showNotification(context,extras.getString("type"), extras.getString("message"),extras.getString("title"));
			// Now do something smart based on the information
		}
	}

	@Override
	public void onError(Context context, String errorId) {
		Log.e("C2DM", "Error occured!!!");
	}

}
