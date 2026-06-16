package com.chaquo.python.console;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class MLPSampleActivity extends AppCompatActivity {

    private EditText etMbti, etHolland, etDisc;
    private EditText etO, etC, etE, etA, etN;
    private EditText etToan, etLy, etHoa, etSinh, etVan, etAnh, etTin, etDia, etSu;
    private Button btnPredict, btnUploadTranscript;
    private TextView tvResult, tvLoadingText;
    private LinearLayout layoutLoading, layoutResultContainer;
    private String lastPredictionResult = "";
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> { if (uri != null) handleImageSelected(uri); }
    );

    private Uri cameraImageUri;
    private final ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> { if (success && cameraImageUri != null) handleImageSelected(cameraImageUri); }
    );

    private final ActivityResultLauncher<String> cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> { if (isGranted) openCamera(); }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mlp_sample);

        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();

        btnPredict.setOnClickListener(v -> performPrediction());
        btnUploadTranscript.setOnClickListener(v -> showImageSourceDialog());

        // KHI NHẤN VÀO KẾT QUẢ -> MỞ CHAT ĐỂ AI TƯ VẤN CHI TIẾT
        layoutResultContainer.setOnClickListener(v -> {
            if (!lastPredictionResult.isEmpty()) {
                Intent intent = new Intent(this, ChatBotActivity.class);
                intent.putExtra("PRESET_MESSAGE", "Dựa trên kết quả dự đoán MLP sau đây, hãy tư vấn chi tiết cho tôi về định hướng nghề nghiệp: " + lastPredictionResult);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Hãy thực hiện dự đoán trước khi nhấn vào đây", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews() {
        etMbti = findViewById(R.id.etMbti);
        etHolland = findViewById(R.id.etHolland);
        etDisc = findViewById(R.id.etDisc);
        etO = findViewById(R.id.etO); etC = findViewById(R.id.etC);
        etE = findViewById(R.id.etE); etA = findViewById(R.id.etA); etN = findViewById(R.id.etN);
        
        etToan = findViewById(R.id.etToan); etLy = findViewById(R.id.etLy);
        etHoa = findViewById(R.id.etHoa); etSinh = findViewById(R.id.etSinh);
        etVan = findViewById(R.id.etVan); etAnh = findViewById(R.id.etAnh);
        etTin = findViewById(R.id.etTin); etDia = findViewById(R.id.etDia); etSu = findViewById(R.id.etSu);
        
        btnPredict = findViewById(R.id.btnPredict);
        btnUploadTranscript = findViewById(R.id.btnUploadTranscript);
        tvResult = findViewById(R.id.tvResult);
        tvLoadingText = findViewById(R.id.tvLoadingText);
        layoutLoading = findViewById(R.id.layoutLoading);
        layoutResultContainer = findViewById(R.id.layoutResultContainer);

        // Mặc định
        etMbti.setText("INTJ"); etHolland.setText("R"); etDisc.setText("D");
    }

    private void showImageSourceDialog() {
        String[] options = {"Chụp ảnh", "Chọn từ thư viện"};
        new MaterialAlertDialogBuilder(this).setTitle("Nạp bảng điểm").setItems(options, (d, w) -> {
            if (w == 0) openCamera(); else galleryLauncher.launch("image/*");
        }).show();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            return;
        }
        try {
            File photoFile = File.createTempFile("mlp_cam_", ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            cameraImageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
            cameraLauncher.launch(cameraImageUri);
        } catch (IOException e) {
            Toast.makeText(this, "Lỗi Camera", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleImageSelected(Uri uri) {
        showLoading("AI ĐANG ĐỌC BẢNG ĐIỂM...");
        new Thread(() -> {
            try {
                File tempFile = createTempFileFromUri(uri);
                Python py = Python.getInstance();
                PyObject pyModule = py.getModule("transcript_ocr");
                PyObject result = pyModule.callAttr("process_transcript_image", tempFile.getAbsolutePath());
                
                runOnUiThread(() -> {
                    fillGradesFromOcr(result.toString());
                    layoutLoading.setVisibility(View.GONE);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi OCR: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    layoutLoading.setVisibility(View.GONE);
                });
            }
        }).start();
    }

    private void fillGradesFromOcr(String jsonStr) {
        try {
            JSONObject obj = new JSONObject(jsonStr);
            if (!obj.has("bang_diem")) return;
            JSONArray arr = obj.getJSONArray("bang_diem");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject item = arr.getJSONObject(i);
                String mon = item.optString("mon_hoc", "").toLowerCase();
                String diem = String.valueOf(item.optDouble("diem_tb", 0.0));
                
                if (mon.contains("toán")) etToan.setText(diem);
                else if (mon.contains("lý")) etLy.setText(diem);
                else if (mon.contains("hóa")) etHoa.setText(diem);
                else if (mon.contains("văn")) etVan.setText(diem);
                else if (mon.contains("anh")) etAnh.setText(diem);
                else if (mon.contains("sinh")) etSinh.setText(diem);
                else if (mon.contains("tin")) etTin.setText(diem);
                else if (mon.contains("địa")) etDia.setText(diem);
                else if (mon.contains("sử")) etSu.setText(diem);
            }
            Toast.makeText(this, "Đã tự động điền điểm từ ảnh", Toast.LENGTH_SHORT).show();
        } catch (Exception ignored) {}
    }

    private File createTempFileFromUri(Uri uri) throws IOException {
        File tempFile = File.createTempFile("ocr_mlp_", ".jpg", getCacheDir());
        try (InputStream is = getContentResolver().openInputStream(uri);
             FileOutputStream os = new FileOutputStream(tempFile)) {
            byte[] buf = new byte[1024]; int len;
            while ((len = is.read(buf)) > 0) os.write(buf, 0, len);
        }
        return tempFile;
    }

    private void performPrediction() {
        showLoading("AI ĐANG PHÂN TÍCH...");
        new Thread(() -> {
            try {
                Python py = Python.getInstance();
                PyObject pyModule = py.getModule("career_mlp");
                pyModule.callAttr("init_model");

                PyObject top5Res = pyModule.callAttr("predict_career_top_5", 
                        etMbti.getText().toString(), etHolland.getText().toString(),
                        Float.parseFloat(etO.getText().toString()), Float.parseFloat(etC.getText().toString()),
                        Float.parseFloat(etE.getText().toString()), Float.parseFloat(etA.getText().toString()),
                        Float.parseFloat(etN.getText().toString()), etDisc.getText().toString(),
                        Float.parseFloat(etToan.getText().toString()), Float.parseFloat(etLy.getText().toString()),
                        Float.parseFloat(etHoa.getText().toString()), Float.parseFloat(etSinh.getText().toString()),
                        Float.parseFloat(etVan.getText().toString()), Float.parseFloat(etAnh.getText().toString()),
                        Float.parseFloat(etTin.getText().toString()), Float.parseFloat(etDia.getText().toString()),
                        Float.parseFloat(etSu.getText().toString()));

                List<PyObject> list = top5Res.asList();
                StringBuilder sb = new StringBuilder("KẾT QUẢ DỰ ĐOÁN TOP 5:\n\n");
                for (int i = 0; i < list.size(); i++) {
                    sb.append(String.format(Locale.getDefault(), "%d. %s: %.1f%%\n", 
                        (i+1), list.get(i).get("career"), list.get(i).get("probability").toDouble()));
                }

                lastPredictionResult = sb.toString();

                runOnUiThread(() -> {
                    tvResult.setText(sb.toString() + "\n(Nhấn vào đây để Trợ lý AI tư vấn chi tiết)");
                    layoutLoading.setVisibility(View.GONE);
                });
            } catch (Exception ex) {
                runOnUiThread(() -> {
                    tvResult.setText("Lỗi: " + ex.getMessage());
                    layoutLoading.setVisibility(View.GONE);
                });
            }
        }).start();
    }

    private void showLoading(String text) {
        runOnUiThread(() -> {
            tvLoadingText.setText(text);
            layoutLoading.setVisibility(View.VISIBLE);
            layoutLoading.bringToFront();
        });
    }
}
