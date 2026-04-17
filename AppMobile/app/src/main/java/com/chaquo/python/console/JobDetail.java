package com.chaquo.python.console;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chaquo.python.model.CongViec;
import com.chaquo.python.model.RoadmapStep;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JobDetail extends AppCompatActivity {

    private TextView tvJobTitleDetail, tvJobCategory, tvSalaryDetail, tvHotnessDetail, tvEducationDetail, tvJobDescriptionDetail;
    private RecyclerView rvRoadmap;
    private RoadmapAdapter roadmapAdapter;
    private List<RoadmapStep> roadmapList;
    private FirebaseFirestore db;
    private String jobCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        jobCode = getIntent().getStringExtra("jobCode");
        
        if (jobCode == null) {
            jobCode = "CV-001";
        }

        db = FirebaseFirestore.getInstance();
        
        initViews();
        loadJobDetailsFromFirestore();
        loadRoadmapFromFirestore();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        tvJobTitleDetail = findViewById(R.id.tvJobTitleDetail);
        tvJobCategory = findViewById(R.id.tvJobCategory);
        tvSalaryDetail = findViewById(R.id.tvSalaryDetail);
        tvHotnessDetail = findViewById(R.id.tvHotnessDetail);
        tvEducationDetail = findViewById(R.id.tvEducationDetail);
        tvJobDescriptionDetail = findViewById(R.id.tvJobDescriptionDetail);

        rvRoadmap = findViewById(R.id.rvRoadmap);
        roadmapList = new ArrayList<>();
        roadmapAdapter = new RoadmapAdapter(roadmapList);
        rvRoadmap.setLayoutManager(new LinearLayoutManager(this));
        rvRoadmap.setAdapter(roadmapAdapter);
    }

    private void loadJobDetailsFromFirestore() {
        db.collection("CongViec").document(jobCode).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String title = documentSnapshot.getString("TenCongViec");
                        Long minSalary = documentSnapshot.getLong("LuongToiThieu");
                        Long maxSalary = documentSnapshot.getLong("LuongToiDa");
                        String hot = documentSnapshot.getString("DoHot");
                        String education = documentSnapshot.getString("YeuCauDaoTao");
                        String description = documentSnapshot.getString("MoTa");
                        String industryId = documentSnapshot.getString("MaNganh");

                        tvJobTitleDetail.setText(title != null ? title : "");
                        tvSalaryDetail.setText(formatSalary(minSalary != null ? minSalary : 0) + " - " + formatSalary(maxSalary != null ? maxSalary : 0));
                        tvHotnessDetail.setText(hot != null ? hot : "");
                        tvEducationDetail.setText(education != null ? education : "");
                        tvJobDescriptionDetail.setText(description != null ? description : "");
                        
                        if (industryId != null) fetchIndustryName(industryId);
                    }
                })
                .addOnFailureListener(e -> Log.e("JobDetail", "Error loading job details", e));
    }

    private void fetchIndustryName(String maNganh) {
        db.collection("Nganh").document(maNganh).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        tvJobCategory.setText(doc.getString("TenNganh"));
                    }
                });
    }

    private void loadRoadmapFromFirestore() {
        db.collection("LoTrinh").document(jobCode).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        roadmapList.clear();
                        Map<String, Object> data = documentSnapshot.getData();
                        if (data == null) return;

                        List<Map<String, Object>> steps = null;
                        if (data.containsKey("steps") && data.get("steps") instanceof List) {
                            steps = (List<Map<String, Object>>) data.get("steps");
                        } else if (data.containsKey("data") && data.get("data") instanceof List) {
                            steps = (List<Map<String, Object>>) data.get("data");
                        }

                        if (steps != null) {
                            for (Map<String, Object> stepMap : steps) {
                                addStepFromMap(stepMap);
                            }
                        } else if (data.containsKey("0")) {
                            for (int i = 0; ; i++) {
                                Object stepData = data.get(String.valueOf(i));
                                if (stepData instanceof Map) {
                                    addStepFromMap((Map<String, Object>) stepData);
                                } else {
                                    break;
                                }
                            }
                        }
                        roadmapAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> Log.e("JobDetail", "Error loading roadmap", e));
    }

    private void addStepFromMap(Map<String, Object> stepMap) {
        RoadmapStep step = new RoadmapStep();
        if (stepMap.containsKey("BuocSo")) {
            Object val = stepMap.get("BuocSo");
            step.setBuocSo(val instanceof Long ? ((Long) val).intValue() : (val instanceof Integer ? (Integer) val : 0));
        }
        if (stepMap.containsKey("TenBuoc")) step.setTenBuoc((String) stepMap.get("TenBuoc"));
        if (stepMap.containsKey("MoTa")) step.setMoTa((String) stepMap.get("MoTa"));
        roadmapList.add(step);
    }

    private String formatSalary(long salary) {
        if (salary >= 1000000) return (salary / 1000000) + " Triệu";
        return String.valueOf(salary);
    }
}