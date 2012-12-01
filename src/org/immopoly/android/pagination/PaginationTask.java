package org.immopoly.android.pagination;

import android.os.AsyncTask;

/**
 * AsyncTask to load pages of data.	 
 * @author tobi
 */
public abstract class PaginationTask extends AsyncTask<Void, Void, Integer> {

	protected PaginationValues mPagination;
	private UserPaginationDataListener mListener;
	
	public PaginationTask(PaginationValues pagination, UserPaginationDataListener listener) {
		mPagination = pagination;
		mListener = listener;
	}
	
	@Override
	protected Integer doInBackground(Void... params) {
		return null;
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		mPagination.appendToOffset(result);
		if(mListener != null)
			mListener.onMoreData();
	}

}
