package com.chaquo.python.console;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
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
    private TextView tvStartAction;
    private View btnOrientation, btnTrend, btnJobs;
    private FloatingActionButton fabChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        
        db = FirebaseFirestore.getInstance();
        
        kiemTraVaImportDuLieu();

        tvWelcome = findViewById(R.id.tvWelcome);
        recyclerTrendingJobs = findViewById(R.id.recyclerTrendingJobs);
        tvStartAction = findViewById(R.id.tvStartAction);
        btnOrientation = findViewById(R.id.btnOrientation);
        btnTrend = findViewById(R.id.btnTrend);
        btnJobs = findViewById(R.id.btnJobs);
        fabChat = findViewById(R.id.fabChat);

        userId = getIntent().getStringExtra("USER_ID");

        // Nhấn nút bắt đầu ngay -> QuestionBank
        tvStartAction.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, QuestionBank.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

        // Nhấn Định hướng -> Dẫn đến danh sách bài test (QuestionBank)
        if (btnOrientation != null) {
            btnOrientation.setOnClickListener(v -> {
                Intent intent = new Intent(Home.this, QuestionBank.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
            });
        }

        // Nhấn Xu hướng -> Dẫn đến màn hình xu hướng (Trend)
        if (btnTrend != null) {
            btnTrend.setOnClickListener(v -> {
                Intent intent = new Intent(Home.this, Trend.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
            });
        }

        if (btnJobs != null) {
            btnJobs.setOnClickListener(v -> {
                Intent intent = new Intent(Home.this, JobCategoriesActivity.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
            });
        }

        if (fabChat != null) {
            fabChat.setOnClickListener(v -> {
                Intent intent = new Intent(Home.this, ChatBotActivity.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
            });
        }

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

    private void kiemTraVaImportDuLieu() {
        db.collection("Nganh").limit(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().isEmpty()) {
                FirestoreImporter.importData(this);
                Toast.makeText(this, "Đang khởi tạo dữ liệu mẫu...", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupRecyclerView() {
        jobList = new ArrayList<>();
        jobAdapter = new JobAdapter(jobList);
        recyclerTrendingJobs.setLayoutManager(new LinearLayoutManager(this));
        recyclerTrendingJobs.setAdapter(jobAdapter);
    }

    private void taiDanhSachCongViec() {
        db.collection("CongViec")
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    jobList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        CongViec job = document.toObject(CongViec.class);
                        job.setMaCongViec(document.getId());
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
                return true;
            } else if (id == R.id.it_account) {
                Intent intent = new Intent(getApplicationContext(), Account.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }
}
