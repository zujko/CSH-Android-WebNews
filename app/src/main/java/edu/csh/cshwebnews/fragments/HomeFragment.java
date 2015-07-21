package edu.csh.cshwebnews.fragments;


import android.content.ContentResolver;
import android.content.SyncStatusObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.commonsware.cwac.merge.MergeAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import edu.csh.cshwebnews.R;
import edu.csh.cshwebnews.Utility;
import edu.csh.cshwebnews.adapters.PostListAdapter;
import edu.csh.cshwebnews.database.WebNewsContract;
import edu.csh.cshwebnews.network.WebNewsSyncAdapter;

public class HomeFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private MergeAdapter mMergeAdapter;
    private PostListAdapter mTodayAdapter;
    private PostListAdapter mYesterdayAdapter;
    private PostListAdapter mThisMonthAdapter;
    private ListView mListview;
    private SwipeRefreshLayout swipeContainer;
    private SyncStatusObserver mSyncObserver;
    private Object mSyncHandle;
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

        if(isAdded()) {
            Bundle args = getArguments();
            args.putString("until","this month");
            args.putBoolean("get_newsgroups",false);
            WebNewsSyncAdapter.syncImmediately(getActivity(),args);
        }

        setUpRefreshLayout(rootView);

        todayText           = (TextView) inflater.inflate(R.layout.divider_fullbleed,null);
        yesterdayText       = (TextView) inflater.inflate(R.layout.divider_fullbleed,null);
        thisMonthText = (TextView) inflater.inflate(R.layout.divider_fullbleed,null);

        mMergeAdapter = new MergeAdapter();
        setUpMergeAdapter(inflater);

        mListview = (ListView) rootView.findViewById(R.id.listview);
        mListview.setAdapter(mMergeAdapter);

        getLoaderManager().initLoader(TODAY_LOADER, getArguments(), this);
        getLoaderManager().initLoader(YESTERDAY_LOADER, getArguments(), this);
        getLoaderManager().initLoader(THIS_MONTH_LOADER, getArguments(), this);
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
        mSyncObserver = new SyncStatusObserver() {
            @Override
            public void onStatusChanged(int which) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(isAdded() && !Utility.isSyncActive(Utility.getAccount(getActivity()),getString(R.string.content_authority))) {
                            swipeContainer.setRefreshing(false);
                        }
                    }
                });
            }
        };
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
}
