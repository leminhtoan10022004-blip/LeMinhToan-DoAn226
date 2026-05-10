package com.chaquo.python.console;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
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
    private ExtendedFloatingActionButton fabSaveRoadmap;
    private String userId;
    private boolean isFollowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        jobCode = getIntent().getStringExtra("jobCode");
        
        if (jobCode == null) {
            jobCode = "CV-001";
        }

        // Lấy userId từ SharedPreferences
        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = pref.getString("USER_ID", null);

        db = FirebaseFirestore.getInstance();
        
        initViews();
        loadJobDetailsFromFirestore();
        loadRoadmapFromFirestore();
        setupSampleResources();
        checkFollowStatus();

        if (fabSaveRoadmap != null) {
            fabSaveRoadmap.setOnClickListener(v -> toggleFollowStatus());
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

        imgJobHeader = findViewById(R.id.imgJobHeader);
        tvJobTitleDetail = findViewById(R.id.tvJobTitleDetail);
        tvJobCategory = findViewById(R.id.tvJobCategory);
        tvSalaryDetail = findViewById(R.id.tvSalaryDetail);
        tvHotnessDetail = findViewById(R.id.tvHotnessDetail);
        tvEducationDetail = findViewById(R.id.tvEducationDetail);
        tvJobDescriptionDetail = findViewById(R.id.tvJobDescriptionDetail);
        fabSaveRoadmap = findViewById(R.id.fabSaveRoadmap);
        
        layoutBooks = findViewById(R.id.layoutBooks);
        layoutGames = findViewById(R.id.layoutGames);

        rvRoadmap = findViewById(R.id.rvRoadmap);
        roadmapList = new ArrayList<>();
        roadmapAdapter = new RoadmapAdapter(roadmapList);
        rvRoadmap.setLayoutManager(new LinearLayoutManager(this));
        rvRoadmap.setNestedScrollingEnabled(false);
        rvRoadmap.setAdapter(roadmapAdapter);
    }

    private void checkFollowStatus() {
        if (userId == null || jobCode == null) return;

        db.collection("NguoiDung_BanTin")
                .document(userId + "_" + jobCode)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    isFollowing = documentSnapshot.exists();
                    updateFollowButtonUI();
                });
    }

    private void updateFollowButtonUI() {
        if (isFollowing) {
            fabSaveRoadmap.setText("Lộ trình đã theo dõi");
            fabSaveRoadmap.setIconResource(android.R.drawable.ic_menu_delete);
            fabSaveRoadmap.setAlpha(0.6f);
        } else {
            fabSaveRoadmap.setText("Bắt đầu lộ trình");
            fabSaveRoadmap.setIconResource(android.R.drawable.ic_input_add);
            fabSaveRoadmap.setAlpha(1.0f);
        }
    }

    private void toggleFollowStatus() {
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để thực hiện", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isFollowing) {
            new AlertDialog.Builder(this)
                    .setTitle("Hủy theo dõi")
                    .setMessage("Bạn có chắc chắn muốn ngừng theo dõi lộ trình này không?")
                    .setPositiveButton("Hủy theo dõi", (dialog, which) -> {
                        db.collection("NguoiDung_BanTin")
                                .document(userId + "_" + jobCode)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    isFollowing = false;
                                    updateFollowButtonUI();
                                    Toast.makeText(JobDetail.this, "Đã hủy theo dõi lộ trình", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(JobDetail.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Đóng", null)
                    .show();
        } else {
            Map<String, Object> data = new HashMap<>();
            data.put("MaNguoiDung", userId);
            data.put("MaBanTin", jobCode); // Ở đây tạm coi mỗi lộ trình là 1 bản tin đặc biệt
            data.put("TrangThai", "Đang theo dõi");
            data.put("NgayDocLanCuoi", Timestamp.now());
            data.put("YeuThich", true);
            // Thêm trường này để hiển thị UI nhanh hơn
            data.put("TenCongViec", tvJobTitleDetail.getText().toString());

            db.collection("NguoiDung_BanTin")
                    .document(userId + "_" + jobCode)
                    .set(data)
                    .addOnSuccessListener(aVoid -> {
                        isFollowing = true;
                        updateFollowButtonUI();
                        Toast.makeText(JobDetail.this, "Đã bắt đầu theo dõi lộ trình!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(JobDetail.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
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
        db.collection("LoTrinh").document(jobCode).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        roadmapList.clear();
                        List<Map<String, Object>> steps = (List<Map<String, Object>>) documentSnapshot.get("data");

                        if (steps != null) {
                            for (Map<String, Object> stepMap : steps) {
                                addStepFromMap(stepMap);
                            }
                        }
                        roadmapAdapter.notifyDataSetChanged();
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
