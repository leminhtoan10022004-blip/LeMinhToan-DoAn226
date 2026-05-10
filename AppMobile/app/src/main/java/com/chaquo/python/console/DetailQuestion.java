package com.chaquo.python.console;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
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

import com.chaquo.python.model.BaiTest;
import com.chaquo.python.model.CauHoi;
import com.chaquo.python.model.DapAn;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DetailQuestion extends AppCompatActivity {

    private TextView tvQuestionProgress, tvTimer, tvQuestionText;
    private RecyclerView rvOptions;
    private MaterialButton btnBack, btnNext;
    
    private FirebaseFirestore db;
    private String testId;
    private List<CauHoi> questionList = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private Map<Integer, DapAn> userChoices = new HashMap<>();
    
    private CountDownTimer countDownTimer;
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail_question);
        
        db = FirebaseFirestore.getInstance();
        testId = getIntent().getStringExtra("TEST_ID");

        initViews();
        loadTestData();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        tvQuestionProgress = findViewById(R.id.tvQuestionProgress);
        tvTimer = findViewById(R.id.tvTimer);
        tvQuestionText = findViewById(R.id.tvQuestionText);
        rvOptions = findViewById(R.id.rvOptions);
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);

        rvOptions.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--;
                displayQuestion();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentQuestionIndex < questionList.size() - 1) {
                currentQuestionIndex++;
                displayQuestion();
            } else {
                showSubmitConfirmDialog();
            }
        });
    }

    private void loadTestData() {
        if (testId == null) return;

        db.collection("BaiTest").document(testId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    BaiTest test = documentSnapshot.toObject(BaiTest.class);
                    if (test != null && test.getDanhSachCauHoi() != null) {
                        questionList.addAll(test.getDanhSachCauHoi());
                        startTime = System.currentTimeMillis();
                        startTimer(test.getThoiGian() * 60 * 1000);
                        displayQuestion();
                    }
                });
    }

    private void displayQuestion() {
        if (questionList.isEmpty()) return;

        CauHoi currentQuestion = questionList.get(currentQuestionIndex);
        tvQuestionProgress.setText(String.format(Locale.getDefault(), "Câu hỏi %d/%d", currentQuestionIndex + 1, questionList.size()));
        tvQuestionText.setText(currentQuestion.getNoiDung());

        btnNext.setText(currentQuestionIndex == questionList.size() - 1 ? "Nộp bài" : "Tiếp");
        btnBack.setVisibility(currentQuestionIndex == 0 ? View.INVISIBLE : View.VISIBLE);

        OptionAdapter adapter = new OptionAdapter(currentQuestion.getDapAn(), 
                userChoices.get(currentQuestionIndex), 
                selectedOption -> userChoices.put(currentQuestionIndex, selectedOption));
        rvOptions.setAdapter(adapter);
    }

    private void startTimer(long millis) {
        if (countDownTimer != null) countDownTimer.cancel();
        countDownTimer = new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                tvTimer.setText(String.format(Locale.getDefault(), "Thời gian còn lại %02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                submitTest();
            }
        }.start();
    }

    private void showSubmitConfirmDialog() {
        if (userChoices.size() < questionList.size()) {
            Toast.makeText(this, "Bạn chưa hoàn thành tất cả câu hỏi!", Toast.LENGTH_SHORT).show();
            return;
        }
        submitTest();
    }

    private void submitTest() {
        if (countDownTimer != null) countDownTimer.cancel();
        
        // 1. Tính toán điểm số
        Map<String, Integer> traitScores = new HashMap<>();
        String topTrait = "";
        int maxScore = -1;

        for (DapAn selected : userChoices.values()) {
            String trait = selected.getMaThangDo();
            int currentScore = traitScores.getOrDefault(trait, 0);
            int newScore = currentScore + selected.getGiaTri();
            traitScores.put(trait, newScore);

            if (newScore > maxScore) {
                maxScore = newScore;
                topTrait = trait;
            }
        }

        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = pref.getString("USER_ID", "anonymous");
        long endTime = System.currentTimeMillis();

        // 2. Tạo mã ID tự động cho Document
        String ketQuaId = db.collection("KetQuaPhanTich").document().getId();
        String lichSuId = db.collection("LichSuLamBai").document().getId();

        // 3. Chuẩn bị dữ liệu KetQuaPhanTich
        Map<String, Object> ketQuaData = new HashMap<>();
        ketQuaData.put("MaKetQua", ketQuaId);
        ketQuaData.put("KetQuaChiTiet", traitScores);
        ketQuaData.put("MaNganhPhuHop", topTrait);
        ketQuaData.put("DuLieuChiTiet", new HashMap<>());

        // 4. Chuẩn bị dữ liệu LichSuLamBai
        Map<String, Object> lichSuData = new HashMap<>();
        lichSuData.put("MaLichSu", lichSuId);
        lichSuData.put("MaNguoiDung", userId);
        lichSuData.put("MaTest", testId);
        lichSuData.put("ThoiGianBD", startTime);
        lichSuData.put("ThoiGianKT", endTime);
        lichSuData.put("MaKetQua", ketQuaId);
        lichSuData.put("TrangThai", "Hoàn thành");

        // 5. Lưu đồng thời (WriteBatch)
        WriteBatch batch = db.batch();
        batch.set(db.collection("KetQuaPhanTich").document(ketQuaId), ketQuaData);
        batch.set(db.collection("LichSuLamBai").document(lichSuId), lichSuData);

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Nộp bài thành công!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, TestResuilts.class);
            intent.putExtra("RESULT_ID", ketQuaId);
            startActivity(intent);
            finish();
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Error saving data", e);
            Toast.makeText(this, "Lỗi khi nộp bài: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
