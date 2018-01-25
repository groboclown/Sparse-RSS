/*
 * Sparse rss
 * 
 * Copyright (c) 2010-2012 Stefan Handschuh
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package net.groboclown.groborss.provider;

import java.io.File;

import net.groboclown.groborss.handler.PictureFilenameFilter;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

public class FeedData {
	public static final String CONTENT = "content://";
	
	public static final String AUTHORITY = "net.groboclown.groborss.provider.FeedData";
	
	static final String TYPE_PRIMARY_KEY = "INTEGER PRIMARY KEY AUTOINCREMENT";
	
	protected static final String TYPE_TEXT = "TEXT";

	static final String TYPE_TEXT_UNIQUE = "TEXT UNIQUE";
	
	protected static final String TYPE_DATETIME = "DATETIME";
	
	protected static final String TYPE_INT = "INT";

	static final String TYPE_INT_7 = "INTEGER(7)";

	static final String TYPE_BLOB = "BLOB";

	protected static final String TYPE_BOOLEAN = "INTEGER(1)";
	
	public static final String FEED_DEFAULTSORTORDER = FeedColumns.PRIORITY;

	static final String TABLE_FEEDS = "feeds";

	static final String TABLE_ENTRIES = "entries";


	static final DbTable DB_TABLE_FEEDS = new FeedColumns();
	static final DbTable DB_TABLE_ENTRIES = new EntryColumns();
	public static final DbTable[] TABLES = { DB_TABLE_FEEDS, DB_TABLE_ENTRIES };


	public static class FeedColumns extends DbTable implements BaseColumns {
		public static final Uri CONTENT_URI = parseUri(CONTENT + AUTHORITY + "/feeds");
		
		public static final String URL = "url";
		
		public static final String NAME = "name";
		
		public static final String OTHER_ALERT_RINGTONE = "other_alertringtone";
		
		public static final String ALERT_RINGTONE = "alertringtone";
		
		public static final String SKIP_ALERT = "skipalert";
		
		public static final String LASTUPDATE = "lastupdate";
		
		public static final String ICON = "icon";
		
		public static final String ERROR = "error";
		
		public static final String PRIORITY = "priority";
		
		public static final String FETCHMODE = "fetchmode";
		
		public static final String REALLASTUPDATE = "reallastupdate";
		
		public static final String WIFIONLY = "wifionly";

		public static final String HOMEPAGE = "homepage";

		public static final String ENTRY_LINK_IMG_PATTERN = "imgpattern";
		
		public static final String[] COLUMNS = new String[] {_ID, URL, NAME, LASTUPDATE, ICON, ERROR, PRIORITY, FETCHMODE, REALLASTUPDATE, ALERT_RINGTONE, OTHER_ALERT_RINGTONE, SKIP_ALERT, WIFIONLY, HOMEPAGE, ENTRY_LINK_IMG_PATTERN};
		
		public static final String[] TYPES = new String[] {TYPE_PRIMARY_KEY, TYPE_TEXT_UNIQUE, TYPE_TEXT, TYPE_DATETIME, TYPE_BLOB, TYPE_TEXT, TYPE_INT, TYPE_INT, TYPE_DATETIME, TYPE_TEXT, TYPE_INT, TYPE_INT, TYPE_BOOLEAN, TYPE_TEXT, TYPE_TEXT};

		private FeedColumns() {
			super(TABLE_FEEDS, COLUMNS, TYPES);
		}

		public static Uri CONTENT_URI(String feedId) {
			return parseUri(CONTENT + AUTHORITY + "/feeds/" + feedId);
		}
		
		public static Uri CONTENT_URI(long feedId) {
			return parseUri(CONTENT + AUTHORITY + "/feeds/" + feedId);
		}
	}
	
	public static class EntryColumns extends DbTable implements BaseColumns {
		public static final String FEED_ID = "feedid";
		
		public static final String TITLE = "title";
		
		public static final String ABSTRACT = "abstract";
		
		public static final String DATE = "date";
		
		public static final String READDATE = "readdate";
		
		public static final String LINK = "link";
		
		public static final String FAVORITE = "favorite";
		
		public static final String ENCLOSURE = "enclosure";
		
		public static final String GUID = "guid";
		
		public static final String AUTHOR = "author";

		public static final String LINK_IMG_URL = "linkimgurl";
		
		public static final String[] COLUMNS = new String[] {_ID, FEED_ID, TITLE, ABSTRACT, DATE, READDATE, LINK, FAVORITE, ENCLOSURE, GUID, AUTHOR, LINK_IMG_URL};
		
		public static final String[] TYPES = new String[] {TYPE_PRIMARY_KEY, TYPE_INT_7, TYPE_TEXT, TYPE_TEXT, TYPE_DATETIME, TYPE_DATETIME, TYPE_TEXT, TYPE_BOOLEAN, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT};

		public static Uri CONTENT_URI = parseUri(CONTENT + AUTHORITY + "/entries");
		
		public static Uri FAVORITES_CONTENT_URI = parseUri(CONTENT + AUTHORITY + "/favorites");

		private EntryColumns() {
			super(TABLE_ENTRIES, COLUMNS, TYPES);
		}

		public static Uri CONTENT_URI(String feedId) {
			return parseUri(CONTENT + AUTHORITY + "/feeds/" + feedId + "/entries");
		}

		public static Uri ENTRY_CONTENT_URI(String entryId) {
			return parseUri(CONTENT + AUTHORITY + "/entries/" + entryId);
		}
		
		public static Uri PARENT_URI(String path) {
			return parseUri(CONTENT + AUTHORITY + path.substring(0, path.lastIndexOf('/')));
		}
		
	}


	public static DbTableFacadeFactory getActivityFactory(@NonNull Activity source) {
		return new DbTableFacadeFactory.ContextFactory(source,
				TABLE_FEEDS, FeedColumns.CONTENT_URI,
				TABLE_ENTRIES, EntryColumns.CONTENT_URI);
	}
		
	private static String[] IDPROJECTION = new String[] {FeedData.EntryColumns._ID};
	
	public static void deletePicturesOfFeedAsync(final Context context, final Uri entriesUri, final String selection) {
		if (FeedDataContentProvider.IMAGEFOLDER_FILE.exists()) {
			new Thread() {
				public void run() {
					deletePicturesOfFeed(context, entriesUri, selection);
				}
			}.start();
		}
	}
	
	public static synchronized void deletePicturesOfFeed(Context context, Uri entriesUri, String selection) {
		if (FeedDataContentProvider.IMAGEFOLDER_FILE.exists()) {
			PictureFilenameFilter filenameFilter = new PictureFilenameFilter();
			
			Cursor cursor = context.getContentResolver().query(entriesUri, IDPROJECTION, selection, null, null);
			
			while (cursor.moveToNext()) {
				filenameFilter.setEntryId(cursor.getString(0));
				
				File[] files = FeedDataContentProvider.IMAGEFOLDER_FILE.listFiles(filenameFilter);
				
				for (int n = 0, i = files != null ? files.length : 0; n < i; n++) {
					files[n].delete();
				}
			}
			cursor.close();
		}
	}
	
	public static synchronized void deletePicturesOfEntry(String entryId) {
		if (FeedDataContentProvider.IMAGEFOLDER_FILE.exists()) {
			PictureFilenameFilter filenameFilter = new PictureFilenameFilter(entryId);
			
			File[] files = FeedDataContentProvider.IMAGEFOLDER_FILE.listFiles(filenameFilter);
			
			for (int n = 0, i = files != null ? files.length : 0; n < i; n++) {
				files[n].delete();
			}
		}
	}
	

	// Unit test compatibility
    // When run in a unit test, the Uri class isn't implemented,
    // so it generates a runtime exception.  Unit tests don't care
    // about this, so just return null.
	private static Uri parseUri(String s) {
		try {
			return Uri.parse(s);
		} catch (RuntimeException e) {
			// ignore
			return null;
		}
	}
}