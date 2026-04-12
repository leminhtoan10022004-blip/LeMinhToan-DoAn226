package com.chaquo.python.console;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chaquo.python.model.CongViec;
import com.chaquo.python.model.NguoiDung;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Home extends AppCompatActivity {
    private String userId;
    private FirebaseFirestore db;
    private TextView tvWelcome;
    private RecyclerView recyclerTrendingJobs;
    private JobAdapter jobAdapter;
    private List<CongViec> jobList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        
        db = FirebaseFirestore.getInstance();
        tvWelcome = findViewById(R.id.tvWelcome);
        recyclerTrendingJobs = findViewById(R.id.recyclerTrendingJobs);

        // Nhận ID người dùng từ Intent gửi từ màn hình Login
        userId = getIntent().getStringExtra("USER_ID");

        if (userId != null) {
            taiDuLieuNguoiDung(userId);
        }

        setupRecyclerView();
        taiDanhSachCongViec();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        setupNavigation();
    }

    private void setupRecyclerView() {
        jobList = new ArrayList<>();
        jobAdapter = new JobAdapter(jobList);
        recyclerTrendingJobs.setLayoutManager(new LinearLayoutManager(this));
        recyclerTrendingJobs.setAdapter(jobAdapter);
    }

    private void taiDanhSachCongViec() {
        db.collection("CongViec")
                .limit(10) // Lấy 10 công việc tiêu biểu
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    jobList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        CongViec job = document.toObject(CongViec.class);
                        jobList.add(job);
                    }
                    jobAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Home.this, "Không thể tải danh sách công việc", Toast.LENGTH_SHORT).show();
                });
    }

    private void taiDuLieuNguoiDung(String id) {
        db.collection("NguoiDung").document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        NguoiDung user = documentSnapshot.toObject(NguoiDung.class);
                        if (user != null) {
                            tvWelcome.setText("Chào bạn " + user.getTen());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Home.this, "Lỗi khi tải tên người dùng", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.it_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.it_home) {
                return true;
            } else if (id == R.id.it_notification) {
                Intent intent = new Intent(getApplicationContext(), Notification.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.it_account) {
                Intent intent = new Intent(getApplicationContext(), Account.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }
}