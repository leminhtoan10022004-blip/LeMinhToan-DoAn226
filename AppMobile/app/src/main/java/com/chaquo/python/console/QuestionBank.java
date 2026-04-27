package com.chaquo.python.console;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chaquo.python.model.BaiTest;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QuestionBank extends AppCompatActivity {

    private RecyclerView rvQuizzes;
    private QuizAdapter quizAdapter;
    private List<BaiTest> quizList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // UI Elements
    private ImageView btnBack;
    private LinearLayout layoutLockOverlay;
    private MaterialButton btnUploadTranscript, btnStartAIAnalysis;
    private TextView tvOcrStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_question_bank);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        loadDataFromFirestore();
        checkTestCompletion();
    }

    private void initViews() {
        rvQuizzes = findViewById(R.id.rvQuizzes);
        btnBack = findViewById(R.id.btnBack);
        
        quizList = new ArrayList<>();
        quizAdapter = new QuizAdapter(this, quizList);
        
        rvQuizzes.setLayoutManager(new GridLayoutManager(this, 2));
        rvQuizzes.setAdapter(quizAdapter);
        
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // AI Views
        layoutLockOverlay = findViewById(R.id.layoutLockOverlay);
        btnUploadTranscript = findViewById(R.id.btnUploadTranscript);
        btnStartAIAnalysis = findViewById(R.id.btnStartAIAnalysis);
        tvOcrStatus = findViewById(R.id.tvOcrStatus);

        // Back button logic
        btnBack.setOnClickListener(v -> {
            finish(); // Đóng activity hiện tại để quay lại màn hình trước đó
        });

        btnUploadTranscript.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng tải bảng điểm đang phát triển", Toast.LENGTH_SHORT).show();
        });

        btnStartAIAnalysis.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng phân tích AI đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private void checkTestCompletion() {
        if (mAuth.getCurrentUser() == null) return;

        String userId = mAuth.getCurrentUser().getUid();
        
        db.collection("KetQuaTest")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Set<String> completedTestTypes = new HashSet<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String type = doc.getString("testType");
                        if (type != null) {
                            completedTestTypes.add(type.toUpperCase());
                        }
                    }

                    if (completedTestTypes.size() >= 3) {
                        unlockAIFeature();
                    } else {
                        lockAIFeature();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("QuestionBank", "Error checking completion", e);
                    lockAIFeature();
                });
    }

    private void unlockAIFeature() {
        if (layoutLockOverlay != null) {
            layoutLockOverlay.setVisibility(View.GONE);
        }
        btnUploadTranscript.setEnabled(true);
        btnStartAIAnalysis.setEnabled(true);
    }

    private void lockAIFeature() {
        if (layoutLockOverlay != null) {
            layoutLockOverlay.setVisibility(View.VISIBLE);
        }
        btnUploadTranscript.setEnabled(false);
        btnStartAIAnalysis.setEnabled(false);
    }

    private void loadDataFromFirestore() {
        db.collection("BaiTest")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        quizList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            BaiTest quiz = document.toObject(BaiTest.class);
                            quiz.setMaTest(document.getId());
                            quizList.add(quiz);
                        }
                        quizAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("QuestionBank", "Error getting documents: ", task.getException());
                        Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
