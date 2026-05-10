package com.chaquo.python.console;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chaquo.python.model.LichSuLamBai;
import com.chaquo.python.model.NguoiDung;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class Account extends AppCompatActivity {

    private TextView tvFullName, tvEmail, tvPhone, tvRole;
    private MaterialButton btnLogout, btnEditProfile;
    private ImageButton btnBack;
    private FirebaseFirestore db;
    private String userId;

    private RecyclerView rvHistory;
    private TestHistoryAdapter historyAdapter;
    private List<LichSuLamBai> historyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account);
        
        db = FirebaseFirestore.getInstance();
        
        // Lấy userId từ Intent hoặc SharedPreferences
        userId = getIntent().getStringExtra("USER_ID");
        if (userId == null) {
            SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            userId = pref.getString("USER_ID", null);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            return insets;
        });

        initViews();
        setupBottomNavigation();
        
        if (userId != null) {
            loadUserDataFromFirestore();
            loadTestHistory();
        }

        btnLogout.setOnClickListener(v -> {
            SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            pref.edit().clear().apply();
            startActivity(new Intent(this, Login.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });

        btnBack.setOnClickListener(v -> onBackPressed());
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });
    }

    private void initViews() {
        tvFullName = findViewById(R.id.tv_full_name);
        tvEmail = findViewById(R.id.tv_email);
        tvPhone = findViewById(R.id.tv_phone);
        tvRole = findViewById(R.id.tv_user_role);
        btnLogout = findViewById(R.id.btn_logout);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnBack = findViewById(R.id.btn_back);
        rvHistory = findViewById(R.id.rv_test_history);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new TestHistoryAdapter(historyList);
        rvHistory.setAdapter(historyAdapter);
    }

    private void loadUserDataFromFirestore() {
        db.collection("NguoiDung").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        NguoiDung user = doc.toObject(NguoiDung.class);
                        if (user != null) {
                            tvFullName.setText(user.getHo() + " " + user.getTen());
                            tvEmail.setText(user.getEmail());
                            tvPhone.setText(user.getSDT());
                            tvRole.setText(user.getVaiTro());
                        }
                    }
                });
    }

    private void loadTestHistory() {
        // Tạm thời bỏ .orderBy để không yêu cầu Index, giúp dữ liệu hiện lên ngay để bạn kiểm tra
        db.collection("LichSuLamBai")
                .whereEqualTo("MaNguoiDung", userId)
                .limit(10) 
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    historyList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        LichSuLamBai item = doc.toObject(LichSuLamBai.class);
                        if (item != null) historyList.add(item);
                    }
                    historyAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("HISTORY_ERROR", "Lỗi: " + e.getMessage());
                    Toast.makeText(this, "Vui lòng click link trong Logcat để tạo Index", Toast.LENGTH_LONG).show();
                });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.it_account);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.it_home) {
                startActivity(new Intent(this, Home.class).putExtra("USER_ID", userId));
                finish();
                return true;
            }
            return id == R.id.it_account;
        });
    }
}
