package com.chaquo.python.console;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
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
    
    // BỘ NHỚ ĐỆM: Lưu trữ lựa chọn của người dùng (Thứ tự câu hỏi -> Đáp án đã chọn)
    private Map<Integer, DapAn> userChoices = new HashMap<>();
    
    private CountDownTimer countDownTimer;

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

        // Sử dụng OptionAdapter với cơ chế bộ nhớ đệm
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
        
        Map<String, Integer> traitScores = new HashMap<>();
        for (DapAn selected : userChoices.values()) {
            String trait = selected.getMaThangDo();
            int currentScore = traitScores.getOrDefault(trait, 0);
            traitScores.put(trait, currentScore + selected.getGiaTri());
        }

        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = pref.getString("USER_ID", "anonymous");

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("MaNguoiDung", userId);
        resultData.put("MaTest", testId);
        resultData.put("KetQuaChiTiet", traitScores);
        resultData.put("NgayLam", System.currentTimeMillis());

        db.collection("KetQuaTest").add(resultData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Đã nộp bài thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, TestResuilts.class);
                    intent.putExtra("RESULT_ID", documentReference.getId());
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi nộp bài", Toast.LENGTH_SHORT).show());
    }
}