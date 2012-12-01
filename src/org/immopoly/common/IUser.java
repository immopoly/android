package org.immopoly.common;

import java.util.List;

import org.json.JSONObject;

public interface IUser extends ISimpleUser{
	public static final String KEY_TOKEN = "token";
	public static final String KEY_HISTORY_LIST = "historyList";
	public static final String KEY_RESULT_LIST = "resultlist.resultlist";
	public static final String KEY_ACTIONITEM_LIST = "actionItemList";

	public String getToken();

	public void setToken(String token);

	public void setPortfolio(JSONObject portfolio);

	public void setHistory(List<History> history);

	public History instantiateHistory(JSONObject jsonObject);

	public ActionItem instantiateActionItem(JSONObject jsonObject);

	public void setActionItems(List<ActionItem> actionItems);
	
	public void addHistoryEntries(List<History> historyEntries);

}
