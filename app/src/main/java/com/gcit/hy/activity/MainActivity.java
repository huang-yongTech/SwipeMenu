package com.gcit.hy.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.gcit.hy.R;
import com.gcit.hy.adapter.ListViewAdapter;
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
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
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
//
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                switch (view.getId()) {
//                    case R.id.content_view:
//                        Toast.makeText(MainActivity.this, "内容点击", Toast.LENGTH_SHORT).show();
//                        break;
//                    case R.id.delete_view:
//                        Toast.makeText(MainActivity.this, "删除按钮点击", Toast.LENGTH_SHORT).show();
//                        break;
//                }
//            }
//        });
//    }
}
