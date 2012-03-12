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

package org.immopoly.android.helper;

import org.immopoly.android.R;
import org.immopoly.android.model.OAuthData;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.util.DisplayMetrics;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class Settings {

	private static ImageListDownloader exposeImageDownloader;
	private static float screenDesity;
	
	public static void shareMessage(Context context, String title,
			String message, String link) {
		final String tag = "@immopoly";
		/*
		 * This code can limit the size of the messages which will be shared, 
		 * not needed at the moment
		final int maxMessage = 140;
		int overhead = maxMessage - message.length() - link.length() - 1
				- tag.length() - 1 - title.length() - 1;
		if (overhead < 0) {
			message = message.substring((-1)*overhead, message.length() - 1);
		}
		message = title + message;
		*/
		message += " " + link + " " + tag;
		final Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, title);
		intent.putExtra(Intent.EXTRA_TEXT, message);
		context.startActivity(Intent.createChooser(intent, "Share"));
	}

	public static boolean isOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm.getActiveNetworkInfo() == null) {
			return false;
		}
		return cm.getActiveNetworkInfo().isConnectedOrConnecting();

	}

	public static String getFlatLink(String exposeID, boolean mobile) {
		if(mobile)
			return OAuthData.sExposeUrl + exposeID;
		else
			return OAuthData.sExposeUrlWeb + exposeID;
	}
	
	public static ImageListDownloader getExposeImageDownloader( Context ctx ) {
		if ( exposeImageDownloader == null ) {
			Bitmap loadingBmp = ((BitmapDrawable) ctx.getResources().getDrawable( R.drawable.loading )).getBitmap();
			Bitmap fallbackBmp = ((BitmapDrawable) ctx.getResources().getDrawable( R.drawable.portfolio_fallback )).getBitmap();
			Animation loadingAni = AnimationUtils.loadAnimation( ctx,R.anim.loading_animation );
			exposeImageDownloader = new ImageListDownloader( loadingBmp, loadingAni, fallbackBmp );
		}
		return exposeImageDownloader;
	}


	
	public static float getScreenDensity( Activity activity) {
		if ( screenDesity == 0.0f ) {
			DisplayMetrics metrics = new DisplayMetrics();
			activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
			screenDesity = metrics.density;
		}
		return screenDesity;
	}
	
	public static float dp2px( Activity activity, float dp ) {
		if ( screenDesity == 0.0f ) 
			getScreenDensity(activity);
		return screenDesity*dp;
	}
	
}
