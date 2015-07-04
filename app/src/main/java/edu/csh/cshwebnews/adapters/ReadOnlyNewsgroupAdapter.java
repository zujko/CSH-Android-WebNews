package edu.csh.cshwebnews.adapters;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.csh.cshwebnews.R;
import edu.csh.cshwebnews.database.WebNewsContract;

public class ReadOnlyNewsgroupAdapter extends CursorAdapter {

    public static class ViewHolder {
        public final TextView newsgroupTextView;
        public final TextView unreadTextView;

        public ViewHolder(View view) {
            newsgroupTextView = (TextView) view.findViewById(R.id.drawer_list_newsgroup_textview);
            unreadTextView = (TextView) view.findViewById(R.id.drawer_list_unread_textview);
        }
    }

    public ReadOnlyNewsgroupAdapter(Context context, Cursor c, int flags) {
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
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.inactive_color,typedValue,true);

        viewHolder.newsgroupTextView.setText(cursor.getString(WebNewsContract.NEWSGROUP_COL_NAME));
        if(cursor.getInt(WebNewsContract.NEWSGROUP_COL_UNREAD_COUNT) > 0){
            viewHolder.unreadTextView.setText(cursor.getInt(WebNewsContract.NEWSGROUP_COL_UNREAD_COUNT));
        }
        viewHolder.newsgroupTextView.setTextColor(context.getResources().getColor(typedValue.resourceId));
        viewHolder.unreadTextView.setTextColor(context.getResources().getColor(typedValue.resourceId));
    }
}