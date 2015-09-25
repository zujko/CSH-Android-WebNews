package edu.csh.cshwebnews.adapters;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import edu.csh.cshwebnews.R;
import edu.csh.cshwebnews.Utility;
import edu.csh.cshwebnews.database.WebNewsContract;

public class PostAdapter extends CursorAdapter {

    public static class ViewHolder {
        @Bind(R.id.post_author_image) ImageView mAuthorImage;
        @Bind(R.id.post_author_text) TextView mAuthorText;
        @Bind(R.id.post_summary_text) TextView mSummaryText;
        @Bind(R.id.post_date_text) TextView mDateText;
        @Bind(R.id.post_head_more_image) ImageView mMoreImage;
        @Bind(R.id.post_head_newsgroup_text) TextView mNewsgroupText;
        @Bind(R.id.post_head_full_date_text) TextView mFullDateText;
        @Bind(R.id.post_head_view_headers_text) TextView mViewHeadersClickableText;
        @Bind(R.id.post_head_headers_text) TextView mHeadersText;
        @Bind(R.id.post_body_text) TextView mBodyText;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public PostAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_list_item,parent,false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(R.string.viewholder_tag,viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag(R.string.viewholder_tag);
        view.setTag(R.string.postid_tag, cursor.getString(WebNewsContract.COL_ID));

        if(Utility.expandedStates.get(cursor.getString(WebNewsContract.COL_ID))) {
            viewHolder.mSummaryText.setVisibility(View.GONE);
            viewHolder.mDateText.setVisibility(View.GONE);
            viewHolder.mNewsgroupText.setVisibility(View.VISIBLE);
            viewHolder.mFullDateText.setVisibility(View.VISIBLE);
            viewHolder.mViewHeadersClickableText.setVisibility(View.VISIBLE);
            viewHolder.mBodyText.setVisibility(View.VISIBLE);
        } else {
            viewHolder.mSummaryText.setVisibility(View.VISIBLE);
            viewHolder.mDateText.setVisibility(View.VISIBLE);
            viewHolder.mNewsgroupText.setVisibility(View.GONE);
            viewHolder.mFullDateText.setVisibility(View.GONE);
            viewHolder.mViewHeadersClickableText.setVisibility(View.GONE);
            viewHolder.mBodyText.setVisibility(View.GONE);
        }
        Picasso.with(context)
                .load(cursor.getString(WebNewsContract.COL_AUTHOR_AVATAR_URL)+"&d=mm")
                .placeholder(R.drawable.placeholder)
                .resize(45,45)
                .tag(context)
                .noFade()
                .into(viewHolder.mAuthorImage);

        viewHolder.mAuthorText.setText(cursor.getString(WebNewsContract.COL_AUTHOR_NAME));
        viewHolder.mSummaryText.setText(cursor.getString(WebNewsContract.COL_BODY_SUMMARY));
        viewHolder.mDateText.setText(cursor.getString(WebNewsContract.COL_CREATED_AT));
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.more_icon,typedValue,true);
        viewHolder.mMoreImage.setImageResource(typedValue.resourceId);

        viewHolder.mNewsgroupText.setText(cursor.getString(WebNewsContract.COL_NEWSGROUP_IDS));
        //TODO Fix this date to be more readable
        viewHolder.mFullDateText.setText(cursor.getString(WebNewsContract.COL_RAW_DATE));
        viewHolder.mHeadersText.setText(cursor.getString(WebNewsContract.COL_HEADERS));
        viewHolder.mBodyText.setText(cursor.getString(WebNewsContract.COL_BODY));
    }
}
