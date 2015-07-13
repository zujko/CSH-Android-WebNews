package edu.csh.cshwebnews.activities;

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
import android.widget.ImageView;
import android.widget.Spinner;

import edu.csh.cshwebnews.R;
import edu.csh.cshwebnews.adapters.NewsgroupSpinnerAdapter;
import edu.csh.cshwebnews.database.WebNewsContract;

public class NewPostActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int NEWSGROUP_LOADER = 0;
    private Toolbar mToolbar;
    private Spinner mSpinner;
    private NewsgroupSpinnerAdapter mSpinnerAdapter;
    private ImageView mDownArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        setUpToolbar();

        mSpinnerAdapter = new NewsgroupSpinnerAdapter(this,null,0);
        mSpinner = (Spinner) findViewById(R.id.spinner);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
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
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSpinnerAdapter.swapCursor(null);
    }
}
