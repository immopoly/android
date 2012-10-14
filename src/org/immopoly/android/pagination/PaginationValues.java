package org.immopoly.android.pagination;

/**
 * Used for PaginationTask to handle loading pages of data.
 * @author tobi
 */
public class PaginationValues {
	protected int offset;
	private boolean moreData;
	private boolean loading;
	
	public PaginationValues() {
		loading = false;
	}
	
	public int getOffset() {
		return offset;
	}
	public void appendOffset(int newOffset) {
		moreData = newOffset > 0 ? true : false;
		this.offset += offset;
	}
	public int getLimit() {
		return 20;
	}
	
	public boolean hasMoreData(){
		return moreData;
	}
	
	public boolean isLoading(){
		return loading;
	}
	
	public void setLoading(boolean isLoading){
		loading = isLoading;
	}
}
