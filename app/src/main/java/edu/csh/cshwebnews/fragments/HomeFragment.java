package edu.csh.cshwebnews.fragments;


import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.commonsware.cwac.merge.MergeAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import edu.csh.cshwebnews.R;
import edu.csh.cshwebnews.adapters.PostListAdapter;
import edu.csh.cshwebnews.database.WebNewsContract;
import edu.csh.cshwebnews.network.WebNewsSyncAdapter;

public class HomeFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private MergeAdapter mMergeAdapter;
    private PostListAdapter mTodayAdapter;
    private PostListAdapter mYesterdayAdapter;
    private PostListAdapter mThisMonthAdapter;
    private ListView mListview;

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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = WebNewsContract.PostEntry.RAW_DATE + " DESC";
        String[] selectionArgs = null;
        String selection = null;

        Calendar c = Calendar.getInstance();

        switch (id) {
            case TODAY_LOADER:
                selection = WebNewsContract.PostEntry.CREATED_AT + " LIKE ?" +
                        " AND " + WebNewsContract.PostEntry.ANCESTOR_IDS + " = ?";
                selectionArgs = new String[]{"%"+":"+"%","[]"};
                break;
            case YESTERDAY_LOADER:
                selection = WebNewsContract.PostEntry.CREATED_AT +" LIKE ?" +
                        " AND " + WebNewsContract.PostEntry.ANCESTOR_IDS + " = ?";
                SimpleDateFormat monthDate = new SimpleDateFormat("MMM");
                selectionArgs = new String[]{monthDate.format(c.getTime())+ " "+(c.get(Calendar.DAY_OF_MONTH)-1),"[]"};
                break;
            case THIS_MONTH_LOADER:
                selection = WebNewsContract.PostEntry.CREATED_AT + " LIKE ? AND " +
                        WebNewsContract.PostEntry.CREATED_AT + " != ?" +
                        " AND " + WebNewsContract.PostEntry.ANCESTOR_IDS + " = ?";
                SimpleDateFormat monthDateItem = new SimpleDateFormat("MMM");
                selectionArgs = new String[]{monthDateItem.format(c.getTime())+"%",monthDateItem.format(c.getTime())+ " " +(c.get(Calendar.DAY_OF_MONTH)-1),"[]"};
                break;
        }

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
                break;
            case YESTERDAY_LOADER:
                mYesterdayAdapter.swapCursor(data);
                break;
            case THIS_MONTH_LOADER:
                mThisMonthAdapter.swapCursor(data);
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

    private void setUpMergeAdapter(LayoutInflater inflater) {
        TextView todayText = (TextView) inflater.inflate(R.layout.divider_fullbleed,null);
        todayText.setText(getString(R.string.home_today));
        mMergeAdapter.addView(todayText);
        mTodayAdapter = new PostListAdapter(getActivity(),null,0);
        mMergeAdapter.addAdapter(mTodayAdapter);

        TextView yesterdayText = (TextView) inflater.inflate(R.layout.divider_fullbleed,null);
        yesterdayText.setText(getString(R.string.home_yesterday));
        mMergeAdapter.addView(yesterdayText);
        mYesterdayAdapter = new PostListAdapter(getActivity(),null,0);
        mMergeAdapter.addAdapter(mYesterdayAdapter);

        TextView thisMonthTextView = (TextView) inflater.inflate(R.layout.divider_fullbleed,null);
        thisMonthTextView.setText(getString(R.string.home_this_month));
        mMergeAdapter.addView(thisMonthTextView);
        mThisMonthAdapter = new PostListAdapter(getActivity(),null,0);
        mMergeAdapter.addAdapter(mThisMonthAdapter);

    }
}
