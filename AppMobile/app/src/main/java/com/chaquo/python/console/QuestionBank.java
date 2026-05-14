package com.chaquo.python.console;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import androidx.annotation.NonNull;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class QuestionBank extends AppCompatActivity {

    private QuizAdapter quizAdapter;
    private List<BaiTest> quizList;
    private FirebaseFirestore db;
    private String userId;
    private LinearLayout layoutLockOverlay;
    private MaterialButton btnUploadTranscript, btnStartAIAnalysis;
    private TextView tvOcrStatus;

    private String selectedImagePath = null;
    private final Map<String, KetQuaPhanTich> userTestResults = new HashMap<>();

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCameraIntent();
                } else {
                    Toast.makeText(this, "Bạn cần cấp quyền Camera để chụp ảnh bảng điểm", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_question_bank);

        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

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

    @Override
    protected void onResume() {
        super.onResume();
        checkTestCompletion();
    }

    private void initViews() {
        RecyclerView rvQuizzes = findViewById(R.id.rvQuizzes);
        ImageView btnBack = findViewById(R.id.btnBack);
        layoutLockOverlay = findViewById(R.id.layoutLockOverlay);
        btnUploadTranscript = findViewById(R.id.btnUploadTranscript);
        btnStartAIAnalysis = findViewById(R.id.btnStartAIAnalysis);
        tvOcrStatus = findViewById(R.id.tvOcrStatus);

        quizList = new ArrayList<>();
        quizAdapter = new QuizAdapter(this, quizList);
        rvQuizzes.setLayoutManager(new GridLayoutManager(this, 2));
        rvQuizzes.setAdapter(quizAdapter);

        btnBack.setOnClickListener(v -> finish());

        if (layoutLockOverlay != null) {
            layoutLockOverlay.setOnClickListener(v -> {
                validateAndUnlock(true); 
            });
        }

        btnUploadTranscript.setOnClickListener(v -> showImageSourceDialog());
        btnStartAIAnalysis.setOnClickListener(v -> {
            if (selectedImagePath != null) {
                runTranscriptAnalysis(selectedImagePath);
            } else {
                Toast.makeText(this, "Vui lòng cung cấp ảnh bảng điểm", Toast.LENGTH_SHORT).show();
            }
        });
        
        unlockAIFeature();
    }

    private void showImageSourceDialog() {
        String[] options = {"Chụp ảnh mới", "Chọn từ thư viện"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cung cấp bảng điểm")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openCamera();
                    else openGallery();
                })
                .show();
    }

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    tvOcrStatus.setText("Đã chụp ảnh thành công");
                    tvOcrStatus.setVisibility(View.VISIBLE);
                    btnStartAIAnalysis.setVisibility(View.VISIBLE);
                } else {
                    selectedImagePath = null;
                    Toast.makeText(this, "Đã hủy chụp ảnh", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    selectedImagePath = getPathFromURI(uri);
                    if (selectedImagePath != null) {
                        tvOcrStatus.setText("Đã chọn ảnh từ thư viện");
                        tvOcrStatus.setVisibility(View.VISIBLE);
                        btnStartAIAnalysis.setVisibility(View.VISIBLE);
                    }
                }
            }
    );

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED) {
            startCameraIntent();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            try {
                File photoFile = createImageFile();
                Uri photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                // Cấp quyền ghi cho intent Camera
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                selectedImagePath = photoFile.getAbsolutePath();
                cameraLauncher.launch(intent);
            } catch (IOException ex) {
                Toast.makeText(this, "Không thể tạo file để lưu ảnh", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Thiết bị không tìm thấy ứng dụng Camera", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs();
        }
        return File.createTempFile("TRANSCRIPT_" + timeStamp, ".jpg", storageDir);
    }

    private String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        try (Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                res = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            }
        } catch (Exception e) {
            Log.e("QuestionBank", "Error getting path from URI", e);
        }
        return res;
    }

    private void checkTestCompletion() {
        if (userId == null) {
            unlockAIFeature(); 
            return;
        }

        db.collection("KetQuaPhanTich")
                .whereEqualTo("MaNguoiDung", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userTestResults.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        KetQuaPhanTich res = doc.toObject(KetQuaPhanTich.class);
                        String tid = doc.getString("MaTest");
                        if (tid == null) tid = doc.getString("maTest");
                        if (tid == null) tid = doc.getId(); 

                        if (tid != null) {
                            userTestResults.put(tid.toUpperCase(), res);
                        }
                    }
                    validateAndUnlock(false);
                })
                .addOnFailureListener(e -> unlockAIFeature());
    }

    private void validateAndUnlock(boolean showToast) {
        unlockAIFeature();
    }

    private void unlockAIFeature() {
        runOnUiThread(() -> {
            if (layoutLockOverlay != null) layoutLockOverlay.setVisibility(View.GONE);
            btnUploadTranscript.setEnabled(true);
        });
    }

    private void runTranscriptAnalysis(String imagePath) {
        tvOcrStatus.setText("AI đang đọc bảng điểm...");
        btnStartAIAnalysis.setEnabled(false);

        new Thread(() -> {
            try {
                Python py = Python.getInstance();
                try (PyObject result = py.getModule("transcript_ocr").callAttr("process_transcript_image", imagePath)) {
                    String jsonStr = result.toString();
                    runOnUiThread(() -> processAIPrediction(jsonStr));
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    tvOcrStatus.setText("Lỗi AI: " + e.getMessage());
                    btnStartAIAnalysis.setEnabled(true);
                });
            }
        }).start();
    }

    private void processAIPrediction(String transcriptJson) {
        try {
            JSONObject obj = new JSONObject(transcriptJson);
            if (obj.has("error")) {
                tvOcrStatus.setText("Lỗi: " + obj.getString("error"));
                btnStartAIAnalysis.setEnabled(true);
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
            tvOcrStatus.setText("Lỗi dữ liệu: " + e.getMessage());
            btnStartAIAnalysis.setEnabled(true);
        }
    }

    private void runMLPPrediction(String mbti, String holland, String disc, float[] big5, Map<String, Float> g) {
        new Thread(() -> {
            try {
                Python py = Python.getInstance();
                PyObject pyModule = py.getModule("career_mlp");
                pyModule.callAttr("init_model");

                try (PyObject res = pyModule.callAttr("predict_career",
                        mbti, holland, big5[0], big5[1], big5[2], big5[3], big5[4], disc,
                        g.get("Toan"), g.get("Ly"), g.get("Hoa"), g.get("Sinh"), g.get("Van"),
                        g.get("Anh"), g.get("Tin"), g.get("Dia"), g.get("Su"))) {

                    String career = res.toString();
                    runOnUiThread(() -> {
                        Intent intent = new Intent(this, TestResuilts.class);
                        intent.putExtra("AI_CAREER_RESULT", career);
                        startActivity(intent);
                        btnStartAIAnalysis.setEnabled(true);
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    tvOcrStatus.setText("Lỗi dự đoán: " + e.getMessage());
                    btnStartAIAnalysis.setEnabled(true);
                });
            }
        }).start();
    }

    private int getSafeScore(Map<String, Integer> scores, String key, int def) {
        if (scores == null) return def;
        Integer val = scores.get(key);
        return (val != null) ? val : def;
    }

    private String getMBTICode() {
        KetQuaPhanTich res = userTestResults.get("BT-002");
        if (res == null || res.getKetQuaChiTiet() == null) return "INTP";
        Map<String, Integer> s = res.getKetQuaChiTiet();
        return (getSafeScore(s, "E", 0) >= getSafeScore(s, "I", 0) ? "E" : "I") +
               (getSafeScore(s, "S", 0) >= getSafeScore(s, "N", 0) ? "S" : "N") +
               (getSafeScore(s, "T", 0) >= getSafeScore(s, "F", 0) ? "T" : "F") +
               (getSafeScore(s, "J", 0) >= getSafeScore(s, "P", 0) ? "J" : "P");
    }

    private String getHollandCode() {
        KetQuaPhanTich res = userTestResults.get("BT-001");
        if (res == null || res.getKetQuaChiTiet() == null) return "R";
        Map<String, Integer> scores = res.getKetQuaChiTiet();
        String maxKey = "R";
        int maxScore = -1;
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            if (entry.getValue() != null && entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                maxKey = entry.getKey();
            }
        }
        return maxKey;
    }

    private String getDISCCode() {
        KetQuaPhanTich res = userTestResults.get("BT-004");
        if (res == null || res.getKetQuaChiTiet() == null) return "D";
        Map<String, Integer> scores = res.getKetQuaChiTiet();
        String maxKey = "D";
        int maxScore = -1;
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            if (entry.getValue() != null && entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                maxKey = entry.getKey();
            }
        }
        return maxKey;
    }

    private float[] getBig5Values() {
        KetQuaPhanTich res = userTestResults.get("BT-003");
        if (res == null || res.getKetQuaChiTiet() == null) return new float[]{0.5f, 0.5f, 0.5f, 0.5f, 0.5f};
        Map<String, Integer> s = res.getKetQuaChiTiet();
        return new float[]{
                getSafeScore(s, "O", 50) / 100f,
                getSafeScore(s, "C", 50) / 100f,
                getSafeScore(s, "E", 50) / 100f,
                getSafeScore(s, "A", 50) / 100f,
                getSafeScore(s, "N", 50) / 100f
        };
    }

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
}
