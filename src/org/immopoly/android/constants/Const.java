package org.immopoly.android.constants;

public class Const {

	public static final String LOG_TAG = "IMPO";

	// Intents

	// Intent open webview
	public static final String EXPOSE_ID = "exposeID";
	public static final String EXPOSE_NAME = "exposeName";
	public static final String EXPOSE_DESC = "exposeDescription";
	public static final String EXPOSE_URL = "exposeURL";
	public static final String EXPOSE_IN_PORTOFOLIO = "exposeInPortfolio";
	
//	public static final String EXPOSE_OWNED = "exposeOwned";

	// Intent add portofolio
	public static final String EXPOSE_ADD_PORTIFOLIO = "addToPortifolio";
	public static final String EXPOSE_RELEASE_PORTIFOLIO = "releaseFromPortifolio";

	public static final String SOURCE = "source";

	// immopoly
	public static final String EXPOSE_PICTURE_SMALL = "exposeDescription";

	public static final String SHARED_PREF_EXPOSE_WEBVIEW = "exposeWebView";
	public static final String KEY_VISITED = "visited";

	public static final String AUTH_URL = "oauth_url";
	public static final long EXPOSE_THRESHOLD_OLD = 1000L * 60L * 60L * 24L
			* 30L;
	public static final long EXPOSE_THRESHOLD_NEW = 1000L * 60L * 60L * 24L
			* 7L;

	public static final String MESSAGE_IMMOPOLY_EXCEPTION = "org.immopoly.common.ImmopolyException";

	public static final String IMMOPOLY_EMAIL = "immopolyteam@gmail.com";
	public static final int ANALYTICS_INTERVAL = 20;

	public static final int USER_SIGNUP = 110;

	// IS24 search control - search different radii until at least SEARCH_MIN_RESULTS flats are found  
	public static final float[] SEARCH_RADII 	   = { 1, 3, 10 }; // in km
	public static final int     SEARCH_MIN_RESULTS = 50;		   // ignored for last search radius
	public static final int     SEARCH_MAX_RESULTS = 80;		   // forced result limit. fernglas me, baby!
}
