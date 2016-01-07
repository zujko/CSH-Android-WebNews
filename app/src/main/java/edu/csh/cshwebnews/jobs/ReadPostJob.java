package edu.csh.cshwebnews.jobs;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.path.android.jobqueue.RetryConstraint;

import edu.csh.cshwebnews.Utility;
import edu.csh.cshwebnews.WebNewsApplication;
import edu.csh.cshwebnews.database.WebNewsContract;
import edu.csh.cshwebnews.models.JobPriority;
import edu.csh.cshwebnews.models.requests.UnreadRequestBody;
import retrofit.Response;

public class ReadPostJob extends Job {

    String post;
    Context context;

    public ReadPostJob(String post, Context context) {
        super(new Params(JobPriority.VERY_HIGH).requireNetwork());
        this.post = post;
        this.context = context;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        Response<com.squareup.okhttp.Response> response = Utility.webNewsService.markPostRead(new UnreadRequestBody(post)).execute();
        if (response.isSuccess()) {
            Log.d("READ POST","SUCCESS");
            updateDB();
            WebNewsApplication.getJobManager().addJob(new LoadNewsGroupsJob(context));
        } else {
            Log.e("READ POST",response.message());
        }
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount,
                                                     int maxRunCount) {
        return RetryConstraint.createExponentialBackoff(runCount, 1000);
    }


    private void updateDB() {
        ContentValues values = new ContentValues();
        values.put(WebNewsContract.PostEntry.UNREAD_CLASS, "manual");
        String where = WebNewsContract.PostEntry._ID + " = ?";
        String[] args = new String[] {post};
        context.getContentResolver().update(WebNewsContract.PostEntry.CONTENT_URI,values,where,args);
    }
}
