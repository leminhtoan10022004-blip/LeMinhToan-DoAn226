package com.chaquo.python.console;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.chaquo.python.model.NguoiDung;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

public class Login extends AppCompatActivity {
    private GoogleSignInClient mGoogleSignInClient;
    private static final int MA_YEU_CAU_GOOGLE = 9001;
    private MaterialButton btnGoogle;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPassword.setTypeface(android.graphics.Typeface.DEFAULT);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        btnGoogle = findViewById(R.id.btnGoogle);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, SignUp.class);
            startActivity(intent);
        });

        btnGoogle.setOnClickListener(v -> DangNhapTaiKhoanGoogle());

        btnLogin.setOnClickListener(v -> thucHienDangNhap());
    }

    private void DangNhapTaiKhoanGoogle() {
        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, MA_YEU_CAU_GOOGLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MA_YEU_CAU_GOOGLE) {
            Task<GoogleSignInAccount> nhiemVu = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount taiKhoan = nhiemVu.getResult(ApiException.class);
                if (taiKhoan != null) {
                    xacThucGoogleVoiFirebase(taiKhoan.getIdToken());
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Lỗi kết nối Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void xacThucGoogleVoiFirebase(String idToken) {
        AuthCredential chungChi = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(chungChi)
                .addOnCompleteListener(this, nhiemVu -> {
                    if (nhiemVu.isSuccessful()) {
                        FirebaseUser nguoiDungFirebase = mAuth.getCurrentUser();
                        if (nguoiDungFirebase != null) {
                            String email = nguoiDungFirebase.getEmail();
                            db.collection("NguoiDung")
                                    .whereEqualTo("Email", email)
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                        if (!queryDocumentSnapshots.isEmpty()) {
                                            DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                                            String userId = document.getId();
                                            vàoTrangHome(userId);
                                        } else {
                                            Toast.makeText(this, "Tài khoản chưa được đăng ký trong hệ thống!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Xác thực Firebase thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void thucHienDangNhap() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Vui lòng nhập Email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }

        db.collection("NguoiDung")
                .whereEqualTo("Email", email)
                .whereEqualTo("MatKhau", password)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                            String userId = document.getId();
                            vàoTrangHome(userId);
                        } else {
                            Toast.makeText(Login.this, "Email hoặc mật khẩu không đúng!", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định";
                        Toast.makeText(Login.this, "Lỗi kết nối: " + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void vàoTrangHome(String userId) {
        Intent intent = new Intent(Login.this, Home.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
        finish();
    }
}