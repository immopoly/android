package org.immopoly.android.notification;

import org.immopoly.android.R;
import org.immopoly.android.app.ImmopolyActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;


public class UserNotification {
	
	
	public static void showNotification(Context context, String type, String message,String title){
		
		final int typeOfNotification = Integer.parseInt(type);
		int icon;
		switch(typeOfNotification){
		
		case 1:
			icon = R.drawable.house;        // icon from resources
			break;
		case 2:
			icon = R.drawable.house_drawn;        // icon from resources
			break;
		default:
			icon = R.drawable.ic_map_button;
			break;
		}
		
		CharSequence tickerText = message;              // ticker-text
		long when = System.currentTimeMillis();         // notification time
		Context appContext = context.getApplicationContext();      // application Context
		CharSequence contentTitle = title;  // message title
		CharSequence contentText = message;      // message text
		
		// TODO need to define proper starting point
		Intent notificationIntent = new Intent(context, ImmopolyActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);


		// the next two lines initialize the Notification, using the configurations above
		Notification notification = new Notification(icon, tickerText, when);
		notification.setLatestEventInfo(appContext, contentTitle, contentText, contentIntent);
		notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
		
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) appContext.getSystemService(ns);
		mNotificationManager.notify(1, notification);
	}
}
