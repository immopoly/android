/*
 * This is the Android component of Immopoly
 * http://immopoly.appspot.com
 * Copyright (C) 2011 Tobias Sasse
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */

package org.immopoly.android.provider;

import java.net.MalformedURLException;
import java.net.URL;

import org.immopoly.android.helper.WebHelper;
import org.immopoly.android.model.OAuthData;
import org.immopoly.android.model.Region;
import org.immopoly.android.model.Regions;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

public class RegionProvider extends ContentProvider {

	private static final int REGION = 100;

	public static final String REGION_SEARCH_API = "region";

	public static final String CONTENT_AUTHORITY = "org.immopoly.android.provider.region_provider";

	private static final UriMatcher sUriMatcher = buildUriMatcher();

	public static final int QUERY_LIMIT = 20;
	public static final int SEARCH_THRESHOLD = 1;

	public static final Uri CONTENT_URI = Uri.parse("content://"
			+ CONTENT_AUTHORITY + "/region/"
			+ SearchManager.SUGGEST_URI_PATH_QUERY);

	public static final String[] CURSOR_COLUMS = new String[] {
			RegionColumns.REGION_ID, SearchManager.SUGGEST_COLUMN_TEXT_1,
			SearchManager.SUGGEST_COLUMN_TEXT_2,
			SearchManager.SUGGEST_COLUMN_INTENT_DATA, };

	public interface RegionColumns {
		/** Unique string identifying this block of time. */
		String REGION_ID = "_id";
		/** Title describing this block of time. */
		String REGION_NAME = "region_name";

	}

	/**
	 * Build and return a {@link UriMatcher} that catches all {@link Uri}
	 * variations supported by this {@link ContentProvider}.
	 */
	private static UriMatcher buildUriMatcher() {
		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		// matcher.addURI(CONTENT_AUTHORITY, "blocks/between/*/*",
		// BLOCKS_BETWEEN);
		// matcher.addURI(CONTENT_AUTHORITY, "blocks/*", BLOCKS_ID);
		// matcher.addURI(CONTENT_AUTHORITY, "blocks/*/sessions",
		// BLOCKS_ID_SESSIONS);

		matcher.addURI(CONTENT_AUTHORITY, "region/"
				+ SearchManager.SUGGEST_URI_PATH_QUERY, REGION);

		return matcher;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case REGION:
			return REGION_SEARCH_API;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		switch (sUriMatcher.match(uri)) {
		case REGION:
			if (selectionArgs != null) {
				if (selectionArgs[0].length() > SEARCH_THRESHOLD) {
					MatrixCursor dataCursor = new MatrixCursor(CURSOR_COLUMS);

					JSONObject obj = getRegionArray(selectionArgs[0]);
					Regions.parseJSON(obj);
					int i = 1;
					for (Region r : Regions.getData()) {
						dataCursor.addRow(new Object[] { i, r.name,
								"Flats:" + r.amount, r.name });
						i++;
					}
					return dataCursor;
				}
			}
		}
		return null;
	}

	private JSONObject getRegionArray(String search) {
		try {
			StringBuilder sb = new StringBuilder();

			URL url = new URL(sb.append(OAuthData.SERVER).append(
					OAuthData.SEARCH_PREFIX).append("region.json?q=").append(
					search).toString());
			JSONObject obj;

			obj = WebHelper.getHttpData(url, true, getContext());
			return obj;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
