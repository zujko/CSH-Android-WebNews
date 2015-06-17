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

import java.io.IOException;
import java.util.Vector;

import edu.csh.cshwebnews.R;
import edu.csh.cshwebnews.database.WebNewsContract;
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
            authToken = AccountManager.get(getContext()).blockingGetAuthToken(account, WebNewsAccount.AUTHTOKEN_TYPE, true);
            WebNewsService service = ServiceGenerator.createService(WebNewsService.class, WebNewsService.BASE_URL, authToken, WebNewsAccount.AUTHTOKEN_TYPE);
            String newsGroupId;
            if(extras.getString("newsgroup_id") == null || extras.getString("newsgroup_id").equals("null")){
                newsGroupId = null;
            } else {
                newsGroupId = extras.getString("newsgroup_id");
            }
            RetrievingPosts posts = service.syncGetPosts("false",
                    extras.getString("as_threads"),
                    null,
                    null,
                    null,
                    "11",//limit
                    null,
                    newsGroupId,
                    extras.getString("offset"),
                    "true", //as_roots
                    "false",
                    "false",
                    "false",
                    null, null);

            NewsGroups newsGroups = service.syncGetNewsGroups();


            Vector<ContentValues> cVVector = new Vector<ContentValues>(posts.getListOfPosts().size());
            Vector<ContentValues> nGVector = new Vector<ContentValues>(newsGroups.getNewsGroupList().size());

            for(Post postObj : posts.getListOfPosts()) {
                ContentValues values = new ContentValues();
                values.put(WebNewsContract.PostEntry._ID,postObj.getId());
                values.put(WebNewsContract.PostEntry.ANCESTOR_IDS,postObj.getListOfAncestorIds().toString());
                values.put(WebNewsContract.PostEntry.BODY,postObj.getBody());
                values.put(WebNewsContract.PostEntry.CREATED_AT, postObj.getCreatedAt());
                values.put(WebNewsContract.PostEntry.FOLLOWUP_NEWSGROUP_ID, postObj.getFollowupNewsgroupId());
                values.put(WebNewsContract.PostEntry.HAD_ATTACHMENTS,postObj.hadAttachments());
                values.put(WebNewsContract.PostEntry.HEADERS,postObj.getHeaders());
                values.put(WebNewsContract.PostEntry.IS_DETHREADED,postObj.isDethreaded());
                values.put(WebNewsContract.PostEntry.IS_STARRED,postObj.isStarred());
                values.put(WebNewsContract.PostEntry.MESSAGE_ID,postObj.getMessageId());
                values.put(WebNewsContract.PostEntry.PERSONAL_LEVEL,postObj.getPersonalLevel());

                if(postObj.getSticky().getDisplayName() == null){
                    values.put(WebNewsContract.PostEntry.IS_STICKIED,"false");
                } else {
                    values.put(WebNewsContract.PostEntry.IS_STICKIED,"true");
                }

                values.put(WebNewsContract.PostEntry.SUBJECT,postObj.getSubject());
                values.put(WebNewsContract.PostEntry.NEWSGROUP_IDS,postObj.getListOfNewsgroupIds().toString());
                values.put(WebNewsContract.PostEntry.TOTAL_STARS,postObj.getStarsTotal());

                if(extras.getString("as_threads") != null && extras.getString("as_threads").equals("true")){
                    values.put(WebNewsContract.PostEntry.CHILD_IDS,postObj.getChildIds().toString());
                    values.put(WebNewsContract.PostEntry.DESCENDANT_IDS,postObj.getDescendantIds().toString());
                }

                values.put(WebNewsContract.PostEntry.AUTHOR_NAME,postObj.getAuthor().getName());
                values.put(WebNewsContract.PostEntry.AUTHOR_EMAIL,postObj.getAuthor().getEmail());
                values.put(WebNewsContract.PostEntry.UNREAD_CLASS,postObj.getUnreadClass());
                cVVector.add(values);
            }

            for(NewsGroups.NewsGroup newsGroup : newsGroups.getNewsGroupList()) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(WebNewsContract.NewsGroupEntry._ID,newsGroup.getId());
                contentValues.put(WebNewsContract.NewsGroupEntry.DESCRIPTION,newsGroup.getDescription());
                contentValues.put(WebNewsContract.NewsGroupEntry.MAX_UNREAD_LEVEL,newsGroup.getMaxUnreadLevel());
                contentValues.put(WebNewsContract.NewsGroupEntry.NAME,newsGroup.getName());
                contentValues.put(WebNewsContract.NewsGroupEntry.NEWEST_POST_AT,newsGroup.getNewestPostAt());
                contentValues.put(WebNewsContract.NewsGroupEntry.OLDEST_POST_AT,newsGroup.getOldestPostAt());
                contentValues.put(WebNewsContract.NewsGroupEntry.POSTING_ALLOWED,newsGroup.postingAllowed());
                contentValues.put(WebNewsContract.NewsGroupEntry.UNREAD_COUNT,newsGroup.getUnreadCount());
                nGVector.add(contentValues);
            }

            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                getContext().getContentResolver().bulkInsert(WebNewsContract.PostEntry.CONTENT_URI, cvArray);
            }

            if(nGVector.size() > 0) {
                ContentValues[] nGArray = new ContentValues[nGVector.size()];
                nGVector.toArray(nGArray);
                getContext().getContentResolver().bulkInsert(WebNewsContract.NewsGroupEntry.CONTENT_URI,nGArray);
            }
        }
        catch (RetrofitError e) {
            if(e.getResponse().getStatus() == 401){
                AccountManager.get(getContext()).invalidateAuthToken(WebNewsAccount.ACCOUNT_TYPE,authToken);
            }
            Log.e("RETROFIT ERROR", "Reason: " +e.getResponse().getReason()+"\n" +
                                    "Message: " +e.getMessage() +"\n" +
                                    "URL: "+e.getResponse().getUrl()+"\n");
        }
        catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        }

    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context, Bundle bundle) {
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(AccountManager.get(context).getAccountsByType("edu.csh.cshwebnews")[0],
                context.getString(R.string.content_authority), bundle);
    }
}
