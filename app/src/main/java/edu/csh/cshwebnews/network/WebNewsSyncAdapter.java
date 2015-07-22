package edu.csh.cshwebnews.network;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.greenrobot.event.EventBus;
import edu.csh.cshwebnews.R;
import edu.csh.cshwebnews.Utility;
import edu.csh.cshwebnews.database.WebNewsContract;
import edu.csh.cshwebnews.events.FinishLoadingEvent;
import edu.csh.cshwebnews.models.NewsGroups;
import edu.csh.cshwebnews.models.Post;
import edu.csh.cshwebnews.models.RetrievingPosts;
import edu.csh.cshwebnews.models.WebNewsAccount;
import retrofit.RetrofitError;

public class WebNewsSyncAdapter extends AbstractThreadedSyncAdapter {

    String authToken;

    public WebNewsSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        try {
            if(Utility.webNewsService == null) {
                setWebnewsService(account);
            }

            if(extras.getBoolean("get_posts",true)) {
                RetrievingPosts posts = Utility.webNewsService.syncGetPosts("false", //as_meta
                        extras.getBoolean("as_threads"), //as_threads
                        null, //authors
                        null, //keywords
                        null, //keywords_match
                        "20",//limit
                        null, //min_unread_level
                        extras.getString("newsgroup_id"), //newsGroupId
                        extras.getInt("offset"), //offset
                        extras.getBoolean("only_roots"), //only_roots
                        extras.getBoolean("only_starred"), //only_starred
                        extras.getBoolean("only_sticky"), //only_sticky
                        "false", //reverse_order
                        null, //since
                        extras.getString("until") //until
                );
                List<ContentValues> postList = new ArrayList<ContentValues>(posts.getListOfPosts().size());

                Calendar c = Calendar.getInstance();
                DateTimeFormatter dateTimeFormat = ISODateTimeFormat.dateTimeNoMillis();
                DateTime date;

                for(Post postObj : posts.getListOfPosts()) {
                    ContentValues values = new ContentValues();
                    values.put(WebNewsContract.PostEntry._ID,postObj.getId());
                    values.put(WebNewsContract.PostEntry.ANCESTOR_IDS,postObj.getListOfAncestorIds().toString());
                    values.put(WebNewsContract.PostEntry.BODY,postObj.getBody());

                    if(postObj.getBody().length() > 200) {
                        values.put(WebNewsContract.PostEntry.BODY_SUMMARY,postObj.getBody().substring(0,200));
                    } else {
                        values.put(WebNewsContract.PostEntry.BODY_SUMMARY,postObj.getBody());
                    }

                    date = dateTimeFormat.parseDateTime(postObj.getCreatedAt());
                    String finalDate;

                    if(date.getYear() == c.get(Calendar.YEAR)) {
                        if(date.getDayOfYear() == c.get(Calendar.DAY_OF_YEAR)) {
                            finalDate = date.toString("HH:mm", Locale.US);
                        } else {
                            finalDate = date.monthOfYear().getAsShortText()+ " " + date.getDayOfMonth();
                        }
                    } else {
                        finalDate = date.toString("MM/dd/yyyy", Locale.US);
                    }

                    values.put(WebNewsContract.PostEntry.CREATED_AT, finalDate);
                    values.put(WebNewsContract.PostEntry.RAW_DATE, postObj.getCreatedAt());
                    values.put(WebNewsContract.PostEntry.FOLLOWUP_NEWSGROUP_ID, postObj.getFollowupNewsgroupId());
                    values.put(WebNewsContract.PostEntry.HAD_ATTACHMENTS,postObj.hadAttachments());
                    values.put(WebNewsContract.PostEntry.HEADERS,postObj.getHeaders());
                    values.put(WebNewsContract.PostEntry.IS_DETHREADED,postObj.isDethreaded());

                    if(postObj.isStarred()) {
                        values.put(WebNewsContract.PostEntry.IS_STARRED,1);
                    } else {
                        values.put(WebNewsContract.PostEntry.IS_STARRED,0);
                    }

                    values.put(WebNewsContract.PostEntry.MESSAGE_ID,postObj.getMessageId());
                    values.put(WebNewsContract.PostEntry.PERSONAL_LEVEL,postObj.getPersonalLevel());

                    if(postObj.getSticky().getDisplayName() == null){
                        values.put(WebNewsContract.PostEntry.IS_STICKIED,0);
                    } else {
                        values.put(WebNewsContract.PostEntry.IS_STICKIED,1);
                    }

                    values.put(WebNewsContract.PostEntry.SUBJECT,postObj.getSubject());
                    values.put(WebNewsContract.PostEntry.NEWSGROUP_IDS,postObj.getListOfNewsgroupIds().toString());
                    values.put(WebNewsContract.PostEntry.TOTAL_STARS,postObj.getStarsTotal());

                    if(extras.getBoolean("as_threads")){
                        values.put(WebNewsContract.PostEntry.CHILD_IDS,postObj.getChildIds().toString());
                        values.put(WebNewsContract.PostEntry.DESCENDANT_IDS,postObj.getDescendantIds().toString());
                    }

                    values.put(WebNewsContract.PostEntry.AUTHOR_NAME,postObj.getAuthor().getName());
                    values.put(WebNewsContract.PostEntry.AUTHOR_EMAIL,postObj.getAuthor().getEmail());
                    values.put(WebNewsContract.PostEntry.AUTHOR_AVATAR_URL,postObj.getAuthor().getAvatarUrl());
                    values.put(WebNewsContract.PostEntry.UNREAD_CLASS,postObj.getUnreadClass());
                    postList.add(values);
                }
                if (postList.size() > 0) {
                    ContentValues[] cvArray = new ContentValues[postList.size()];
                    postList.toArray(cvArray);
                    getContext().getContentResolver().bulkInsert(WebNewsContract.PostEntry.CONTENT_URI, cvArray);
                }
            }

            if(extras.getBoolean("get_newsgroups",true)) {
                NewsGroups newsGroups = Utility.webNewsService.syncGetNewsGroups();

                List<ContentValues> newsgroupList = new ArrayList<ContentValues>(newsGroups.getNewsGroupList().size());

                for(NewsGroups.NewsGroup newsGroup : newsGroups.getNewsGroupList()) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(WebNewsContract.NewsGroupEntry._ID,newsGroup.getId());
                    contentValues.put(WebNewsContract.NewsGroupEntry.DESCRIPTION,newsGroup.getDescription());
                    contentValues.put(WebNewsContract.NewsGroupEntry.MAX_UNREAD_LEVEL,newsGroup.getMaxUnreadLevel());
                    contentValues.put(WebNewsContract.NewsGroupEntry.NAME,newsGroup.getName());
                    contentValues.put(WebNewsContract.NewsGroupEntry.NEWEST_POST_AT,newsGroup.getNewestPostAt());
                    contentValues.put(WebNewsContract.NewsGroupEntry.OLDEST_POST_AT,newsGroup.getOldestPostAt());

                    if(newsGroup.postingAllowed()) {
                        contentValues.put(WebNewsContract.NewsGroupEntry.POSTING_ALLOWED,1);
                    } else {
                        contentValues.put(WebNewsContract.NewsGroupEntry.POSTING_ALLOWED,0);
                    }

                    contentValues.put(WebNewsContract.NewsGroupEntry.UNREAD_COUNT,newsGroup.getUnreadCount());
                    newsgroupList.add(contentValues);
                }

                if(newsgroupList.size() > 0) {
                    ContentValues[] nGArray = new ContentValues[newsgroupList.size()];
                    newsgroupList.toArray(nGArray);
                    getContext().getContentResolver().bulkInsert(WebNewsContract.NewsGroupEntry.CONTENT_URI,nGArray);
                }
            }
            EventBus.getDefault().post(new FinishLoadingEvent(true,null));
        }
        catch (RetrofitError e) {
            EventBus.getDefault().post(new FinishLoadingEvent(false,e.getResponse().getReason()));
            if(e.getResponse() != null && e.getResponse().getStatus() == 401){
                AccountManager.get(getContext()).invalidateAuthToken(WebNewsAccount.ACCOUNT_TYPE,authToken);
            }
            Log.e("RETROFIT ERROR", "Response: " + e.getResponse()+"\n" +
                                    "Message: " +e.getMessage() +"\n");
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context, Bundle bundle) {
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(AccountManager.get(context).getAccountsByType(WebNewsAccount.ACCOUNT_TYPE)[0],
                context.getString(R.string.content_authority), bundle);
    }

    private void setWebnewsService(Account account) {
        try {
            authToken = AccountManager.get(getContext()).blockingGetAuthToken(account, WebNewsAccount.AUTHTOKEN_TYPE, true);
            Utility.webNewsService = ServiceGenerator.createService(WebNewsService.class, WebNewsService.BASE_URL, authToken, WebNewsAccount.AUTHTOKEN_TYPE);
        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        }

    }
}
