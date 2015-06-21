package edu.csh.cshwebnews.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the database.
 */
public class WebNewsContract {

    public static final String CONTENT_AUTHORITY = "edu.csh.cshwebnews";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_NEWSGROUPS = "newsgroups";
    public static final String PATH_POSTS = "posts";
    public static final String PATH_USER = "user";

    public static final String[] POST_COLUMNS = {
            WebNewsContract.PostEntry.TABLE_NAME + "." + WebNewsContract.PostEntry._ID,
            WebNewsContract.PostEntry.BODY,
            WebNewsContract.PostEntry.CREATED_AT,
            WebNewsContract.PostEntry.FOLLOWUP_NEWSGROUP_ID,
            WebNewsContract.PostEntry.HAD_ATTACHMENTS,
            WebNewsContract.PostEntry.HEADERS,
            WebNewsContract.PostEntry.IS_DETHREADED,
            WebNewsContract.PostEntry.IS_STARRED,
            WebNewsContract.PostEntry.MESSAGE_ID,
            WebNewsContract.PostEntry.PERSONAL_LEVEL,
            WebNewsContract.PostEntry.IS_STICKIED,
            WebNewsContract.PostEntry.SUBJECT,
            WebNewsContract.PostEntry.NEWSGROUP_IDS,
            WebNewsContract.PostEntry.TOTAL_STARS,
            WebNewsContract.PostEntry.CHILD_IDS,
            WebNewsContract.PostEntry.DESCENDANT_IDS,
            WebNewsContract.PostEntry.AUTHOR_NAME,
            WebNewsContract.PostEntry.AUTHOR_EMAIL,
            WebNewsContract.PostEntry.UNREAD_CLASS
    };
    public static final int COL_ID = 0;
    public static final int COL_BODY = 1;
    public static final int COL_CREATED_AT = 2;
    public static final int COL_FOLLOWUP_NEWSGROUP_ID = 3;
    public static final int COL_HAD_ATTACHMENTS = 4;
    public static final int COL_HEADERS = 5;
    public static final int COL_IS_DETHREADED = 6;
    public static final int COL_IS_STARRED = 7;
    public static final int COL_MESSAGE_ID = 8;
    public static final int COL_PERSONAL_LEVEL = 9;
    public static final int COL_IS_STICKIED = 10;
    public static final int COL_SUBJECT  = 11;
    public static final int COL_NEWSGROUP_IDS =12;
    public static final int COL_TOTAL_STARS = 13;
    public static final int COL_CHILD_IDS = 14;
    public static final int COL_DESCENDANT_IDS =15;
    public static final int COL_AUTHOR_NAME = 16;
    public static final int COL_AUTHOR_EMAIL = 17;
    public static final int COL_UNREAD_CLASS = 18;


    public static final class NewsGroupEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_NEWSGROUPS).build();


        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NEWSGROUPS;

        public static String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NEWSGROUPS;

        public static final String TABLE_NAME       = "newsgroups";

        public static final String DESCRIPTION      = "description";

        public static final String MAX_UNREAD_LEVEL = "max_unread_level";

        public static final String NAME             = "name";

        public static final String NEWEST_POST_AT   = "newest_post_at";

        public static final String OLDEST_POST_AT   = "oldest_post_at";

        public static final String POSTING_ALLOWED  = "posting_allowed";

        public static final String UNREAD_COUNT     = "unread_count";


        public static Uri buildNewsGroupUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }

    public static final class PostEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_POSTS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POSTS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POSTS;

        public static final String TABLE_NAME       = "posts";

        public static final String ANCESTOR_IDS     = "ancestor_ids";

        public static final String BODY             = "body";

        public static final String CREATED_AT       = "created_at";

        public static final String FOLLOWUP_NEWSGROUP_ID = "followup_newsgroup_id";

        public static final String HAD_ATTACHMENTS  = "had_attachments";

        public static final String HEADERS          = "headers";

        public static final String NEWSGROUP_IDS    = "newsgroup_ids";

        public static final String IS_DETHREADED    = "is_dethreaded";

        public static final String IS_STARRED       = "is_starred";

        public static final String MESSAGE_ID       = "message_id";

        public static final String PERSONAL_LEVEL   = "personal_level";

        public static final String IS_STICKIED      = "sticky";

        public static final String SUBJECT          = "subject";

        public static final String TOTAL_STARS      = "total_stars";

        public static final String UNREAD_CLASS     = "unread_class";

        public static final String CHILD_IDS        = "child_ids";

        public static final String DESCENDANT_IDS   = "descendant_ids";

        public static final String AUTHOR_NAME      = "author_name";

        public static final String AUTHOR_EMAIL     = "author_email";

        public static Uri buildPostsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI,id);
        }

    }

    public static final class UserEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_USER).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USER;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USER;

        public static final String TABLE_NAME   = "user";

        public static final String USERNAME     = "username";

        public static final String EMAIL        = "email";

        public static final String DISPLAY_NAME = "display_name";

        public static final String CREATED_AT   = "created_at";

        public static final String IS_ADMIN     = "is_admin";

        public static Uri buildUserUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI,id);
        }
    }

}