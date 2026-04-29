package com.chaquo.python.console;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private LinearLayout layoutBooks, layoutGames;

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
        
        // Cài đặt icon cho Sách và Game mẫu với liên kết
        setupSampleResources();
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
        
        layoutBooks = findViewById(R.id.layoutBooks);
        layoutGames = findViewById(R.id.layoutGames);

        rvRoadmap = findViewById(R.id.rvRoadmap);
        roadmapList = new ArrayList<>();
        roadmapAdapter = new RoadmapAdapter(roadmapList);
        rvRoadmap.setLayoutManager(new LinearLayoutManager(this));
        rvRoadmap.setAdapter(roadmapAdapter);
    }

    private void setupSampleResources() {
        layoutBooks.removeAllViews();
        layoutGames.removeAllViews();

        addResourceIcon(layoutBooks, "Code dạo", "https://tiki.vn/search?q=code+dao+ky+su", android.R.drawable.ic_menu_info_details);
        addResourceIcon(layoutBooks, "Clean Code", "https://tiki.vn/search?q=clean+code", android.R.drawable.ic_menu_info_details);
        addResourceIcon(layoutBooks, "Soft Skills", "https://tiki.vn/search?q=soft+skills", android.R.drawable.ic_menu_info_details);

        addResourceIcon(layoutGames, "Grasshopper", "https://play.google.com/store/apps/details?id=com.area120.grasshopper", android.R.drawable.ic_menu_view);
        addResourceIcon(layoutGames, "CodeCombat", "https://codecombat.com/", android.R.drawable.ic_menu_view);
        addResourceIcon(layoutGames, "Elevate", "https://play.google.com/store/apps/details?id=com.wonder", android.R.drawable.ic_menu_view);
    }

    private void addResourceIcon(LinearLayout parent, String label, String url, int iconRes) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        container.setPadding(0, 0, 40, 0);

        ImageView icon = new ImageView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(140, 140);
        icon.setLayoutParams(params);
        icon.setImageResource(iconRes);
        icon.setBackgroundResource(android.R.drawable.btn_default_small);
        icon.setPadding(25, 25, 25, 25);
        icon.setElevation(4f);

        TextView text = new TextView(this);
        text.setText(label);
        text.setTextSize(10);
        text.setGravity(Gravity.CENTER);
        text.setTextColor(Color.DKGRAY);
        text.setPadding(0, 8, 0, 0);

        container.addView(icon);
        container.addView(text);

        container.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        parent.addView(container);
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
