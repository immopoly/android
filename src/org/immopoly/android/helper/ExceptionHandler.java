package org.immopoly.android.helper;

import org.immopoly.android.app.UserDataManager;
import org.immopoly.android.model.ImmopolyException;

import android.content.Context;
import android.widget.Toast;

public class ExceptionHandler {
	
	public static void handleException(Context ctx, ImmopolyException ex){
		if(null == ex){
			Toast.makeText(ctx, "Error", Toast.LENGTH_LONG).show();
			return;
		}
		
		switch (ex.getErrorCode()) {
			case ImmopolyException.TOKEN_NOT_FOUND:
				UserDataManager.instance.logout();
				break;
	
			default:
				break;
		}
		
		Toast.makeText(ctx, ex.getMessage(), Toast.LENGTH_LONG).show();
	}

}
