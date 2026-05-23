package com.chaquo.python.console;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.chaquo.python.model.NguoiDung;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class SignUp extends AppCompatActivity {

    private TextInputEditText etLastName, etFirstName, etEmail, etPhone, etPassword, etConfirmPassword;
    private MaterialButton btnSignUp;
    private ImageButton btnBack;
    private TextView tvLoginLink;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        initViews();

        btnBack.setOnClickListener(v -> finish());
        tvLoginLink.setOnClickListener(v -> finish());
        btnSignUp.setOnClickListener(v -> thucHienDangKy());
    }

    private void initViews() {
        etLastName = findViewById(R.id.etLastName);
        etFirstName = findViewById(R.id.etFirstName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnBack = findViewById(R.id.btnBack);
        tvLoginLink = findViewById(R.id.tvLoginLink);
    }

    private void thucHienDangKy() {
        String email = etEmail.getText().toString().trim();
        String matKhau = etPassword.getText().toString().trim();
        String xacNhanMK = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(matKhau)) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!matKhau.equals(xacNhanMK)) {
            etConfirmPassword.setError("Mật khẩu không khớp");
            return;
        }

        btnSignUp.setEnabled(false);

        db.collection("NguoiDung")
                .whereEqualTo("Email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        if (!task.getResult().isEmpty()) {
                            btnSignUp.setEnabled(true);
                            etEmail.setError("Email đã tồn tại!");
                        } else {
                            // Email hợp lệ -> Sang bước 2: Lưu dữ liệu
                            luuThongTinNguoiDung(email, matKhau);
                        }
                    } else {
                        btnSignUp.setEnabled(true);
                        Toast.makeText(this, "Lỗi kết nối Firestore", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void luuThongTinNguoiDung(String email, String matKhau) {
        String maND = "ND" + System.currentTimeMillis();
        NguoiDung user = new NguoiDung();

        user.setMaNguoiDung(maND);
        user.setEmail(email);
        user.setMatKhau(matKhau);
        user.setHo(etLastName.getText().toString().trim());
        user.setTen(etFirstName.getText().toString().trim());
        user.setSDT(etPhone.getText().toString().trim());
        user.setNgayTao(new Timestamp(new Date()));
        user.setVaiTro("User");
        user.setTrangThai("Active");

        db.collection("NguoiDung").document(maND)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSignUp.setEnabled(true);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}