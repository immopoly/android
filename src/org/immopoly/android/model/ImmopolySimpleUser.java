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

import org.immopoly.common.Badge;
import org.immopoly.common.SimpleUser;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class ImmopolySimpleUser extends SimpleUser implements Parcelable {

	private String mUserName;
	private String mEmail;
	private String mTwitter;
	private double mBalance;
	private double sLastProvision;
	private double sLastRent;
	private int maxExposes;
	private int numExposes;
	private List<ImmopolyBadge> badges;

	public ImmopolySimpleUser() {
		badges = new ArrayList<ImmopolyBadge>();
	}

	@Override
	public double getBalance() {
		return mBalance;
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
	public void setUsername(String username) {
		mUserName = username;
	}

	@Override
	public JSONObject toJSON() {
		return null;
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

	@Override
	public void setMaxExposes(int maxExposes) {
		this.maxExposes = maxExposes;
	}

	public int getMaxExposes() {
		return maxExposes;
	}
	
	@Override
	public void setNumExposes(int numExposes) {
		this.numExposes = numExposes;
	}
	
	public int getNumExposes(){
		return this.numExposes;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		
	}
	
}
