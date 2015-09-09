package edu.csh.cshwebnews.fragments;


import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import edu.csh.cshwebnews.R;
import edu.csh.cshwebnews.adapters.PostAdapter;
import edu.csh.cshwebnews.database.WebNewsContract;


public class PostFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

    @Bind(R.id.post_list) ListView mPostListView;
    @Bind(R.id.post_head_star_image) ImageView mStarImage;
    @Bind(R.id.post_head_subject_text) TextView mSubjectText;
    private PostAdapter mPostAdapter;

    public static final int POST_LOADER = 6;

    public PostFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_post,container,false);
        ButterKnife.bind(this,rootView);
        mSubjectText.setText(getArguments().getString("subject"));

        mPostAdapter = new PostAdapter(getActivity(),null,0);
        mPostListView.setAdapter(mPostAdapter);
        mPostListView.setOnItemClickListener(this);

        getLoaderManager().initLoader(POST_LOADER, getArguments(), this);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = WebNewsContract.PostEntry.RAW_DATE + " DESC";
        String[] selectionArgs = new String[]{"%"+args.getString("id")+"%"};
        String selection = WebNewsContract.PostEntry.ANCESTOR_IDS + " LIKE ?";

        return new CursorLoader(getActivity(),
                WebNewsContract.PostEntry.CONTENT_URI,
                WebNewsContract.POST_COLUMNS,
                selection,
                selectionArgs,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mPostAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPostAdapter.swapCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
