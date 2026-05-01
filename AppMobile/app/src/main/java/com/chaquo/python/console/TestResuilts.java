package com.chaquo.python.console;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.chaquo.python.model.CongViec;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TestResuilts extends AppCompatActivity {

    private TextView tvResultDescription;
    private RecyclerView rvResultJobs;
    private JobAdapter jobAdapter;
    private List<CongViec> jobList;
    private FirebaseFirestore db;
    private LinearLayout layoutTopScales;
    private ProgressBar loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_test_resuilts);

        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        db = FirebaseFirestore.getInstance();
        initViews();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String resultId = getIntent().getStringExtra("RESULT_ID");
        if (resultId != null) {
            fetchTestResultAndRecommend(resultId);
        } else {
            // Test data for debugging
            tvResultDescription.setText("Không tìm thấy dữ liệu kết quả.");
        }
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        tvResultDescription = findViewById(R.id.tvResultDescription);
        rvResultJobs = findViewById(R.id.rvResultJobs);
        layoutTopScales = findViewById(R.id.layoutTopScales);
        loadingBar = findViewById(R.id.loadingBar);
        
        jobList = new ArrayList<>();
        jobAdapter = new JobAdapter(jobList);
        rvResultJobs.setLayoutManager(new LinearLayoutManager(this));
        rvResultJobs.setAdapter(jobAdapter);
    }

    private void fetchTestResultAndRecommend(String resultId) {
        loadingBar.setVisibility(View.VISIBLE);
        db.collection("KetQuaTest").document(resultId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> ketQua = (Map<String, Object>) documentSnapshot.get("KetQuaChiTiet");
                        if (ketQua != null) {
                            processResults(ketQua);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    loadingBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi tải kết quả test", Toast.LENGTH_SHORT).show();
                });
    }

    private void processResults(Map<String, Object> scores) {
        // 1. Tìm các thang đo có điểm cao nhất để hiển thị
        List<Map.Entry<String, Double>> sortedScores = new LinkedList<>(getConvertedScores(scores).entrySet());
        Collections.sort(sortedScores, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        List<String> topScales = new ArrayList<>();
        layoutTopScales.removeAllViews();
        
        int count = 0;
        for (Map.Entry<String, Double> entry : sortedScores) {
            if (count >= 4) break;
            topScales.add(entry.getKey());
            addScaleToUI(entry.getKey(), entry.getValue());
            count++;
        }

        // 2. Chạy Python Recommender dựa trên thiết kế ERD (CongViec_ThangDo)
        runPythonRecommendation(topScales);
    }

    private Map<String, Double> getConvertedScores(Map<String, Object> raw) {
        Map<String, Double> converted = new HashMap<>();
        for (Map.Entry<String, Object> entry : raw.entrySet()) {
            if (entry.getValue() instanceof Number) {
                converted.put(entry.getKey(), ((Number) entry.getValue()).doubleValue());
            }
        }
        return converted;
    }

    private void addScaleToUI(String name, Double value) {
        TextView textView = new TextView(this);
        textView.setText("• " + name + ": " + value + " điểm");
        textView.setTextColor(Color.DKGRAY);
        textView.setPadding(0, 8, 0, 8);
        textView.setTextSize(16);
        layoutTopScales.addView(textView);
    }

    private void runPythonRecommendation(List<String> topScales) {
        new Thread(() -> {
            try {
                Python py = Python.getInstance();
                PyObject pyModule = py.getModule("career_recommender");
                
                String scalesJson = new Gson().toJson(topScales);
                String erdJson = loadJSONFromAsset("ERD.json");
                
                if (erdJson == null) {
                    throw new Exception("Không thể tải file ERD.json từ assets.");
                }

                PyObject result = pyModule.callAttr("recommend_jobs", scalesJson, erdJson);
                
                String jsonResponse = result.toString();
                JSONArray recommendedJobs = new JSONArray(jsonResponse);
                
                runOnUiThread(() -> {
                    loadingBar.setVisibility(View.GONE);
                    if (recommendedJobs.length() > 0) {
                        loadJobsFromFirestore(recommendedJobs);
                    } else {
                        tvResultDescription.setText("Không tìm thấy nghề nghiệp phù hợp hoàn toàn. Hãy thử khám phá thêm các lĩnh vực khác!");
                    }
                });

            } catch (Exception e) {
                Log.e("PYTHON_ERROR", "Error: " + e.getMessage());
                runOnUiThread(() -> {
                    loadingBar.setVisibility(View.GONE);
                    tvResultDescription.setText("Lỗi xử lý AI: " + e.getMessage());
                });
            }
        }).start();
    }

    private String loadJSONFromAsset(String fileName) {
        String json;
        try {
            InputStream is = getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Log.e("ASSET_ERROR", "Không tìm thấy file " + fileName, ex);
            return null;
        }
        return json;
    }

    private void loadJobsFromFirestore(JSONArray recommendedJobs) {
        jobList.clear();
        final int[] loadedCount = {0};
        
        try {
            JSONObject topJob = recommendedJobs.getJSONObject(0);
            tvResultDescription.setText("Dựa trên phân tích, " + topJob.getString("TenCongViec") + 
                    " là nghề nghiệp phù hợp nhất với bạn. Hệ thống cũng gợi ý thêm một số lựa chọn tiềm năng dưới đây.");

            for (int i = 0; i < recommendedJobs.length(); i++) {
                String jobId = recommendedJobs.getJSONObject(i).getString("MaCongViec");
                db.collection("CongViec").document(jobId).get()
                        .addOnSuccessListener(doc -> {
                            if (doc.exists()) {
                                jobList.add(doc.toObject(CongViec.class));
                            }
                            loadedCount[0]++;
                            if (loadedCount[0] == recommendedJobs.length()) {
                                jobAdapter.notifyDataSetChanged();
                            }
                        });
            }
        } catch (Exception e) {
            Log.e("UI_ERROR", "Error: " + e.getMessage());
        }
    }
}
