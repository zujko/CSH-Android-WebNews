package edu.csh.cshwebnews.activities;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.danlew.android.joda.JodaTimeAndroid;

import edu.csh.cshwebnews.R;
import edu.csh.cshwebnews.ScrimInsetsFrameLayout;
import edu.csh.cshwebnews.Utility;
import edu.csh.cshwebnews.adapters.DrawerListAdapter;
import edu.csh.cshwebnews.database.WebNewsContract;
import edu.csh.cshwebnews.fragments.PostListFragment;
import edu.csh.cshwebnews.network.WebNewsSyncAdapter;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    final int NEWSGROUP_LOADER = 1;
    DrawerListAdapter mListAdapter;
    ScrimInsetsFrameLayout mInsetsFrameLayout;
    ListView drawerListView;
    ActionBarDrawerToggle drawerToggle;
    Toolbar toolBar;
    DrawerLayout drawer;
    String newsgroupNameState;
    Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WebNewsSyncAdapter.syncImmediately(getApplicationContext(), new Bundle());

        JodaTimeAndroid.init(this);

        toolBar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolBar);

        createFragment(savedInstanceState);

        createHeader();

        createNavigationDrawer();

        getSupportLoaderManager().initLoader(NEWSGROUP_LOADER, null, this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("name",newsgroupNameState);
        getSupportFragmentManager().putFragment(outState, "currentFragment", currentFragment);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String sortOrder = WebNewsContract.NewsGroupEntry.NAME;

        Uri newsgroupUri = WebNewsContract.NewsGroupEntry.CONTENT_URI;

        return new CursorLoader(this,
                newsgroupUri,
                DrawerListAdapter.NEWSGROUP_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mListAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mListAdapter.swapCursor(null);
    }

    private void selectNewsgroup(final long id, int position, final View view) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getSupportFragmentManager().beginTransaction().remove(currentFragment).commit();
                currentFragment = new PostListFragment();
                Bundle args = new Bundle();
                args.putString("newsgroup_id", String.valueOf(id));
                args.putString("as_threads","false");
                currentFragment.setArguments(args);
                getSupportFragmentManager().beginTransaction().replace(R.id.frag_container, currentFragment).commit();
                TextView newsgroup = (TextView) view.findViewById(R.id.drawer_list_newsgroup_textview);
                getSupportActionBar().setTitle(newsgroup.getText());
                newsgroupNameState = newsgroup.getText().toString();
            }
        }, 300);
    }

    private void createFragment(Bundle savedInstanceState) {
        if(savedInstanceState != null){
            currentFragment = getSupportFragmentManager().getFragment(savedInstanceState,"currentFragment");
            newsgroupNameState = savedInstanceState.getString("name");
            getSupportActionBar().setTitle(savedInstanceState.getString("name"));
        } else {
            currentFragment = new PostListFragment();
            Bundle args = new Bundle();
            args.putString("newsgroup_id", "null");
            args.putString("as_threads","false");
            currentFragment.setArguments(args);
            getSupportFragmentManager().beginTransaction().replace(R.id.frag_container, currentFragment).commit();
            newsgroupNameState = "Home";
            getSupportActionBar().setTitle("Home");
        }
    }

    private void createNavigationDrawer() {
        mListAdapter = new DrawerListAdapter(getApplicationContext(),null,0);
        drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                drawerListView.setItemChecked(position, true);
                drawer.closeDrawer(mInsetsFrameLayout);
                selectNewsgroup(id,position,view);
            }
        });
        drawerListView.setAdapter(mListAdapter);

        drawer = (DrawerLayout) findViewById(R.id.DrawerLayout);
        drawer.setStatusBarBackground(R.color.csh_pink_dark);

        drawerToggle = new ActionBarDrawerToggle(this,drawer,toolBar,R.string.app_name,R.string.app_name) {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if(drawerView != null){
                    super.onDrawerSlide(drawerView, 0);
                } else {
                    super.onDrawerSlide(drawerView, slideOffset);
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawer.setDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    private void createHeader() {
        mInsetsFrameLayout = (ScrimInsetsFrameLayout) findViewById(R.id.scrimInsetsFrameLayout);
        drawerListView = (ListView) findViewById(R.id.drawer_listview);
        ViewGroup header = (ViewGroup) getLayoutInflater().inflate(R.layout.drawer_header, drawerListView, false);

        TextView username = (TextView) header.findViewById(R.id.drawer_header_name_textview);
        TextView email = (TextView) header.findViewById(R.id.drawer_header_email_textview);
        ImageView userImage = (ImageView) header.findViewById(R.id.drawer_header_user_imageview);

        Cursor cur = getContentResolver().query(WebNewsContract.UserEntry.CONTENT_URI,null,null,null,null);
        cur.moveToFirst();
        username.setText(cur.getString(WebNewsContract.USER_COL_USERNAME));

        String emailStr = cur.getString(WebNewsContract.USER_COL_EMAIL);
        if(emailStr == null) {
            Picasso.with(getApplicationContext())
                    .load(R.drawable.placeholder)
                    .tag(this)
                    .into(userImage);
        } else {
            email.setText(emailStr);
            String emailHash = Utility.md5Hex(emailStr);
            Picasso.with(getApplicationContext())
                    .load("http://www.gravatar.com/avatar/" + emailHash + "?s=70&d=mm")
                    .placeholder(R.drawable.placeholder)
                    .tag(this)
                    .into(userImage);
        }

        header.setEnabled(false);
        header.setOnClickListener(null);

        cur.close();
        drawerListView.addHeaderView(header);
    }
}
