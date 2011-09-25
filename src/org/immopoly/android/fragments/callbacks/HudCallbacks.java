package org.immopoly.android.fragments.callbacks;

import android.content.Intent;
import android.view.View;

public interface HudCallbacks {

	public void updateHud(Intent data, int element);
	public void onHudAction(View view);
}
