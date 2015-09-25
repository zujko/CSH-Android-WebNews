package edu.csh.cshwebnews.activities;

import android.animation.ValueAnimator;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.commonsware.cwac.merge.MergeAdapter;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import edu.csh.cshwebnews.R;
import edu.csh.cshwebnews.ScrimInsetsFrameLayout;
import edu.csh.cshwebnews.Utility;
import edu.csh.cshwebnews.WebNewsApplication;
import edu.csh.cshwebnews.adapters.DrawerListAdapter;
import edu.csh.cshwebnews.adapters.DrawerListFooterAdapter;
import edu.csh.cshwebnews.adapters.DrawerListHeaderItemsAdapter;
import edu.csh.cshwebnews.adapters.ReadOnlyNewsgroupAdapter;
import edu.csh.cshwebnews.database.WebNewsContract;
import edu.csh.cshwebnews.events.AnimateToolbarEvent;
import edu.csh.cshwebnews.fragments.HomeFragment;
import edu.csh.cshwebnews.fragments.PostFragment;
import edu.csh.cshwebnews.fragments.PostListFragment;
import edu.csh.cshwebnews.jobs.LoadNewsGroupsJob;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

    @Bind(R.id.tool_bar) Toolbar toolBar;
    @Bind(R.id.DrawerLayout) DrawerLayout drawer;
    @Bind(R.id.drawer_listview) ListView drawerListView;
    DrawerListAdapter mListAdapter;
    ReadOnlyNewsgroupAdapter mReadOnlyAdapter;
    ScrimInsetsFrameLayout mInsetsFrameLayout;
    ActionBarDrawerToggle drawerToggle;
    private static String newsgroupNameState;
    Fragment currentFragment;
    MergeAdapter mergeAdapter;
    private int iconState = 0;
    final int NEWSGROUP_LOADER = 0;
    final int READ_ONLY_NEWSGROUP_LOADER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mergeAdapter = new MergeAdapter();

        createMergeAdapter();

        WebNewsApplication.getJobManager().addJobInBackground(new LoadNewsGroupsJob(getApplicationContext()));

        setSupportActionBar(toolBar);

        createFragment(savedInstanceState);

        createNavigationDrawer();

        if(savedInstanceState != null) {
            if(savedInstanceState.getString("icon","0").equals("1")) {
                onEventMainThread(new AnimateToolbarEvent(true));
            }
        }

        getSupportLoaderManager().initLoader(NEWSGROUP_LOADER, null, this);
        getSupportLoaderManager().initLoader(READ_ONLY_NEWSGROUP_LOADER, null, this);
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
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("name", newsgroupNameState);
        outState.putString("icon", String.valueOf(iconState));
        getSupportFragmentManager().putFragment(outState, "currentFragment", currentFragment);
    }





    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = WebNewsContract.NewsGroupEntry._ID;
        Uri newsgroupUri = WebNewsContract.NewsGroupEntry.CONTENT_URI;
        String[] selectionArgs = null;
        switch (id) {
            case NEWSGROUP_LOADER:
                selectionArgs = new String[]{"1"};
                break;
            case READ_ONLY_NEWSGROUP_LOADER:
                selectionArgs = new String[]{"0"};
                break;
            default:
                Log.e("MainActivity", "Invalid loader id");
        }

        return new CursorLoader(this,
                newsgroupUri,
                WebNewsContract.NEWSGROUP_COLUMNS,
                WebNewsContract.NewsGroupEntry.POSTING_ALLOWED + " = ?",
                selectionArgs,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()){
            case NEWSGROUP_LOADER:
                mListAdapter.swapCursor(data);
                break;
            case READ_ONLY_NEWSGROUP_LOADER:
                mReadOnlyAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()){
            case NEWSGROUP_LOADER:
                mListAdapter.swapCursor(null);
                break;
            case READ_ONLY_NEWSGROUP_LOADER:
                mReadOnlyAdapter.swapCursor(null);
        }
    }

    /**
     * Creates the merge adapter for the navigation drawer by combining all adapters and views
     */
    private void createMergeAdapter() {
        LayoutInflater inflater = getLayoutInflater();

        //Adds space between static items and the header
        mergeAdapter.addView(inflater.inflate(R.layout.space_layout, null));
        //Adds static items
        mergeAdapter.addAdapter(new DrawerListHeaderItemsAdapter(this, Utility.DRAWER_HEADER_ITEMS));
        //Adds divider between static items and newgroups
        mergeAdapter.addView(inflater.inflate(R.layout.divider_text_layout, null));
        //Adds newsgroups
        mListAdapter = new DrawerListAdapter(this,null,0);
        mergeAdapter.addAdapter(mListAdapter);
        //Adds divider between newsgroups and read-only newsgroups
        LinearLayout dividerLayout = (LinearLayout) inflater.inflate(R.layout.divider_text_layout, null);
        TextView textView = (TextView) dividerLayout.findViewById(R.id.divider_text);
        textView.setText("Read-Only");
        mergeAdapter.addView(dividerLayout);

        //Adds read-only newsgroup section
        mReadOnlyAdapter = new ReadOnlyNewsgroupAdapter(this,null,0);
        mergeAdapter.addAdapter(mReadOnlyAdapter);
        //Adds divider
        mergeAdapter.addView(inflater.inflate(R.layout.divider_layout,null));
        //Adds static footer items
        mergeAdapter.addAdapter(new DrawerListFooterAdapter(this,Utility.DRAWER_FOOTER));
        //Adds space at the bottom
        mergeAdapter.addView(inflater.inflate(R.layout.space_layout,null));
    }

    /**
     * Creates a fragment based on information from savedInstanceState, if savedInstanceState is null
     * a "Home" fragment is created
     * @param savedInstanceState
     */
    private void createFragment(Bundle savedInstanceState) {
        if(savedInstanceState != null){
            currentFragment = getSupportFragmentManager().getFragment(savedInstanceState,"currentFragment");
            newsgroupNameState = savedInstanceState.getString("name");
            getSupportActionBar().setTitle(savedInstanceState.getString("name"));
        } else {
            currentFragment = new HomeFragment();
            Bundle args = new Bundle();
            args.putString("newsgroup_id", null);
            args.putBoolean("as_threads",false);
            args.putBoolean("only_starred", false);
            args.putBoolean("only_sticky", false);
            args.getBoolean("only_roots",true);
            currentFragment.setArguments(args);
            getSupportFragmentManager().beginTransaction().replace(R.id.frag_container, currentFragment).commit();
            newsgroupNameState = "Home";
            getSupportActionBar().setTitle("Home");
        }
    }

    /**
     * Creates the navigation drawer
     */
    private void createNavigationDrawer() {
        createHeader();

        drawerListView.setOnItemClickListener(this);

        drawerListView.setAdapter(mergeAdapter);

        drawer.setStatusBarBackground(R.color.csh_pink_dark);

        drawerToggle = new ActionBarDrawerToggle(this,drawer,toolBar,R.string.app_name,R.string.app_name);
        drawer.setDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    /**
     * Creates the header in the navigation drawer
     */
    private void createHeader() {
        mInsetsFrameLayout = (ScrimInsetsFrameLayout) findViewById(R.id.scrimInsetsFrameLayout);

        ViewGroup header = (ViewGroup) getLayoutInflater().inflate(R.layout.drawer_header, drawerListView, false);

        TextView username = (TextView) header.findViewById(R.id.drawer_header_name_textview);
        TextView email = (TextView) header.findViewById(R.id.drawer_header_email_textview);
        ImageView userImage = (ImageView) header.findViewById(R.id.drawer_header_user_imageview);

        Cursor cur = getContentResolver().query(WebNewsContract.UserEntry.CONTENT_URI, null, null, null, null);
        cur.moveToFirst();

        username.setText(cur.getString(WebNewsContract.USER_COL_USERNAME));
        String emailStr = cur.getString(WebNewsContract.USER_COL_EMAIL);
        email.setText(emailStr);

        Picasso.with(this)
                .load(cur.getString(WebNewsContract.USER_COL_AVATAR_URL)+"&s=70")
                .placeholder(R.drawable.placeholder)
                .resize(64,64)
                .tag(this)
                .noFade()
                .into(userImage);

        header.setEnabled(false);
        header.setOnClickListener(null);

        cur.close();
        drawerListView.addHeaderView(header);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch ((int)id) {
            case Utility.DRAWER_FOOTER_SETTINGS_ID:
                //TODO Start Settings activity
                break;
            case Utility.DRAWER_FOOTER_ABOUT_ID:
                //TODO start About activity
                break;
            default:
                drawerListView.setItemChecked(position, true);
                drawer.closeDrawer(mInsetsFrameLayout);
                selectNewsgroup(view);
        }

    }

    /**
     * Starts a newsgroup fragment when one is selected from the navigation drawer
     */
    private void selectNewsgroup(final View view) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //*********************************************
                // FIX THIS TO WORK WITH THE NEW API
                //**********************************************
                getSupportFragmentManager().beginTransaction().remove(currentFragment).commit();

                TextView newsgroup = (TextView) view.findViewById(R.id.drawer_list_newsgroup_textview);
                String title = newsgroup.getText().toString();
                getSupportActionBar().setTitle(title);

                if (title.equals("Home")) {
                    Bundle args = new Bundle();
                    args.putString("newsgroup_id", null);
                    args.putBoolean("only_starred", false);
                    args.putBoolean("only_sticky", false);
                    args.putBoolean("as_threads", false);
                    args.putBoolean("only_roots", true);
                    currentFragment = new HomeFragment();
                    currentFragment.setArguments(args);
                    getSupportFragmentManager().beginTransaction().replace(R.id.frag_container, currentFragment).commit();
                } else {
                    currentFragment = new PostListFragment();
                    Bundle args = createFragmentBundle(title);
                    args.putString("newsgroup", title);
                    currentFragment.setArguments(args);
                    getSupportFragmentManager().beginTransaction().replace(R.id.frag_container, currentFragment).commit();
                }
                newsgroupNameState = title;

            }
        }, 300);
    }

    @Override
    public void onBackPressed() {
        for(Fragment fragment : getSupportFragmentManager().getFragments()) {
            if(fragment instanceof PostFragment) {
                onEventMainThread(new AnimateToolbarEvent(false));
            }
        }
        super.onBackPressed();
    }

    /**
     * Helper function for creating a bundle to pass into a fragment
     * @param id
     * @return
     */
    private Bundle createFragmentBundle(String id) {
        Bundle args = new Bundle();
        switch (id) {
            case "Starred":
                args.putString("newsgroup_id",null);
                args.putBoolean("only_sticky",false);
                args.putBoolean("only_starred",true);
                break;
            case "Stickied":
                args.putString("newsgroup_id",null);
                args.putBoolean("only_starred", false);
                args.putBoolean("only_sticky",true);
                break;
            default:
                args.putString("newsgroup_id", id);
                args.putBoolean("only_starred", false);
                args.putBoolean("only_sticky",false);
                break;
        }
        args.putBoolean("as_threads", false);
        args.putBoolean("only_roots", true);

        return args;
    }

    public void onEventMainThread(AnimateToolbarEvent event) {
        ValueAnimator anim;
        if(event.ANIM_TO_ARROW) {
            iconState = 1;
            anim = ValueAnimator.ofFloat(0, 1);
            getSupportActionBar().setTitle("");
        } else {
            iconState = 0;
            anim = ValueAnimator.ofFloat(1,0);
            getSupportActionBar().setTitle(newsgroupNameState);
        }
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float slideOffset = (Float) valueAnimator.getAnimatedValue();
                drawerToggle.onDrawerSlide(drawer, slideOffset);
            }
        });
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setDuration(300);
        anim.start();
    }
}
