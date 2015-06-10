package edu.csh.cshwebnews.database;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;


public class WebNewsProvider extends ContentProvider {

    private static final UriMatcher mUriMatcher = buildUriMatcher();
    private WebNewsDbHelper mDbHelper;

    static final int NEWSGROUPS = 10;
    static final int POSTS = 20;
    static final int USER = 40;

    private static final SQLiteQueryBuilder mQueryBuilder;

    static {
        mQueryBuilder = new SQLiteQueryBuilder();

        mQueryBuilder.setTables(
                WebNewsContract.UserEntry.TABLE_NAME +", "
                        + WebNewsContract.NewsGroupEntry.TABLE_NAME + ", "
                        + WebNewsContract.UserEntry.TABLE_NAME + ", "
                        + WebNewsContract.PostEntry.TABLE_NAME);
    }


    @Override
    public boolean onCreate() {
        mDbHelper = new WebNewsDbHelper(getContext());
        return true;
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = WebNewsContract.CONTENT_AUTHORITY;

        matcher.addURI(authority,WebNewsContract.PATH_NEWSGROUPS,NEWSGROUPS);
        matcher.addURI(authority,WebNewsContract.PATH_POSTS, POSTS);
        matcher.addURI(authority,WebNewsContract.PATH_USER,USER);
        return matcher;
    }

    @Override
    public String getType(Uri uri) {
        final int match = mUriMatcher.match(uri);
        switch (match) {
            case NEWSGROUPS:
                return WebNewsContract.NewsGroupEntry.CONTENT_TYPE;
            case POSTS:
                return WebNewsContract.PostEntry.CONTENT_TYPE;
            case USER:
                return WebNewsContract.UserEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);

        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        final int match = mUriMatcher.match(uri);
        switch (match) {
            case NEWSGROUPS:
                cursor = mDbHelper.getReadableDatabase().query(
                        WebNewsContract.NewsGroupEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case POSTS:
                cursor = mDbHelper.getReadableDatabase().query(
                        WebNewsContract.PostEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case USER:
                cursor = mDbHelper.getReadableDatabase().query(
                        WebNewsContract.UserEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);

        }

        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Uri finalUri;
        final int match = mUriMatcher.match(uri);
        switch(match) {
            case NEWSGROUPS: {
                long id = db.insert(WebNewsContract.NewsGroupEntry.TABLE_NAME,null,values);
                if(id > 0)
                    finalUri = WebNewsContract.NewsGroupEntry.buildNewsGroupUri(id);
                else
                    throw new android.database.SQLException("Failed to insert row into: " + uri);
                break;
            }
            case POSTS: {
                long id = db.insert(WebNewsContract.PostEntry.TABLE_NAME,null,values);
                if(id > 0)
                    finalUri = WebNewsContract.PostEntry.buildPostsUri(id);
                else
                    throw new android.database.SQLException("Failed to insert row into: " + uri);
                break;
            }
            case USER: {
                long id = db.insert(WebNewsContract.UserEntry.TABLE_NAME,null,values);
                if(id > 0)
                    finalUri = WebNewsContract.UserEntry.buildUserUri(id);
                else
                    throw new android.database.SQLException("Failed to insert row into: " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return finalUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsDeleted;
        if(null == selection) selection = "1";
        final int match = mUriMatcher.match(uri);
        switch (match) {
            case NEWSGROUPS:
                rowsDeleted = db.delete(WebNewsContract.NewsGroupEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case POSTS:
                rowsDeleted = db.delete(WebNewsContract.PostEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case USER:
                rowsDeleted = db.delete(WebNewsContract.UserEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }

        if(rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsUpdated;
        final int match = mUriMatcher.match(uri);
        switch (match) {
            case NEWSGROUPS:
                rowsUpdated = db.update(WebNewsContract.NewsGroupEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case POSTS:
                rowsUpdated = db.update(WebNewsContract.PostEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case USER:
                rowsUpdated = db.update(WebNewsContract.UserEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }

        if(rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return rowsUpdated;
    }


    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = mUriMatcher.match(uri);
        switch (match) {
            case NEWSGROUPS: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for(int x = 0; x<values.length; x++) {
                        long id = db.insert(WebNewsContract.NewsGroupEntry.TABLE_NAME,
                                null,
                                values[x]);
                        if(id != -1) returnCount++;
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri,null);
                return returnCount;
            }
            case POSTS: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for(int x = 0; x<values.length; x++) {
                        long id = db.insert(WebNewsContract.PostEntry.TABLE_NAME,
                                null,
                                values[x]);
                        if(id != -1) returnCount++;
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri,null);
                return returnCount;
            }
            default:
                return super.bulkInsert(uri,values);
        }
    }

    //Used for the testing framework
    @Override
    @TargetApi(11)
    public void shutdown() {
        mDbHelper.close();
        super.shutdown();
    }


}
