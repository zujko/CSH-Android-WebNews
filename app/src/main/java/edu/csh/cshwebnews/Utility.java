package edu.csh.cshwebnews;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import edu.csh.cshwebnews.database.WebNewsContract;
import edu.csh.cshwebnews.network.WebNewsService;

public class Utility {

    public static final String[] DRAWER_HEADER_ITEMS = {
            "Home", "Starred", "Stickied"
    };
    public static final int DRAWER_ITEM_HOME = 0;
    public static final int DRAWER_ITEM_STARRED = 1;
    public static final int DRAWER_ITEM_STICKIED = 2;

    public static final String[] DRAWER_FOOTER = {
            "Settings","About"
    };
    public static final long[] DRAWER_FOOTER_IDS = {
            -9,-8
    };
    public static final int DRAWER_FOOTER_SETTINGS_ID = -9;
    public static final int DRAWER_FOOTER_ABOUT_ID = -8;
    public static final int DRAWER_FOOTER_SETTINGS = 0;
    public static final int DRAWER_FOOTER_ABOUT = 1;

    public static final String CANCEL_NEWSGROUP_ID = "control.cancel";

    public static WebNewsService webNewsService = null;

    public static String clientId;
    public static String clientSecret;

    public static HashMap<String,Boolean> expandedStates;

    /**
     * Checks if the device is connected to a network
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Returns account associated with the application
     */
    public static Account getAccount(Context context) {
        return AccountManager.get(context).getAccountsByType(context.getString(R.string.account_type))[0];
    }

    /**
     * Returns the position of an item given its id
     * @param id
     * @param cursor
     * @return
     */
    public static int getPosition(String id, Cursor cursor) {
        for(int x = 0, items = cursor.getCount(); x < items; x++ ) {
            cursor.moveToPosition(x);
            if(cursor.getString(WebNewsContract.NEWSGROUP_COL_ID).equals(id)) {
                return x;
            }
        }
        return 0;
    }

    /**
     * Returns a formatted body string with > appended to the start of each line.
     * @param author
     * @param replyBody
     * @return
     */
    public static String replyBody(String author, String replyBody) {
        StringBuilder body = new StringBuilder();
        body.append(author);
        body.append(" wrote:\n\n");

        BufferedReader bufReader = new BufferedReader(new StringReader(replyBody));

        String line;
        try {
            while( (line=bufReader.readLine()) != null ) {
                body.append(">");
                body.append(line);
                body.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        body.append("\n");
        return body.toString();
    }
}
