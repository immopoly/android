package org.immopoly.android.fragments;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

import org.immopoly.android.R;
import org.immopoly.android.constants.Const;
import org.immopoly.android.helper.TrackingManager;
import org.immopoly.android.model.ImmopolySimpleUser;
import org.immopoly.android.pagination.ProgressIndicator;
import org.immopoly.android.tasks.GetOtherUserTask;
import org.immopoly.android.widget.BadgesView;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class SimpleUserFragment extends DialogFragment implements ProgressIndicator{
	
	private String userName;
	private final static String EXTRA_USERNAME="user";
	private GoogleAnalyticsTracker mTracker;
	private final static NumberFormat nFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY);
	private ProgressBar progress;
	private ImmopolySimpleUser mUser;
	
	/**
	 * Create a new instance of MyFragment that will be initialized with the
	 * given arguments.
	 */
	public static SimpleUserFragment newInstance(String otherUserName) {
		SimpleUserFragment f = new SimpleUserFragment();
		Bundle b = new Bundle();
		b.putString(EXTRA_USERNAME, otherUserName );
		f.setArguments( b );
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		userName = getArguments().getString(EXTRA_USERNAME);
		// dialog with no title
		setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Dialog);
		
		mTracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker in manual dispatch mode...
		mTracker.startNewSession(TrackingManager.UA_ACCOUNT,
				Const.ANALYTICS_INTERVAL, getActivity().getApplicationContext());

		mTracker.trackPageView(TrackingManager.VIEW_OTHER_PROFILE);
	}
	
	@Override
	public void onViewCreated(View v, Bundle savedInstanceState) {
		super.onViewCreated(v, savedInstanceState);
		progress = (ProgressBar) getView().findViewById(R.id.progress);
		
		if(savedInstanceState != null && (mUser = savedInstanceState.getParcelable("user")) != null)
			displayUserData(mUser);
		else
			new MyGetSimpleUserTask(getActivity()).execute(userName);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.dialog_fragment_simpleuser_profile,container, false);
		((TextView)view.findViewById(R.id.username)).setText(userName);
		return view;
	}
	
	class MyGetSimpleUserTask extends GetOtherUserTask{
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			indicateProgress(true);
		}
		
		public MyGetSimpleUserTask(Context context) {
			super(context);
		}
		
		@Override
		protected void onPostExecute(ImmopolySimpleUser user) {
			super.onPostExecute(user);
			indicateProgress(false);
			if(mException != null)
				dismiss();
			else if(user != null){
				mUser = user;
				
				displayUserData(mUser);
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle arg0) {
		super.onSaveInstanceState(arg0);
		if(mUser != null)
			arg0.putParcelable("user", mUser);
	}
	
	@Override
	public void indicateProgress(boolean isBusy) {
		progress.setVisibility(isBusy ? View.VISIBLE : View.GONE);
	}

	private void displayUserData(ImmopolySimpleUser user) {
		((BadgesView)getView().findViewById(R.id.gridview)).initWithBadges(user.getBadges(), mTracker);
		
		TextView currentCostTextView = (TextView) getView().findViewById(R.id.current_cost);
		if (currentCostTextView != null) {
			nFormat.setMinimumIntegerDigits(1);
			nFormat.setMaximumFractionDigits(0);
			nFormat.setCurrency(Currency.getInstance(Locale.GERMANY));
			
			currentCostTextView.setText(nFormat.format(user.getLastRent()));
		}
		
		String calculatedCosts = nFormat.format((int) (user.getLastRent() * 30));
		TextView calculatedCostTextView = (TextView) getView().findViewById(R.id.calculated_cost);
		if (calculatedCostTextView != null) {
			calculatedCostTextView.setText(calculatedCosts);
		}
		
		String lastProvision = nFormat.format((int) (user.getLastProvision()));
		TextView lastProvisionTextView = (TextView) getView().findViewById(R.id.last_provision);
		if (lastProvisionTextView != null) {
			lastProvisionTextView
			.setText(lastProvision);
		}
		
		TextView balanceCostTextView = (TextView) getView().findViewById(R.id.current_balance);
		if (balanceCostTextView != null) {
			balanceCostTextView.setText(nFormat.format(user.getBalance()));
		}
		
		TextView numFlatsView = (TextView) getView().findViewById( R.id.num_flats );
		numFlatsView.setText( user.getNumExposes() +" / " + user.getMaxExposes() );
	}
}
