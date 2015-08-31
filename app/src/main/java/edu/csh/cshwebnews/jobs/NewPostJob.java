package edu.csh.cshwebnews.jobs;

import android.os.Bundle;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import de.greenrobot.event.EventBus;
import edu.csh.cshwebnews.Utility;
import edu.csh.cshwebnews.events.NewPostEvent;
import edu.csh.cshwebnews.models.JobPriority;
import edu.csh.cshwebnews.models.requests.PostRequestBody;
import retrofit.RetrofitError;
import retrofit.client.Response;

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
        Integer followupId = null;
        Integer parentId = null;

        if(arguments.getInt(FOLLOWUP_ID, -10) != -10) followupId = arguments.getInt(FOLLOWUP_ID, 0);

        if(arguments.getInt(PARENT_ID, -10) != -10) parentId = arguments.getInt(PARENT_ID, 0);

        try {
            Response response = Utility.webNewsService.blockingPost(new PostRequestBody(subject, newsgroupId, body, parentId, followupId, postingHost));

            if(response.getStatus() == 201 || response.getStatus() == 202) {
                EventBus.getDefault().post(new NewPostEvent(true,null));
            } else {
                EventBus.getDefault().post(new NewPostEvent(false,response.getReason()));
            }

        } catch (RetrofitError e) {
            EventBus.getDefault().post(new NewPostEvent(false,e.getResponse().getReason()));
        } catch (Exception e) {
            EventBus.getDefault().post(new NewPostEvent(false,e.getMessage()));
        }

    }

    @Override
    protected void onCancel() {}

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
