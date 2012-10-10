package org.immopoly.android.adapter;


import java.util.List;

import org.immopoly.android.model.ImmopolyBadge;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class BadgeAdapter extends BaseAdapter {

	private List<ImmopolyBadge> badges;
	
	public BadgeAdapter(List<ImmopolyBadge> _badges) {
		badges = _badges;
	}
	
	@Override
	public int getCount() {
		return -1;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return null;
	}

}
