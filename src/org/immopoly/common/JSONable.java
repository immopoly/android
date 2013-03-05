package org.immopoly.common;

import org.json.JSONException;
import org.json.JSONObject;

public interface JSONable {
	public void fromJSON(JSONObject o) throws JSONException;
	public JSONObject toJSON() throws JSONException;
}
