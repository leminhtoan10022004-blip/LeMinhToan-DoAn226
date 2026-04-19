package com.chaquo.python.console;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private List<Map<String, Object>> newsList;

    public NewsAdapter(List<Map<String, Object>> newsList) {
        this.newsList = newsList;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news_card, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        Map<String, Object> news = newsList.get(position);
        
        holder.tvNewsTitle.setText((String) news.get("TieuDe"));
        holder.tvNewsSummary.setText((String) news.get("TomTat"));
        holder.tvNewsTag.setText((String) news.get("LoaiTin"));
    }

    @Override
    public int getItemCount() {
        return newsList != null ? newsList.size() : 0;
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView tvNewsTitle, tvNewsSummary, tvNewsTag;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNewsTitle = itemView.findViewById(R.id.tvNewsTitle);
            tvNewsSummary = itemView.findViewById(R.id.tvNewsSummary);
            tvNewsTag = itemView.findViewById(R.id.tvNewsTag);
        }
    }
}