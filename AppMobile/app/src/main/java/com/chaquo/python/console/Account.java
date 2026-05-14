package com.chaquo.python.console;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import com.chaquo.python.model.NguoiDungBanTin;
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

    private TextView tvFullName, tvEmail, tvPhone, tvRole, tvNoInterests;
    private MaterialButton btnLogout, btnEditProfile;
    private ImageButton btnBack;
    private FirebaseFirestore db;
    private String userId;

    private RecyclerView rvHistory, rvInterests;
    private TestHistoryAdapter historyAdapter;
    private InterestAdapter interestAdapter;
    private List<LichSuLamBai> historyList = new ArrayList<>();
    private List<NguoiDungBanTin> interestList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account);
        
        db = FirebaseFirestore.getInstance();
        
        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = pref.getString("USER_ID", null);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, 0, systemBars.right, 0);
                return insets;
            });
        }

        initViews();
        setupBottomNavigation();
        
        if (userId != null) {
            loadUserDataFromFirestore();
            loadTestHistory();
            loadInterests();
        }

        btnLogout.setOnClickListener(v -> {
            SharedPreferences prefLogout = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            prefLogout.edit().clear().apply();
            startActivity(new Intent(this, Login.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, Home.class);
            intent.putExtra("USER_ID", userId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        
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
        tvNoInterests = findViewById(R.id.tv_no_interests);
        btnLogout = findViewById(R.id.btn_logout);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnBack = findViewById(R.id.btn_back);
        
        rvHistory = findViewById(R.id.rv_test_history);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new TestHistoryAdapter(historyList);
        rvHistory.setAdapter(historyAdapter);

        rvInterests = findViewById(R.id.rv_interests);
        rvInterests.setLayoutManager(new LinearLayoutManager(this));
        interestAdapter = new InterestAdapter(interestList, userId);
        rvInterests.setAdapter(interestAdapter);
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
                });
    }

    private void loadInterests() {
        db.collection("NguoiDung_BanTin")
                .whereEqualTo("MaNguoiDung", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    interestList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        NguoiDungBanTin item = doc.toObject(NguoiDungBanTin.class);
                        if (item != null) interestList.add(item);
                    }
                    interestAdapter.notifyDataSetChanged();
                    tvNoInterests.setVisibility(interestList.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> Log.e("ACCOUNT_ERROR", "Lỗi tải quan tâm: " + e.getMessage()));
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.it_account);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.it_home) {
                Intent intent = new Intent(this, Home.class);
                intent.putExtra("USER_ID", userId);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                return true;
            }
            return id == R.id.it_account;
        });
    }
}
