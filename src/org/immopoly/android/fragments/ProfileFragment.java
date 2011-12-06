package org.immopoly.android.fragments;

import org.immopoly.android.R;
import org.immopoly.android.app.UserDataListener;
import org.immopoly.android.app.UserDataManager;
import org.immopoly.android.helper.ImageListDownloader;
import org.immopoly.android.helper.Settings;
import org.immopoly.android.model.ImmopolyBadge;
import org.immopoly.android.model.ImmopolyUser;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ProfileFragment extends Fragment implements UserDataListener {

	private ImageListDownloader imageDownloader;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.fragment_profile, container, false);
		UserDataManager.instance.addUserDataListener(this);
		imageDownloader = Settings.getExposeImageDownloader(getActivity());
		updateVisibility(layout);
		return layout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		GridView gridView = (GridView) getView().findViewById(R.id.gridview);
		final BadgeAdapter badgeAdapter = new BadgeAdapter();
		gridView.setAdapter(badgeAdapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				if (null != badgeAdapter.getItem(position)) {
					Toast.makeText(getActivity(), badgeAdapter.getItem(position).getText(), Toast.LENGTH_LONG);
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setMessage(badgeAdapter.getItem(position).getText()).setCancelable(false).setPositiveButton("Tschüss",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									getActivity().finish();
								}
							});
					AlertDialog alert = builder.create();
					alert.show();
				}
			}
		});
		((TextView) getView().findViewById(R.id.username)).setText(ImmopolyUser.getInstance().getUserName());
	}

	class BadgeAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			int count =ImmopolyUser.getInstance().getBadges().size();
			if (count>5)
				return count;
			else
				return 5;
		}

		@Override
		public ImmopolyBadge getItem(int position) {
			if (position < ImmopolyUser.getInstance().getBadges().size())
				return ImmopolyUser.getInstance().getBadges().get(position);

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
			final ImmopolyBadge b = getItem(position);
			if(null!=b){
				imageDownloader.download(b.getUrl(), imageView);
			}else
				imageView.setImageResource(R.drawable.badge_empty);
			return imageView;
		}
	}

	private void updateVisibility(View v) {
		if (null == v)
			return;
		if (UserDataManager.instance.getState() == UserDataManager.LOGGED_IN) {
			// helptext deaktiveren
			v.findViewById(R.id.profile_notloggedin).setVisibility(View.GONE);
		} else {
			v.findViewById(R.id.profile_notloggedin).setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onDestroyView() {
		UserDataManager.instance.removeUserDataListener(this);
		super.onDestroyView();
	}

	@Override
	public void onUserDataUpdated() {
		updateVisibility(getView());
	}
}
