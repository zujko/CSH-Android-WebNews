package edu.csh.cshwebnews.fragments;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.commonsware.cwac.merge.MergeAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import edu.csh.cshwebnews.R;
import edu.csh.cshwebnews.Utility;
import edu.csh.cshwebnews.activities.NewPostActivity;
import edu.csh.cshwebnews.adapters.PostListAdapter;
import edu.csh.cshwebnews.database.WebNewsContract;
import edu.csh.cshwebnews.events.FinishLoadingEvent;
import edu.csh.cshwebnews.network.WebNewsSyncAdapter;

public class HomeFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

    @Bind(R.id.fab) FloatingActionButton mFloatingActionButton;
    @Bind(R.id.listview) ListView mListView;
    @Bind(R.id.swipeContainer) SwipeRefreshLayout swipeContainer;
    private MergeAdapter mMergeAdapter;
    private PostListAdapter mTodayAdapter;
    private PostListAdapter mYesterdayAdapter;
    private PostListAdapter mThisMonthAdapter;
    private TextView todayText;
    private TextView yesterdayText;
    private TextView thisMonthText;

    private static final int TODAY_LOADER = 0;
    private static final int YESTERDAY_LOADER = 1;
    private static final int THIS_MONTH_LOADER = 3;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main,container,false);

        ButterKnife.bind(this, rootView);

        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAdded()) {
                    Intent intent = new Intent(getActivity(), NewPostActivity.class);
                    startActivity(intent);
                }
            }
        });

        if(isAdded()) {
            Bundle args = getArguments();
            args.putString("until","this month");
            args.putBoolean("get_newsgroups",false);
            WebNewsSyncAdapter.syncImmediately(getActivity(),args);
        }

        setUpRefreshLayout(rootView);

        todayText           = (TextView) inflater.inflate(R.layout.divider_fullbleed,null);
        yesterdayText       = (TextView) inflater.inflate(R.layout.divider_fullbleed,null);
        thisMonthText       = (TextView) inflater.inflate(R.layout.divider_fullbleed,null);

        mMergeAdapter = new MergeAdapter();
        setUpMergeAdapter(inflater);

        mListView.setAdapter(mMergeAdapter);
        mListView.setOnItemClickListener(this);

        getLoaderManager().initLoader(TODAY_LOADER, getArguments(), this);
        getLoaderManager().initLoader(YESTERDAY_LOADER, getArguments(), this);
        getLoaderManager().initLoader(THIS_MONTH_LOADER, getArguments(), this);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
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
        String[] selectionArgs = null;
        String selection = null;

        Calendar c = Calendar.getInstance();

        switch (id) {
            case TODAY_LOADER:
                selection = WebNewsContract.PostEntry.CREATED_AT + " LIKE ?" +
                        " AND " + WebNewsContract.PostEntry.ANCESTOR_IDS + " = ?";
                selectionArgs = new String[]{"%"+":"+"%","[]","%"+Utility.CANCEL_NEWSGROUP_ID+"%"};
                break;
            case YESTERDAY_LOADER:
                selection = WebNewsContract.PostEntry.CREATED_AT +" LIKE ?" +
                        " AND " + WebNewsContract.PostEntry.ANCESTOR_IDS + " = ?";
                SimpleDateFormat monthDate = new SimpleDateFormat("MMM");
                selectionArgs = new String[]{monthDate.format(c.getTime())+ " "+(c.get(Calendar.DAY_OF_MONTH)-1),"[]","%"+Utility.CANCEL_NEWSGROUP_ID+"%"};
                break;
            case THIS_MONTH_LOADER:
                selection = WebNewsContract.PostEntry.CREATED_AT + " LIKE ? AND " +
                        WebNewsContract.PostEntry.CREATED_AT + " != ?" +
                        " AND " + WebNewsContract.PostEntry.ANCESTOR_IDS + " = ?";
                SimpleDateFormat monthDateItem = new SimpleDateFormat("MMM");
                selectionArgs = new String[]{monthDateItem.format(c.getTime())+"%",monthDateItem.format(c.getTime())+ " " +(c.get(Calendar.DAY_OF_MONTH)-1),"[]","%"+Utility.CANCEL_NEWSGROUP_ID+"%"};
                break;
        }

        selection += " AND " + WebNewsContract.PostEntry.NEWSGROUP_IDS + " NOT LIKE ?";

        return new CursorLoader(getActivity(),
                WebNewsContract.PostEntry.CONTENT_URI,
                WebNewsContract.POST_COLUMNS,
                selection,
                selectionArgs,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case TODAY_LOADER:
                mTodayAdapter.swapCursor(data);
                if(data.getCount() == 0) {
                    mMergeAdapter.setActive(todayText,false);
                } else {
                    mMergeAdapter.setActive(todayText,true);
                }
                break;
            case YESTERDAY_LOADER:
                mYesterdayAdapter.swapCursor(data);
                if(data.getCount() == 0) {
                    mMergeAdapter.setActive(yesterdayText,false);
                } else {
                    mMergeAdapter.setActive(yesterdayText,true);
                }
                break;
            case THIS_MONTH_LOADER:
                mThisMonthAdapter.swapCursor(data);
                if(data.getCount() == 0) {
                    mMergeAdapter.setActive(thisMonthText,false);
                } else {
                    mMergeAdapter.setActive(thisMonthText,true);
                }
                break;
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case TODAY_LOADER:
                mThisMonthAdapter.swapCursor(null);
                break;
            case YESTERDAY_LOADER:
                mYesterdayAdapter.swapCursor(null);
                break;
            case THIS_MONTH_LOADER:
                mThisMonthAdapter.swapCursor(null);
        }

    }

    private void setUpRefreshLayout(final View rootView) {
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

    private void noNetworkSnackbar(final View rootView) {
        //TODO Wait for bug fix so that snackbar will display indefinitely
        // (Currently setting a custom duration does not work)
        swipeContainer.setRefreshing(false);
        Snackbar.make(rootView, getString(R.string.error_no_network_simple), Snackbar.LENGTH_LONG)
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

    private void setUpMergeAdapter(LayoutInflater inflater) {
        todayText.setText(getString(R.string.home_today));
        mMergeAdapter.addView(todayText);
        mMergeAdapter.setActive(todayText, false);
        mTodayAdapter = new PostListAdapter(getActivity(),null,0);
        mMergeAdapter.addAdapter(mTodayAdapter);

        yesterdayText.setText(getString(R.string.home_yesterday));
        mMergeAdapter.addView(yesterdayText);
        mMergeAdapter.setActive(yesterdayText, false);
        mYesterdayAdapter = new PostListAdapter(getActivity(), null, 0);
        mMergeAdapter.addAdapter(mYesterdayAdapter);

        thisMonthText.setText(getString(R.string.home_this_month));
        mMergeAdapter.addView(thisMonthText);
        mMergeAdapter.setActive(thisMonthText, false);
        mThisMonthAdapter = new PostListAdapter(getActivity(),null,0);
        mMergeAdapter.addAdapter(mThisMonthAdapter);
    }

    public void onEventMainThread(FinishLoadingEvent event) {
        swipeContainer.setRefreshing(false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PostFragment newFragment = new PostFragment();

        //TODO Start background job to get data about the post and mark the post as unread

        //Send post id to the PostFragment
        Bundle bundle = new Bundle();
        bundle.putLong("id",id);
        newFragment.setArguments(bundle);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.frag_container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
