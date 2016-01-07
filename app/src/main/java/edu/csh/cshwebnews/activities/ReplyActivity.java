package edu.csh.cshwebnews.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import edu.csh.cshwebnews.R;
import edu.csh.cshwebnews.Utility;
import edu.csh.cshwebnews.WebNewsApplication;
import edu.csh.cshwebnews.events.NewPostEvent;
import edu.csh.cshwebnews.jobs.NewPostJob;

public class ReplyActivity extends AppCompatActivity {

    @Bind(R.id.tool_bar) Toolbar mToolbar;
    @Bind(R.id.body_edittext) EditText mBodyText;
    @Bind(R.id.subject_edittext) EditText mSubjectText;
    @Bind(R.id.newsgroup_text) TextView mNewsgroupTextView;
    Intent extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);
        extras = getIntent();
        Log.d("ReplyActivity","ID EXTRA: "+extras.getStringExtra("id"));

        ButterKnife.bind(this);

        mBodyText.requestFocus();

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Reply");
        getSupportActionBar().setHomeButtonEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.csh_pink_dark));
        }

        mSubjectText.setText("Re: " + extras.getStringExtra("subject"));
        mNewsgroupTextView.setText(extras.getStringExtra("newsgroup"));
        mBodyText.setText(Utility.replyBody(extras.getStringExtra("author"),extras.getStringExtra("body")));
        mBodyText.setSelection(mBodyText.getText().length());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_post, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_send:
                post();
                break;
            case R.id.action_settings:
                break;
            case R.id.action_save:
        }

        return super.onOptionsItemSelected(item);
    }

    private void post() {
        Bundle args = new Bundle();
        args.putString(NewPostJob.BODY, mBodyText.getText().toString());
        args.putString(NewPostJob.NEWSGROUP_ID, extras.getStringExtra("newsgroup").trim());
        args.putString(NewPostJob.SUBJECT, mSubjectText.getText().toString());
        args.putString(NewPostJob.PARENT_ID,extras.getStringExtra("id"));

        Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();

        WebNewsApplication.getJobManager().addJobInBackground(new NewPostJob(args));
    }

    public void onEventMainThread(NewPostEvent event) {
        if(event.success) {
            Toast.makeText(this, getString(R.string.new_post_successful_post), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, getString(R.string.new_post_error_post) + "\n" + event.errorMessage, Toast.LENGTH_SHORT).show();
            Log.e("POST ERROR",event.errorMessage);
        }
    }

}
