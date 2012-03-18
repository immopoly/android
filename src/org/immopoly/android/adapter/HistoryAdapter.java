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
import org.immopoly.android.constants.Const;
import org.immopoly.android.model.ImmopolyHistory;
import org.immopoly.android.model.ImmopolyUser;
import org.immopoly.common.History;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class HistoryAdapter extends BaseAdapter {

	private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy - HH:mm", Locale.GERMANY);
	
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

			holder.date = (TextView) convertView.findViewById(R.id.date);
			holder.text = (TextView) convertView.findViewById(R.id.historyText);
			holder.icon = (ImageView) convertView.findViewById(R.id.icon);
			holder.bttn = (ImageView) convertView.findViewById(R.id.show_expo_btn);
		}

		ImmopolyHistory entry = (ImmopolyHistory) getItem(position);

		boolean showButton = false;
		int type = entry.getType();
		switch (type) {
		case History.TYPE_EXPOSE_ADDED:
		case History.TYPE_EXPOSE_REMOVED:
			holder.icon.setImageResource( R.drawable.history_btn_info );
			showButton = entry.getExposeId() > 0;
			break;
		case History.TYPE_EXPOSE_SOLD:
			holder.icon.setImageResource( R.drawable.history_btn_star );
			break;
		case History.TYPE_EXPOSE_MONOPOLY_POSITIVE:
			holder.icon.setImageResource( R.drawable.history_btn_star );
			break;
		case History.TYPE_DAILY_PROVISION:
			holder.icon.setImageResource( R.drawable.history_btn_star );
			break;
		case History.TYPE_EXPOSE_MONOPOLY_NEGATIVE:
			holder.icon.setImageResource( R.drawable.history_btn_attention );
			break;
		case History.TYPE_DAILY_RENT:
			holder.icon.setImageResource( R.drawable.history_btn_attention );
			break;
		}
		holder.date.setText(sdf.format(entry.getTime()));
		holder.text.setText(entry.getText());
		holder.bttn.setVisibility( showButton ? View.VISIBLE : View.GONE );
		return convertView;
	}

	class ViewHolder {
		TextView date;
		TextView text;
		ImageView icon;
		ImageView bttn;
	}

	@Override
	public boolean isEnabled(int position) {
		ImmopolyHistory entry = (ImmopolyHistory) getItem(position);
		if ( entry == null )
			return false;
		return entry.getExposeId() > 0 && entry.getType() == History.TYPE_EXPOSE_ADDED ||
										  entry.getType() == History.TYPE_EXPOSE_REMOVED;
	}

}
