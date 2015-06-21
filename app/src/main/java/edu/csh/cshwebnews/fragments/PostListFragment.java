package edu.csh.cshwebnews.fragments;

import android.database.Cursor;
import android.net.Uri;
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

import edu.csh.cshwebnews.R;
import edu.csh.cshwebnews.adapters.PostListAdapter;
import edu.csh.cshwebnews.database.WebNewsContract;
import edu.csh.cshwebnews.network.WebNewsSyncAdapter;


public class PostListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private PostListAdapter mListAdapter;
    private ListView mListView;
    Bundle instanceState;

    private static final int POST_LOADER = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        WebNewsSyncAdapter.syncImmediately(getActivity(), getArguments());
        mListAdapter = new PostListAdapter(getActivity(),null,0);
        View rootView = inflater.inflate(R.layout.fragment_main,container,false);
        mListView = (ListView) rootView.findViewById(R.id.listview);
        mListView.setAdapter(mListAdapter);

        getLoaderManager().initLoader(POST_LOADER, getArguments(), this);
        return rootView;
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
        outState.putParcelable("state", mListView.onSaveInstanceState());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = WebNewsContract.PostEntry._ID + " DESC";

        Uri postUri = WebNewsContract.PostEntry.CONTENT_URI;

        if(args == null || args.getString("newsgroup_id").equals("null")) {
            return new CursorLoader(getActivity(),
                    postUri,
                    WebNewsContract.POST_COLUMNS,
                    null,
                    null,
                    sortOrder);
        } else {
            return new CursorLoader(getActivity(),
                    postUri,
                    WebNewsContract.POST_COLUMNS,
                    WebNewsContract.PostEntry.NEWSGROUP_IDS + " LIKE ?",
                    new String[]{"%"+args.getString("newsgroup_id")+"%"},
                    sortOrder);
        }
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
}
