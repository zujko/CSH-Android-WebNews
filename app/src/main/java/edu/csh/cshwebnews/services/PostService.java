package edu.csh.cshwebnews.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import edu.csh.cshwebnews.Utility;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PostService extends IntentService {

    public static final String NOTIFICATION = "edu.csh.cshwenews.services.postservice";
    public static final String POST_SUCCESS = "POST SUCCESS";

    public static final String BODY = "body";
    public static final String FOLLOWUP_ID = "followup_newsgroup_id";
    public static final String NEWSGROUP_ID = "newsgroup_id";
    public static final String PARENT_ID = "parent_id";
    public static final String POSTING_HOST = "posting_host";
    public static final String SUBJECT = "subject";


    public PostService() {
        super("PostService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String body = intent.getStringExtra(BODY);
        String postingHost = intent.getStringExtra(POSTING_HOST);
        String subject = intent.getStringExtra(SUBJECT);
        String newsgroupId = intent.getStringExtra(NEWSGROUP_ID);
        Integer followupId;
        Integer parentId;
        if(intent.getIntExtra(FOLLOWUP_ID,-10) != -10) {
            followupId = intent.getIntExtra(FOLLOWUP_ID,0);
        } else {
            followupId = null;
        }
        if(intent.getIntExtra(PARENT_ID,-10) != -10) {
            parentId = intent.getIntExtra(PARENT_ID,0);
        } else {
            parentId = null;
        }

        Utility.webNewsService.post(body, followupId, newsgroupId,
                parentId, postingHost, subject, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                if(response.getStatus() == 201 || response.getStatus() == 202) {
                    publishResults(true);
                } else {
                    publishResults(false);
                    Log.e("POSTSERVICE", "FAILED ON SUCCESS: " + response.getReason());
                }
            }

            @Override
            public void failure(RetrofitError error) {
                publishResults(false);
                Log.e("POSTSERVICE","FAILED: "+error.getResponse().getReason() +"\n STATUS: "+error.getResponse().getStatus() +
                " \nURL: "+error.getUrl());
            }
        });
    }

    private void publishResults(boolean result) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(POST_SUCCESS,result);
        sendBroadcast(intent);
    }
}
