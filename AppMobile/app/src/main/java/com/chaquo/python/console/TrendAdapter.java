package com.chaquo.python.console;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class TrendAdapter extends RecyclerView.Adapter<TrendAdapter.TrendViewHolder> {

    private List<Map<String, Object>> trendList;

    public TrendAdapter(List<Map<String, Object>> trendList) {
        this.trendList = trendList;
    }

    @NonNull
    @Override
    public TrendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_jod_category_card, parent, false);
        return new TrendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrendViewHolder holder, int position) {
        Map<String, Object> trend = trendList.get(position);
        
        // Lấy dữ liệu từ Map (đã map từ Firestore XuHuong + Nganh)
        String tenNganh = (String) trend.get("TenNganh");
        String slgTuyen = (String) trend.get("SlgTuyen");
        String tyLe = (String) trend.get("TyLe");

        holder.tvCategoryTitle.setText(tenNganh != null ? tenNganh : "N/A");
        holder.tvCategoryPositions.setText(slgTuyen != null ? slgTuyen : "0");
        holder.tvCategoryGrowth.setText("▲ " + (tyLe != null ? tyLe : "0%"));
        
        // Lương trung bình để tạm hoặc lấy từ ngành nếu có
        holder.tvCategorySalary.setText("15 - 35"); 
    }

    @Override
    public int getItemCount() {
        return trendList != null ? trendList.size() : 0;
    }

    public static class TrendViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryTitle, tvCategoryPositions, tvCategoryGrowth, tvCategorySalary;

        public TrendViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryTitle = itemView.findViewById(R.id.tvCategoryTitle);
            tvCategoryPositions = itemView.findViewById(R.id.tvCategoryPositions);
            tvCategoryGrowth = itemView.findViewById(R.id.tvCategoryGrowth);
            tvCategorySalary = itemView.findViewById(R.id.tvCategorySalary);
        }
    }
}