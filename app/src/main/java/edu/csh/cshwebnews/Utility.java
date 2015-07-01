package edu.csh.cshwebnews;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    public static String hex(byte[] array) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < array.length; ++i) {
            sb.append(Integer.toHexString((array[i]
                    & 0xFF) | 0x100).substring(1,3));
        }
        return sb.toString();
    }

    public static String md5Hex (String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return hex (md.digest(message.getBytes("CP1252")));
        } catch (NoSuchAlgorithmException e) {
        } catch (UnsupportedEncodingException e) {
        }
        return null;
    }

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
     * Checks if there is currently a sync running with the specified authority
     */
    public static boolean isSyncActive(Account account, String authority) {
        for(SyncInfo syncInfo : ContentResolver.getCurrentSyncs()) {
            if(syncInfo.account.equals(account) && syncInfo.authority.equals(authority)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns account associated with the application
     */
    public static Account getAccount(Context context) {
        return AccountManager.get(context).getAccountsByType(context.getString(R.string.account_type))[0];
    }
}
