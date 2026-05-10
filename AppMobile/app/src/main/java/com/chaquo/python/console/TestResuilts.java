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
import com.chaquo.python.model.KetQuaPhanTich;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
        db.collection("KetQuaPhanTich").document(resultId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        KetQuaPhanTich ketQua = documentSnapshot.toObject(KetQuaPhanTich.class);
                        if (ketQua != null && ketQua.getKetQuaChiTiet() != null) {
                            processResults(ketQua.getKetQuaChiTiet());
                        }
                    } else {
                        loadingBar.setVisibility(View.GONE);
                        tvResultDescription.setText("Kết quả không tồn tại.");
                    }
                })
                .addOnFailureListener(e -> {
                    loadingBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi tải kết quả phân tích", Toast.LENGTH_SHORT).show();
                });
    }

    private void processResults(Map<String, Integer> scores) {
        List<Map.Entry<String, Integer>> sortedScores = new LinkedList<>(scores.entrySet());
        Collections.sort(sortedScores, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        layoutTopScales.removeAllViews();
        List<String> topScales = new ArrayList<>();
        
        int count = 0;
        for (Map.Entry<String, Integer> entry : sortedScores) {
            if (count >= 3) break;
            topScales.add(entry.getKey());
            addScaleToUI(entry.getKey(), entry.getValue());
            count++;
        }
        runPythonRecommendation(topScales);
    }

    private void addScaleToUI(String name, Integer value) {
        TextView textView = new TextView(this);
        textView.setText("• Nhóm " + name + ": " + value + " điểm");
        textView.setTextColor(Color.parseColor("#1976D2"));
        textView.setPadding(0, 8, 0, 8);
        textView.setTextSize(16);
        textView.setTypeface(null, android.graphics.Typeface.BOLD);
        layoutTopScales.addView(textView);
    }

    private void runPythonRecommendation(List<String> topScales) {
        new Thread(() -> {
            try {
                Python py = Python.getInstance();
                PyObject pyModule = py.getModule("career_recommender");
                
                String scalesJson = new Gson().toJson(topScales);
                String erdJson = loadJSONFromAsset("ERD.json");
                
                if (erdJson == null) throw new Exception("Thiếu file ERD.json");

                PyObject result = pyModule.callAttr("recommend_jobs", scalesJson, erdJson);
                JSONArray recommendedJobs = new JSONArray(result.toString());
                
                runOnUiThread(() -> {
                    loadingBar.setVisibility(View.GONE);
                    if (recommendedJobs.length() > 0) {
                        loadJobsFromFirestore(recommendedJobs);
                    } else {
                        tvResultDescription.setText("Không tìm thấy nghề nghiệp phù hợp nhất. Hãy thử khám phá thêm các danh mục khác.");
                    }
                });

            } catch (Exception e) {
                Log.e("AI_ERROR", "Error: " + e.getMessage());
                runOnUiThread(() -> {
                    loadingBar.setVisibility(View.GONE);
                    tvResultDescription.setText("Lỗi xử lý phân tích AI.");
                });
            }
        }).start();
    }

    private String loadJSONFromAsset(String fileName) {
        try {
            InputStream is = getAssets().open(fileName);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            return new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return null;
        }
    }

    private void loadJobsFromFirestore(JSONArray recommendedJobs) {
        jobList.clear();
        try {
            JSONObject topJob = recommendedJobs.getJSONObject(0);
            tvResultDescription.setText("Kết quả phân tích cho thấy bạn có xu hướng phù hợp nhất với công việc: " + 
                    topJob.getString("TenCongViec") + ". Dưới đây là danh sách gợi ý chi tiết:");

            for (int i = 0; i < Math.min(recommendedJobs.length(), 5); i++) {
                String jobId = recommendedJobs.getJSONObject(i).getString("MaCongViec");
                db.collection("CongViec").document(jobId).get()
                        .addOnSuccessListener(doc -> {
                            if (doc.exists()) {
                                jobList.add(doc.toObject(CongViec.class));
                                jobAdapter.notifyDataSetChanged();
                            }
                        });
            }
        } catch (Exception e) {
            Log.e("UI_ERROR", "Error updating UI: " + e.getMessage());
        }
    }
}
