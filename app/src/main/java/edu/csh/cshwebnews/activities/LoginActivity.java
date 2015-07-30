package edu.csh.cshwebnews.activities;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import edu.csh.cshwebnews.R;
import edu.csh.cshwebnews.Utility;
import edu.csh.cshwebnews.WebNewsApplication;
import edu.csh.cshwebnews.events.FinishLoginEvent;
import edu.csh.cshwebnews.jobs.GetAuthTokenJob;
import edu.csh.cshwebnews.models.WebNewsAccount;
import edu.csh.cshwebnews.network.WebNewsService;


public class LoginActivity extends AccountAuthenticatorActivity {

    @Bind(R.id.web_oauth) WebView loginWebView;
    @Bind(R.id.csh_logo) ImageView mImageView;
    @Bind(R.id.loading_textview) TextView mTextView;
    @Bind(R.id.error_textview) TextView mErrorTextView;
    @Bind(R.id.refresh_button) Button mRefreshButton;
    private AccountManager accountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_signed_in),false)) {
            startActivity(new Intent(this,MainActivity.class));
            finish();
        } else {

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.black));
            }

            accountManager  = AccountManager.get(getBaseContext());

            ButterKnife.bind(this);

            mRefreshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mErrorTextView.setVisibility(View.GONE);
                    mRefreshButton.setVisibility(View.GONE);
                    loginWebView.loadUrl(WebNewsService.BASE_URL + "/oauth/authorize" +
                            "?client_id=" + Utility.clientId + "&redirect_uri=" + WebNewsService.REDIRECT_URI + "&response_type=code");
                }
            });

            mImageView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate));
            loginWebView.setVisibility(View.GONE);

            createAuthWebView();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    /**
     * Creates the webview for authentication
     */
    private void createAuthWebView() {
        loginWebView.getSettings().setJavaScriptEnabled(true);
        loginWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.setVisibility(View.GONE);
                mImageView.setVisibility(View.VISIBLE);
                mTextView.setVisibility(View.VISIBLE);
                if (url != null && url.startsWith(WebNewsService.REDIRECT_URI)) {
                    getAccessToken(url);
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                view.setVisibility(View.VISIBLE);
                mImageView.clearAnimation();
                mImageView.setVisibility(View.GONE);
                mTextView.setVisibility(View.GONE);
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                view.loadUrl("");
                view.setVisibility(View.GONE);
                mErrorTextView.setVisibility(View.VISIBLE);
                mRefreshButton.setVisibility(View.VISIBLE);
                mErrorTextView.setText("Error loading page...\n" + description);
            }
        });

        loginWebView.loadUrl(WebNewsService.BASE_URL + "/oauth/authorize" +
                "?client_id=" + Utility.clientId + "&redirect_uri=" + WebNewsService.REDIRECT_URI + "&response_type=code");
    }

    /**
     * Helper function to run a GetAuthTokenJob
     * @param url
     */
    private void getAccessToken(String url) {
        String code = Uri.parse(url).getQueryParameter("code");
        if(code != null) {
            WebNewsApplication.getJobManager().addJobInBackground(new GetAuthTokenJob(code,getContentResolver()));
        } else {
            Toast.makeText(this,"Error signing in",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Adds account and starts the main activity
     * @param intent
     */
    private void finishLogin(Intent intent) {
        mImageView.clearAnimation();
        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountPassword = intent.getStringExtra(WebNewsAccount.PARAM_USER_PASS);
        final Account account = new Account(accountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));

        accountManager.addAccountExplicitly(account, accountPassword, null);
        accountManager.setAuthToken(account, WebNewsAccount.AUTHTOKEN_TYPE, intent.getStringExtra(AccountManager.KEY_AUTHTOKEN));

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putBoolean(getString(R.string.pref_signed_in), true).apply();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void onEventMainThread(FinishLoginEvent event) {
        if(event.success) {
            finishLogin(event.intent);
        } else {
            Toast.makeText(this,"Error!\n"+event.reason,Toast.LENGTH_SHORT).show();
        }
    }
}
