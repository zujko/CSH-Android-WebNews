package edu.csh.cshwebnews.activities;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import edu.csh.cshwebnews.R;
import edu.csh.cshwebnews.database.WebNewsContract;
import edu.csh.cshwebnews.models.AccessToken;
import edu.csh.cshwebnews.models.User;
import edu.csh.cshwebnews.models.WebNewsAccount;
import edu.csh.cshwebnews.network.ServiceGenerator;
import edu.csh.cshwebnews.network.WebNewsService;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class LoginActivity extends AccountAuthenticatorActivity {

    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String PARAM_USER_PASS = "USER_PASS";

    private String clientId;
    private String clientSecret;

    AccountManager accountManager;
    WebView loginWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_signed_in),false)) {
            startActivity(new Intent(this,MainActivity.class));
            finish();
        } else {

            accountManager  = AccountManager.get(getBaseContext());
            loginWebView = (WebView) findViewById(R.id.web_oauth);

            createAuthWebView();
        }
    }

    /**
     * Creates the webview for authentication
     */
    private void createAuthWebView() {
        loginWebView.getSettings().setJavaScriptEnabled(true);
        loginWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && url.startsWith(WebNewsService.REDIRECT_URI)) {
                    getAccessToken(url);
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(getBaseContext(), "An error occurred, please try signing in again", Toast.LENGTH_LONG).show();
            }
        });

        loginWebView.loadUrl(WebNewsService.BASE_URL + "/oauth/authorize" +
                "?client_id=" + clientId + "&redirect_uri=" + WebNewsService.REDIRECT_URI + "&response_type=code");
    }

    /**
     * Gets an access token using code from the url callback
     *
     * @param url the callback url which contains the code
     */
    private void getAccessToken(final String url) {
        //Get auth code from callback uri
        String code = Uri.parse(url).getQueryParameter("code");

        if (code != null) {
            WebNewsService generator = ServiceGenerator.createService(WebNewsService.class,
                    WebNewsService.BASE_URL, null, null);

            //Get an access token
            generator.getAccessToken("authorization_code", code, WebNewsService.REDIRECT_URI, clientId, clientSecret,
                    new Callback<AccessToken>() {
                        @Override
                        public void success(final AccessToken accessToken, Response response) {
                            final Intent result = new Intent();

                            WebNewsService webNewsService= ServiceGenerator.createService(WebNewsService.class,
                                    WebNewsService.BASE_URL, accessToken.getAccessToken(), accessToken.getTokenType());
                            // Get user data
                            webNewsService.getUser(new Callback<User>() {

                                @Override
                                public void success(User user, Response response) {
                                    //Put user data in the db
                                    ContentValues userValues = new ContentValues();
                                    userValues.put(WebNewsContract.UserEntry._ID,1);
                                    userValues.put(WebNewsContract.UserEntry.USERNAME,user.getUserName());
                                    userValues.put(WebNewsContract.UserEntry.DISPLAY_NAME,user.getDisplayName());
                                    userValues.put(WebNewsContract.UserEntry.EMAIL,user.getUserName()+"@csh.rit.edu");
                                    userValues.put(WebNewsContract.UserEntry.AVATAR_URL,user.getAvatarUrl());
                                    userValues.put(WebNewsContract.UserEntry.IS_ADMIN,user.isAdmin());
                                    userValues.put(WebNewsContract.UserEntry.CREATED_AT,user.getCreatedAt());
                                    getBaseContext().getContentResolver().insert(WebNewsContract.UserEntry.CONTENT_URI,userValues);

                                    result.putExtra(AccountManager.KEY_ACCOUNT_NAME, user.getUserName());
                                    result.putExtra(AccountManager.KEY_AUTHTOKEN, accessToken.getAccessToken());
                                    result.putExtra(PARAM_USER_PASS, accessToken.getRefreshToken());
                                    result.putExtra(AccountManager.KEY_ACCOUNT_TYPE, WebNewsAccount.ACCOUNT_TYPE);
                                    finishLogin(result);
                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    Toast.makeText(getBaseContext(),"FAILED TO GET USER",Toast.LENGTH_SHORT).show();
                                }
                            });

                            loginWebView.destroy();
                            loginWebView = null;
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Toast.makeText(getBaseContext(), "Error getting access token, please try signing in again", Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void finishLogin(Intent intent) {
        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountPassword = intent.getStringExtra(PARAM_USER_PASS);
        final Account account = new Account(accountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));

        accountManager.addAccountExplicitly(account, accountPassword, null);
        accountManager.setAuthToken(account, WebNewsAccount.AUTHTOKEN_TYPE, intent.getStringExtra(AccountManager.KEY_AUTHTOKEN));

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putBoolean(getString(R.string.pref_signed_in),true).apply();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

}
