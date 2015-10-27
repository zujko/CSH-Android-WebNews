package edu.csh.cshwebnews.jobs;

import android.os.Bundle;
import android.util.Log;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import de.greenrobot.event.EventBus;
import edu.csh.cshwebnews.Utility;
import edu.csh.cshwebnews.events.NewPostEvent;
import edu.csh.cshwebnews.models.JobPriority;
import edu.csh.cshwebnews.models.requests.PostRequestBody;
import retrofit.Response;

public class NewPostJob extends Job {

    public static final String BODY = "body";
    public static final String FOLLOWUP_ID = "followup_newsgroup_id";
    public static final String NEWSGROUP_ID = "newsgroup_id";
    public static final String PARENT_ID = "parent_id";
    public static final String POSTING_HOST = "posting_host";
    public static final String SUBJECT = "subject";
    private Bundle arguments;

    public NewPostJob(Bundle bundle) {
        super(new Params(JobPriority.VERY_HIGH).requireNetwork());
        this.arguments = bundle;
    }

    @Override
    public void onAdded() {}

    @Override
    public void onRun() throws Throwable {
        String body = arguments.getString(BODY);
        String postingHost = arguments.getString(POSTING_HOST);
        String subject = arguments.getString(SUBJECT);
        String newsgroupId = arguments.getString(NEWSGROUP_ID);
        String followupId = arguments.getString(FOLLOWUP_ID, null);
        String parentId = arguments.getString(PARENT_ID, null);

        Log.d("NEWSGROUP",newsgroupId);

        Response<com.squareup.okhttp.Response> response = Utility.webNewsService.post(new PostRequestBody(subject, newsgroupId, body, parentId, followupId, postingHost)).execute();

        if(response.code() == 201 || response.code() == 202) {
            EventBus.getDefault().post(new NewPostEvent(true,null));
        } else {
            EventBus.getDefault().post(new NewPostEvent(false,response.errorBody().string()));
        }
    }

    @Override
    protected void onCancel() {}

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
