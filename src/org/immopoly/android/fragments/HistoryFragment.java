package org.immopoly.android.fragments;

import java.util.List;

import org.immopoly.android.R;
import org.immopoly.android.adapter.HistoryAdapter;
import org.immopoly.android.app.UserDataManager;
import org.immopoly.android.constants.Const;
import org.immopoly.android.helper.TrackingManager;
import org.immopoly.android.model.Flat;
import org.immopoly.android.model.Flats;
import org.immopoly.android.model.ImmopolyHistory;
import org.immopoly.android.model.ImmopolyUser;
import org.immopoly.android.pagination.HistoryPaginationValues;
import org.immopoly.android.pagination.ProgressIndicator;
import org.immopoly.android.pagination.UserPaginationHistoryListener;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class HistoryFragment extends ListFragment implements UserPaginationHistoryListener, ProgressIndicator{
	private GoogleAnalyticsTracker mTracker;
	private HistoryPaginationValues mPaginationValues;
	private ProgressBar progress;
	
	@Override
	public void onActivityCreated(Bundle arg0) {
		super.onActivityCreated(arg0);
		getListView().setDivider(null);
		getListView().addFooterView(LayoutInflater.from(getActivity()).inflate(R.layout.progress_footer, null));
		progress = (ProgressBar) getListView().findViewById(R.id.progress);
	}

	@Override
	public void onResume() {
		super.onResume();
		onUserDataUpdated();
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		UserDataManager.instance.addUserDataListener(this);
		mPaginationValues = new HistoryPaginationValues();
		mTracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker in manual dispatch mode...
		mTracker.startNewSession(TrackingManager.UA_ACCOUNT,
				Const.ANALYTICS_INTERVAL, getActivity().getApplicationContext());

		mTracker.trackPageView(TrackingManager.VIEW_HISTORY);
	}
	
	@Override
	public void onDestroyView() {
		UserDataManager.instance.removeUserDataListener(this);
		mTracker.stopSession();
		super.onDestroyView();
	}

	@Override
	public void onUserDataUpdated() {
		setListAdapter(null);
		int userState = UserDataManager.instance.getState();
		if ( userState == UserDataManager.LOGGED_IN) {
			setListAdapter(new HistoryAdapter(getActivity()));
			getListView().setOnScrollListener(new HistoryEndlessListener());
			setEmptyText("Noch keine Einträge in der History");
		} else if ( userState == UserDataManager.LOGIN_PENDING ) {
			setEmptyText("Wird geladen...");
		} else {
			setEmptyText("Anmelden um hier History Einträge zu sehen");
		}
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		//FIXME: This is not fired, moved logic to adapter
		
		List<ImmopolyHistory> history = ImmopolyUser.getInstance().getHistory();
		ImmopolyHistory entry = history.get(position);
		
		if(entry.getOtherUsername() != null){
			DialogFragment newFragment = SimpleUserFragment.newInstance(entry.getOtherUsername());
			newFragment.show(getFragmentManager(), "dialog");
			mTracker.trackEvent(TrackingManager.CATEGORY_CLICKS, TrackingManager.ACTION_OTHER_PROFILE,
					TrackingManager.LABEL_EXPOSE_MAP, 0);
		}else{
			long fid = entry.getExposeId();
			
			// get flat object from users portfolio or create empty Flat object with just an id
			Flats userFlats = ImmopolyUser.getInstance().getPortfolio();
			Flat flat = null;
			for ( Flat f : userFlats ) {
				if ( f.uid == fid ) {
					flat = f;
					break;
				}
			}
			if ( flat == null ) {
				flat = new Flat();
				flat.uid = (int) fid;
			}
			
			DialogFragment newFragment = ExposeFragment.newInstance(flat);
			newFragment.show(getActivity().getSupportFragmentManager(), "dialog");
		}
	}

	@Override
	public void onMoreData() {
		((HistoryAdapter)getListAdapter()).notifyDataSetChanged();
		indicateProgress(false);
		if(mPaginationValues.hasMoreData()){
			mPaginationValues.setLoading(false);
		}
	}
	
	class HistoryEndlessListener implements OnScrollListener{

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {	
			if (ImmopolyUser.getInstance().getHistory().size() >= HistoryPaginationValues.HISTORY_DEFAULT_RESPONSE_SIZE && !mPaginationValues.isLoading() && (totalItemCount - (firstVisibleItem+visibleItemCount)) <= 5) {
				indicateProgress(true);
				mPaginationValues.setLoading(true);
				UserDataManager.instance.loadMoreHistory(mPaginationValues);
			}
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {}
	}

	@Override
	public void indicateProgress(boolean progress) {
		if(this.progress != null)
			this.progress.setVisibility(progress ? View.VISIBLE : View.GONE);
	}
}