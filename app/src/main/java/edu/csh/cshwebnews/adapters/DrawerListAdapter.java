package edu.csh.cshwebnews.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.csh.cshwebnews.R;
import edu.csh.cshwebnews.database.WebNewsContract;

public class DrawerListAdapter extends CursorAdapter {

    public static class ViewHolder {
        public final TextView newsgroupTextView;
        public final TextView unreadTextView;

        public ViewHolder(View view) {
            newsgroupTextView = (TextView) view.findViewById(R.id.drawer_list_newsgroup_textview);
            unreadTextView = (TextView) view.findViewById(R.id.drawer_list_unread_textview);
        }
    }

    public DrawerListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public long getItemId(int position) {
        Cursor cur = getCursor();
        cur.moveToPosition(position);
        return (long) cur.getInt(WebNewsContract.NEWSGROUP_COL_ID);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.drawer_list_item,parent,false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.newsgroupTextView.setText(cursor.getString(WebNewsContract.NEWSGROUP_COL_NAME));
        if(cursor.getInt(WebNewsContract.NEWSGROUP_COL_UNREAD_COUNT) > 0){
            if(cursor.getInt(WebNewsContract.NEWSGROUP_COL_UNREAD_COUNT) > 99) {
                viewHolder.unreadTextView.setText("99+");
            } else {
                viewHolder.unreadTextView.setText(String.valueOf(cursor.getInt(WebNewsContract.NEWSGROUP_COL_UNREAD_COUNT)));
            }
            viewHolder.unreadTextView.setVisibility(View.VISIBLE);
            viewHolder.newsgroupTextView.setTypeface(null, Typeface.BOLD);
            viewHolder.unreadTextView.setTypeface(null,Typeface.BOLD);
        } else {
            viewHolder.unreadTextView.setVisibility(View.GONE);
            viewHolder.newsgroupTextView.setTypeface(null, Typeface.NORMAL);
            viewHolder.unreadTextView.setTypeface(null, Typeface.NORMAL);
        }
    }
}
