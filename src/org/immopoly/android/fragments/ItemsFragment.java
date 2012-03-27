package org.immopoly.android.fragments;

import java.util.List;

import org.immopoly.android.R;
import org.immopoly.android.helper.ImageListDownloader;
import org.immopoly.android.model.ImmopolyActionItem;
import org.immopoly.android.model.ImmopolyUser;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class ItemsFragment extends DialogFragment implements OnItemClickListener {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		GridView grid = new GridView(getActivity());
		grid.setAdapter(new SimpleAdapter(getActivity()));
		grid.setColumnWidth(GridView.AUTO_FIT);
		grid.setOnItemClickListener(this);

		return new AlertDialog.Builder(getActivity()).setView(grid).show();
	}

	class SimpleAdapter extends BaseAdapter {

		private ImageListDownloader mDownloader = new ImageListDownloader(null, null, null);
		private LayoutInflater mInflater;
		private List<ImmopolyActionItem> mActionItems;

		public SimpleAdapter(Context context) {
			mActionItems = ImmopolyUser.getInstance().getActionItems();
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mActionItems.size();
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
			ImmopolyActionItem item = mActionItems.get(position);

			View inflate = mInflater.inflate(R.layout.action_item, null);
			ImageView image = (ImageView) inflate.findViewById(R.id.image);
			mDownloader.download(item.getImageUrl(), image);
			((TextView) inflate.findViewById(R.id.title)).setText(item.getText() + " (" + item.getAmount() + ")");
			return inflate;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ImmopolyActionItem item = ImmopolyUser.getInstance().getActionItems().get(position);
		if (item.getAmount() > 0)
			ItemActivateFragment.newInstance(position).show(getFragmentManager(), "activate");
		dismiss();
	}

}
