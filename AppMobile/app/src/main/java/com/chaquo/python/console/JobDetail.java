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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chaquo.python.model.CongViec;
import com.chaquo.python.model.RoadmapStep;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JobDetail extends AppCompatActivity {

    private static final String TAG = "JobDetail";
    private TextView tvJobTitleDetail, tvJobCategory, tvSalaryDetail, tvHotnessDetail, tvEducationDetail, tvJobDescriptionDetail;
    private ImageView imgJobHeader;
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
        Log.d(TAG, "onCreate: jobCode = " + jobCode);
        
        if (jobCode == null) {
            jobCode = "CV-001";
        }

        db = FirebaseFirestore.getInstance();
        
        initViews();
        loadJobDetailsFromFirestore();
        loadRoadmapFromFirestore();
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

        imgJobHeader = findViewById(R.id.imgJobHeader);
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
        rvRoadmap.setNestedScrollingEnabled(false);
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
                        String imageUrl = documentSnapshot.getString("HinhAnh");

                        tvJobTitleDetail.setText(title != null ? title : "");
                        tvSalaryDetail.setText(formatSalary(minSalary != null ? minSalary : 0) + " - " + formatSalary(maxSalary != null ? maxSalary : 0));
                        tvHotnessDetail.setText(hot != null ? hot : "");
                        tvEducationDetail.setText(education != null ? education : "");
                        tvJobDescriptionDetail.setText(description != null ? description : "");
                        
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.background)
                                    .error(R.drawable.background)
                                    .into(imgJobHeader);
                        }

                        if (industryId != null) fetchIndustryName(industryId);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading job details", e));
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
        Log.d(TAG, "loadRoadmapFromFirestore: loading for " + jobCode);
        db.collection("LoTrinh").document(jobCode).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "loadRoadmapFromFirestore: Document exists");
                        roadmapList.clear();
                        
                        // Theo hình ảnh bạn gửi, danh sách các bước nằm trong trường "data"
                        List<Map<String, Object>> steps = (List<Map<String, Object>>) documentSnapshot.get("data");

                        if (steps != null) {
                            Log.d(TAG, "loadRoadmapFromFirestore: found " + steps.size() + " steps");
                            for (Map<String, Object> stepMap : steps) {
                                addStepFromMap(stepMap);
                            }
                        } else {
                            Log.d(TAG, "loadRoadmapFromFirestore: 'data' field not found or is not a list");
                        }
                        roadmapAdapter.notifyDataSetChanged();
                    } else {
                        Log.d(TAG, "loadRoadmapFromFirestore: Document does not exist for " + jobCode);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading roadmap", e));
    }

    private void addStepFromMap(Map<String, Object> stepMap) {
        if (stepMap == null) return;
        RoadmapStep step = new RoadmapStep();
        
        Object buocSo = stepMap.get("BuocSo");
        if (buocSo instanceof Long) step.setBuocSo(((Long) buocSo).intValue());
        else if (buocSo instanceof Integer) step.setBuocSo((Integer) buocSo);
        
        step.setTenBuoc((String) stepMap.get("TenBuoc"));
        step.setMoTa((String) stepMap.get("MoTa"));
        step.setThoiGian((String) stepMap.get("ThoiGian"));
        step.setHinhAnh((String) stepMap.get("HinhAnh"));
        
        if (stepMap.containsKey("KyNang") && stepMap.get("KyNang") instanceof List) {
            step.setKyNang((List<String>) stepMap.get("KyNang"));
        }
        
        roadmapList.add(step);
    }

    private String formatSalary(long salary) {
        if (salary >= 1000000) return (salary / 1000000) + " Triệu";
        return String.valueOf(salary);
    }

    private void setupSampleResources() {
        layoutBooks.removeAllViews();
        layoutGames.removeAllViews();
        addResourceIcon(layoutBooks, "Tài liệu 1", "https://google.com", R.drawable.information);
        addResourceIcon(layoutGames, "Trò chơi 1", "https://google.com", R.drawable.orientation);
    }

    private void addResourceIcon(LinearLayout parent, String label, String url, int iconRes) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        container.setPadding(0, 0, 40, 0);

        ImageView icon = new ImageView(this);
        icon.setLayoutParams(new LinearLayout.LayoutParams(120, 120));
        icon.setImageResource(iconRes);
        icon.setPadding(10, 10, 10, 10);

        TextView text = new TextView(this);
        text.setText(label);
        text.setTextSize(10);
        text.setGravity(Gravity.CENTER);

        container.addView(icon);
        container.addView(text);
        container.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))));
        parent.addView(container);
    }
}
