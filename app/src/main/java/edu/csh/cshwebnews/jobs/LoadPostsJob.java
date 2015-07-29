package edu.csh.cshwebnews.jobs;

import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import de.greenrobot.event.EventBus;
import edu.csh.cshwebnews.Utility;
import edu.csh.cshwebnews.database.WebNewsContract;
import edu.csh.cshwebnews.events.FinishLoadingEvent;
import edu.csh.cshwebnews.models.JobPriority;
import edu.csh.cshwebnews.models.Post;
import edu.csh.cshwebnews.models.RetrievingPosts;
import edu.csh.cshwebnews.models.WebNewsAccount;
import retrofit.RetrofitError;

public class LoadPostsJob extends Job {

    private Bundle args;
    private Context context;

    public LoadPostsJob(Bundle args, Context context) {
        super(new Params(JobPriority.VERY_HIGH).requireNetwork());
        this.args = args;
        this.context = context;
    }

    @Override
    public void onAdded() {}

    @Override
    public void onRun() throws Throwable {
        try {
            RetrievingPosts posts = Utility.webNewsService.blockingGetPosts("false", //as_meta
                    args.getBoolean("as_threads"), //as_threads
                    null, //authors
                    null, //keywords
                    null, //keywords_match
                    "20", //limit
                    null, //min_unread_level
                    args.getString("newsgroup_id"), //newsGroupId
                    args.getInt("offset"), //offset
                    args.getBoolean("only_roots"), //only_roots
                    args.getBoolean("only_starred"), //only_starred
                    args.getBoolean("only_sticky"), //only_sticky
                    "false", //reverse_order
                    null, //since
                    args.getString("until") //until
            );

            int size = posts.getListOfPosts().size();

            if (size > 0) {
                ContentValues[] postList = new ContentValues[size];

                Calendar c = Calendar.getInstance();
                DateTimeFormatter dateTimeFormat = ISODateTimeFormat.dateTimeNoMillis();
                DateTime date;

                for (int i = 0; i < size; i++) {
                    ContentValues values = new ContentValues();
                    Post postObj = posts.getListOfPosts().get(i);
                    values.put(WebNewsContract.PostEntry._ID, postObj.getId());
                    values.put(WebNewsContract.PostEntry.ANCESTOR_IDS, postObj.getListOfAncestorIds().toString());
                    values.put(WebNewsContract.PostEntry.BODY, postObj.getBody());

                    if (postObj.getBody().length() > 200) {
                        values.put(WebNewsContract.PostEntry.BODY_SUMMARY, postObj.getBody().substring(0, 200));
                    } else {
                        values.put(WebNewsContract.PostEntry.BODY_SUMMARY, postObj.getBody());
                    }

                    date = dateTimeFormat.parseDateTime(postObj.getCreatedAt());
                    String finalDate;

                    if (date.getYear() == c.get(Calendar.YEAR)) {
                        if (date.getDayOfYear() == c.get(Calendar.DAY_OF_YEAR)) {
                            finalDate = date.toString("HH:mm", Locale.US);
                        } else {
                            finalDate = date.monthOfYear().getAsShortText() + " " + date.getDayOfMonth();
                        }
                    } else {
                        finalDate = date.toString("MM/dd/yyyy", Locale.US);
                    }

                    values.put(WebNewsContract.PostEntry.CREATED_AT, finalDate);
                    values.put(WebNewsContract.PostEntry.RAW_DATE, postObj.getCreatedAt());
                    values.put(WebNewsContract.PostEntry.FOLLOWUP_NEWSGROUP_ID, postObj.getFollowupNewsgroupId());
                    values.put(WebNewsContract.PostEntry.HAD_ATTACHMENTS, postObj.hadAttachments());
                    values.put(WebNewsContract.PostEntry.HEADERS, postObj.getHeaders());
                    values.put(WebNewsContract.PostEntry.IS_DETHREADED, postObj.isDethreaded());

                    if (postObj.isStarred()) {
                        values.put(WebNewsContract.PostEntry.IS_STARRED, 1);
                    } else {
                        values.put(WebNewsContract.PostEntry.IS_STARRED, 0);
                    }

                    values.put(WebNewsContract.PostEntry.MESSAGE_ID, postObj.getMessageId());
                    values.put(WebNewsContract.PostEntry.PERSONAL_LEVEL, postObj.getPersonalLevel());

                    if (postObj.getSticky().getDisplayName() == null) {
                        values.put(WebNewsContract.PostEntry.IS_STICKIED, 0);
                    } else {
                        values.put(WebNewsContract.PostEntry.IS_STICKIED, 1);
                    }

                    values.put(WebNewsContract.PostEntry.SUBJECT, postObj.getSubject());
                    values.put(WebNewsContract.PostEntry.NEWSGROUP_IDS, postObj.getListOfNewsgroupIds().toString());
                    values.put(WebNewsContract.PostEntry.TOTAL_STARS, postObj.getStarsTotal());

                    if (args.getBoolean("as_threads")) {
                        values.put(WebNewsContract.PostEntry.CHILD_IDS, postObj.getChildIds().toString());
                        values.put(WebNewsContract.PostEntry.DESCENDANT_IDS, postObj.getDescendantIds().toString());
                    }

                    values.put(WebNewsContract.PostEntry.AUTHOR_NAME, postObj.getAuthor().getName());
                    values.put(WebNewsContract.PostEntry.AUTHOR_EMAIL, postObj.getAuthor().getEmail());
                    values.put(WebNewsContract.PostEntry.AUTHOR_AVATAR_URL, postObj.getAuthor().getAvatarUrl());
                    values.put(WebNewsContract.PostEntry.UNREAD_CLASS, postObj.getUnreadClass());
                    postList[i] = values;
                }
                context.getContentResolver().bulkInsert(WebNewsContract.PostEntry.CONTENT_URI, postList);
                EventBus.getDefault().post(new FinishLoadingEvent(true, null));
            }
        } catch (RetrofitError e) {
            if(e.getResponse().getStatus() == 401) {
                invalidateAuthToken();
                throw e;
            }
        }
        EventBus.getDefault().post(new FinishLoadingEvent(true,null));
    }

    private void invalidateAuthToken() throws AuthenticatorException, OperationCanceledException, IOException {
        String authToken = AccountManager.get(context).blockingGetAuthToken(Utility.getAccount(context),WebNewsAccount.AUTHTOKEN_TYPE,false);
        AccountManager.get(context).invalidateAuthToken(WebNewsAccount.ACCOUNT_TYPE,authToken);
    }

    @Override
    protected void onCancel() {}

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return throwable instanceof RetrofitError;
    }
}
