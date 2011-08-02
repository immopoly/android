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

	public String mText;
	public long mTime;
	public int mtype;
	public Double mAmount=null;

	@Override
	public void setText(String text) {
		mText = text;
	}

	@Override
	public void setTime(long time) {
		mTime = time;
	}

	@Override
	public JSONObject toJSON() throws JSONException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setType(int type) {
		mtype = type;
	}


	@Override
	public void setAmount(double amount) {
		mAmount=amount;
	}

	@Override
	public void setType2(int type2) {
		mtype = type2;
	}
}
