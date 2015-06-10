package edu.csh.cshwebnews;


import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

import edu.csh.cshwebnews.database.WebNewsContract;
import edu.csh.cshwebnews.database.WebNewsDbHelper;

public class TestUtilities extends AndroidTestCase {

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    static ContentValues createNewsgroupValues() {
        ContentValues testValues = new ContentValues();
        testValues.put(WebNewsContract.NewsGroupEntry._ID, 1);
        testValues.put(WebNewsContract.NewsGroupEntry.NAME, "csh.foo");
        testValues.put(WebNewsContract.NewsGroupEntry.DESCRIPTION, "Foo Discussion");
        testValues.put(WebNewsContract.NewsGroupEntry.POSTING_ALLOWED, "true");
        testValues.put(WebNewsContract.NewsGroupEntry.UNREAD_COUNT, 2);
        testValues.put(WebNewsContract.NewsGroupEntry.MAX_UNREAD_LEVEL, 2);
        testValues.put(WebNewsContract.NewsGroupEntry.NEWEST_POST_AT, "2014-11-07T13:30:31-05:00");
        testValues.put(WebNewsContract.NewsGroupEntry.OLDEST_POST_AT, "2009-11-16T13:30:32-05:00");
        return testValues;
    }

    static long insertNewsgroupValues(Context context) {
        WebNewsDbHelper dbHelper = new WebNewsDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createNewsgroupValues();

        long newsGroupRowId;
        newsGroupRowId = db.insert(WebNewsContract.NewsGroupEntry.TABLE_NAME, null, testValues);

        assertTrue("Error: Failure to insert NewsGroup Values", newsGroupRowId != -1);

        return newsGroupRowId;
    }

    static ContentValues createUserValues() {
        ContentValues testValues = new ContentValues();
        testValues.put(WebNewsContract.UserEntry.DISPLAY_NAME, "CSHer");
        testValues.put(WebNewsContract.UserEntry.CREATED_AT,"2014-11-14T23:48:05-05:00");
        testValues.put(WebNewsContract.UserEntry.IS_ADMIN,"false");
        testValues.put(WebNewsContract.UserEntry.EMAIL,"test@csh.rit.edu");
        testValues.put(WebNewsContract.UserEntry._ID, 1);
        return testValues;
    }

    static long insertUserValues(Context context) {
        WebNewsDbHelper dbHelper = new WebNewsDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createUserValues();

        long userRowId;
        userRowId = db.insert(WebNewsContract.UserEntry.TABLE_NAME, null, testValues);

        assertTrue("Error: Failure to insert User Values", userRowId != -1);

        return userRowId;
    }

    static ContentValues createPostValues() {
        ContentValues testValues = new ContentValues();
        testValues.put(WebNewsContract.PostEntry._ID, 1);
        testValues.put(WebNewsContract.PostEntry.ANCESTOR_IDS, "[]");
        testValues.put(WebNewsContract.PostEntry.BODY, "...post body...");
        testValues.put(WebNewsContract.PostEntry.CREATED_AT, "2014-11-14T23:48:05-05:00");
        testValues.put(WebNewsContract.PostEntry.FOLLOWUP_NEWSGROUP_ID, "NULL");
        testValues.put(WebNewsContract.PostEntry.HEADERS, "...post headers...");
        testValues.put(WebNewsContract.PostEntry.IS_DETHREADED, "false");
        testValues.put(WebNewsContract.PostEntry.IS_STARRED, "false");
        testValues.put(WebNewsContract.PostEntry.HAD_ATTACHMENTS, "false");
        testValues.put(WebNewsContract.PostEntry.MESSAGE_ID, "somethingsomething@asdf.net");
        testValues.put(WebNewsContract.PostEntry.NEWSGROUP_IDS, "[ 1 ]");
        testValues.put(WebNewsContract.PostEntry.PERSONAL_LEVEL, 0);
        testValues.put(WebNewsContract.PostEntry.IS_STICKIED, 1);
        testValues.put(WebNewsContract.PostEntry.TOTAL_STARS, 2);
        testValues.put(WebNewsContract.PostEntry.SUBJECT, "Suscipit est illo a consequuntur");
        testValues.put(WebNewsContract.PostEntry.UNREAD_CLASS, "auto");
        testValues.put(WebNewsContract.PostEntry.AUTHOR_NAME,"CSHUSER");
        testValues.put(WebNewsContract.PostEntry.AUTHOR_EMAIL,"CSHUSER@csh.rit.edu");

        return testValues;
    }

    static long insertPostValues(Context context) {
        WebNewsDbHelper dbHelper = new WebNewsDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createPostValues();

        long postRowId;
        postRowId = db.insert(WebNewsContract.PostEntry.TABLE_NAME, null, testValues);

        assertTrue("Error: Failure to insert Post Values", postRowId != -1);

        return postRowId;
    }



    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }


    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
