package edu.csh.cshwebnews.jobs;

import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import de.greenrobot.event.EventBus;
import edu.csh.cshwebnews.Utility;
import edu.csh.cshwebnews.database.WebNewsContract;
import edu.csh.cshwebnews.events.FinishLoginEvent;
import edu.csh.cshwebnews.exceptions.ResponseException;
import edu.csh.cshwebnews.models.AccessToken;
import edu.csh.cshwebnews.models.JobPriority;
import edu.csh.cshwebnews.models.User;
import edu.csh.cshwebnews.models.WebNewsAccount;
import edu.csh.cshwebnews.network.ServiceGenerator;
import edu.csh.cshwebnews.network.WebNewsService;
import retrofit.Response;

public class GetAuthTokenJob extends Job {

    private String code;
    private ContentResolver mContentResolver;

    public GetAuthTokenJob(String code, ContentResolver contentResolver) {
        super(new Params(JobPriority.VERY_HIGH).requireNetwork());
        this.code = code;
        this.mContentResolver = contentResolver;
    }

    @Override
    public void onAdded() {}

    @Override
    public void onRun() throws Throwable {
        WebNewsService generator = ServiceGenerator.createService(WebNewsService.class,
                WebNewsService.BASE_URL, null, null);
        try {
            //Try getting the access token
            Response<AccessToken> response = generator.getAccessToken("authorization_code", code, WebNewsService.REDIRECT_URI, Utility.clientId, Utility.clientSecret).execute();
            if(!response.isSuccess()) {
                throw new ResponseException(response.errorBody().string());
            }

            AccessToken token = response.body();
            Utility.webNewsService = ServiceGenerator.createService(WebNewsService.class, WebNewsService.BASE_URL,token.getAccessToken(),token.getTokenType());

            //Try getting users data
            Response<User> userResponse = Utility.webNewsService.getUser().execute();
            if(!userResponse.isSuccess()) {
                throw new ResponseException(userResponse.errorBody().string());
            }

            User user = userResponse.body();

            saveToDb(user);

            Intent args = new Intent();
            args.putExtra(AccountManager.KEY_ACCOUNT_NAME, user.getUserName());
            args.putExtra(AccountManager.KEY_AUTHTOKEN, token.getAccessToken());
            args.putExtra(WebNewsAccount.PARAM_USER_PASS, token.getRefreshToken());
            args.putExtra(AccountManager.KEY_ACCOUNT_TYPE, WebNewsAccount.ACCOUNT_TYPE);

            EventBus.getDefault().post(new FinishLoginEvent(true,null,args));
        } catch (ResponseException e) {
            EventBus.getDefault().post(new FinishLoginEvent(false,e.getMessage(),null));
        }

    }

    @Override
    protected void onCancel() {}

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }

    /**
     * Saves users information to the database
     * @param user
     */
    private void saveToDb(User user) {
        ContentValues userValues = new ContentValues();
        userValues.put(WebNewsContract.UserEntry._ID,1);
        userValues.put(WebNewsContract.UserEntry.USERNAME,user.getUserName());
        userValues.put(WebNewsContract.UserEntry.DISPLAY_NAME,user.getDisplayName());
        userValues.put(WebNewsContract.UserEntry.EMAIL,user.getUserName()+"@csh.rit.edu");
        userValues.put(WebNewsContract.UserEntry.AVATAR_URL,user.getAvatarUrl());
        userValues.put(WebNewsContract.UserEntry.IS_ADMIN,user.isAdmin());
        userValues.put(WebNewsContract.UserEntry.CREATED_AT,user.getCreatedAt());
        mContentResolver.insert(WebNewsContract.UserEntry.CONTENT_URI, userValues);
    }
}
