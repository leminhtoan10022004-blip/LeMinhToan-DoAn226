package com.chaquo.python.console;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Trend extends AppCompatActivity {

    private RecyclerView rvTrendingCategories;
    private TrendAdapter trendAdapter;
    private List<Map<String, Object>> trendList;
    private FirebaseFirestore db;
    private ImageButton btnBackTrend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trend);
        
        db = FirebaseFirestore.getInstance();
        
        initViews();
        loadTrendingData();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        btnBackTrend = findViewById(R.id.btnBackTrend);
        if (btnBackTrend != null) {
            btnBackTrend.setOnClickListener(v -> finish());
        }

        rvTrendingCategories = findViewById(R.id.rvTrendingCategories);
        trendList = new ArrayList<>();
        trendAdapter = new TrendAdapter(trendList);
        rvTrendingCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvTrendingCategories.setAdapter(trendAdapter);
    }

    private void loadTrendingData() {
        db.collection("XuHuong")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    trendList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Map<String, Object> xuHuong = document.getData();
                        String maNganh = (String) xuHuong.get("MaNganh");
                        
                        if (maNganh != null) {
                            fetchNganhDetails(maNganh, xuHuong);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Không thể tải dữ liệu xu hướng", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchNganhDetails(String maNganh, Map<String, Object> xuHuong) {
        db.collection("Nganh").document(maNganh).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> finalData = new HashMap<>(xuHuong);
                        finalData.put("TenNganh", documentSnapshot.getString("TenNganh"));
                        trendList.add(finalData);
                        trendAdapter.notifyDataSetChanged();
                    }
                });
    }
}