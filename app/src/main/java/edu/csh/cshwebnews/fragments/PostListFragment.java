package edu.csh.cshwebnews.fragments;

import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.ListView;

import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import edu.csh.cshwebnews.R;
import edu.csh.cshwebnews.Utility;
import edu.csh.cshwebnews.WebNewsApplication;
import edu.csh.cshwebnews.activities.NewPostActivity;
import edu.csh.cshwebnews.activities.PostActivity;
import edu.csh.cshwebnews.adapters.PostListAdapter;
import edu.csh.cshwebnews.database.WebNewsContract;
import edu.csh.cshwebnews.events.FinishLoadingEvent;
import edu.csh.cshwebnews.jobs.LoadPostsJob;
import edu.csh.cshwebnews.jobs.ReadPostJob;
import edu.csh.cshwebnews.network.WebNewsSyncAdapter;


public class PostListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AbsListView.OnScrollListener, AdapterView.OnItemClickListener {

    @Bind(R.id.fab) FloatingActionButton floatingActionButton;
    @Bind(R.id.listview) ListView mListView;
    @Bind(R.id.swipeContainer) SwipeRefreshLayout swipeContainer;
    private PostListAdapter mListAdapter;
    private View mProgressBarLayout;
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

        ButterKnife.bind(this,rootView);

        mProgressBarLayout = inflater.inflate(R.layout.post_list_layout_footer,null);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAdded()) {
                    Intent intent = new Intent(getActivity(), NewPostActivity.class);
                    intent.putExtra("newsgroup_id", getArguments().getString("newsgroup_id"));
                    startActivity(intent);
                }
            }
        });

        setupRefreshLayout(rootView);

        if(!Utility.isNetworkConnected(getActivity())) {
            noNetworkSnackbar(rootView);
        }

        mListAdapter = new PostListAdapter(getActivity(),null,0);
        mListView.setFooterDividersEnabled(false);
        mListView.addFooterView(mProgressBarLayout);
        mListView.setOnScrollListener(this);
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(this);

        getLoaderManager().initLoader(POST_LOADER, getArguments(), this);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState != null) {
            instanceState = savedInstanceState;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mListView != null) {
            outState.putParcelable("state", mListView.onSaveInstanceState());
        }
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
        if(instanceState != null && instanceState.getParcelable("state") != null) {
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

        Snackbar.make(rootView, getString(R.string.error_no_network_simple),Snackbar.LENGTH_INDEFINITE)
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
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Utility.isNetworkConnected(getActivity())) {
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
                args.putInt("offset", totalItemCount);
                WebNewsApplication.getJobManager().addJobInBackground(new LoadPostsJob(args,getActivity().getApplicationContext()));
                loading = true;
            }
        }
        if(loading && (firstVisibleItem + visibleItemCount) == totalItemCount) {
            if(mListView.getFooterViewsCount() == 0) {
                mListView.addFooterView(mProgressBarLayout);
            }
        }

    }

    public void onEventMainThread(FinishLoadingEvent event) {
        mListView.removeFooterView(mProgressBarLayout);
        if(swipeContainer.isRefreshing()) {
            swipeContainer.setRefreshing(false);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String unread = (String) view.getTag(R.string.unreadclass_tag);
        if(unread != null) {
            WebNewsApplication.getJobManager().addJobInBackground(new ReadPostJob((String)view.getTag(R.string.postid_tag), getActivity()));
        }

        Bundle bundle = new Bundle();
        bundle.putString("id", (String) view.getTag(R.string.postid_tag));
        bundle.putString("subject",(String)view.getTag(R.string.subjecttext_tag));
        bundle.putString("body",(String)view.getTag(R.string.bodytext_tag));
        bundle.putString("image_url",(String)view.getTag(R.string.authorurl_tag));
        bundle.putString("simple_date",(String)view.getTag(R.string.simpledate_tag));
        bundle.putString("newsgroup",getArguments().getString("newsgroup"));
        bundle.putString("author_name", (String) view.getTag(R.string.authorname_tag));
        bundle.putBoolean("as_threads",true);
        bundle.putBoolean("load_with_id", true);

        WebNewsApplication.getJobManager().addJobInBackground(new LoadPostsJob(bundle, getActivity()));

        Intent intent = new Intent(getActivity(), PostActivity.class);
        intent.putExtra("bundle",bundle);
        startActivity(intent);
    }


}
