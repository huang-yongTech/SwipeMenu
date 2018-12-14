package com.gcit.hy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gcit.hy.R;
import com.hy.slidedraglayout.SlideDragLayout;

import java.util.List;

public class ListViewAdapter extends BaseAdapter {
    private Context context;
    private List<String> list;

    public ListViewAdapter(Context context, List<String> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_view_sweep, parent, false);
            holder.sweepView = convertView.findViewById(R.id.sweep_view);
            holder.contentView = convertView.findViewById(R.id.content_view);
            holder.deleteView = convertView.findViewById(R.id.delete_view);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.contentView.setText(list.get(position));
        return convertView;
    }

    class ViewHolder {
        SlideDragLayout sweepView;
        TextView contentView;
        TextView deleteView;
    }
}