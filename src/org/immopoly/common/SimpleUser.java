package org.immopoly.common;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class SimpleUser implements JSONable , ISimpleUser{
	protected JSONObject user;
	protected JSONObject info;
	
	
	@Override
	public void fromJSON(JSONObject o) {
		try {
			user = o.getJSONObject(KEY_USER_OBJECT);
			setUsername(user.getString(KEY_USERNAME));
			setEmail(user.optString(KEY_EMAIL));
			setTwitter(user.optString(KEY_TWITTER));
			info = user.getJSONObject(KEY_INFO);
			setBalance(info.getDouble(KEY_BALANCE));
			setLastRent(info.getDouble(KEY_LAST_RENT));
			setLastProvision(info.getDouble(KEY_LAST_PROVISION));

			if (info.has(KEY_NUM_EXPOSES))
				setNumExposes(info.getInt(KEY_NUM_EXPOSES));

			if (info.has(KEY_MAX_EXPOSES))
				setMaxExposes(info.getInt(KEY_MAX_EXPOSES));

			List<Badge> badges = new ArrayList<Badge>();
			JSONArray badgesList = info.getJSONArray(KEY_BADGES_LIST);
			for (int i = 0; i < badgesList.length(); i++) {
				Badge b = instantiateBadge(badgesList.getJSONObject(i));
				badges.add(b);
			}
			setBadges(badges);

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
