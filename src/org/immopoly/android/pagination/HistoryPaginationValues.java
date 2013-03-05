package org.immopoly.android.pagination;

/**
 * PaginationValues for History Entries
 * @author tobi
 */
public class HistoryPaginationValues extends PaginationValues {
	
	/**
	 * Number of History Entries included in UserInfo
	 */
	public final static int HISTORY_DEFAULT_RESPONSE_SIZE = 20;
	
	public HistoryPaginationValues() {
		super();
		offset = HISTORY_DEFAULT_RESPONSE_SIZE;
	}
}
