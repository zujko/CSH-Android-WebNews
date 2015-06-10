package edu.csh.cshwebnews;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import edu.csh.cshwebnews.database.WebNewsContract;
import edu.csh.cshwebnews.database.WebNewsDbHelper;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(WebNewsDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WebNewsDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() {
        WebNewsDbHelper dbHelper = new WebNewsDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Tests user table
        ContentValues userValues = TestUtilities.createUserValues();
        long userRowId;
        userRowId = db.insert(WebNewsContract.UserEntry.TABLE_NAME, null, userValues);

        // Verify we got a row back.
        assertTrue(userRowId != -1);
        Log.d(LOG_TAG, "userRowId: " + userRowId);

        Cursor userCursor = db.query(
                WebNewsContract.UserEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        TestUtilities.validateCursor("User Cursor Error", userCursor, userValues);

        // Tests newsgroup table
        ContentValues newsgroupValues = TestUtilities.createNewsgroupValues();

        long newsgroupRowId = db.insert(WebNewsContract.NewsGroupEntry.TABLE_NAME, null, newsgroupValues);
        assertTrue(newsgroupRowId != -1);
        Log.d(LOG_TAG, "newsgroupRowId: " + newsgroupRowId);

        Cursor newsgroupCursor = db.query(
                WebNewsContract.NewsGroupEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        TestUtilities.validateCursor("Newsgroup Cursor Error", newsgroupCursor, newsgroupValues);

        // Tests post table
        ContentValues postValues = TestUtilities.createPostValues();

        long postRowId = db.insert(WebNewsContract.PostEntry.TABLE_NAME, null, postValues);
        assertTrue(postRowId != -1);
        Log.d(LOG_TAG, "postRowId: " + postRowId);

        Cursor postCursor = db.query(
                WebNewsContract.PostEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        TestUtilities.validateCursor("Post Cursor Error", postCursor, postValues);

        dbHelper.close();
    }



}