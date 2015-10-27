package edu.csh.cshwebnews.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import edu.csh.cshwebnews.R;
import edu.csh.cshwebnews.Utility;
import edu.csh.cshwebnews.WebNewsApplication;
import edu.csh.cshwebnews.adapters.NewsgroupSpinnerAdapter;
import edu.csh.cshwebnews.database.WebNewsContract;
import edu.csh.cshwebnews.events.NewPostEvent;
import edu.csh.cshwebnews.jobs.NewPostJob;

public class NewPostActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.spinner) Spinner mSpinner;
    @Bind(R.id.down_arrow_image) ImageView mDownArrow;
    @Bind(R.id.body_edittext) EditText mBodyText;
    @Bind(R.id.subject_edittext) EditText mSubjectText;

    private NewsgroupSpinnerAdapter mSpinnerAdapter;
    private static String newsgroupId = null;
    private static final int NEWSGROUP_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.push_up_in, R.anim.fade_out);
        setContentView(R.layout.activity_new_post);

        newsgroupId = getIntent().getStringExtra("newsgroup_id");

        ButterKnife.bind(this);

        setUpToolbar();

        mSpinnerAdapter = new NewsgroupSpinnerAdapter(this,null,0);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                newsgroupId = ((TextView)view).getText().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mSpinner.setAdapter(mSpinnerAdapter);

        mDownArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpinner.performClick();
            }
        });

        getSupportLoaderManager().initLoader(NEWSGROUP_LOADER, null, this);
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
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.push_down_in, R.anim.fade_out);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_post, menu);
        return true;
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

    /**
     * Helper method to setup the toolbar
     */
    private void setUpToolbar() {
        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_back_arrow);

        setSupportActionBar(mToolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.csh_pink_dark));
        }

        getSupportActionBar().setTitle(getString(R.string.activity_new_post_title));

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = WebNewsContract.NewsGroupEntry._ID;
        Uri newsgroupUri = WebNewsContract.NewsGroupEntry.CONTENT_URI;

        return new CursorLoader(this,
                newsgroupUri,
                WebNewsContract.NEWSGROUP_COLUMNS,
                WebNewsContract.NewsGroupEntry.POSTING_ALLOWED + " = ?",
                new String[]{"1"},
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mSpinnerAdapter.swapCursor(data);
        if (newsgroupId != null) {
            try {
                mSpinner.setSelection(Utility.getPosition(newsgroupId, data));
            } catch (Exception e) {
                mSpinner.setSelection(0);
                mSpinner.performClick();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSpinnerAdapter.swapCursor(null);
    }

    /**
     * Starts a NewPostJob
     */
    private void post() {
        Bundle args = new Bundle();
        args.putString(NewPostJob.BODY, mBodyText.getText().toString());
        args.putString(NewPostJob.NEWSGROUP_ID, newsgroupId.trim());
        args.putString(NewPostJob.SUBJECT, mSubjectText.getText().toString());

        Toast.makeText(this,"Posting...",Toast.LENGTH_SHORT).show();

        WebNewsApplication.getJobManager().addJobInBackground(new NewPostJob(args));
    }

    public void onEventMainThread(NewPostEvent event) {
        if(event.success) {
            Toast.makeText(this, getString(R.string.new_post_successful_post), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, getString(R.string.new_post_error_post) + "\n" + event.errorMessage, Toast.LENGTH_SHORT).show();
        }
    }
}
