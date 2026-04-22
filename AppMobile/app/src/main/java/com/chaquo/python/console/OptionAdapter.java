package com.chaquo.python.console;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chaquo.python.model.DapAn;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class OptionAdapter extends RecyclerView.Adapter<OptionAdapter.OptionViewHolder> {

    private List<DapAn> options;
    private DapAn selectedOption;
    private OnOptionSelectedListener listener;

    public interface OnOptionSelectedListener {
        void onOptionSelected(DapAn option);
    }

    public OptionAdapter(List<DapAn> options, DapAn selectedOption, OnOptionSelectedListener listener) {
        this.options = options;
        this.selectedOption = selectedOption;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_option, parent, false);
        return new OptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OptionViewHolder holder, int position) {
        DapAn option = options.get(position);
        holder.tvOptionText.setText(option.getNoiDung());

        // Hiển thị trạng thái đã chọn (highlight)
        boolean isSelected = (selectedOption != null && selectedOption.getMaDapAn().equals(option.getMaDapAn()));
        
        MaterialCardView card = (MaterialCardView) holder.itemView;
        if (isSelected) {
            card.setStrokeColor(Color.parseColor("#4A148C")); // Màu tím đậm khi chọn
            card.setStrokeWidth(4);
            card.setCardBackgroundColor(Color.parseColor("#F3E5F5"));
        } else {
            card.setStrokeColor(Color.parseColor("#E0E0E0")); // Màu xám nhạt mặc định
            card.setStrokeWidth(2);
            card.setCardBackgroundColor(Color.WHITE);
        }

        holder.itemView.setOnClickListener(v -> {
            selectedOption = option;
            notifyDataSetChanged();
            if (listener != null) {
                listener.onOptionSelected(option);
            }
        });
    }

    @Override
    public int getItemCount() {
        return options != null ? options.size() : 0;
    }

    public static class OptionViewHolder extends RecyclerView.ViewHolder {
        TextView tvOptionText;

        public OptionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOptionText = itemView.findViewById(R.id.tvOptionText);
        }
    }
}