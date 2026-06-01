package com.chaquo.python.console;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.chaquo.python.model.RoadmapStep;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
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
    private FloatingActionButton fabAiRoadmap;
    private String userId;
    private boolean isFollowing = false;

    // Dữ liệu để gửi cho AI
    private Map<String, Object> jobDataMap = new HashMap<>();
    private List<Map<String, Object>> booksList = new ArrayList<>();
    private List<Map<String, Object>> gamesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        jobCode = getIntent().getStringExtra("jobCode");
        if (jobCode == null || jobCode.isEmpty()) jobCode = "CV-001";

        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = pref.getString("USER_ID", null);

        db = FirebaseFirestore.getInstance();
        
        initViews();
        resolveJobCodeAndLoadData();
        checkFollowStatus();

        if (fabSaveRoadmap != null) {
            fabSaveRoadmap.setOnClickListener(v -> toggleFollowStatus());
        }

        if (fabAiRoadmap != null) {
            fabAiRoadmap.setOnClickListener(v -> openAiRoadmapAssistant());
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
        fabAiRoadmap = findViewById(R.id.fabAiRoadmap);
        
        layoutBooks = findViewById(R.id.layoutBooks);
        layoutGames = findViewById(R.id.layoutGames);

        rvRoadmap = findViewById(R.id.rvRoadmap);
        roadmapList = new ArrayList<>();
        roadmapAdapter = new RoadmapAdapter(roadmapList);
        rvRoadmap.setLayoutManager(new LinearLayoutManager(this));
        rvRoadmap.setNestedScrollingEnabled(false);
        rvRoadmap.setAdapter(roadmapAdapter);
    }

    private void updateAdapterContext() {
        if (roadmapAdapter == null) return;
        
        jobDataMap.put("TenCongViec", tvJobTitleDetail.getText().toString());
        jobDataMap.put("MoTa", tvJobDescriptionDetail.getText().toString());
        jobDataMap.put("YeuCauDaoTao", tvEducationDetail.getText().toString());
        
        Gson gson = new Gson();
        String jobJson = gson.toJson(jobDataMap);
        String stepsJson = gson.toJson(roadmapList);
        
        Map<String, Object> resources = new HashMap<>();
        resources.put("books", booksList);
        resources.put("games", gamesList);
        String resJson = gson.toJson(resources);
        
        roadmapAdapter.setContextData(jobJson, stepsJson, resJson);
    }

    private void openAiRoadmapAssistant() {
        updateAdapterContext(); // Đảm bảo dữ liệu mới nhất
        Intent intent = new Intent(this, ChatBotActivity.class);
        intent.putExtra("isRoadmapMode", true);
        
        Gson gson = new Gson();
        intent.putExtra("jobDetailJson", gson.toJson(jobDataMap));
        intent.putExtra("roadmapStepsJson", gson.toJson(roadmapList));
        
        Map<String, Object> resources = new HashMap<>();
        resources.put("books", booksList);
        resources.put("games", gamesList);
        intent.putExtra("resourcesJson", gson.toJson(resources));
        
        startActivity(intent);
    }

    private void resolveJobCodeAndLoadData() {
        if (jobCode.startsWith("BT-")) {
            db.collection("BanTin").document(jobCode).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String realCode = doc.getString("MaCongViec");
                            if (realCode != null) this.jobCode = realCode;
                        } else {
                            this.jobCode = resolveJobCodeFromAssets(this.jobCode);
                        }
                        updateAndLoad();
                    })
                    .addOnFailureListener(e -> {
                        this.jobCode = resolveJobCodeFromAssets(this.jobCode);
                        updateAndLoad();
                    });
        } else {
            updateAndLoad();
        }
    }

    private void updateAndLoad() {
        roadmapAdapter.setJobCode(this.jobCode);
        loadAllData();
    }

    private String resolveJobCodeFromAssets(String btCode) {
        try {
            InputStream is = getAssets().open("ERD.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            JSONObject root = new JSONObject(new String(buffer, StandardCharsets.UTF_8));
            JSONObject banTinRoot = root.getJSONObject("BanTin");
            if (banTinRoot.has(btCode)) {
                return banTinRoot.getJSONObject(btCode).optString("MaCongViec", btCode);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error resolving code from assets", e);
        }
        return btCode;
    }

    private void loadAllData() {
        loadJobDetailsFromFirestore();
        loadRoadmap();
        loadBooksFromFirestore();
        loadGamesFromFirestore();
    }

    private void loadRoadmap() {
        db.collection("LoTrinh").document(jobCode).get()
                .addOnSuccessListener(documentSnapshot -> {
                    roadmapList.clear();
                    if (documentSnapshot.exists()) {
                        Map<String, Object> data = documentSnapshot.getData();
                        if (data != null) {
                            if (data.containsKey("steps") && data.get("steps") instanceof List) {
                                List<Map<String, Object>> steps = (List<Map<String, Object>>) data.get("steps");
                                for (Map<String, Object> stepMap : steps) addStepFromMap(stepMap);
                            } else {
                                for (Object val : data.values()) {
                                    if (val instanceof Map) addStepFromMap((Map<String, Object>) val);
                                }
                            }
                        }
                        if (roadmapList.isEmpty()) loadRoadmapFromAssets();
                        else sortAndRefreshRoadmap();
                    } else {
                        loadRoadmapFromAssets();
                    }
                })
                .addOnFailureListener(e -> loadRoadmapFromAssets());
    }

    private void loadRoadmapFromAssets() {
        try {
            InputStream is = getAssets().open("ERD.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            JSONObject root = new JSONObject(new String(buffer, StandardCharsets.UTF_8));
            JSONObject loTrinhRoot = root.getJSONObject("LoTrinh");

            if (loTrinhRoot.has(jobCode)) {
                roadmapList.clear();
                JSONArray stepsArray = loTrinhRoot.getJSONArray(jobCode);
                for (int i = 0; i < stepsArray.length(); i++) {
                    JSONObject stepJson = stepsArray.getJSONObject(i);
                    RoadmapStep step = new RoadmapStep();
                    step.setBuocSo(stepJson.optInt("BuocSo"));
                    step.setTenBuoc(stepJson.optString("TenBuoc"));
                    step.setMoTa(stepJson.optString("MoTa"));
                    step.setThoiGian(stepJson.optString("ThoiGian", "Đang cập nhật"));
                    step.setHinhAnh(stepJson.optString("HinhAnh", ""));
                    roadmapList.add(step);
                }
                sortAndRefreshRoadmap();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading from assets", e);
        }
    }

    private void addStepFromMap(Map<String, Object> stepMap) {
        if (stepMap == null) return;
        RoadmapStep step = new RoadmapStep();
        Object buocSoObj = stepMap.get("BuocSo");
        if (buocSoObj instanceof Long) step.setBuocSo(((Long) buocSoObj).intValue());
        else if (buocSoObj instanceof Integer) step.setBuocSo((Integer) buocSoObj);
        step.setTenBuoc((String) stepMap.get("TenBuoc"));
        step.setMoTa((String) stepMap.get("MoTa"));
        step.setThoiGian((String) stepMap.get("ThoiGian"));
        step.setHinhAnh((String) stepMap.get("HinhAnh"));
        roadmapList.add(step);
    }

    private void sortAndRefreshRoadmap() {
        Collections.sort(roadmapList, (s1, s2) -> Integer.compare(s1.getBuocSo(), s2.getBuocSo()));
        runOnUiThread(() -> {
            roadmapAdapter.notifyDataSetChanged();
            updateAdapterContext();
        });
    }

    private void loadJobDetailsFromFirestore() {
        db.collection("CongViec").document(jobCode).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        tvJobTitleDetail.setText(doc.getString("TenCongViec"));
                        Long min = doc.getLong("LuongToiThieu");
                        Long max = doc.getLong("LuongToiDa");
                        tvSalaryDetail.setText(formatSalary(min != null ? min : 0) + " - " + formatSalary(max != null ? max : 0));
                        tvHotnessDetail.setText(doc.getString("DoHot"));
                        tvEducationDetail.setText(doc.getString("YeuCauDaoTao"));
                        tvJobDescriptionDetail.setText(doc.getString("MoTa"));
                        String imageUrl = doc.getString("HinhAnh");
                        if (imageUrl != null) loadResourceImage(imageUrl, imgJobHeader, R.drawable.background, 0);

                        String industryId = doc.getString("MaNganh");
                        if (industryId != null) {
                            db.collection("Nganh").document(industryId).get()
                                    .addOnSuccessListener(d -> { if (d.exists()) tvJobCategory.setText(d.getString("TenNganh")); });
                        }
                        updateAdapterContext();
                    }
                });
    }

    private void checkFollowStatus() {
        if (userId == null) return;
        db.collection("NguoiDung_BanTin").document(userId + "_" + jobCode).get()
                .addOnSuccessListener(doc -> { isFollowing = doc.exists(); updateFollowButtonUI(); });
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
            db.collection("NguoiDung_BanTin").document(userId + "_" + jobCode).delete()
                    .addOnSuccessListener(aVoid -> { isFollowing = false; updateFollowButtonUI(); });
        } else {
            Map<String, Object> data = new HashMap<>();
            data.put("MaNguoiDung", userId);
            data.put("MaBanTin", jobCode);
            data.put("TrangThai", "Đang theo dõi");
            data.put("NgayDocLanCuoi", Timestamp.now());
            data.put("TenCongViec", tvJobTitleDetail.getText().toString());
            db.collection("NguoiDung_BanTin").document(userId + "_" + jobCode).set(data)
                    .addOnSuccessListener(aVoid -> { isFollowing = true; updateFollowButtonUI(); });
        }
    }

    private String formatSalary(long salary) {
        return (salary >= 1000000) ? (salary / 1000000) + " Triệu" : String.valueOf(salary);
    }

    private void loadBooksFromFirestore() {
        layoutBooks.removeAllViews();
        booksList.clear();
        db.collection("Sach").whereEqualTo("MaCongViec", jobCode).get().addOnSuccessListener(snaps -> {
            for (QueryDocumentSnapshot doc : snaps) {
                booksList.add(doc.getData());
                addResourceItem(layoutBooks, doc.getString("TenSach"), doc.getString("DuongDan"), doc.getString("HinhAnh"), R.drawable.information, 65, 95, 6);
            }
            if (snaps.isEmpty()) addEmptyMessage(layoutBooks, "Chưa có sách gợi ý");
            updateAdapterContext();
        });
    }

    private void loadGamesFromFirestore() {
        layoutGames.removeAllViews();
        gamesList.clear();
        db.collection("TroChoi").whereEqualTo("MaCongViec", jobCode).get().addOnSuccessListener(snaps -> {
            for (QueryDocumentSnapshot doc : snaps) {
                gamesList.add(doc.getData());
                addResourceItem(layoutGames, doc.getString("TenTroChoi"), doc.getString("DuongDan"), doc.getString("Icon"), R.drawable.orientation, 60, 60, 12);
            }
            if (snaps.isEmpty()) addEmptyMessage(layoutGames, "Chưa có trò chơi mô phỏng");
            updateAdapterContext();
        });
    }

    private void addEmptyMessage(LinearLayout parent, String message) {
        TextView tv = new TextView(this);
        tv.setText(message); tv.setTextSize(11); tv.setTextColor(Color.GRAY);
        tv.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8));
        parent.addView(tv);
    }

    private void addResourceItem(LinearLayout parent, String label, String url, String imageSource, int defaultIcon, int widthDp, int heightDp, int radiusDp) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL); container.setGravity(Gravity.CENTER_HORIZONTAL);
        container.setPadding(0, 0, dpToPx(20), 0);
        container.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(widthDp + 20), ViewGroup.LayoutParams.WRAP_CONTENT));

        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(widthDp), dpToPx(heightDp)));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        loadResourceImage(imageSource, imageView, defaultIcon, radiusDp);

        TextView textView = new TextView(this);
        textView.setText(label); textView.setTextSize(9); textView.setTextColor(Color.parseColor("#555555"));
        textView.setGravity(Gravity.CENTER); textView.setMaxLines(2);

        container.addView(imageView); container.addView(textView);
        container.setOnClickListener(v -> { if (url != null && !url.isEmpty()) startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); });
        parent.addView(container);
    }

    private void loadResourceImage(String source, ImageView imageView, int placeholder, int radiusDp) {
        if (source == null || source.isEmpty()) { imageView.setImageResource(placeholder); return; }
        Object reqSource = source.startsWith("http") ? source : getResources().getIdentifier(source.replace(".png","").replace(".webp",""), "drawable", getPackageName());
        if (reqSource.equals(0)) reqSource = placeholder;
        Glide.with(this).load(reqSource).placeholder(placeholder).error(placeholder)
                .transform(new CenterCrop(), new RoundedCorners(dpToPx(radiusDp > 0 ? radiusDp : 1))).into(imageView);
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
