package org.immopoly.android.pagination;

import org.immopoly.android.app.UserDataListener;

/**
 * Listener that handles new data from PaginationTask
 * @author tobi
 */
public interface UserPaginationDataListener extends UserDataListener {
	public void onMoreData();
}
