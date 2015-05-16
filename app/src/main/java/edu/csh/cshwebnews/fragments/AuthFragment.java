package edu.csh.cshwebnews.fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import edu.csh.cshwebnews.R;
import edu.csh.cshwebnews.models.AccessToken;
import edu.csh.cshwebnews.network.ServiceGenerator;
import edu.csh.cshwebnews.network.WebNewsService;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AuthFragment extends DialogFragment {

    private String clientId;
    private String clientSecret;
    private WebView loginWebView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dialog_auth, container, false);
        loginWebView = (WebView) v.findViewById(R.id.web_oauth);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        createAuthWebView();
        loginWebView.loadUrl(WebNewsService.BASE_URL + "/oauth/authorize" +
                "?client_id=" + clientId + "&redirect_uri=" + WebNewsService.REDIRECT_URI + "&response_type=code");
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
                        public void success(AccessToken accessToken, Response response) {
                            getActivity().getSharedPreferences("data",Context.MODE_PRIVATE).edit().putString("token",accessToken.getAccessToken()).apply();
                            getActivity().getSharedPreferences("data",Context.MODE_PRIVATE).edit().putString("tokenType",accessToken.getTokenType()).apply();
                            loginWebView.destroy();
                            loginWebView = null;
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Toast.makeText(getActivity(), "Error getting access token, please try signing in again", Toast.LENGTH_LONG).show();
                            getDialog().dismiss();
                        }
                    });
        }
    }

    /**
     * Creates the webview for CSH WebNews
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
                Toast.makeText(getActivity(), "An error occurred, please try signing in again", Toast.LENGTH_LONG).show();
                getDialog().dismiss();
            }
        });
    }

}
