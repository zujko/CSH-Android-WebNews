package edu.csh.cshwebnews.adapters;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.csh.cshwebnews.R;
import edu.csh.cshwebnews.database.WebNewsContract;

public class NewsgroupSpinnerAdapter extends CursorAdapter {

    public static class ViewHolder {
        public final TextView newsgroupTextView;

        public ViewHolder(View view) {
            newsgroupTextView = (TextView) view.findViewById(R.id.item_textview);
        }
    }

    public NewsgroupSpinnerAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.newsgroup_spinner_item,parent,false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.newsgroupTextView.setText(cursor.getString(WebNewsContract.NEWSGROUP_COL_ID));
    }
}
