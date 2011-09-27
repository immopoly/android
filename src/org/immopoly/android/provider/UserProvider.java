package org.immopoly.android.provider;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.immopoly.android.helper.WebHelper;
import org.immopoly.android.model.ImmopolyUser;
import org.immopoly.common.User;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class UserProvider extends ContentProvider {

	public static final String AUTHORITY = "org.immopoly.android.provider.user_provider";

	private static UriMatcher sUriMatcher;

	private static final int USER = 1;
	private static final int USER_MONEY = 2;

	private static final String USER_STRING = "user";
	private static final String USER_BALANCE_STRING = "balance";

	public static final Uri CONTENT_URI_USER = Uri.parse("content://"
			+ UserProvider.AUTHORITY + "/" + USER_STRING);
	public static final Uri CONTENT_URI_USER_BALANCE = Uri.parse("content://"
			+ UserProvider.AUTHORITY + "/" + USER_BALANCE_STRING);

	private static HashMap<String, String> mUserProjectionMap;

	private static final String[] USER_CURSOR_COLUMS = new String[] {
			User.KEY_ID, User.KEY_USERNAME, User.KEY_EMAIL, User.KEY_INFO,
			User.KEY_LAST_PROVISION, User.KEY_LAST_RENT, User.KEY_BALANCE };

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, User.KEY_USERNAME, USER);
		sUriMatcher.addURI(AUTHORITY, User.KEY_BALANCE, USER_MONEY);

		mUserProjectionMap = new HashMap<String, String>();
		mUserProjectionMap.put(User.KEY_ID, User.KEY_ID);
		mUserProjectionMap.put(User.KEY_USERNAME, User.KEY_USERNAME);
		mUserProjectionMap.put(User.KEY_BALANCE, User.KEY_BALANCE);
		mUserProjectionMap
				.put(User.KEY_LAST_PROVISION, User.KEY_LAST_PROVISION);
		mUserProjectionMap.put(User.KEY_LAST_RENT, User.KEY_LAST_RENT);
	}

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Cursor query(Uri arg0, String[] arg1, String arg2, String[] arg3,
			String arg4) {
		Cursor c = buildMatrixCursor(1);
		c.setNotificationUri(getContext().getContentResolver(), arg0);
		return c;
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return 0;
	}

	private Cursor buildMatrixCursor(int type) {
		MatrixCursor matrixCursor;

		// String[] columns = new String[]{
		// User.KEY_ID,User.KEY_USERNAME,User.KEY_BALANCE,User.KEY_LAST_RENT};
		matrixCursor = new MatrixCursor(USER_CURSOR_COLUMS);
		ImmopolyUser user = ImmopolyUser.getInstance();
		if (user == null || user.getUserName() == null
				|| user.getUserName().length() <= 0) {
			JSONObject obj = null;
			try {
				ImmopolyUser.getInstance().readToken(getContext());
				obj = WebHelper.getHttpData(new URL(WebHelper.SERVER_URL_PREFIX
						+ "/user/info?token="
						+ ImmopolyUser.getInstance().getToken()), false,
						getContext());

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (obj == null || obj.has("org.immopoly.common.ImmopolyException")) {
				user = null;
			} else {
				ImmopolyUser.getInstance().fromJSON(obj);
				user = ImmopolyUser.getInstance();

				// getContentResolver().delete(
				// FlatsProvider.CONTENT_URI,
				// org.immopoly.android.provider.FlatsProvider.Flat.FLAT_ID
				// + " > 0", null);
				Cursor cur = getContext().getContentResolver().query(
						FlatsProvider.CONTENT_URI, null, null, null, null);
				if (cur.getCount() > 0) {
					boolean isIn;
					int current;
					cur.moveToFirst();
					do {
						isIn = false;
						current = -1;
						for (int i = 0; i < user.flats.size(); i++) {
							if (cur.getInt(cur
									.getColumnIndex(FlatsProvider.Flat.FLAT_ID)) == user.flats
									.get(i).uid) {
								isIn = true;
								current = i;
								break;
							}
						}
						if (isIn == false) {
							// delete
							deleteFlat(cur
									.getInt(cur
											.getColumnIndex(FlatsProvider.Flat.FLAT_ID)));
						} else if (current != -1) {
							user.flats.remove(current);
						}
					} while (cur.moveToNext());
				}
				for (org.immopoly.android.model.Flat f : user.flats) {
					addFlat(f);
				}
			}
		}
		if (user != null) {
			// User.KEY_ID, User.KEY_USERNAME, User.KEY_EMAIL, User.KEY_INFO,
			// User.KEY_LAST_PROVISION, User.KEY_LAST_RENT, User.KEY_BALANCE
			matrixCursor.addRow(new Object[] { 1, user.getUserName(),
					user.getEmail(), null, user.getLastProvision(),
					user.getLastRent(), user.getBalance() });
		}
		return matrixCursor;
	}

	private void deleteFlat(int id) {
		getContext().getContentResolver().delete(FlatsProvider.CONTENT_URI,
				FlatsProvider.Flat.FLAT_ID + "=" + id, null);
	}

	private void addFlat(org.immopoly.android.model.Flat f) {
		ContentValues values;
		values = new ContentValues();
		values.put(org.immopoly.android.provider.FlatsProvider.Flat.FLAT_ID,
				f.uid);
		values.put(org.immopoly.android.provider.FlatsProvider.Flat.FLAT_NAME,
				f.name);
		values.put(
				org.immopoly.android.provider.FlatsProvider.Flat.FLAT_DESCRIPTION,
				"-");
		values.put(
				org.immopoly.android.provider.FlatsProvider.Flat.FLAT_LATITUDE,
				f.lat);
		values.put(
				org.immopoly.android.provider.FlatsProvider.Flat.FLAT_LONGITUDE,
				f.lng);
		values.put(
				org.immopoly.android.provider.FlatsProvider.Flat.FLAT_CREATIONDATE,
				f.creationDate);
		getContext().getContentResolver().insert(FlatsProvider.CONTENT_URI,
				values);
	}
}
