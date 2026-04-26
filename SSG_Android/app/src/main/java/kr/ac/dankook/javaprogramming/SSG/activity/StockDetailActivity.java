package kr.ac.dankook.javaprogramming.SSG.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import kr.ac.dankook.javaprogramming.SSG.R;

public class StockDetailActivity extends AppCompatActivity {

    private TextView tvStockName;
    private EditText etQuantity;
    private Button btnBuy, btnSell;
    // TODO: CandleStickChart 추가 예정 (MPAndroidChart 라이브러리)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);

        tvStockName = findViewById(R.id.tvStockName);
        etQuantity = findViewById(R.id.et_quantity);
        btnBuy = findViewById(R.id.btn_buy);
        btnSell = findViewById(R.id.btn_sell);

        String symbol = getIntent().getStringExtra("symbol");
        String stockName = getIntent().getStringExtra("stockName");

        if (stockName != null) {
            tvStockName.setText(stockName);
        }

        // TODO: 캔들스틱 차트 구현 예정
        // TODO: 매수/매도 버튼 클릭 시 서버 API 호출 구현 예정
    }
}
