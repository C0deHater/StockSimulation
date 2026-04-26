package kr.ac.dankook.javaprogramming.SSG.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import kr.ac.dankook.javaprogramming.SSG.R;
import kr.ac.dankook.javaprogramming.SSG.dto.StockListResponse;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.ViewHolder> {

    private List<StockListResponse> stockList;

    public StockAdapter(List<StockListResponse> stockList) {
        this.stockList = stockList;
    }

    public void updateData(List<StockListResponse> newList) {
        this.stockList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stock, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StockListResponse stock = stockList.get(position);
        holder.tvStockName.setText(stock.getStockName());
        holder.tvStockCode.setText(stock.getStockCode());

        if (stock.getCurrentPrice() != null) {
            holder.tvCurrentPrice.setText(String.format("%,.0f원", stock.getCurrentPrice()));
        }

        // TODO: 등락률에 따른 색상 변경 구현 예정
    }

    @Override
    public int getItemCount() {
        return stockList != null ? stockList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStockName, tvStockCode, tvCurrentPrice;

        ViewHolder(View itemView) {
            super(itemView);
            tvStockName = itemView.findViewById(R.id.tv_stock_name);
            tvStockCode = itemView.findViewById(R.id.tv_stock_code);
            tvCurrentPrice = itemView.findViewById(R.id.tv_current_price);
        }
    }
}
