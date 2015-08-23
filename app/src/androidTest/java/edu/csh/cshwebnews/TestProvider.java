package edu.csh.cshwebnews;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.UUID;

import edu.csh.cshwebnews.database.WebNewsContract;
import edu.csh.cshwebnews.database.WebNewsDbHelper;
import edu.csh.cshwebnews.database.WebNewsProvider;

/**
 * Tests for the content provider
 */
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();


    /**
     * Deletes all records using the WebNews ContentProvider
     */
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                WebNewsContract.NewsGroupEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                WebNewsContract.PostEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                WebNewsContract.UserEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                WebNewsContract.NewsGroupEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from the newsgroups table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                WebNewsContract.PostEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from the posts table during delete", 0, cursor.getCount());
        cursor.close();


        cursor = mContext.getContentResolver().query(
                WebNewsContract.UserEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from the user table during delete", 0, cursor.getCount());
        cursor.close();
    }


    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }


    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                WebNewsProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            assertEquals("Error: WebNewsProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + WebNewsContract.CONTENT_AUTHORITY,
                    providerInfo.authority, WebNewsContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // Provider isn't registered correctly
            assertTrue("Error: WebNewsProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    /**
     * Verifies that the ContentProvider returns the correct type for each type of URI that it can handle.
     */
    public void testGetType() {
        // content://edu.csh.cshwebnews/newsgroups/
        String type = mContext.getContentResolver().getType(WebNewsContract.NewsGroupEntry.CONTENT_URI);

        assertEquals("Error: the NewsGroupEntry CONTENT_URI should return NewsGroupEntry.CONTENT_TYPE",
                WebNewsContract.NewsGroupEntry.CONTENT_TYPE, type);

        // content://edu.csh.cshwebnews/posts/
        type = mContext.getContentResolver().getType(WebNewsContract.PostEntry.CONTENT_URI);

        assertEquals("Error: the PostEntry CONTENT_URI should return PostEntry.CONTENT_TYPE",
                WebNewsContract.PostEntry.CONTENT_TYPE, type);

        // content://edu.csh.cshwebnews/user/
        type = mContext.getContentResolver().getType(WebNewsContract.UserEntry.CONTENT_URI);

        assertEquals("Error: the UserEntry CONTENT_URI should return UserEntry.CONTENT_TYPE",
                WebNewsContract.UserEntry.CONTENT_TYPE, type);
    }



    /**
     * Tests querying using the ContentProvider by directly inserting data into the database then
     * querying them with the ContentProvider
     */
    public void testBasicUserQuery() {
        WebNewsDbHelper dbHelper = new WebNewsDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createUserValues();
        long userRowId = db.insert(WebNewsContract.UserEntry.TABLE_NAME, null, testValues);
        assertTrue("Unable to insert UserEntry into the Database", userRowId != -1);

        db.close();

        Cursor userCursor = mContext.getContentResolver().query(
                WebNewsContract.UserEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        TestUtilities.validateCursor("testBasicUserQuery", userCursor, testValues);
    }

    /**
     * Tests querying using the ContentProvider by directly inserting data into the database then
     * querying them with the ContentProvider
     */
    public void testBasicNewsGroupQuery() {
        WebNewsDbHelper dbHelper = new WebNewsDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createNewsgroupValues();
        long newsGroupRowId = db.insert(WebNewsContract.NewsGroupEntry.TABLE_NAME, null, testValues);
        assertTrue("Unable to insert NewsGroupEntry into the Database", newsGroupRowId != -1);

        db.close();

        Cursor newsGroupCursor = mContext.getContentResolver().query(
                WebNewsContract.NewsGroupEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        TestUtilities.validateCursor("testBasicNewsGroupQuery", newsGroupCursor, testValues);
    }

    /**
     * Tests querying using the ContentProvider by directly inserting data into the database then
     * querying them with the ContentProvider
     */
    public void testBasicPostQuery() {
        WebNewsDbHelper dbHelper = new WebNewsDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createPostValues();
        long postRowId = db.insert(WebNewsContract.PostEntry.TABLE_NAME, null, testValues);
        assertTrue("Unable to insert PostEntry into the Database", postRowId != -1);

        db.close();

        Cursor postCursor = mContext.getContentResolver().query(
                WebNewsContract.PostEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        TestUtilities.validateCursor("testBasicPostQuery", postCursor, testValues);
    }

    /**
     *  Tests updating entries using the ContentProvider
     */
    public void testUpdateUser() {
        // Create a new map of values, where column names are the keys
        ContentValues values = TestUtilities.createUserValues();

        Uri userUri = mContext.getContentResolver().
                insert(WebNewsContract.UserEntry.CONTENT_URI, values);
        long userRowId = ContentUris.parseId(userUri);

        assertTrue(userRowId != -1);
        Log.d(LOG_TAG, "New row id: " + userRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(WebNewsContract.UserEntry._ID, userRowId);
        updatedValues.put(WebNewsContract.UserEntry.DISPLAY_NAME, "cooldisplayname");
        updatedValues.put(WebNewsContract.UserEntry.EMAIL,"cool@email.com");

        // Create a cursor with observer to make sure that the content provider is notifying
        // the observers
        Cursor userCursor = mContext.getContentResolver().query(WebNewsContract.UserEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        userCursor.registerContentObserver(tco);

        int count = mContext.getContentResolver().update(
                WebNewsContract.UserEntry.CONTENT_URI, updatedValues, WebNewsContract.UserEntry._ID + "= ?",
                new String[] { Long.toString(userRowId)});
        assertEquals(count, 1);

        // Test to make sure the observer is called.
        tco.waitForNotificationOrFail();

        userCursor.unregisterContentObserver(tco);
        userCursor.close();

        Cursor cursor = mContext.getContentResolver().query(
                WebNewsContract.UserEntry.CONTENT_URI,
                null,   // projection
                WebNewsContract.UserEntry._ID + " = " + userRowId,
                null,   // Values for the "where" clause
                null    // sort order
        );

        TestUtilities.validateCursor("testUpdateUser.  Error validating user entry update.",
                cursor, updatedValues);

        cursor.close();
    }

    /**
     *  Tests updating entries using the ContentProvider
     */
    public void testUpdateNewsGroup() {
        // Create a new map of values, where column names are the keys
        ContentValues values = TestUtilities.createNewsgroupValues();

        Uri newsGroupUri = mContext.getContentResolver().
                insert(WebNewsContract.NewsGroupEntry.CONTENT_URI, values);
        long newsGroupRowId = ContentUris.parseId(newsGroupUri);

        // Verify we got a row back.
        assertTrue(newsGroupRowId != -1);
        Log.d(LOG_TAG, "New row id: " + newsGroupRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(WebNewsContract.NewsGroupEntry.MAX_UNREAD_LEVEL,2);
        updatedValues.put(WebNewsContract.NewsGroupEntry.UNREAD_COUNT,9);

        // Create a cursor with observer to make sure that the content provider is notifying
        // the observers
        Cursor userCursor = mContext.getContentResolver().query(WebNewsContract.NewsGroupEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        userCursor.registerContentObserver(tco);

        int count = mContext.getContentResolver().update(
                WebNewsContract.NewsGroupEntry.CONTENT_URI, updatedValues, WebNewsContract.NewsGroupEntry._ID + "= ?",
                new String[] { Long.toString(newsGroupRowId)});
        assertEquals(count, 1);

        // Test to make sure our observer is called.
        tco.waitForNotificationOrFail();

        userCursor.unregisterContentObserver(tco);
        userCursor.close();

        Cursor cursor = mContext.getContentResolver().query(
                WebNewsContract.NewsGroupEntry.CONTENT_URI,
                null,   // projection
                WebNewsContract.UserEntry._ID + " = " + newsGroupRowId,
                null,   // Values for the "where" clause
                null    // sort order
        );

        TestUtilities.validateCursor("testUpdateNewsGroup.  Error validating newsgroup entry update.",
                cursor, updatedValues);

        cursor.close();
    }

    /**
     * Tests deleting entries using the ContentProvider
     */
    public void testDeleteRecords() {
        testInsertReadProvider();

        // Register a content observer for the user delete.
        TestUtilities.TestContentObserver userObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(WebNewsContract.UserEntry.CONTENT_URI, true, userObserver);

        // Register a content observer for the post delete.
        TestUtilities.TestContentObserver postObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(WebNewsContract.PostEntry.CONTENT_URI, true, postObserver);

        deleteAllRecordsFromProvider();

        userObserver.waitForNotificationOrFail();
        postObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(userObserver);
        mContext.getContentResolver().unregisterContentObserver(postObserver);
    }

    public void testInsertReadProvider() {
        ContentValues testValues = TestUtilities.createUserValues();

        // Register a content observer for the insert
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(WebNewsContract.UserEntry.CONTENT_URI, true, tco);
        Uri userUri = mContext.getContentResolver().insert(WebNewsContract.UserEntry.CONTENT_URI, testValues);

        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long userRowId = ContentUris.parseId(userUri);

        // Verify we got a row back.
        assertTrue(userRowId != -1);

        Cursor cursor = mContext.getContentResolver().query(
                WebNewsContract.UserEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating UserEntry.",
                cursor, testValues);

        insertReaderProviderPostEntry();
    }

    public void insertReaderProviderPostEntry() {
        ContentValues testValues = TestUtilities.createPostValues();

        // Register a content observer for our insert
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(WebNewsContract.PostEntry.CONTENT_URI, true, tco);
        Uri userUri = mContext.getContentResolver().insert(WebNewsContract.PostEntry.CONTENT_URI, testValues);

        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long postRowId = ContentUris.parseId(userUri);

        // Verify we got a row back.
        assertTrue(postRowId != -1);

        Cursor cursor = mContext.getContentResolver().query(
                WebNewsContract.PostEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating UserEntry.",
                cursor, testValues);
    }

    /**
     * Tests bulk inserts using the ContentProvider
     */
    public void testBulkInsertPosts() {
        ContentValues[] bulkInsertContentValues = createBulkInsertPostValues();

        // Register a content observer for the bulk insert.
        TestUtilities.TestContentObserver postObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(WebNewsContract.PostEntry.CONTENT_URI, true, postObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(WebNewsContract.PostEntry.CONTENT_URI, bulkInsertContentValues);

        postObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(postObserver);

        assertEquals(insertCount, BULK_INSERT_POSTS_TO_INSERT);

        Cursor cursor = mContext.getContentResolver().query(
                WebNewsContract.PostEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                WebNewsContract.PostEntry._ID + " ASC"  // sort order == by _ID ASCENDING
        );

        //Checks if the amount of entries is the amount we wanted to insert
        assertEquals(cursor.getCount(), BULK_INSERT_POSTS_TO_INSERT);

        //Checks if each entry is correct
        cursor.moveToFirst();
        for ( int i = 0; i < BULK_INSERT_POSTS_TO_INSERT; i++, cursor.moveToNext() ) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating PostEntry " + i,
                    cursor, bulkInsertContentValues[i]);
        }
        cursor.close();
    }

    /**
     * Tests bulk inserts using the ContentProvider
     */
    public void testBulkInsertNewsGroups() {
        ContentValues[] bulkInsertContentValues = createBulkInsertNewsGroupValues();

        // Register a content observer for the bulk insert.
        TestUtilities.TestContentObserver newsGroupObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(WebNewsContract.NewsGroupEntry.CONTENT_URI, true, newsGroupObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(WebNewsContract.NewsGroupEntry.CONTENT_URI, bulkInsertContentValues);

        newsGroupObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(newsGroupObserver);

        assertEquals(insertCount, BULK_INSERT_NEWSGROUPS_TO_INSERT);

        Cursor cursor = mContext.getContentResolver().query(
                WebNewsContract.NewsGroupEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                WebNewsContract.NewsGroupEntry._ID + " ASC"  // sort order == by _ID ASCENDING
        );

        //Checks if the amount of entries is the amount we wanted to insert
        assertEquals(cursor.getCount(), BULK_INSERT_NEWSGROUPS_TO_INSERT);

        //Checks if each entry is correct
        cursor.moveToFirst();
        for ( int i = 0; i < BULK_INSERT_NEWSGROUPS_TO_INSERT; i++, cursor.moveToNext() ) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating NewsGroupEntry " + i,
                    cursor, bulkInsertContentValues[i]);
        }
        cursor.close();
    }


    static private final int BULK_INSERT_POSTS_TO_INSERT = 100;
    static ContentValues[] createBulkInsertPostValues() {
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_POSTS_TO_INSERT];

        for ( int i = 0; i < BULK_INSERT_POSTS_TO_INSERT; i++) {
            ContentValues testValues = new ContentValues();
            testValues.put(WebNewsContract.PostEntry._ID, i);
            testValues.put(WebNewsContract.PostEntry.ANCESTOR_IDS, "[" + UUID.randomUUID().toString() +"]");
            testValues.put(WebNewsContract.PostEntry.BODY, UUID.randomUUID().toString());
            testValues.put(WebNewsContract.PostEntry.CREATED_AT, UUID.randomUUID().toString());
            testValues.put(WebNewsContract.PostEntry.FOLLOWUP_NEWSGROUP_ID, UUID.randomUUID().toString());
            testValues.put(WebNewsContract.PostEntry.HEADERS, UUID.randomUUID().toString());
            testValues.put(WebNewsContract.PostEntry.IS_DETHREADED, "false");
            testValues.put(WebNewsContract.PostEntry.IS_STARRED, "false");
            testValues.put(WebNewsContract.PostEntry.HAD_ATTACHMENTS, "false");
            testValues.put(WebNewsContract.PostEntry.NEWSGROUP_IDS, "[" + UUID.randomUUID().toString()+  "]");
            testValues.put(WebNewsContract.PostEntry.PERSONAL_LEVEL, 0);
            testValues.put(WebNewsContract.PostEntry.IS_STICKIED, 1);
            testValues.put(WebNewsContract.PostEntry.TOTAL_STARS, 2);
            testValues.put(WebNewsContract.PostEntry.SUBJECT, UUID.randomUUID().toString());
            testValues.put(WebNewsContract.PostEntry.UNREAD_CLASS, UUID.randomUUID().toString());
            testValues.put(WebNewsContract.PostEntry.AUTHOR_EMAIL,UUID.randomUUID().toString());
            testValues.put(WebNewsContract.PostEntry.AUTHOR_NAME,UUID.randomUUID().toString());
            returnContentValues[i] = testValues;
        }

        return returnContentValues;
    }

    static private final int BULK_INSERT_NEWSGROUPS_TO_INSERT = 35;
    static ContentValues[] createBulkInsertNewsGroupValues() {
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_NEWSGROUPS_TO_INSERT];

        for(int i = 0; i < BULK_INSERT_NEWSGROUPS_TO_INSERT; i++) {
            ContentValues testValues = new ContentValues();
            testValues.put(WebNewsContract.NewsGroupEntry._ID, i);
            testValues.put(WebNewsContract.NewsGroupEntry.DESCRIPTION, UUID.randomUUID().toString());
            testValues.put(WebNewsContract.NewsGroupEntry.POSTING_ALLOWED, "true");
            testValues.put(WebNewsContract.NewsGroupEntry.UNREAD_COUNT, 2);
            testValues.put(WebNewsContract.NewsGroupEntry.MAX_UNREAD_LEVEL, 2);
            testValues.put(WebNewsContract.NewsGroupEntry.NEWEST_POST_AT, "2014-11-07T13:30:31-05:00");
            testValues.put(WebNewsContract.NewsGroupEntry.OLDEST_POST_AT, "2009-11-16T13:30:32-05:00");
            returnContentValues[i] = testValues;
        }

        return returnContentValues;
    }

}
