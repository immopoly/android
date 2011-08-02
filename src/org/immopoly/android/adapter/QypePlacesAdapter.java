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

import java.util.ArrayList;

import org.immopoly.android.R;
import org.immopoly.android.model.QypePlace;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class QypePlacesAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private ArrayList<QypePlace> data;

	public QypePlacesAdapter(Activity context, ArrayList<QypePlace> data) {
		this.data = data;
		inflater = context.getLayoutInflater();

	}

	public int getCount() {
		int size = 0;

		size = data.size();

		return size;

	}

	public QypePlace getItem(int arg0) {
		return data.get(arg0);
	}

	public long getItemId(int arg0) {
		return arg0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.flat_list_item, parent,
					false);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (holder == null) {
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.desciption = (TextView) convertView
					.findViewById(R.id.description);
		}
		holder.name.setText(getItem(position).title);
		holder.desciption.setText(String.valueOf(getItem(position).rating));
		return convertView;
	}

	class ViewHolder {
		TextView name;
		TextView desciption;
	}
}
