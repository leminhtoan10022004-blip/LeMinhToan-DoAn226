package com.chaquo.python.console;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.chaquo.python.model.NguoiDung;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

public class Account extends AppCompatActivity {

    private TextView tvFullName, tvEmail, tvPhone, tvRole;
    private MaterialButton btnLogout, btnEditProfile;
    private ImageButton btnBack;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account);
        
        db = FirebaseFirestore.getInstance();
        userId = getIntent().getStringExtra("USER_ID");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            return insets;
        });

        initViews();
        setupBottomNavigation();
        loadUserDataFromFirestore();

        btnLogout.setOnClickListener(v -> {
            // Xóa dữ liệu đăng nhập để yêu cầu đăng nhập lại
            SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            pref.edit().clear().apply();

            Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void initViews() {
        tvFullName = findViewById(R.id.tv_full_name);
        tvEmail = findViewById(R.id.tv_email);
        tvPhone = findViewById(R.id.tv_phone);
        tvRole = findViewById(R.id.tv_user_role);

        btnLogout = findViewById(R.id.btn_logout);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnBack = findViewById(R.id.btn_back);
    }

    private void loadUserDataFromFirestore() {
        if (userId == null) return;

        db.collection("NguoiDung").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        NguoiDung user = documentSnapshot.toObject(NguoiDung.class);
                        if (user != null) {
                            String hoTen = user.getHo() + " " + user.getTen();
                            tvFullName.setText(hoTen.trim());
                            tvEmail.setText(user.getEmail());
                            tvPhone.setText(user.getSDT());
                            tvRole.setText(user.getVaiTro());
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải thông tin", Toast.LENGTH_SHORT).show());
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.it_account);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.it_home) {
                Intent intent = new Intent(getApplicationContext(), Home.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.it_notification) {
                Intent intent = new Intent(getApplicationContext(), Notification.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return id == R.id.it_account;
        });
    }
}