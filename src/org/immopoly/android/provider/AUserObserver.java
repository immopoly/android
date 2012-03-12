package org.immopoly.android.provider;

import android.database.ContentObserver;
import android.os.Handler;

public abstract class AUserObserver extends ContentObserver {

	public AUserObserver(Handler handler) {
		super(handler);
	}

	public void onChange(boolean selfChange) {
		
	}

}
