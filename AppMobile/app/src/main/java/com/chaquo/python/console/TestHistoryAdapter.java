package com.chaquo.python.console;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chaquo.python.model.LichSuLamBai;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TestHistoryAdapter extends RecyclerView.Adapter<TestHistoryAdapter.ViewHolder> {
    private List<LichSuLamBai> historyList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public TestHistoryAdapter(List<LichSuLamBai> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_test_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LichSuLamBai history = historyList.get(position);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateStr = sdf.format(new Date(history.getThoiGianKT()));
        holder.tvDate.setText("(" + dateStr + ")");

        if (history.getMaTest() != null) {
            db.collection("BaiTest").document(history.getMaTest()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            holder.tvType.setText(documentSnapshot.getString("TieuDe"));
                        } else {
                            holder.tvType.setText("Bài test không xác định");
                        }
                    });
        } else {
            holder.tvType.setText("Bài test không xác định");
        }

        if (history.getMaKetQua() != null) {
            db.collection("KetQuaPhanTich").document(history.getMaKetQua()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String nganh = doc.getString("MaNganhPhuHop");
                            holder.tvResult.setText("Kết quả: " + (nganh != null ? nganh : "Đã hoàn thành"));
                        } else {
                            holder.tvResult.setText("Trạng thái: " + history.getTrangThai());
                        }
                    });
        } else {
            holder.tvResult.setText("Trạng thái: " + history.getTrangThai());
        }

        holder.tvAction.setOnClickListener(v -> {
            if (history.getMaKetQua() != null) {
                Intent intent = new Intent(v.getContext(), TestResuilts.class);
                intent.putExtra("RESULT_ID", history.getMaKetQua());
                v.getContext().startActivity(intent);
            }
        });

        holder.tvAction.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvDate, tvResult, tvAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tv_test_type);
            tvDate = itemView.findViewById(R.id.tv_test_date);
            tvResult = itemView.findViewById(R.id.tv_test_result);
            tvAction = itemView.findViewById(R.id.tv_retest_action);
        }
    }
}
