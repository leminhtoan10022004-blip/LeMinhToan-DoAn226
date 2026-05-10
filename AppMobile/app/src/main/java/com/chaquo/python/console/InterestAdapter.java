package com.chaquo.python.console;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chaquo.python.model.NguoiDungBanTin;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class InterestAdapter extends RecyclerView.Adapter<InterestAdapter.ViewHolder> {
    private List<NguoiDungBanTin> interestList;
    private String userId;

    public InterestAdapter(List<NguoiDungBanTin> interestList, String userId) {
        this.interestList = interestList;
        this.userId = userId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_interest, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NguoiDungBanTin interest = interestList.get(position);
        holder.tvName.setText(interest.getTenCongViec());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), JobDetail.class);
            intent.putExtra("jobCode", interest.getMaBanTin());
            v.getContext().startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Hủy theo dõi")
                    .setMessage("Bạn có chắc chắn muốn ngừng theo dõi lộ trình này?")
                    .setPositiveButton("Hủy", (dialog, which) -> {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("NguoiDung_BanTin")
                                .document(userId + "_" + interest.getMaBanTin())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    int currentPos = holder.getAdapterPosition();
                                    if (currentPos != RecyclerView.NO_POSITION) {
                                        interestList.remove(currentPos);
                                        notifyItemRemoved(currentPos);
                                        notifyItemRangeChanged(currentPos, interestList.size());
                                        Toast.makeText(v.getContext(), "Đã hủy theo dõi", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(v.getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Đóng", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return interestList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvOpen;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_interest_name);
            tvOpen = itemView.findViewById(R.id.tv_open_roadmap);
            btnDelete = itemView.findViewById(R.id.btn_delete_interest);
        }
    }
}
