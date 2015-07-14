package edu.csh.cshwebnews.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.Toast;

import edu.csh.cshwebnews.R;
import edu.csh.cshwebnews.Utility;
import edu.csh.cshwebnews.adapters.NewsgroupSpinnerAdapter;
import edu.csh.cshwebnews.database.WebNewsContract;
import edu.csh.cshwebnews.services.PostService;

public class NewPostActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int NEWSGROUP_LOADER = 0;
    private Toolbar mToolbar;
    private Spinner mSpinner;
    private NewsgroupSpinnerAdapter mSpinnerAdapter;
    private ImageView mDownArrow;
    private EditText mBodyText;
    private EditText mSubjectText;
    private static String newsgroupId = null;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if(bundle != null) {
                if(bundle.getBoolean(PostService.POST_SUCCESS)){
                    Toast.makeText(getApplicationContext(),"Posted!",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(),"Post Failed",Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        newsgroupId = getIntent().getStringExtra("newsgroup_id");

        mBodyText = (EditText) findViewById(R.id.body_edittext);
        mSubjectText = (EditText) findViewById(R.id.subject_edittext);

        setUpToolbar();

        mSpinnerAdapter = new NewsgroupSpinnerAdapter(this,null,0);
        mSpinner = (Spinner) findViewById(R.id.spinner);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                newsgroupId = String.valueOf(id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSpinner.setAdapter(mSpinnerAdapter);

        mDownArrow = (ImageView) findViewById(R.id.down_arrow_image);
        mDownArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpinner.performClick();
            }
        });

        getSupportLoaderManager().initLoader(NEWSGROUP_LOADER, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mBroadcastReceiver, new IntentFilter(PostService.NOTIFICATION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
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
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_back_arrow);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        setSupportActionBar(mToolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.csh_pink_dark));
        }

        getSupportActionBar().setTitle(getString(R.string.activity_new_post_title));

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = WebNewsContract.NewsGroupEntry.NAME;
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
        if(newsgroupId != null) {
            mSpinner.setSelection(Utility.cursorSearch(Integer.valueOf(newsgroupId),data));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSpinnerAdapter.swapCursor(null);
    }

    private void post() {
        Intent intent = new Intent(this, PostService.class);
        intent.putExtra(PostService.BODY,mBodyText.getText().toString());
        intent.putExtra(PostService.NEWSGROUP_ID,newsgroupId);
        intent.putExtra(PostService.SUBJECT,mSubjectText.getText().toString());
        Toast.makeText(this,"Posting...",Toast.LENGTH_SHORT).show();
        startService(intent);
    }
}
