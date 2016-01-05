package edu.csh.cshwebnews.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import edu.csh.cshwebnews.R;

public class ReplyActivity extends AppCompatActivity {

    @Bind(R.id.tool_bar) Toolbar mToolbar;
    @Bind(R.id.body_edittext) EditText mBodyText;
    @Bind(R.id.subject_edittext) EditText mSubjectText;
    @Bind(R.id.newsgroup_text) TextView mNewsgroupTextView;
    Intent extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);
        extras = getIntent();

        ButterKnife.bind(this);

        mBodyText.requestFocus();

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Reply");
        getSupportActionBar().setHomeButtonEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.csh_pink_dark));
        }

        mSubjectText.setText("Re: " + extras.getStringExtra("subject"));
        mNewsgroupTextView.setText(extras.getStringExtra("newsgroup"));

    }

}
