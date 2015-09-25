package edu.csh.cshwebnews.fragments;


import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.commonsware.cwac.merge.MergeAdapter;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import edu.csh.cshwebnews.R;
import edu.csh.cshwebnews.Utility;
import edu.csh.cshwebnews.adapters.PostAdapter;
import edu.csh.cshwebnews.database.WebNewsContract;


public class PostFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

    private ListView mPostListView;
    @Bind(R.id.post_head_star_image) ImageView mStarImage;
    @Bind(R.id.post_head_author_image) ImageView mAuthorImage;
    @Bind(R.id.post_head_subject_text) TextView mSubjectText;
    @Bind(R.id.post_head_body_text) TextView mBodyText;
    @Bind(R.id.post_head_date_text) TextView mDateText;
    @Bind(R.id.post_head_newsgroup_text) TextView mNewsgroupText;
    @Bind(R.id.post_head_author_text) TextView mAuthorNameText;
    private PostAdapter mPostAdapter;
    private MergeAdapter mMergeAdapter;

    public static final int POST_LOADER = 6;
    private static boolean setExpandableItems;
    public static final String LIST_INSTANCE_STATE = "SAVED_STATE";
    private Parcelable mListInstanceState;

    public PostFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            mListInstanceState = savedInstanceState.getParcelable(LIST_INSTANCE_STATE);
            setExpandableItems = false;
        } else{
            Utility.expandedStates = new HashMap<>();
            setExpandableItems = true;
        }


        View rootView = inflater.inflate(R.layout.fragment_post,container,false);
        mPostListView = (ListView) rootView.findViewById(R.id.post_list);

        mMergeAdapter = new MergeAdapter();

        createHeader(savedInstanceState);

        mPostAdapter = new PostAdapter(getActivity(),null,0);
        mMergeAdapter.addAdapter(mPostAdapter);
        mPostListView.setOnItemClickListener(this);
        mPostListView.setAdapter(mMergeAdapter);

        if(mListInstanceState != null) {
            mPostListView.onRestoreInstanceState(mListInstanceState);
        }

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

    private void createHeader(Bundle savedInstanceState) {
        LayoutInflater inflater = getLayoutInflater(savedInstanceState);
        RelativeLayout rootLayout = (RelativeLayout) inflater.inflate(R.layout.post_head_layout,null);
        ButterKnife.bind(this, rootLayout);

        mSubjectText.setText(getArguments().getString("subject"));
        mBodyText.setText(getArguments().getString("body"));
        mDateText.setText(getArguments().getString("simple_date"));
        mNewsgroupText.setText(getArguments().getString("newsgroup"));
        mAuthorNameText.setText(getArguments().getString("author_name"));

        Picasso.with(getActivity())
                .load(getArguments().getString("image_url"))
                .resize(45,45)
                .noFade()
                .placeholder(R.drawable.placeholder)
                .into(mAuthorImage);

        mMergeAdapter.addView(rootLayout);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(setExpandableItems) {
            int size = data.getCount();
            for(int x=0; x<size ; x++) {
                data.moveToPosition(x);
                Utility.expandedStates.put(data.getString(WebNewsContract.COL_ID),false);
            }
            data.moveToFirst();
        }
        mPostAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPostAdapter.swapCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(Utility.expandedStates.get(view.getTag(R.string.postid_tag))) {
            view.findViewById(R.id.post_summary_text).setVisibility(View.VISIBLE);
            view.findViewById(R.id.post_date_text).setVisibility(View.VISIBLE);
            view.findViewById(R.id.post_head_newsgroup_text).setVisibility(View.GONE);
            view.findViewById(R.id.post_head_full_date_text).setVisibility(View.GONE);
            view.findViewById(R.id.post_head_view_headers_text).setVisibility(View.GONE);
            view.findViewById(R.id.post_body_text).setVisibility(View.GONE);
            Utility.expandedStates.put((String)view.getTag(R.string.postid_tag),false);
        } else {
            view.findViewById(R.id.post_summary_text).setVisibility(View.GONE);
            view.findViewById(R.id.post_date_text).setVisibility(View.GONE);
            view.findViewById(R.id.post_head_newsgroup_text).setVisibility(View.VISIBLE);
            view.findViewById(R.id.post_head_full_date_text).setVisibility(View.VISIBLE);
            view.findViewById(R.id.post_head_view_headers_text).setVisibility(View.VISIBLE);
            view.findViewById(R.id.post_body_text).setVisibility(View.VISIBLE);
            Utility.expandedStates.put((String)view.getTag(R.string.postid_tag),true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(LIST_INSTANCE_STATE, mPostListView.onSaveInstanceState());
    }
}
