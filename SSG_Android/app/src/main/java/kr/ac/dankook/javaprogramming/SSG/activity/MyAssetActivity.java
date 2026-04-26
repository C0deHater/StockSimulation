package kr.ac.dankook.javaprogramming.SSG.activity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import kr.ac.dankook.javaprogramming.SSG.R;
import kr.ac.dankook.javaprogramming.SSG.adapter.MyAssetAdapter;

public class MyAssetActivity extends AppCompatActivity {

    private TextView tvTotalAsset, tvCashBalance, tvStockValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myasset);

        tvTotalAsset = findViewById(R.id.tv_total_asset);
        tvCashBalance = findViewById(R.id.tv_cash_balance);
        tvStockValue = findViewById(R.id.tv_stock_value);

        RecyclerView rvPortfolio = findViewById(R.id.rv_my_portfolio);
        rvPortfolio.setLayoutManager(new LinearLayoutManager(this));
        rvPortfolio.setAdapter(new MyAssetAdapter(new ArrayList<>()));

        // TODO: 1초마다 서버에서 자산 정보를 가져와 갱신하는 로직 구현 예정
    }
}
