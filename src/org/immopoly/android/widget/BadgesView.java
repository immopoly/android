package org.immopoly.android.widget;

import java.util.List;

import org.immopoly.android.R;
import org.immopoly.android.helper.ImageListDownloader;
import org.immopoly.android.helper.Settings;
import org.immopoly.android.helper.TrackingManager;
import org.immopoly.android.model.ImmopolyBadge;

import android.app.AlertDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class BadgesView extends GridView {

	private List<ImmopolyBadge> badges;
	private Context context;
	private int badgeSize;
	private int badgePadding;
	private ImageListDownloader imageDownloader;
	private GoogleAnalyticsTracker mTracker;
	
	public BadgesView(Context _context, AttributeSet attrs) {
		super(_context, attrs);
		context = _context;
		badgeSize = (int) Settings.dp2px(_context, 100);
		badgePadding = (int) Settings.dp2px(_context, 8);
		imageDownloader = Settings.getExposeImageDownloader(_context);
	}
	
	public void initWithBadges(List<ImmopolyBadge> _badges,GoogleAnalyticsTracker _tracker){
		badges = _badges;
		mTracker = _tracker;
		
		final BadgeAdapter badgeAdapter = new BadgeAdapter();
		setAdapter(badgeAdapter);
		setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long arg3) {
				ImmopolyBadge item = badgeAdapter.getItem(position);

				if (item != null) {
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					// builder.setTitle(R.string.badge_info);
					View layout = LayoutInflater.from(context).inflate(
							R.layout.badge_dialog, null);
					builder.setView(layout);
					if (view instanceof ImageView) {
						ImageView badgeImage = (ImageView) layout
								.findViewById(R.id.badgeImage);
						ImageView imageView = (ImageView) view;
						badgeImage.setImageDrawable(imageView.getDrawable());
					}
					mTracker.trackEvent(TrackingManager.CATEGORY_CLICKS,
							TrackingManager.ACTION_BADGE,
							TrackingManager.LABEL_VIEW, item.getType());
					((TextView) layout.findViewById(R.id.badgeText))
							.setText(item.getText());

					builder.setPositiveButton("Tschüss", null);
					builder.create().show();
				}
			}
		});
		
		
	}
	
	class BadgeAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			int count = badges.size();
			if (count > 5)
				return count;
			else
				return 5;
		}

		@Override
		public ImmopolyBadge getItem(int position) {
			if (position < badges.size())
				return badges.get(position);

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
				imageView = new ImageView(context);
				imageView.setTag("badge_image");
				imageView.setLayoutParams(new GridView.LayoutParams(badgeSize,
						badgeSize));
				// imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				// imageView.setPadding(8, 8, 8, 8);
				imageView.setPadding(badgePadding, badgePadding, badgePadding,
						badgePadding);
			} else {
				imageView = (ImageView) convertView;
			}
			final ImmopolyBadge b = getItem(position);
			if (null != b) {
				imageDownloader.download(b.getUrl(), imageView);
			} else
				imageView.setImageResource(R.drawable.badge_empty);
			return imageView;
		}
	}

}
