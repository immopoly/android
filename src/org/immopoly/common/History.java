package org.immopoly.common;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class History implements JSONable {

	public static final int TYPE_EXPOSE_ADDED=1;
	public static final int TYPE_EXPOSE_SOLD=2;

	public static final int TYPE_EXPOSE_MONOPOLY_POSITIVE=3;
	public static final int TYPE_EXPOSE_MONOPOLY_NEGATIVE=4;

	//history2
	public static final int TYPE_DAILY_PROVISION=5;
	public static final int TYPE_DAILY_RENT=6;
	// new
	public static final int TYPE_EXPOSE_REMOVED = 7;

	// badge ab 100
	public static final int TYPE_BADGE_ADDED = 100;

	// mandatory
	public abstract void setTime(long time);
	public abstract void setText(String text);
	public abstract void setType(int type);

	// optional
	public abstract void setAmount(double amount);
	public abstract void setExposeId(long exposeId);
	public abstract void setOtherUserName(String userName);

	protected History() {
	}

	public History(JSONObject o) {
		fromJSON(o);
	}

	@Override
	public void fromJSON(JSONObject o) {
		try {
			JSONObject h = o.getJSONObject(getJSONObjectKey());
			setTime(h.getLong("time"));
			setText(h.getString("text"));
			setType(h.getInt("type"));
			// if(h.has("type2"))
			// setType2(h.getInt("type2"));
			if(h.has("amount"))
				setAmount(h.getDouble("amount"));
			if (h.has("exposeId"))
				setExposeId(h.getLong("exposeId"));
			
			setOtherUserName(h.optString("otherUsername"));

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	protected String getJSONObjectKey() {
		return "org.immopoly.common.History";
	}
}
