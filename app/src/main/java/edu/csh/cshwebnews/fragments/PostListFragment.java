package edu.csh.cshwebnews.fragments;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.squareup.picasso.Picasso;

import edu.csh.cshwebnews.R;
import edu.csh.cshwebnews.Utility;
import edu.csh.cshwebnews.activities.NewPostActivity;
import edu.csh.cshwebnews.adapters.PostListAdapter;
import edu.csh.cshwebnews.database.WebNewsContract;
import edu.csh.cshwebnews.network.WebNewsSyncAdapter;


public class PostListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AbsListView.OnScrollListener {

    private PostListAdapter mListAdapter;
    private ListView mListView;
    private View mProgressBarLayout;
    private SyncStatusObserver mSyncObserver;
    private Object mSyncHandle;
    private SwipeRefreshLayout swipeContainer;
    private FloatingActionButton floatingActionButton;
    Bundle instanceState;

    private int visibleThreshold = 5;
    private int currentPage = 0;
    private int previousTotalItemCount = 0;
    private boolean loading = true;
    private int startingPageIndex = 0;

    private static final int POST_LOADER = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main,container,false);
        mProgressBarLayout = inflater.inflate(R.layout.post_list_layout_footer,null);
        floatingActionButton = (FloatingActionButton) rootView.findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isAdded()) {
                    Intent intent = new Intent(getActivity(), NewPostActivity.class);
                    intent.putExtra("newsgroup_id",getArguments().getString("newsgroup_id"));
                    startActivity(intent);
                }
            }
        });

        setupRefreshLayout(rootView);

        if(!Utility.isNetworkConnected(getActivity())) {
            noNetworkSnackbar(rootView);
        }

        mListAdapter = new PostListAdapter(getActivity(),null,0);
        mListView = (ListView) rootView.findViewById(R.id.listview);
        mListView.setFooterDividersEnabled(false);
        mListView.addFooterView(mProgressBarLayout);
        mListView.setOnScrollListener(this);
        mListView.setAdapter(mListAdapter);

        getLoaderManager().initLoader(POST_LOADER, getArguments(), this);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING | ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        mSyncHandle = ContentResolver.addStatusChangeListener(mask, mSyncObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mSyncHandle != null) {
            ContentResolver.removeStatusChangeListener(mSyncHandle);
            mSyncHandle = null;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState != null) {
            instanceState = savedInstanceState;
        }

        mSyncObserver = new SyncStatusObserver() {
            @Override
            public void onStatusChanged(int which) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(isAdded() && !Utility.isSyncActive(Utility.getAccount(getActivity()),getString(R.string.content_authority))) {
                            mListView.removeFooterView(mProgressBarLayout);
                            swipeContainer.setRefreshing(false);
                        }
                    }
                });
            }
        };

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("state", mListView.onSaveInstanceState());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = WebNewsContract.PostEntry.RAW_DATE + " DESC";

        Uri postUri = WebNewsContract.PostEntry.CONTENT_URI;
        String selection;
        String[] selectionArgs = null;

        if (args.getBoolean("only_starred")) {
            selection = WebNewsContract.PostEntry.IS_STARRED + " = 1";
        } else if(args.getBoolean("only_sticky")) {
            selection = WebNewsContract.PostEntry.IS_STICKIED + " = 1";
        } else if(args.getString("newsgroup_id") == null) {
            selection = WebNewsContract.PostEntry.ANCESTOR_IDS + " = ?";
            selectionArgs = new String[]{"[]"};
        }
        else {
            selection = WebNewsContract.PostEntry.NEWSGROUP_IDS + " LIKE ? AND "+ WebNewsContract.PostEntry.ANCESTOR_IDS+ " = ?";
            selectionArgs = new String[]{"%"+args.getString("newsgroup_id")+"%","[]"};
        }

        return new CursorLoader(getActivity(),
                postUri,
                WebNewsContract.POST_COLUMNS,
                selection,
                selectionArgs,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mListAdapter.swapCursor(data);
        if(instanceState != null) {
            mListView.onRestoreInstanceState(instanceState.getParcelable("state"));
        }
        instanceState = null;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mListAdapter.swapCursor(null);
    }

    private void noNetworkSnackbar(final View rootView) {
        swipeContainer.setRefreshing(false);
        //TODO Wait for bug fix so that snackbar will display indefinitely
        // (Currently setting a custom duration does not work)
        Snackbar.make(rootView, getString(R.string.error_no_network_simple),Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.snackbar_refresh), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Utility.isNetworkConnected(getActivity())) {
                            WebNewsSyncAdapter.syncImmediately(getActivity(), getArguments());
                        } else {
                            noNetworkSnackbar(rootView);
                        }
                    }
                })
                .show();
    }

    /**
     * Sets up the swipe to refresh layout
     * @param rootView
     */
    private void setupRefreshLayout(final View rootView) {
        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(Utility.isNetworkConnected(getActivity())) {
                    WebNewsSyncAdapter.syncImmediately(getActivity(), getArguments());
                } else {
                    noNetworkSnackbar(rootView);
                }
            }
        });
        swipeContainer.setColorSchemeResources(R.color.csh_pink,
                R.color.csh_pink_dark, R.color.csh_purple, R.color.csh_purple_dark);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        Picasso picasso = Picasso.with(getActivity());
        if(scrollState == SCROLL_STATE_FLING) {
            picasso.pauseTag(getActivity());
        } else{
            picasso.resumeTag(getActivity());
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        if(totalItemCount < previousTotalItemCount) {
            previousTotalItemCount = totalItemCount;
            if(totalItemCount == 0) loading = true;
        }

        if(loading && (totalItemCount > previousTotalItemCount)) {
            loading = false;
            previousTotalItemCount = totalItemCount;
        }

        if(!loading && (totalItemCount-visibleItemCount)<=(firstVisibleItem+visibleThreshold)){
            if(isAdded()) {
                Bundle args = getArguments();
                args.putInt("offset",totalItemCount);
                args.putBoolean("get_newsgroups",false);
                WebNewsSyncAdapter.syncImmediately(getActivity(),args);
                loading = true;
            }
        }
    }
}
