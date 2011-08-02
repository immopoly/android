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

import org.immopoly.android.R;
import org.immopoly.android.provider.FlatsProvider.Flat;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


public class FlatsCursorAdapter extends SimpleCursorAdapter {

	private int layout;

	// private static ImageListDownloader imageDownloader = new
	// ImageListDownloader();

	public FlatsCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);

		this.layout = layout;
	}

	@Override
	public long getItemId(int position) {
		if (position < getCount()) {
			this.getCursor().moveToPosition(position);
			int columnID = getCursor().getColumnIndex(Flat.FLAT_ID);
			long id = getCursor().getLong(columnID);
			return id;
		} else {
			return -1;
		}
	}

	@Override
	public View newView(Context context, Cursor c, ViewGroup parent) {

		final LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(layout, parent, false);
		return v;
	}

	@Override
	public void bindView(View v, Context context, Cursor c) {

		if (v == null) {
			v = newView(context, c, null);
		}
		int nameCol = c.getColumnIndex(Flat.FLAT_NAME);
		// int urlCol = c.getColumnIndex(Flat.FLAT_URL);

		String name = c.getString(nameCol);
		// String url = c.getString(urlCol);
		ViewHolder holder = null;

		holder = new ViewHolder();
		holder.name = (TextView) v.findViewById(R.id.name);
		// holder.image = (ImageView) v.findViewById(R.id.image);

		holder.name.setText(name);
		// imageDownloader.download(url,holder.image);

	}

	class ViewHolder {
		TextView name;
		ImageView image;
	}
}