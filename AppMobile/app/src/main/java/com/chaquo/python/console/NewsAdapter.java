package com.chaquo.python.console;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chaquo.python.model.BanTin;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private List<BanTin> newsList;

    public NewsAdapter(List<BanTin> newsList) {
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
        BanTin news = newsList.get(position);
        
        holder.tvNewsTitle.setText(news.getTieuDe());
        holder.tvNewsSummary.setText(news.getTomTat());
        holder.tvNewsTag.setText(news.getLoaiTin());
        
        holder.itemView.setOnClickListener(v -> {
            if (news.getMaCongViec() != null && !news.getMaCongViec().isEmpty()) {
                Intent intent = new Intent(v.getContext(), JobDetail.class);
                intent.putExtra("jobCode", news.getMaCongViec());
                v.getContext().startActivity(intent);
            } else {
                Toast.makeText(v.getContext(), "Bản tin này không có lộ trình đính kèm", Toast.LENGTH_SHORT).show();
            }
        });

        String imagePath = news.getHinhAnh();
        if (imagePath != null && !imagePath.isEmpty()) {
            if (imagePath.startsWith("http")) {
                Glide.with(holder.itemView.getContext())
                        .load(imagePath)
                        .placeholder(R.drawable.background)
                        .error(R.drawable.background)
                        .into(holder.ivNewsImage);
            } else {
                StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("news/" + imagePath);
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Glide.with(holder.itemView.getContext())
                            .load(uri)
                            .placeholder(R.drawable.background)
                            .error(R.drawable.background)
                            .into(holder.ivNewsImage);
                }).addOnFailureListener(e -> {
                    holder.ivNewsImage.setImageResource(R.drawable.background);
                });
            }
        } else {
            holder.ivNewsImage.setImageResource(R.drawable.background);
        }
    }

    @Override
    public int getItemCount() {
        return newsList != null ? newsList.size() : 0;
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView tvNewsTitle, tvNewsSummary, tvNewsTag;
        ImageView ivNewsImage;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNewsTitle = itemView.findViewById(R.id.tvNewsTitle);
            tvNewsSummary = itemView.findViewById(R.id.tvNewsSummary);
            tvNewsTag = itemView.findViewById(R.id.tvNewsTag);
            ivNewsImage = itemView.findViewById(R.id.ivNewsImage);
        }
    }
}
