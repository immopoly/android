package org.immopoly.android.tasks;

import java.net.MalformedURLException;
import java.net.URL;

import org.immopoly.android.helper.WebHelper;
import org.immopoly.android.model.ImmopolyUser;
import org.immopoly.android.pagination.PaginationTask;
import org.immopoly.android.pagination.PaginationValues;
import org.immopoly.android.pagination.UserPaginationDataListener;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;

/**
 * Loads new pages of History Entries.
 * @author tobi
 */
public class LoadMoreHistoryTask extends PaginationTask {

	private Context mContext;
	
	public LoadMoreHistoryTask(Context ctx,PaginationValues pagination, UserPaginationDataListener listener) {
		super(pagination, listener);
		mContext = ctx;
	}
	
	@Override
	protected Integer doInBackground(Void... params) {
		
		JSONArray array = null;
		try {
			array = WebHelper.getHttpArrayData(new URL(
					WebHelper.SERVER_URL_PREFIX + "/user/history?token=" + ImmopolyUser.getInstance().getToken()+"&start="+mPagination.getOffset()+"&end="+(mPagination.getOffset()+mPagination.getLimit())),
					false, mContext);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		try {
			ImmopolyUser.getInstance().appendHistoryEntries(array);
			return array.length();
		} catch (JSONException e) {}
		
		return 0;
	}
}
