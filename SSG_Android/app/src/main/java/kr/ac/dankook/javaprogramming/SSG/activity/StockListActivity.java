package kr.ac.dankook.javaprogramming.SSG.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import kr.ac.dankook.javaprogramming.SSG.R;
import kr.ac.dankook.javaprogramming.SSG.adapter.StockAdapter;

public class StockListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private StockAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_list);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StockAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // TODO: 1초마다 서버에서 주식 리스트를 가져와 갱신하는 로직 구현 예정
    }
}
