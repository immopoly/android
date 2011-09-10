package org.immopoly.android.c2dm;

import org.immopoly.android.constants.Const;

import android.content.Context;
import android.content.Intent;
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
			System.out.println(extras.get("payload"));
			// Now do something smart based on the information
		}
	}

	@Override
	public void onError(Context context, String errorId) {
		Log.e("C2DM", "Error occured!!!");
	}

}
