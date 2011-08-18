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

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class FlatsProvider extends ContentProvider {

	private static final String TAG = "FlatsProvider";

	private static final String DATABASE_NAME = "flats.db";

	private static final int DATABASE_VERSION = 1;

	private static final String FLATS_TABLE_NAME = "flats";

	public static final String AUTHORITY = "org.immopoly.android.provider.flats_provider";

	public static final Uri CONTENT_URI = Uri.parse("content://"
			+ FlatsProvider.AUTHORITY + "/flats");

	private static final int FLATS = 1;

	private static HashMap<String, String> flatsProjectionMap;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db
					.execSQL("CREATE TABLE " + FLATS_TABLE_NAME + " ("
							+ Flat.FLAT_ID + " INTEGER PRIMARY KEY,"
							+ Flat.FLAT_NAME + " VARCHAR(255),"
							+ Flat.FLAT_DESCRIPTION + " LONGTEXT,"
							+ Flat.FLAT_LATITUDE + " REAL,"
							+ Flat.FLAT_LONGITUDE + " REAL"
							+ Flat.FLAT_CREATIONDATE + " INTEGER"
							+ " );");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + FLATS_TABLE_NAME);
			onCreate(db);
		}
	}

	private static final UriMatcher sUriMatcher;

	public interface Flat {
		String FLAT_ID = "_id";
		String FLAT_NAME = "flat_name";
		String FLAT_DESCRIPTION = "flat_description";
		String FLAT_LATITUDE = "flat_lat";
		String FLAT_LONGITUDE = "flat_lng";
		String FLAT_CREATIONDATE = "flat_creationDate";
	}

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, FLATS_TABLE_NAME, FLATS);

		flatsProjectionMap = new HashMap<String, String>();
		flatsProjectionMap.put(Flat.FLAT_ID, Flat.FLAT_ID);
		flatsProjectionMap.put(Flat.FLAT_NAME, Flat.FLAT_NAME);
		flatsProjectionMap.put(Flat.FLAT_DESCRIPTION, Flat.FLAT_DESCRIPTION);
		flatsProjectionMap.put(Flat.FLAT_LATITUDE, Flat.FLAT_LATITUDE);
		flatsProjectionMap.put(Flat.FLAT_LONGITUDE, Flat.FLAT_LONGITUDE);
		flatsProjectionMap.put(Flat.FLAT_CREATIONDATE, Flat.FLAT_CREATIONDATE);

	}

	private DatabaseHelper dbHelper;

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		getContext().getContentResolver().notifyChange(arg0, null);
		return db.delete(FLATS_TABLE_NAME, arg1, null);
	}

	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (sUriMatcher.match(uri) != FLATS) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		long rowId = db.insert(FLATS_TABLE_NAME, Flat.FLAT_DESCRIPTION, values);
		if (rowId > 0) {
			Uri noteUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(noteUri, null);
			return noteUri;
		} else {
			throw new SQLException("Failed to insert row into " + uri);
		}

		
	}

	@Override
	public boolean onCreate() {
		dbHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		switch (sUriMatcher.match(uri)) {
		case FLATS:
			qb.setTables(FLATS_TABLE_NAME);
			qb.setProjectionMap(flatsProjectionMap);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null,
				null, sortOrder);

		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return 0;
	}

}
