package edu.csh.cshwebnews.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.facebook.stetho.Stetho;

import edu.csh.cshwebnews.R;
import edu.csh.cshwebnews.models.AccessToken;
import edu.csh.cshwebnews.network.ServiceGenerator;
import edu.csh.cshwebnews.network.WebNewsService;
import mehdi.sakout.dynamicbox.DynamicBox;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MainActivity extends AppCompatActivity {

    private String clientId;
    private String clientSecret;

    DynamicBox box;
    WebView loginWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        box = new DynamicBox(this,R.layout.activity_main);

        /* Stetho for debugging */
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(
                                Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(
                                Stetho.defaultInspectorModulesProvider(this))
                        .build());

        loginWebView = (WebView) findViewById(R.id.webView);
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
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){
                showErrorLayout(description);
            }
        });

        loginWebView.loadUrl(WebNewsService.BASE_URL + "/oauth/authorize" +
                "?client_id=" + clientId + "&redirect_uri=" + WebNewsService.REDIRECT_URI + "&response_type=code");
    }

    private void getAccessToken(String url) {
        String code = Uri.parse(url).getQueryParameter("code");
        if (code != null) {
            WebNewsService generator = ServiceGenerator.createService(WebNewsService.class,
                    WebNewsService.BASE_URL, null, null);

            generator.getAccessToken("authorization_code", code, WebNewsService.REDIRECT_URI, clientId, clientSecret,
                    new Callback<AccessToken>() {
                        @Override
                        public void success(AccessToken accessToken, Response response) {
                            //TODO: Open the 'main' activity
                            loginWebView.destroy();
                            loginWebView = null;
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            loginWebView.setVisibility(View.GONE);
                            showErrorLayout(error.getMessage());
                        }
                    });
        }
    }

    private void showErrorLayout(String errorMsg) {
        box.setOtherExceptionMessage(errorMsg+"\n Please refresh");
        box.setOtherExceptionTitle("Error");

        box.setClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginWebView.setVisibility(View.VISIBLE);
                loginWebView.loadUrl(WebNewsService.BASE_URL + "/oauth/authorize" +
                        "?client_id=" + clientId + "&redirect_uri=" + WebNewsService.REDIRECT_URI + "&response_type=code");
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
