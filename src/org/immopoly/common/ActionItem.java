package org.immopoly.common;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class ActionItem implements JSONable {

	protected ActionItem() {
	}

	public ActionItem(JSONObject o) {
		fromJSON(o);
	}

	public static final int TYPE_ACTION_FREEEXPOSES = 1;

	// mandatory
	public abstract void setType(int type);
	public abstract void setAmount(int amount);
	public abstract void setUrl(String url);
	public abstract void setText(String text);

	@Override
	public void fromJSON(JSONObject o) {
		try {
			JSONObject h = o.getJSONObject(getJSONObjectKey());
			setType(h.getInt("type"));
			setAmount(h.getInt("amount"));
			setText(h.getString("text"));
			setUrl(h.getString("url"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	protected String getJSONObjectKey() {
		return "ActionItem";
	}
}
