package com.gcit.hy.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.gcit.hy.R;
import com.gcit.hy.adapter.RecyclerListAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        init();
    }

    private void init() {
        RecyclerView recyclerView = findViewById(R.id.main_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        List<String> list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            list.add("测试文本-- " + i);
        }
        RecyclerListAdapter adapter = new RecyclerListAdapter(list, this);
        recyclerView.setAdapter(adapter);
    }

//    private void init() {
//        ListView listView = findViewById(R.id.main_recycler_view);
//
//        List<String> list = new ArrayList<>();
//        for (int i = 0; i < 20; i++) {
//            list.add("测试文本-- " + i);
//        }
//
//        ListViewAdapter adapter = new ListViewAdapter(this, list);
//        listView.setAdapter(adapter);
//    }
}
