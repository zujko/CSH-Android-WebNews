package edu.csh.cshwebnews.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import butterknife.Bind;
import butterknife.ButterKnife;
import edu.csh.cshwebnews.R;


public class PostFragment extends Fragment {

    @Bind(R.id.post_list) ListView mPostListView;

    public PostFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_post,container,false);
        ButterKnife.bind(this,rootView);


        return rootView;
    }


}
