package com.gcit.hy.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.gcit.hy.R;
import com.hy.slideitemlayout.SlideItemLayout;

import java.util.List;

public class RecyclerListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<String> list;
    private Context context;

    public RecyclerListAdapter(List<String> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_view_sweep, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final MyViewHolder viewHolder = (MyViewHolder) holder;
        viewHolder.contentView.setText(list.get(position));

        viewHolder.contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "内容点击，位置为：" + viewHolder.getAdapterPosition(), Toast.LENGTH_SHORT).show();
            }
        });

        viewHolder.deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "删除按钮点击，位置为：" + viewHolder.getAdapterPosition(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        SlideItemLayout sweepView;
        TextView contentView;
        TextView deleteView;

        MyViewHolder(View itemView) {
            super(itemView);
            sweepView = itemView.findViewById(R.id.sweep_view);
            contentView = itemView.findViewById(R.id.content_view);
            deleteView = itemView.findViewById(R.id.delete_view);
        }
    }
}