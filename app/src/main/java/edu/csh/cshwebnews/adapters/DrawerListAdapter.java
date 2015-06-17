package edu.csh.cshwebnews.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.csh.cshwebnews.R;
import edu.csh.cshwebnews.database.WebNewsContract;

public class DrawerListAdapter extends CursorAdapter {

    public static final String[] NEWSGROUP_COLUMNS = {
            WebNewsContract.NewsGroupEntry.TABLE_NAME+"."+ WebNewsContract.NewsGroupEntry._ID,
            WebNewsContract.NewsGroupEntry.DESCRIPTION,
            WebNewsContract.NewsGroupEntry.MAX_UNREAD_LEVEL,
            WebNewsContract.NewsGroupEntry.NAME,
            WebNewsContract.NewsGroupEntry.NEWEST_POST_AT,
            WebNewsContract.NewsGroupEntry.OLDEST_POST_AT,
            WebNewsContract.NewsGroupEntry.POSTING_ALLOWED,
            WebNewsContract.NewsGroupEntry.UNREAD_COUNT
    };
    final int COL_ID = 0;
    final int COL_DESC = 1;
    final int COL_MAX_UNREAD = 2;
    final int COL_NAME = 3;
    final int COL_NEWS_POST_AT = 4;
    final int COL_OLDEST_POST_AT = 5;
    final int COL_POSTING_ALLOWED = 6;
    final int COL_UNREAD_COUNT = 7;

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
        return (long) cur.getInt(0);
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
        viewHolder.newsgroupTextView.setText(cursor.getString(COL_NAME));
        Log.d("NEWSGROUP ADAPTER",cursor.getString(COL_NAME)+": "+cursor.getInt(COL_UNREAD_COUNT));
        if(cursor.getInt(COL_UNREAD_COUNT) > 0){
            viewHolder.unreadTextView.setText(cursor.getInt(COL_UNREAD_COUNT));
        }
    }
}
