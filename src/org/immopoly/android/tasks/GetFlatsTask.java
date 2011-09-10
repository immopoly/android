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

/**
 * 
 */
package org.immopoly.android.tasks;

import org.immopoly.android.app.BaseListActivity;
import org.immopoly.android.model.Flat;
import org.immopoly.android.model.Flats;

import android.os.AsyncTask;

/**
 * @author tobias
 * 
 */
public class GetFlatsTask extends AsyncTask<String, Void, Flats> {

	BaseListActivity activity;

	public GetFlatsTask(BaseListActivity arg0) {
		activity = arg0;
	}

	@Override
	protected Flats doInBackground(String... params) {
		Flats flats = new Flats();
		Flat flat;
		flat = new Flat();
		flat.name = "Kiez Bar";
		flat.description = "Nice Flat next to the river";
		flats.add(flat);

		flat = new Flat();
		flat.name = "House Bar";
		flat.description = "Cool flatmates with nice dog, loved the conversation";
		flats.add(flat);

		return flats;

	}

	@Override
	protected void onPostExecute(Flats result) {
		super.onPostExecute(result);
	}

}
