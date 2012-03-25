/*
 * This is the Android component of Immopoly
 * http://immopoly.appspot.com
 * Copyright (C) 2011 Tobias Sasse
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */

package org.immopoly.android.model;

import java.util.ArrayList;
import java.util.List;

import org.immopoly.android.constants.Const;
import org.immopoly.common.ActionItem;
import org.immopoly.common.Badge;
import org.immopoly.common.History;
import org.immopoly.common.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class ImmopolyUser extends User {

	public static final String sPREF_USER = "user";
	public static final String sPREF_TOKEN = "user_token";

	private String mUserToken;
	private String mUserName;
	private String mEmail;
	private String mTwitter;
	private double mBalance;
	private List<ImmopolyHistory> mUserHistory;
	private Flats flats;
	private double sLastProvision;
	private double sLastRent;
	private static ImmopolyUser sInstance = null;

	private List<ImmopolyBadge> badges;

	private List<ImmopolyActionItem> actionItems;

	private int maxExposes;

	
// private static long mTimeUpdated = -1;

	private ImmopolyUser() {
		mUserHistory = new ArrayList<ImmopolyHistory>();
		badges = new ArrayList<ImmopolyBadge>();
		actionItems = new ArrayList<ImmopolyActionItem>();
		flats = new Flats();
	}

	@Override
	public double getBalance() {
		return mBalance;
	}

	/**
	 * 
	 * @param context
	 * @return
	 * @deprecated was soll der schmarn hier? fragt den USerDataManager nach dem status
	 */
	public String readToken(Context context) {
		if (mUserToken == null || mUserToken.length() == 0) {
			SharedPreferences shared = context.getSharedPreferences(sPREF_USER,
					0);
			String userToken = shared.getString(sPREF_TOKEN, "");
			mUserToken = userToken;
		}
		return mUserToken;
	}

	@Override
	public String getToken() {
		return mUserToken;
	}

	@Override
	public String getUserName() {
		return mUserName;
	}

	@Override
	public void setBalance(double balance) {
		mBalance = balance;
	}

	@Override
	public void setToken(String token) {
		mUserToken = token;
	}

	@Override
	public void setUsername(String username) {
		mUserName = username;
	}

	@Override
	public JSONObject toJSON() {
		return null;
	}

	public static ImmopolyUser getInstance() {
		if (sInstance == null) {
			sInstance = new ImmopolyUser();
		}
		return sInstance;
	}
	
	public static void resetInstance(){
		sInstance=null;
	}

	@Override
	public void setPortfolio(JSONObject portfolio) {
		if (portfolio != null) {
			// this.mTimeUpdated = Calendar.getInstance().getTimeInMillis();
			JSONArray results;
			JSONArray resultEntries;
			try {
				Flat item;
				results = portfolio.getJSONArray("resultlistEntries");
				resultEntries = results.getJSONArray(0);

				JSONObject expose;
				JSONObject realEstate;
				flats = new Flats();
				for (int i = 0; i < resultEntries.length(); i++) {
					item = new Flat();
					expose = resultEntries.getJSONObject(i).getJSONObject(
							"expose.expose");
					realEstate = expose.getJSONObject("realEstate");
					item.priceValue = realEstate.optString("baseRent");
					try {
						double price = Double.parseDouble(item.priceValue);
						item.priceValue = Integer.toString( (int) Math.round(price) );
					} catch (Exception e) {} 
					item.name = realEstate.optString("title");
					item.uid = realEstate.optInt("@id");
					item.lat = realEstate.getJSONObject("address")
							.getJSONObject("wgs84Coordinate")
							.optDouble("latitude");
					item.lng = realEstate.getJSONObject("address")
							.getJSONObject("wgs84Coordinate")
							.optDouble("longitude");
					item.takeoverDate  = realEstate.optLong("overtakeDate");
					item.takeoverTries = realEstate.optInt("overtakeTries");
					item.numRooms      = realEstate.optInt("numberOfRooms");
					item.livingSpace   = realEstate.optInt("livingSpace");
					
					if (realEstate.has("titlePicture")) {
						JSONObject objPicture = realEstate.getJSONObject("titlePicture");
						if (objPicture.has("urls") && objPicture.getJSONArray("urls").length() > 0) {
							JSONObject urls = objPicture.getJSONArray("urls").getJSONObject(0).getJSONObject("url");
							if ( urls != null )
							item.titlePictureSmall = urls.optString("@href");
						}
					}
					item.owned         = true;
					flats.add(item);
				}
			} catch (JSONException e) {
				Log.e( Const.LOG_TAG, "Exception while parsing portfolio: ", e );
			}
		}
	}
	
	public Flats getPortfolio() {
		return flats;
	}
	

	@Override
	public History instantiateHistory(JSONObject o) {
		return new ImmopolyHistory(o);
	}

	@Override
	public void setHistory(List<History> history) {
		mUserHistory.clear();
		for (History history2 : history) {
			mUserHistory.add((ImmopolyHistory) history2);
		}

	}

	public List<ImmopolyHistory> getHistory() {
		return mUserHistory;
	}

	@Override
	public void setLastProvision(double lastProvision) {
		sLastProvision = lastProvision;
	}

	@Override
	public void setLastRent(double lastRent) {
		sLastRent = lastRent;
	}

	public double getLastProvision() {
		return sLastProvision;
	}

	public double getLastRent() {
		return sLastRent;
	}

	public String getEmail() {
		return mEmail;
	}

	public String getTwitter() {
		return mTwitter;
	}

	public void setEmail(String email) {
		mEmail = email;
	}

	public void setTwitter(String twitter) {
		mTwitter = twitter;
	}

	public List<ImmopolyBadge> getBadges() {
		return badges;
	}

	@Override
	public Badge instantiateBadge(JSONObject o) {
		return new ImmopolyBadge(o);
	}

	@Override
	public void setBadges(List<Badge> badges) {
		this.badges.clear();
		for (Badge badge: badges) {
			this.badges.add((ImmopolyBadge) badge);
		}

	}

	public List<ImmopolyActionItem> getActionItems() {
		return actionItems;
	}

	@Override
	public ActionItem instantiateActionItem(JSONObject o) {
		return new ImmopolyActionItem(o);
	}

	@Override
	public void setActionItems(List<ActionItem> actionItems) {
		this.actionItems.clear();
		for (ActionItem actionItem : actionItems) {
			this.actionItems.add((ImmopolyActionItem) actionItem);
		}

	}

	@Override
	public void setMaxExposes(int maxExposes) {
		this.maxExposes = maxExposes;
	}

	public int getMaxExposes() {
		return maxExposes;
	}
	
	@Override
	public void setNumExposes(int numExposes) {
		// numExposes == flats.size() !
	}
	
	public void removeActionItem(int type) {
		for (ImmopolyActionItem item : actionItems) {
			if (item.getType() == type) {
				item.removeAmount(1);
			}
		}
	}
}
