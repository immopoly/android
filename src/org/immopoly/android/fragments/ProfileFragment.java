package org.immopoly.android.fragments;

import org.immopoly.android.R;
import org.immopoly.android.app.UserDataListener;
import org.immopoly.android.app.UserDataManager;
import org.immopoly.android.model.ImmopolyUser;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class ProfileFragment extends Fragment implements UserDataListener {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.fragment_profile, container, false);
		UserDataManager.instance.addUserDataListener(this);
		updateVisibility(layout);
		return layout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		GridView gridView = (GridView) getView().findViewById(R.id.gridview);
		gridView.setAdapter(new BadgeAdapter());
		((TextView) getView().findViewById(R.id.username)).setText(ImmopolyUser.getInstance().getUserName());
	}

	class BadgeAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return 5;
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView;
			if (convertView == null) { // if it's not recycled, initialize some
										// attributes
				imageView = new ImageView(getActivity());
				imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setPadding(8, 8, 8, 8);
			} else {
				imageView = (ImageView) convertView;
			}

			imageView.setImageResource(R.drawable.badge_empty);
			return imageView;
		}

	}

	private void updateVisibility(View v) {
		if (UserDataManager.instance.getState() == UserDataManager.LOGGED_IN) {
			// helptext deaktiveren
			v.findViewById(R.id.profile_notloggedin).setVisibility(View.GONE);
		} else {
			v.findViewById(R.id.profile_notloggedin).setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onUserDataUpdated() {
		updateVisibility(getView());
	}
}
