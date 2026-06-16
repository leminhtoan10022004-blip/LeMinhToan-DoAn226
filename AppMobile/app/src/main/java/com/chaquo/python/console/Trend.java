package com.chaquo.python.console;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.chaquo.python.model.BanTin;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Trend extends AppCompatActivity {

    private RecyclerView rvTrend, rvNews, rvSkills;
    private NewsAdapter newsAdapter;
    private SkillAdapter skillAdapter;
    private List<Map<String, Object>> trendList;
    private List<BanTin> newsList;
    private List<Map<String, Object>> skillList;
    private FirebaseFirestore db;
    private TextView tvAiForecast;
    private MaterialCardView cardAiForecast;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private String lastAiResponse = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trend);

        db = FirebaseFirestore.getInstance();
        initViews();
        loadData();
    }

    private void initViews() {
        View btnBack = findViewById(R.id.btnBackTrend);
        btnBack.setOnClickListener(v -> finish());

        // Ngành nghề bùng nổ
        rvTrend = findViewById(R.id.rvTrendingCategories);
        rvTrend.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        trendList = new ArrayList<>();

        // Bản tin thị trường - CHUYỂN VỀ HÀNG NGANG
        rvNews = findViewById(R.id.rvLatestNews);
        rvNews.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        newsList = new ArrayList<>();
        newsAdapter = new NewsAdapter(newsList);
        rvNews.setAdapter(newsAdapter);

        // Kỹ năng được săn đón - Lưới 2 cột
        rvSkills = findViewById(R.id.rvTrendingSkills);
        rvSkills.setLayoutManager(new GridLayoutManager(this, 2));
        skillList = new ArrayList<>();
        skillAdapter = new SkillAdapter(skillList);
        rvSkills.setAdapter(skillAdapter);

        tvAiForecast = findViewById(R.id.tvAiForecast);
        cardAiForecast = findViewById(R.id.cardAiForecast);

        // KHI NHẤN VÀO KHUNG AI DỰ BÁO -> MỞ CHAT
        cardAiForecast.setOnClickListener(v -> {
            if (!lastAiResponse.isEmpty() && !lastAiResponse.startsWith("Đang") && !lastAiResponse.startsWith("Lỗi")) {
                Intent intent = new Intent(Trend.this, ChatBotActivity.class);
                intent.putExtra("PRESET_MESSAGE", "Hãy giải thích chi tiết hơn về dự báo xu hướng này giúp tôi: " + lastAiResponse);
                startActivity(intent);
            }
        });
    }

    private void loadData() {
        // Load xu hướng từ collection "XuHuong"
        db.collection("XuHuong")
                .orderBy("SlgTuyen", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    trendList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        trendList.add(doc.getData());
                    }
                    runAiPrediction();
                })
                .addOnFailureListener(e -> Log.e("Trend", "Lỗi tải XuHuong: " + e.getMessage()));

        // Load tin tức từ collection "BanTin"
        db.collection("BanTin")
                .orderBy("NgayDang", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    newsList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        newsList.add(doc.toObject(BanTin.class));
                    }
                    newsAdapter.notifyDataSetChanged();
                    runAiPrediction();
                })
                .addOnFailureListener(e -> Log.e("Trend", "Lỗi tải BanTin: " + e.getMessage()));

        // Load kỹ năng từ collection "KyNang" (Bổ sung phần này)
        db.collection("KyNang")
                .limit(6)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    skillList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        skillList.add(doc.getData());
                    }
                    skillAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("Trend", "Lỗi tải KyNang: " + e.getMessage()));
    }

    private void runAiPrediction() {
        if (trendList.isEmpty() || newsList.isEmpty()) return;

        tvAiForecast.setText("AI đang phân tích thị trường...");
        executor.execute(() -> {
            try {
                if (!Python.isStarted()) Python.start(new AndroidPlatform(this));
                
                String trendJson = new Gson().toJson(trendList);
                String newsJson = new Gson().toJson(newsList);

                Python py = Python.getInstance();
                PyObject pyModule = py.getModule("chatbot_logic");
                
                // GỌI HÀM GEMINI 2.5 FLASH
                final String aiResponse = pyModule.callAttr("get_trend_prediction", trendJson, newsJson).toString();
                lastAiResponse = aiResponse;

                runOnUiThread(() -> {
                    String formatted = aiResponse.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>")
                            .replace("\n", "<br>");
                    tvAiForecast.setText(HtmlCompat.fromHtml(formatted, HtmlCompat.FROM_HTML_MODE_LEGACY));
                });
            } catch (Exception e) {
                runOnUiThread(() -> tvAiForecast.setText("Lỗi AI: " + e.getMessage()));
            }
        });
    }
}
