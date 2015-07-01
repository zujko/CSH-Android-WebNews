package edu.csh.cshwebnews.adapters;


import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import edu.csh.cshwebnews.R;
import edu.csh.cshwebnews.Utility;

public class DrawerListHeaderItemsAdapter extends ArrayAdapter<String> {

    private final Context mContext;
    private String[] mItems;

    public DrawerListHeaderItemsAdapter(Context context, String[] items) {
        super(context, R.layout.drawer_header_items_layout,items);
        this.mContext = context;
        this.mItems = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder holder = null;

        if(rowView == null) {
            LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();

            rowView = inflater.inflate(R.layout.drawer_header_items_layout,parent,false);
            holder = new ViewHolder();
            holder.itemName = (TextView) rowView.findViewById(R.id.drawer_header_list_item);
            holder.itemIcon = (ImageView) rowView.findViewById(R.id.item_icon);

            rowView.setTag(holder);

        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        TypedValue val = new TypedValue();

        switch (position) {
            case Utility.DRAWER_ITEM_HOME:
                mContext.getTheme().resolveAttribute(R.attr.home_icon,val,false);
                holder.itemIcon.setImageResource(val.data);
                break;
            case Utility.DRAWER_ITEM_STARRED:
                mContext.getTheme().resolveAttribute(R.attr.starred_icon,val,false);
                holder.itemIcon.setImageResource(val.data);
                break;
            case Utility.DRAWER_ITEM_STICKIED:
                mContext.getTheme().resolveAttribute(R.attr.stickied_icon,val,false);
                holder.itemIcon.setImageResource(val.data);
                break;
        }
        holder.itemName.setText(Utility.DRAWER_HEADER_ITEMS[position]);

        return rowView;
    }

    @Override
    public String getItem(int position) {
        return mItems[position];
    }

    @Override
    public long getItemId(int position) {
        return (long) position;
    }

    static class ViewHolder {
        TextView itemName;
        ImageView itemIcon;
    }
}
