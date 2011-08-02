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

package org.immopoly.android.adapter;

import java.util.HashMap;

import org.immopoly.android.R;
import org.immopoly.android.model.Flat;
import org.immopoly.android.model.Flats;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class FlatAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	
	HashMap<String, Integer> alphaIndexer;
	private Flats mFlats;

	public FlatAdapter(Activity context, Flats flats) {
		mFlats = flats;
		inflater = context.getLayoutInflater();

		// alphaIndexer = new HashMap<String, Integer>();
		// // in this hashmap we will store here the positions for
		// // the sections
		//
		// int size = Regions.size();
		// for (int i = size - 1; i >= 0; i--) {
		// Region element = Regions.getRegion(i);
		// alphaIndexer.put(element.name.substring(0, 1), i);
		// //We store the first letter of the word, and its index.
		// //The Hashmap will replace the value for identical keys are putted in
		// }
		//
		// // now we have an hashmap containing for each first-letter
		// // sections(key), the index(value) in where this sections begins
		//
		// // we have now to build the sections(letters to be displayed)
		// // array .it must contains the keys, and must (I do so...) be
		// // ordered alphabetically
		//
		// Set<String> keys = alphaIndexer.keySet(); // set of letters ...sets
		// // cannot be sorted...
		//
		// Iterator<String> it = keys.iterator();
		// ArrayList<String> keyList = new ArrayList<String>(); // list can be
		// // sorted
		//
		// while (it.hasNext()) {
		// String key = it.next();
		// keyList.add(key);
		// }
		//
		// Collections.sort(keyList);
		//
		// sections = new String[keyList.size()]; // simple conversion to an
		// // array of object
		// keyList.toArray(sections);
	}

	public int getCount() {
		int size = 0;

		size = mFlats.size();

		return size;

	}

	public Flat getItem(int arg0) {
		return mFlats.get(arg0);
	}

	public long getItemId(int arg0) {
		return arg0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		// if(convertView != null){
		// holder = (ViewHolder) convertView.getTag();
		// }else{
		holder = new ViewHolder();
		convertView = inflater.inflate(R.layout.flat_list_item, parent, false);
		holder.name = (TextView) convertView.findViewById(R.id.name);
		holder.desciption = (TextView) convertView
				.findViewById(R.id.description);
		// }
		holder.name.setText(mFlats.get(position).name);
		holder.desciption.setText(mFlats.get(position).street);
		return convertView;
	}

	class ViewHolder {
		TextView name;
		TextView desciption;
	}

}
