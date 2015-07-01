package edu.csh.cshwebnews.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WebNewsDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "webnews.db";

    public WebNewsDbHelper(Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_USER_TABLE = "CREATE TABLE " + WebNewsContract.UserEntry.TABLE_NAME
                + " (" + WebNewsContract.UserEntry._ID + " INTEGER, " +
                WebNewsContract.UserEntry.USERNAME + " TEXT, " +
                WebNewsContract.UserEntry.DISPLAY_NAME + " TEXT, " +
                WebNewsContract.UserEntry.EMAIL + " TEXT, " +
                WebNewsContract.UserEntry.CREATED_AT + " TEXT, " +
                WebNewsContract.UserEntry.IS_ADMIN + " TEXT);";

        final String SQL_CREATE_NEWSGROUP_TABLE = "CREATE TABLE " + WebNewsContract.NewsGroupEntry.TABLE_NAME
                + " ("+ WebNewsContract.NewsGroupEntry._ID + " INTEGER PRIMARY KEY ON CONFLICT REPLACE," +
                WebNewsContract.NewsGroupEntry.DESCRIPTION + " TEXT, " +
                WebNewsContract.NewsGroupEntry.MAX_UNREAD_LEVEL + " INTEGER, " +
                WebNewsContract.NewsGroupEntry.NAME + " TEXT, " +
                WebNewsContract.NewsGroupEntry.NEWEST_POST_AT + " TEXT, " +
                WebNewsContract.NewsGroupEntry.OLDEST_POST_AT + " TEXT, " +
                WebNewsContract.NewsGroupEntry.POSTING_ALLOWED + " INTEGER, " +
                WebNewsContract.NewsGroupEntry.UNREAD_COUNT + " INTEGER);";

        final String SQL_CREATE_POST_TABLE = "CREATE TABLE " + WebNewsContract.PostEntry.TABLE_NAME + "(" +
                WebNewsContract.PostEntry._ID + " INTEGER PRIMARY KEY ON CONFLICT REPLACE," +
                WebNewsContract.PostEntry.ANCESTOR_IDS + " TEXT, " +
                WebNewsContract.PostEntry.BODY + " TEXT NOT NULL, " +
                WebNewsContract.PostEntry.CREATED_AT + " TEXT NOT NULL, " +
                WebNewsContract.PostEntry.FOLLOWUP_NEWSGROUP_ID + " INTEGER, " +
                WebNewsContract.PostEntry.HAD_ATTACHMENTS + " TEXT NOT NULL, " +
                WebNewsContract.PostEntry.HEADERS + " TEXT NOT NULL, " +
                WebNewsContract.PostEntry.IS_DETHREADED + " TEXT NOT NULL, " +
                WebNewsContract.PostEntry.IS_STARRED + " INTEGER NOT NULL, " +
                WebNewsContract.PostEntry.MESSAGE_ID + " TEXT NOT NULL, " +
                WebNewsContract.PostEntry.PERSONAL_LEVEL + " INTEGER NOT NULL, " +
                WebNewsContract.PostEntry.IS_STICKIED + " INTEGER NOT NULL, " +
                WebNewsContract.PostEntry.SUBJECT + " TEXT NOT NULL, " +
                WebNewsContract.PostEntry.NEWSGROUP_IDS + " TEXT NOT NULL, " +
                WebNewsContract.PostEntry.TOTAL_STARS + " INTEGER NOT NULL, " +
                WebNewsContract.PostEntry.CHILD_IDS + " TEXT, " +
                WebNewsContract.PostEntry.DESCENDANT_IDS + " TEXT, " +
                WebNewsContract.PostEntry.AUTHOR_NAME + " TEXT, " +
                WebNewsContract.PostEntry.AUTHOR_EMAIL + " TEXT, " +
                WebNewsContract.PostEntry.UNREAD_CLASS + " String);";


        db.execSQL(SQL_CREATE_USER_TABLE);
        db.execSQL(SQL_CREATE_NEWSGROUP_TABLE);
        db.execSQL(SQL_CREATE_POST_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + WebNewsContract.NewsGroupEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + WebNewsContract.PostEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + WebNewsContract.UserEntry.TABLE_NAME);
        onCreate(db);
    }
}
