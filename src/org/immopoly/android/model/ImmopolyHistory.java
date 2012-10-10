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

import org.immopoly.common.History;
import org.json.JSONException;
import org.json.JSONObject;

public class ImmopolyHistory extends History{

	// mandatory
	private String text;
	private long time;
	private int type;

	// optional
	private Double amount;
	private long exposeId;
	private String otherUsername;


	public ImmopolyHistory(JSONObject obj) {
		super(obj);
	}

	@Override
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public void setTime(long time) {
		this.time = time;
	}

	/**
	 * Wird im Client nicht mehr serialisiert.
	 */
	@Override
	public JSONObject toJSON() throws JSONException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setType(int type) {
		this.type = type;
	}


	@Override
	public void setAmount(double amount) {
		this.amount = amount;
	}

	@Override
	public void setExposeId(long exposeId) {
		this.exposeId = exposeId;
	}

	// TODO method should exist in base interface
	public long getExposeId() {
		return exposeId;
	}
	
	public int getType() {
		return type;
	}

	public Double getAmount() {
		return amount;
	}

	public String getText() {
		return text;
	}

	public long getTime() {
		return time;
	}

	@Override
	public void setOtherUserName(String userName) {
		this.otherUsername = userName;
	}
	
	public String getOtherUsername(){
		return otherUsername;
	}

}
