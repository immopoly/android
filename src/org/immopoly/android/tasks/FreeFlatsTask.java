package org.immopoly.android.tasks;

import org.immopoly.android.helper.WebHelper;
import org.immopoly.android.model.Flat;
import org.immopoly.android.model.Flats;
import org.immopoly.android.model.ImmopolyUser;
import org.immopoly.common.ActionItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

public class FreeFlatsTask extends AsyncTask<Flats, Void, Flats> {

	@Override
	protected Flats doInBackground(Flats... params) {
		Flats flats = params[0];
		JSONArray jsonArray = new JSONArray();
		if (flats != null) {
			for (Flat f : flats) {
				jsonArray.put(f.uid);
			}
			try {
				JSONObject obj = new JSONObject();
				obj.put("exposes", jsonArray);
				obj.put("token", ImmopolyUser.getInstance().getToken());
				obj.put("actiontype", ActionItem.TYPE_ACTION_FREEEXPOSES);
				JSONArray flatsArray = WebHelper.postFlatIdsHttpData(WebHelper.SERVER_URL_PREFIX + "/user/action", obj);
				int l = flatsArray.length();
				for (Flat f : flats) {
					for (int i = 0; i < l; i++) {
						if (f.uid == flatsArray.getInt(i)) {
							f.visible = true;
							break;
						} else {
							f.visible = false;
						}
					}
				}
				ImmopolyUser.getInstance().removeActionItem(ActionItem.TYPE_ACTION_FREEEXPOSES);
				return flats;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
