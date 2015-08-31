package edu.csh.cshwebnews.jobs;

import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.ContentValues;
import android.content.Context;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.io.IOException;

import edu.csh.cshwebnews.Utility;
import edu.csh.cshwebnews.database.WebNewsContract;
import edu.csh.cshwebnews.models.JobPriority;
import edu.csh.cshwebnews.models.NewsGroups;
import edu.csh.cshwebnews.models.WebNewsAccount;
import retrofit.RetrofitError;

public class LoadNewsGroupsJob extends Job {

    private Context context;

    public LoadNewsGroupsJob(Context context) {
        super(new Params(JobPriority.VERY_HIGH).requireNetwork());
        this.context = context;
    }

    @Override
    public void onAdded() {}

    @Override
    public void onRun() throws Throwable {
        try {
            NewsGroups newsGroups = Utility.webNewsService.blockingGetNewsGroups();

            int size = newsGroups.getNewsGroupList().size();

            if(size > 0) {
                ContentValues[] contentValues = new ContentValues[size];

                for(int i = 0; i < size; i++) {
                    ContentValues values = new ContentValues();
                    NewsGroups.NewsGroup newsGroup = newsGroups.getNewsGroupList().get(i);
                    values.put(WebNewsContract.NewsGroupEntry._ID,newsGroup.getId());
                    values.put(WebNewsContract.NewsGroupEntry.DESCRIPTION,newsGroup.getDescription());
                    values.put(WebNewsContract.NewsGroupEntry.MAX_UNREAD_LEVEL,newsGroup.getMaxUnreadLevel());
                    values.put(WebNewsContract.NewsGroupEntry.NEWEST_POST_AT,newsGroup.getNewestPostAt());
                    values.put(WebNewsContract.NewsGroupEntry.OLDEST_POST_AT,newsGroup.getOldestPostAt());

                    if(newsGroup.postingAllowed()) {
                        values.put(WebNewsContract.NewsGroupEntry.POSTING_ALLOWED,1);
                    } else {
                        values.put(WebNewsContract.NewsGroupEntry.POSTING_ALLOWED,0);
                    }

                    values.put(WebNewsContract.NewsGroupEntry.UNREAD_COUNT,newsGroup.getUnreadCount());

                    contentValues[i] = values;
                }

                context.getContentResolver().bulkInsert(WebNewsContract.NewsGroupEntry.CONTENT_URI,contentValues);
            }
        } catch (RetrofitError e) {
            if(e.getResponse().getStatus() == 401) {
                invalidateAuthToken();
                throw e;
            }
        }
    }

    private void invalidateAuthToken() throws AuthenticatorException, OperationCanceledException, IOException {
        String authToken = AccountManager.get(context).blockingGetAuthToken(Utility.getAccount(context), WebNewsAccount.AUTHTOKEN_TYPE,false);
        AccountManager.get(context).invalidateAuthToken(WebNewsAccount.ACCOUNT_TYPE,authToken);
    }

    @Override
    protected void onCancel() {}

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return throwable instanceof RetrofitError;
    }
}
