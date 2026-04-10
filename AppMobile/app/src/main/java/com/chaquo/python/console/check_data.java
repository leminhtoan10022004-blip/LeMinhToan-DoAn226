package com.chaquo.python.console;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.chaquo.python.model.NguoiDung;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class check_data extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView tvTotal;
    private TextView tvData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_check_data);

        tvTotal = findViewById(R.id.tvTotal);
        tvData = findViewById(R.id.tvData);

        db = FirebaseFirestore.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        layDuLieuNguoiDung();
    }

    private void layDuLieuNguoiDung() {
        db.collection("maNguoiDung")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        StringBuilder sb = new StringBuilder();
                        int count = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            NguoiDung user = document.toObject(NguoiDung.class);
                            count++;
                            sb.append(count).append(". ")
                              .append(user.getHoTen()).append(" - ")
                              .append(user.getEmail()).append("\n");
                        }

                        tvTotal.setText("Tổng số dữ liệu: " + count);
                        if (count > 0) {
                            tvData.setText(sb.toString());
                        } else {
                            tvData.setText("Không có dữ liệu trong collection 'maNguoiDung'");
                        }

                    } else {
                        Log.e("FirestoreData", "Lỗi lấy dữ liệu: ", task.getException());
                        tvData.setText("Lỗi: " + task.getException().getMessage());
                        Toast.makeText(this, "Lỗi kết nối dữ liệu!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}