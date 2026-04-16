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
        if (jobCode == null) jobCode = "CV-001"; 

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
                        CongViec job = documentSnapshot.toObject(CongViec.class);
                        if (job != null) {
                            tvJobTitleDetail.setText(job.getTenCongViec());
                            tvSalaryDetail.setText(formatSalary(job.getLuongToiThieu()) + " - " + formatSalary(job.getLuongToiDa()));
                            tvHotnessDetail.setText(job.getDoHot());
                            tvEducationDetail.setText(job.getYeuCauDaoTao());
                            tvJobDescriptionDetail.setText(job.getMoTa());
                            fetchIndustryName(job.getMaNganh());
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("JobDetail", "Firestore Error", e));
    }

    private void fetchIndustryName(String maNganh) {
        if (maNganh == null) return;
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

                        // Trường hợp 1: Dữ liệu được lưu dưới dạng Map các số "0", "1", "2"... (Do tool import)
                        if (data.containsKey("0")) {
                            for (int i = 0; ; i++) {
                                Object stepData = data.get(String.valueOf(i));
                                if (stepData instanceof Map) {
                                    addStepFromMap((Map<String, Object>) stepData);
                                } else {
                                    break;
                                }
                            }
                        } 
                        // Trường hợp 2: Dữ liệu là một mảng thực sự nằm trong một field (ví dụ field tên là "steps")
                        else {
                            for (Map.Entry<String, Object> entry : data.entrySet()) {
                                if (entry.getValue() instanceof List) {
                                    List<Map<String, Object>> steps = (List<Map<String, Object>>) entry.getValue();
                                    for (Map<String, Object> stepMap : steps) {
                                        addStepFromMap(stepMap);
                                    }
                                    break;
                                }
                            }
                        }
                        
                        roadmapAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> Log.e("JobDetail", "Lỗi tải lộ trình", e));
    }

    private void addStepFromMap(Map<String, Object> stepMap) {
        RoadmapStep step = new RoadmapStep();
        if (stepMap.containsKey("BuocSo")) {
            Object val = stepMap.get("BuocSo");
            step.setBuocSo(val instanceof Long ? ((Long) val).intValue() : (Integer) val);
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