package edu.csh.cshwebnews.authentication;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

import edu.csh.cshwebnews.R;
import edu.csh.cshwebnews.Utility;
import edu.csh.cshwebnews.activities.LoginActivity;
import edu.csh.cshwebnews.models.AccessToken;
import edu.csh.cshwebnews.models.WebNewsAccount;
import edu.csh.cshwebnews.network.ServiceGenerator;
import edu.csh.cshwebnews.network.WebNewsService;
import retrofit.Response;

public class WebNewsAccountAuthenticator extends AbstractAccountAuthenticator {

    private final Context context;
    private static final String TAG = "ACCOUNT AUTHENTICATOR";

    public WebNewsAccountAuthenticator(Context context) {
        super(context);

        this.context = context;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        final Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(WebNewsAccount.ARG_ACCOUNT_TYPE, accountType);
        intent.putExtra(WebNewsAccount.ARG_AUTH_TYPE, authTokenType);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT,intent);
        return bundle;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        final AccountManager accountManager = AccountManager.get(context);

        String accessToken = accountManager.peekAuthToken(account, authTokenType);

        //If there is no access token try getting one!
        if(TextUtils.isEmpty(accessToken)) {
            final String refreshToken = accountManager.getPassword(account);
            if(refreshToken != null) {
                Response<AccessToken> tokenResponse = null;
                try {
                    tokenResponse = Utility.webNewsService.refreshAccessToken("refresh_token",refreshToken).execute();
                } catch (IOException e) {
                    Log.e(TAG,e.getMessage());
                }
                if(tokenResponse.isSuccess()) {
                    accessToken = tokenResponse.body().getAccessToken();
                } else {
                    Log.e(TAG,tokenResponse.message());
                }
            }
        }

        //If there is an access token, return it
        if(!TextUtils.isEmpty(accessToken)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, accessToken);
            Utility.webNewsService = ServiceGenerator.createService(WebNewsService.class,WebNewsService.BASE_URL, accessToken, WebNewsAccount.AUTHTOKEN_TYPE);
            return result;
        }

        //If this code executes, it means that we couldn't get a token
        //so prompt the user to sign in again.
        final Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(WebNewsAccount.ARG_ACCOUNT_TYPE, account.type);
        intent.putExtra(WebNewsAccount.ARG_AUTH_TYPE, authTokenType);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(context.getString(R.string.pref_signed_in),false).commit();
        return bundle;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        return null;
    }
}
