package kr.ac.dankook.javaprogramming.SSG.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import kr.ac.dankook.javaprogramming.SSG.R;
import kr.ac.dankook.javaprogramming.SSG.dto.Portfolio;

public class MyAssetAdapter extends RecyclerView.Adapter<MyAssetAdapter.ViewHolder> {

    private List<Portfolio> list;

    public MyAssetAdapter(List<Portfolio> list) {
        this.list = list;
    }

    public void updateData(List<Portfolio> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_stock, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Portfolio item = list.get(position);
        holder.name.setText(item.getStockName());
        holder.qty.setText(item.getQuantity() + "주");
        holder.avgPrice.setText(String.format("%,.0f원", item.getAveragePrice()));

        // TODO: 현재가, 수익률 표시 및 색상 구현 예정
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, qty, avgPrice;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_stock_name);
            qty = itemView.findViewById(R.id.tv_stock_quantity);
            avgPrice = itemView.findViewById(R.id.tv_avg_price);
        }
    }
}
