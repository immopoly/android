package org.immopoly.android.constants;

public class Const {

	// Intents

	// Intent open webview
	public static final String EXPOSE_ID = "exposeID";
	public static final String EXPOSE_NAME = "exposeName";
	public static final String EXPOSE_DESC = "exposeDescription";
	public static final String EXPOSE_URL = "exposeURL";
	public static final String EXPOSE_IN_PORTOFOLIO = "exposeInPortfolio";

	// Intent add portofolio
	public static final String EXPOSE_ADD_PORTIFOLIO = "addToPortifolio";

	public static final String SOURCE = "source";

	// immopolly
	public static final String EXPOSE_PICTURE_SMALL = "exposeDescription";

	public static final String SHARED_PREF_EXPOSE_WEBVIEW = "exposeWebView";
	public static final String KEY_VISITED = "visited";

	public static final String AUTH_URL = "oauth_url";
	public static final long EXPOSE_THRESHOLD_OLD = 1000L * 60L * 60L * 24L
			* 30L;
	public static final long EXPOSE_THRESHOLD_NEW = 1000L * 60L * 60L * 24L
			* 7L;

	// background colors by flat 'state' for map_marker_popup
	public static final int OWNED_FLAT_BACKGROUND_COLOR = 0xFF255788;
	public static final int OLD_FLAT_BACKGROUND_COLOR = 0xFF5B605A;
	public static final int NEW_FLAT_BACKGROUND_COLOR = 0xFF3A882D;
	public static final int NORMAL_FLAT_BACKGROUND_COLOR = 0xFFFFBC38;

	public static final String MESSAGE_IMMOPOLY_EXCEPTION = "org.immopoly.common.ImmopolyException";
}
