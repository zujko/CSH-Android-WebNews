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
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;

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
    private int isLoginError = 0;

    private String clientId;
    private String clientSecret;

    AccountManager accountManager;
    WebView loginWebView;
    ImageView logo;
    TextView webNewsText;
    ActionProcessButton button;
    EditText username;
    EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("signed_in",false)) {
            startActivity(new Intent(this,MainActivity.class));
        } else {

            accountManager = AccountManager.get(getBaseContext());
            logo = (ImageView) findViewById(R.id.image_logo);
            button = (ActionProcessButton) findViewById(R.id.btn_login);
            webNewsText = (TextView) findViewById(R.id.textview_webnews);
            username = (EditText) findViewById(R.id.edittext_username);
            password = (EditText) findViewById(R.id.edittext_password);
            loginWebView = (WebView) findViewById(R.id.web_oauth);

            button.setMode(ActionProcessButton.Mode.ENDLESS);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    button.setProgress(1);
                    isLoginError = 0;
                    loginWebView.loadUrl("https://webauth.csh.rit.edu/");
                }
            });

            createAuthWebView();
        }
    }

    /**
     * Creates the webview for authentication
     */
    private void createAuthWebView() {
        loginWebView.getSettings().setJavaScriptEnabled(true);
        loginWebView.getSettings().setDomStorageEnabled(true);
        loginWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                if(consoleMessage.message().equals("1")) {
                    Toast.makeText(getApplicationContext(),"Error! email or username is invalid! Please try again",Toast.LENGTH_SHORT).show();
                    button.setProgress(0);
                    loginWebView.stopLoading();
                }
                return super.onConsoleMessage(consoleMessage);
            }
        });
        loginWebView.setWebViewClient(new WebViewClient() {

            //Super sketchy way to get auth code for OAuth
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d("ONPAGEFINISHED: ",url);
                if(url.contains("webauth") ) {
                    if(isLoginError == 0) {
                        if(username.getText().toString().equals("") || password.getText().toString().equals("")){
                            Toast.makeText(getApplicationContext(),"Error! email or username is invalid! Please try again",Toast.LENGTH_SHORT).show();
                            button.setProgress(0);
                            loginWebView.stopLoading();
                        } else {
                            view.loadUrl("javascript:document.getElementById('login_username').value = " +
                                    "'" + username.getText().toString() + "';" +
                                    "document.getElementById('login_password').value = " +
                                    "'" + password.getText().toString() + "'; " +
                                    "document.getElementById('login_submit').click();");
                            Log.d("WebView", "Javascript injected");
                        }
                    } else if(isLoginError > 0) {
                        view.loadUrl("javascript:" +
                                "if(document.getElementsByClassName('alert alert-error').length > 0){" +
                                "console.log('1');};");
                    }
                    isLoginError++;
                } else if (url.contains("members")) {
                    Log.d("WebView", "Loading oauth url");
                    view.loadUrl(WebNewsService.BASE_URL + "/oauth/authorize" +
                            "?client_id=" + clientId + "&redirect_uri=" + WebNewsService.REDIRECT_URI + "&response_type=code");

                } else if(url.contains("webnews-staging")) {
                    Log.d("WebView","Injection second JS");
                    view.loadUrl("javascript:document.getElementsByName('commit')[0].click();");
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && url.startsWith(WebNewsService.REDIRECT_URI)) {
                    Log.d("WebView","Got token uri");
                    view.clearHistory();
                    view.clearCache(true);
                    view.stopLoading();
                    getAccessToken(url);
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(getBaseContext(), "An error occurred, please try signing in again", Toast.LENGTH_LONG).show();
                button.setProgress(0);
            }
        });
    }

    /**
     * Gets an access token using code from the url callback
     *
     * @param url the callback url which contains the code
     */
    private void getAccessToken(String url) {
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
                                    ContentValues userValues = new ContentValues();
                                    Log.d("LOGIN","EMAIL IS: "+user.getEmail());
                                    userValues.put(WebNewsContract.UserEntry._ID,1);
                                    userValues.put(WebNewsContract.UserEntry.DISPLAY_NAME,user.getDisplayName());
                                    userValues.put(WebNewsContract.UserEntry.EMAIL,user.getEmail());
                                    userValues.put(WebNewsContract.UserEntry.IS_ADMIN,user.isAdmin());
                                    userValues.put(WebNewsContract.UserEntry.CREATED_AT,user.getCreatedAt());
                                    getBaseContext().getContentResolver().insert(WebNewsContract.UserEntry.CONTENT_URI,userValues);

                                    result.putExtra(AccountManager.KEY_ACCOUNT_NAME, user.getUserName());
                                    result.putExtra(AccountManager.KEY_AUTHTOKEN, accessToken.getAccessToken());
                                    result.putExtra(PARAM_USER_PASS, accessToken.getRefreshToken());
                                    result.putExtra(AccountManager.KEY_ACCOUNT_TYPE, "edu.csh.cshwebnews");
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
        button.setProgress(100);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putBoolean("signed_in",true).apply();
        startActivity(new Intent(this, MainActivity.class));
        finish();
        return;
    }

}
