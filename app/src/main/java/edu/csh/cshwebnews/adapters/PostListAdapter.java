package edu.csh.cshwebnews.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import edu.csh.cshwebnews.R;
import edu.csh.cshwebnews.database.WebNewsContract;

public class PostListAdapter extends CursorAdapter {

    public static class ViewHolder {
        public final ImageView authorImage;
        public final ImageView starImageView;
        public final TextView subjectTextView;
        public final TextView authorTextView;
        public final TextView summaryTextView;
        public final TextView dateTextView;
        public final RelativeLayout rootView;

        public ViewHolder(View view) {
            authorImage = (ImageView) view.findViewById(R.id.list_item_author_profile_image);
            starImageView = (ImageView) view.findViewById(R.id.list_item_star_image);
            subjectTextView = (TextView) view.findViewById(R.id.list_item_subject);
            authorTextView = (TextView) view.findViewById(R.id.list_item_author);
            summaryTextView = (TextView) view.findViewById(R.id.list_item_post_summary);
            dateTextView = (TextView) view.findViewById(R.id.list_item_post_date);
            rootView = (RelativeLayout) view.findViewById(R.id.list_item_root_layout);
        }
    }

    public PostListAdapter(Context context, Cursor c,int flags) {
        super(context,c,flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_post_layout,parent,false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(R.string.viewholder_tag,viewHolder);

        return view;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        Cursor cur = getCursor();
        cur.moveToPosition(position);
        return (long) cur.getInt(WebNewsContract.COL_ID);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag(R.string.viewholder_tag);
        view.setTag(R.string.postid_tag, cursor.getString(WebNewsContract.COL_ID));

        //TODO use a tint for the color change
        if(cursor.getInt(WebNewsContract.COL_PERSONAL_LEVEL) == 3) {
            viewHolder.rootView.setBackgroundColor(context.getResources().getColor(R.color.post_green));
        } else {
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.item_background_color,typedValue,true);
            viewHolder.rootView.setBackgroundColor(context.getResources().getColor(typedValue.resourceId));
            typedValue = null;
        }

        viewHolder.authorTextView.setText(cursor.getString(WebNewsContract.COL_AUTHOR_NAME));
        viewHolder.subjectTextView.setText(cursor.getString(WebNewsContract.COL_SUBJECT));
        viewHolder.dateTextView.setText(cursor.getString(WebNewsContract.COL_CREATED_AT));

        if(cursor.getString(WebNewsContract.COL_UNREAD_CLASS) != null) {
            viewHolder.subjectTextView.setTypeface(null, Typeface.BOLD);
            viewHolder.authorTextView.setTypeface(null,Typeface.BOLD);
            viewHolder.dateTextView.setTypeface(null,Typeface.BOLD);
        } else {
            viewHolder.subjectTextView.setTypeface(null,Typeface.NORMAL);
            viewHolder.authorTextView.setTypeface(null,Typeface.NORMAL);
            viewHolder.dateTextView.setTypeface(null,Typeface.NORMAL);
        }

        // If the post is starred, make the star image yellow
        if(cursor.getInt(WebNewsContract.COL_IS_STARRED) == 1) {
            viewHolder.starImageView.setImageResource(R.drawable.ic_star_yellow);
        } else {
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.star_color,typedValue,true);
            viewHolder.starImageView.setImageResource(typedValue.resourceId);
            typedValue = null;
        }

        viewHolder.summaryTextView.setText(cursor.getString(WebNewsContract.COL_BODY_SUMMARY));

        Picasso.with(context)
                .load(cursor.getString(WebNewsContract.COL_AUTHOR_AVATAR_URL)+"&d=mm")
                .placeholder(R.drawable.placeholder)
                .resize(45,45)
                .tag(context)
                .noFade()
                .into(viewHolder.authorImage);

        view.setTag(R.string.unreadclass_tag, cursor.getString(WebNewsContract.COL_UNREAD_CLASS));
        view.setTag(R.string.subjecttext_tag, cursor.getString(WebNewsContract.COL_SUBJECT));
        view.setTag(R.string.bodytext_tag, cursor.getString(WebNewsContract.COL_BODY));
        view.setTag(R.string.authorurl_tag, cursor.getString(WebNewsContract.COL_AUTHOR_AVATAR_URL));
        view.setTag(R.string.simpledate_tag, cursor.getString(WebNewsContract.COL_CREATED_AT));
        view.setTag(R.string.authorname_tag, cursor.getString(WebNewsContract.COL_AUTHOR_NAME));
    }
}
