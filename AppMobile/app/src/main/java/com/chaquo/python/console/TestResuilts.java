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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

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
    private Map<String, String> scaleDescriptions = new HashMap<>();

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

        loadDescriptionsFromFirestore(() -> {
            String resultId = getIntent().getStringExtra("RESULT_ID");
            String aiCareer = getIntent().getStringExtra("AI_CAREER_RESULT");

            if (aiCareer != null) {
                displayAICareerResult(aiCareer);
            } else if (resultId != null) {
                fetchTestResultAndRecommend(resultId);
            } else {
                tvResultDescription.setText("Không tìm thấy dữ liệu kết quả.");
            }
        });
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

    private void loadDescriptionsFromFirestore(final Runnable onComplete) {
        loadingBar.setVisibility(View.VISIBLE);
        db.collection("ThangDo").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String code = doc.getId(); 
                        String desc = doc.getString("MoTa");
                        if (code != null && desc != null) {
                            scaleDescriptions.put(code, desc);
                        }
                    }
                    loadingBar.setVisibility(View.GONE);
                    onComplete.run();
                })
                .addOnFailureListener(e -> {
                    Log.e("TestResults", "Error loading descriptions", e);
                    loadingBar.setVisibility(View.GONE);
                    onComplete.run();
                });
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
        String description = scaleDescriptions.get(name);
        if (description == null) {
            Log.w("TestResults", "No description found for scale: " + name);
            return;
        }

        String formattedDesc = description;
        if (description.contains(":")) {
            String[] parts = description.split(":");
            if (parts.length > 1) {
                String trait = parts[1].trim();
                formattedDesc = "Bạn là người " + trait.substring(0, 1).toLowerCase() + trait.substring(1);
            }
        } else {
            formattedDesc = "Bạn có xu hướng " + description;
        }

        if (!formattedDesc.endsWith(".")) formattedDesc += ".";

        TextView textView = new TextView(this);
        textView.setText("• " + formattedDesc);
        textView.setTextColor(Color.parseColor("#2C3E50"));
        textView.setPadding(0, 12, 0, 12);
        textView.setTextSize(15);
        textView.setLineSpacing(1.1f, 1.1f);
        layoutTopScales.addView(textView);
    }

    private void runPythonRecommendation(List<String> topScales) {
        loadingBar.setVisibility(View.VISIBLE);
        db.collection("CongViec").get().addOnSuccessListener(jobsDocs -> {
            db.collection("CongViec_ThangDo").get().addOnSuccessListener(mappingDocs -> {
                try {
                    JSONObject erdJson = new JSONObject();
                    JSONObject jobsObj = new JSONObject();
                    for (DocumentSnapshot doc : jobsDocs) {
                        jobsObj.put(doc.getId(), new JSONObject(doc.getData()));
                    }
                    erdJson.put("CongViec", jobsObj);

                    JSONObject mappingObj = new JSONObject();
                    for (DocumentSnapshot doc : mappingDocs) {
                        mappingObj.put(doc.getId(), new JSONObject(doc.getData()));
                    }
                    erdJson.put("CongViec_ThangDo", mappingObj);

                    String erdJsonString = erdJson.toString();
                    String scalesJson = new Gson().toJson(topScales);

                    new Thread(() -> {
                        try {
                            Python py = Python.getInstance();
                            PyObject pyModule = py.getModule("career_recommender");
                            PyObject result = pyModule.callAttr("recommend_jobs", scalesJson, erdJsonString);
                            JSONArray recommendedJobs = new JSONArray(result.toString());

                            runOnUiThread(() -> {
                                loadingBar.setVisibility(View.GONE);
                                loadJobsFromFirestore(recommendedJobs);
                            });
                        } catch (Exception e) {
                            Log.e("AI_ERROR", "Error: " + e.getMessage());
                            runOnUiThread(() -> loadingBar.setVisibility(View.GONE));
                        }
                    }).start();

                } catch (Exception e) {
                    Log.e("DATA_ERROR", "Error building JSON", e);
                    loadingBar.setVisibility(View.GONE);
                }
            });
        });
    }

    private void displayAICareerResult(String career) {
        tvResultDescription.setText("Dựa trên phân tích toàn diện từ 4 bài test và kết quả học tập, AI nhận thấy bạn có tiềm năng lớn nhất trong lĩnh vực: " + career);
        tvResultDescription.setTextColor(Color.parseColor("#E91E63"));
        tvResultDescription.setTextSize(18);
        
        db.collection("CongViec")
                .whereEqualTo("TenCongViec", career)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    jobList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        CongViec job = doc.toObject(CongViec.class);
                        job.setMaCongViec(doc.getId());
                        jobList.add(job);
                    }
                    jobAdapter.notifyDataSetChanged();
                });
    }

    private void loadJobsFromFirestore(JSONArray recommendedJobs) {
        jobList.clear();
        try {
            if (recommendedJobs.length() > 0) {
                JSONObject topJob = recommendedJobs.getJSONObject(0);
                tvResultDescription.setText("Dựa trên đặc điểm cá nhân, bạn có xu hướng phù hợp nhất với công việc: " + 
                        topJob.getString("TenCongViec") + ". Dưới đây là lộ trình gợi ý cho bạn:");
            }

            for (int i = 0; i < Math.min(recommendedJobs.length(), 5); i++) {
                String jobId = recommendedJobs.getJSONObject(i).getString("MaCongViec");
                db.collection("CongViec").document(jobId).get()
                        .addOnSuccessListener(doc -> {
                            if (doc.exists()) {
                                CongViec job = doc.toObject(CongViec.class);
                                job.setMaCongViec(doc.getId());
                                jobList.add(job);
                                jobAdapter.notifyDataSetChanged();
                            }
                        });
            }
        } catch (Exception e) {
            Log.e("UI_ERROR", "Error updating UI: " + e.getMessage());
        }
    }
}
