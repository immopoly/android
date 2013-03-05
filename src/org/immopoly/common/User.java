package org.immopoly.common;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class User extends SimpleUser implements JSONable , IUser{
	
	@Override
	public void fromJSON(JSONObject o) {
		try {
			super.fromJSON(o);
			setToken(user.getString(KEY_TOKEN));
			setPortfolio(info.getJSONObject(KEY_RESULT_LIST));
			
			setHistory(getHistoryEntries(info.getJSONArray(KEY_HISTORY_LIST)));

			List<ActionItem> actionItems = new ArrayList<ActionItem>();
			JSONArray actionItemList = info.getJSONArray(KEY_ACTIONITEM_LIST);
			for (int i = 0; i < actionItemList.length(); i++) {
				ActionItem a = instantiateActionItem(actionItemList.getJSONObject(i));
				actionItems.add(a);
			}
			setActionItems(actionItems);

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void appendHistoryEntries(JSONArray array) throws JSONException{
		addHistoryEntries(getHistoryEntries(array));
	}
	
	private List<History> getHistoryEntries(JSONArray historyList) throws JSONException{
		
		List<History> history	=	new ArrayList<History>();
		
		for (int i = 0; i < historyList.length(); i++) {
			History h = instantiateHistory(historyList.getJSONObject(i));
			history.add(h);
		}
		
		return history;
	}
	
}
