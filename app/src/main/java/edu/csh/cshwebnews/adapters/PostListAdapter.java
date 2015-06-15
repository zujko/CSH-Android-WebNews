package edu.csh.cshwebnews.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import edu.csh.cshwebnews.R;
import edu.csh.cshwebnews.Utility;
import edu.csh.cshwebnews.database.WebNewsContract;

public class PostListAdapter extends CursorAdapter {

    public static class ViewHolder {
        public final ImageView authorImage;
        public final ImageView starImageView;
        public final TextView subjectTextView;
        public final TextView authorTextView;
        public final TextView summaryTextView;
        public final TextView dateTextView;
        public final LinearLayout rootView;

        public ViewHolder(View view) {
            authorImage = (ImageView) view.findViewById(R.id.list_item_author_profile_image);
            starImageView = (ImageView) view.findViewById(R.id.list_item_star_image);
            subjectTextView = (TextView) view.findViewById(R.id.list_item_subject);
            authorTextView = (TextView) view.findViewById(R.id.list_item_author);
            summaryTextView = (TextView) view.findViewById(R.id.list_item_post_summary);
            dateTextView = (TextView) view.findViewById(R.id.list_item_post_date);
            rootView = (LinearLayout) view.findViewById(R.id.list_item_root_layout);
        }
    }

    public PostListAdapter(Context context, Cursor c,int flags) {
        super(context,c,flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_post_layout,parent,false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

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
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        //TODO use a tint for the color change
        if(cursor.getInt(WebNewsContract.COL_PERSONAL_LEVEL) == 3) {
            viewHolder.rootView.setBackgroundColor(context.getResources().getColor(R.color.post_green));
        } else {
            viewHolder.rootView.setBackgroundColor(context.getResources().getColor(R.color.material_dark));
        }

        viewHolder.authorTextView.setText(cursor.getString(WebNewsContract.COL_AUTHOR_NAME));

        String emailHash = Utility.md5Hex(cursor.getString(WebNewsContract.COL_AUTHOR_EMAIL));

        Picasso.with(context)
                .load("http://www.gravatar.com/avatar/"+emailHash+"?s=60&d=mm")
                .placeholder(R.drawable.placeholder)
                .tag(context)
                .into(viewHolder.authorImage);

        viewHolder.starImageView.setImageResource(R.drawable.ic_star_border_white_48dp);

        viewHolder.subjectTextView.setText(cursor.getString(WebNewsContract.COL_SUBJECT));

        viewHolder.summaryTextView.setText(cursor.getString(WebNewsContract.COL_BODY));

        viewHolder.dateTextView.setText(cursor.getString(WebNewsContract.COL_CREATED_AT).substring(0, 10));
    }
}
