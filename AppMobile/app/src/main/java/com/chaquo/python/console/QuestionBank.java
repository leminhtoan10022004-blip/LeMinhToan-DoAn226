package com.chaquo.python.console;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.chaquo.python.model.BaiTest;
import com.chaquo.python.model.KetQuaPhanTich;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestionBank extends AppCompatActivity {

    private QuizAdapter quizAdapter;
    private List<BaiTest> quizList;
    private FirebaseFirestore db;
    private String userId;
    private LinearLayout layoutLockOverlay, layoutLoading;
    private MaterialButton btnUploadTranscript, btnStartAIAnalysis;
    private TextView tvOcrStatus, tvLoadingText;

    private String selectedImagePath = null;
    private final Map<String, KetQuaPhanTich> userTestResults = new HashMap<>();

    // Launcher cho Thư viện ảnh
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) handleImageSelected(uri, "Thư viện");
            }
    );

    // Launcher cho Camera
    private Uri cameraImageUri;
    private final ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && cameraImageUri != null) handleImageSelected(cameraImageUri, "Camera");
            }
    );

    // Launcher yêu cầu quyền Camera
    private final ActivityResultLauncher<String> cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) openCamera();
                else Toast.makeText(this, "Cần quyền Camera để chụp ảnh", Toast.LENGTH_SHORT).show();
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_question_bank);

        new Thread(() -> {
            if (!Python.isStarted()) {
                Python.start(new AndroidPlatform(this));
            }
        }).start();

        db = FirebaseFirestore.getInstance();
        userId = getIntent().getStringExtra("USER_ID");
        if (userId == null && FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        initViews();
        loadDataFromFirestore();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        layoutLoading = findViewById(R.id.layoutLoading);
        tvLoadingText = findViewById(R.id.tvLoadingText);
        layoutLockOverlay = findViewById(R.id.layoutLockOverlay);
        btnUploadTranscript = findViewById(R.id.btnUploadTranscript);
        btnStartAIAnalysis = findViewById(R.id.btnStartAIAnalysis);
        tvOcrStatus = findViewById(R.id.tvOcrStatus);
        
        RecyclerView rvQuizzes = findViewById(R.id.rvQuizzes);
        quizList = new ArrayList<>();
        quizAdapter = new QuizAdapter(this, quizList);
        rvQuizzes.setLayoutManager(new GridLayoutManager(this, 2));
        rvQuizzes.setAdapter(quizAdapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        btnUploadTranscript.setOnClickListener(v -> showImageSourceDialog());
        btnStartAIAnalysis.setOnClickListener(v -> {
            if (selectedImagePath != null) runTranscriptAnalysis(selectedImagePath);
            else Toast.makeText(this, "Hãy chọn ảnh bảng điểm trước", Toast.LENGTH_SHORT).show();
        });
    }

    private void handleImageSelected(Uri uri, String source) {
        try {
            File tempFile = createTempFileFromUri(uri);
            selectedImagePath = tempFile.getAbsolutePath();
            tvOcrStatus.setText("Đã nhận ảnh từ " + source + ": " + tempFile.getName());
            Toast.makeText(this, "Đã nạp ảnh thành công", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("QuestionBank", "Error processing image", e);
            Toast.makeText(this, "Lỗi khi xử lý ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private File createTempFileFromUri(Uri uri) throws IOException {
        File tempFile = File.createTempFile("ocr_input_", ".jpg", getCacheDir());
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        return tempFile;
    }

    private void runTranscriptAnalysis(String imagePath) {
        showLoading("AI ĐANG ĐỌC BẢNG ĐIỂM...");
        new Thread(() -> {
            try {
                Python py = Python.getInstance();
                PyObject pyModule = py.getModule("transcript_ocr");
                PyObject result = pyModule.callAttr("process_transcript_image", imagePath);
                String jsonStr = result.toString();
                runOnUiThread(() -> processAIPrediction(jsonStr));
            } catch (Exception e) {
                hideLoadingWithError("Lỗi OCR: " + e.getMessage());
            }
        }).start();
    }

    private void processAIPrediction(String transcriptJson) {
        try {
            JSONObject obj = new JSONObject(transcriptJson);
            if (obj.has("error")) {
                hideLoadingWithError("Lỗi: " + obj.getString("error"));
                return;
            }

            Map<String, Float> grades = new HashMap<>();
            String[] keys = {"Toan", "Ly", "Hoa", "Sinh", "Van", "Anh", "Tin", "Dia", "Su"};
            for (String k : keys) grades.put(k, 8.0f); 

            if (obj.has("bang_diem")) {
                JSONArray arr = obj.getJSONArray("bang_diem");
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject item = arr.getJSONObject(i);
                    String mon = item.optString("mon_hoc", "").toLowerCase();
                    float diem = (float) item.optDouble("diem_tb", 8.0);
                    if (mon.contains("toán")) grades.put("Toan", diem);
                    else if (mon.contains("lý")) grades.put("Ly", diem);
                    else if (mon.contains("hóa")) grades.put("Hoa", diem);
                    else if (mon.contains("văn")) grades.put("Van", diem);
                    else if (mon.contains("anh")) grades.put("Anh", diem);
                    else if (mon.contains("sinh")) grades.put("Sinh", diem);
                    else if (mon.contains("tin")) grades.put("Tin", diem);
                    else if (mon.contains("địa")) grades.put("Dia", diem);
                    else if (mon.contains("sử")) grades.put("Su", diem);
                }
            }
            runMLPPrediction(getMBTICode(), getHollandCode(), getDISCCode(), getBig5Values(), grades);
        } catch (Exception e) {
            hideLoadingWithError("Lỗi xử lý dữ liệu");
        }
    }

    private void runMLPPrediction(String mbti, String holland, String disc, float[] big5, Map<String, Float> g) {
        runOnUiThread(() -> tvLoadingText.setText("AI ĐANG TÌM NGÀNH NGHỀ PHÙ HỢP..."));
        new Thread(() -> {
            try {
                Python py = Python.getInstance();
                PyObject pyModule = py.getModule("career_mlp");
                pyModule.callAttr("init_model");
                PyObject res = pyModule.callAttr("predict_career",
                        mbti, holland, big5[0], big5[1], big5[2], big5[3], big5[4], disc,
                        g.get("Toan"), g.get("Ly"), g.get("Hoa"), g.get("Sinh"), g.get("Van"),
                        g.get("Anh"), g.get("Tin"), g.get("Dia"), g.get("Su"));
                String careerResult = res.toString();
                runOnUiThread(() -> {
                    layoutLoading.setVisibility(View.GONE);
                    btnStartAIAnalysis.setEnabled(true);
                    Intent intent = new Intent(this, TestResuilts.class);
                    intent.putExtra("AI_CAREER_RESULT", careerResult);
                    startActivity(intent);
                });
            } catch (Exception e) {
                hideLoadingWithError("Lỗi dự đoán MLP: " + e.getMessage());
            }
        }).start();
    }

    private void showLoading(String text) {
        btnStartAIAnalysis.setEnabled(false);
        tvLoadingText.setText(text);
        layoutLoading.setVisibility(View.VISIBLE);
        layoutLoading.bringToFront();
    }

    private void hideLoadingWithError(String error) {
        runOnUiThread(() -> {
            layoutLoading.setVisibility(View.GONE);
            btnStartAIAnalysis.setEnabled(true);
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        });
    }

    private String getMBTICode() { return "INTJ"; }
    private String getHollandCode() { return "R"; }
    private String getDISCCode() { return "D"; }
    private float[] getBig5Values() { return new float[]{0.7f, 0.6f, 0.5f, 0.8f, 0.3f}; }

    private void loadDataFromFirestore() {
        db.collection("BaiTest").get().addOnSuccessListener(queryDocumentSnapshots -> {
            quizList.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                BaiTest quiz = doc.toObject(BaiTest.class);
                quiz.setMaTest(doc.getId());
                quizList.add(quiz);
            }
            quizAdapter.notifyDataSetChanged();
        });
    }

    private void showImageSourceDialog() {
        String[] options = {"Chụp ảnh mới", "Chọn từ thư viện"};
        new MaterialAlertDialogBuilder(this).setTitle("Nạp bảng điểm").setItems(options, (d, w) -> {
            if (w == 0) openCamera(); else openGallery();
        }).show();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            return;
        }
        try {
            File photoFile = File.createTempFile("cam_", ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            cameraImageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
            cameraLauncher.launch(cameraImageUri);
        } catch (IOException e) {
            Toast.makeText(this, "Lỗi tạo file ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        galleryLauncher.launch("image/*");
    }
}
