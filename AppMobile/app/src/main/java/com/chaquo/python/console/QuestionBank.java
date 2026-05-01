package com.chaquo.python.console;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.chaquo.python.model.BaiTest;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
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

    private String selectedImagePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_question_bank);

        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        
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
        btnBack.setOnClickListener(v -> finish());

        btnUploadTranscript.setOnClickListener(v -> openGallery());

        btnStartAIAnalysis.setOnClickListener(v -> {
            if (selectedImagePath != null) {
                runTranscriptAnalysis(selectedImagePath);
            } else {
                Toast.makeText(this, "Vui lòng chọn ảnh bảng điểm trước", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    selectedImagePath = getPathFromURI(selectedImageUri);
                    if (selectedImagePath != null) {
                        tvOcrStatus.setText("Đã chọn: " + new File(selectedImagePath).getName());
                        tvOcrStatus.setVisibility(View.VISIBLE);
                        btnStartAIAnalysis.setVisibility(View.VISIBLE);
                    }
                }
            }
    );

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
            cursor.close();
        }
        return res;
    }

    private void runTranscriptAnalysis(String imagePath) {
        tvOcrStatus.setText("Đang phân tích bảng điểm bằng AI...");
        btnStartAIAnalysis.setEnabled(false);

        new Thread(() -> {
            try {
                Python py = Python.getInstance();
                PyObject transcriptOcrModule = py.getModule("transcript_ocr");
                PyObject result = transcriptOcrModule.callAttr("process_transcript_image", imagePath);
                
                String jsonResult = result.toString();
                
                runOnUiThread(() -> {
                    tvOcrStatus.setText("Phân tích hoàn tất!");
                    btnStartAIAnalysis.setEnabled(true);
                    // Ở đây bạn có thể hiển thị kết quả JSON hoặc chuyển sang màn hình tiếp theo
                    Log.d("AI_Analysis", jsonResult);
                    Toast.makeText(this, "Phân tích thành công!", Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                Log.e("AI_Analysis", "Error running Python OCR", e);
                runOnUiThread(() -> {
                    tvOcrStatus.setText("Lỗi phân tích: " + e.getMessage());
                    btnStartAIAnalysis.setEnabled(true);
                });
            }
        }).start();
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
