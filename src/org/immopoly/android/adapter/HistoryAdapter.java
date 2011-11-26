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

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import org.immopoly.android.R;
import org.immopoly.android.model.ImmopolyHistory;
import org.immopoly.android.model.ImmopolyUser;
import org.immopoly.common.History;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class HistoryAdapter extends BaseAdapter {

	private LayoutInflater inflater;


	public HistoryAdapter(Activity context) {
		inflater = context.getLayoutInflater();
	}

	@Override
	public int getCount() {
		List<ImmopolyHistory> h = ImmopolyUser.getInstance().getHistory();
		return h.size();
	}

	@Override
	public History getItem(int arg0) {
		return ImmopolyUser.getInstance().getHistory().get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.history_list_item, parent, false);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (holder == null) {
			holder = new ViewHolder();

			holder.time = (TextView) convertView.findViewById(R.id.time);
			holder.date = (TextView) convertView.findViewById(R.id.date);

			holder.text = (TextView) convertView.findViewById(R.id.historyText);
		}

		int color = Color.BLACK;
		switch (((ImmopolyHistory) getItem(position)).mtype) {
		case History.TYPE_EXPOSE_SOLD:
			color = Color.GREEN;
			break;
		case History.TYPE_EXPOSE_MONOPOLY_POSITIVE:
			color = Color.GREEN;
			break;
		case History.TYPE_DAILY_PROVISION:
			color = Color.GREEN;
			break;
		case History.TYPE_EXPOSE_MONOPOLY_NEGATIVE:
			color = Color.RED;
			break;
		case History.TYPE_DAILY_RENT:
			color = Color.RED;
			break;
		default:
			break;
		}

		holder.date.setTextColor(color);
		holder.date.setText(create_datestring(((ImmopolyHistory) getItem(position)).mTime));
		holder.time.setTextColor(color);
		holder.time.setText(create_timestring(((ImmopolyHistory) getItem(position)).mTime) + " Uhr");
		holder.text.setTextColor(color);
		holder.text.setText(((ImmopolyHistory) getItem(position)).mText);
		return convertView;
	}

	class ViewHolder {
		TextView time;
		TextView date;
		TextView text;
	}

	public static String create_datestring(long timestring) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
		return sdf.format(timestring);
	}

	public static String create_timestring(long timestring) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.GERMANY);
		return sdf.format(timestring);
	}

}
