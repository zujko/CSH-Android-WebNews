package edu.csh.cshwebnews.jobs;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import edu.csh.cshwebnews.Utility;
import edu.csh.cshwebnews.database.WebNewsContract;
import edu.csh.cshwebnews.models.JobPriority;
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
        Response<com.squareup.okhttp.Response> response = Utility.webNewsService.markPostRead(post).execute();
        if (response.isSuccess()) {
            Log.d("READ POST","SUCCESS");
            updateDB();
        } else {
            Log.e("READ POST",response.message());
        }
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }

    private void updateDB() {
        ContentValues values = new ContentValues();
        values.put(WebNewsContract.PostEntry.UNREAD_CLASS, "manual");
        String where = WebNewsContract.PostEntry._ID + " = ?";
        String[] args = new String[] {post};
        context.getContentResolver().update(WebNewsContract.PostEntry.CONTENT_URI,values,where,args);
    }
}
