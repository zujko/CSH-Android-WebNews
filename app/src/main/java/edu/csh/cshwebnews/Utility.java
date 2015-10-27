package edu.csh.cshwebnews;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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

    public static final String CANCEL_NEWSGROUP_ID = "33";

    public static WebNewsService webNewsService = null;

    public static String clientId = "bfe0a6ad79a3cbdce9074000a4e58553c0e1bdf01354edf3eee6f70f1a5f8b35";
    public static String clientSecret = "94f2e89e9e43c52a43d40ac7d8b74cb2e9a6f8d59dcd6706bc1236a3378652a2";

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
}
