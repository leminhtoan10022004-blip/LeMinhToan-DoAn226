package com.chaquo.python.console;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.model.CongViec;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestResuilts extends AppCompatActivity {

    private TextView tvResultDescription;
    private RecyclerView rvResultJobs;
    private JobAdapter jobAdapter;
    private List<CongViec> jobList;
    private FirebaseFirestore db;
    
    // UI components for scores (optional, if you want to update them dynamically)
    private ProgressBar pbTech, pbCreative, pbBiz;
    private TextView tvTech, tvCreative, tvBiz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_test_resuilts);
        
        db = FirebaseFirestore.getInstance();
        initViews();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Nhận ID kết quả từ DetailQuestion
        String resultId = getIntent().getStringExtra("RESULT_ID");
        if (resultId != null) {
            fetchTestResultAndPredict(resultId);
        }
    }

    private void initViews() {
        tvResultDescription = findViewById(R.id.tvResultDescription);
        rvResultJobs = findViewById(R.id.rvResultJobs);
        
        jobList = new ArrayList<>();
        jobAdapter = new JobAdapter(jobList);
        rvResultJobs.setLayoutManager(new LinearLayoutManager(this));
        rvResultJobs.setAdapter(jobAdapter);

        pbTech = findViewById(R.id.scoresLayout).findViewWithTag("pbTech"); // Note: You'd need tags or IDs in XML
        // For simplicity, let's just use the hardcoded progress bars for now or update XML with IDs
    }

    private void fetchTestResultAndPredict(String resultId) {
        db.collection("KetQuaTest").document(resultId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> ketQua = (Map<String, Object>) documentSnapshot.get("KetQuaChiTiet");
                        if (ketQua != null) {
                            runPythonPrediction(ketQua);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải kết quả test", Toast.LENGTH_SHORT).show());
    }

    private void runPythonPrediction(Map<String, Object> scores) {
        try {
            Python py = Python.getInstance();
            PyObject pyModule = py.getModule("career_mlp");

            // Trích xuất dữ liệu từ Map (Giả sử các key tương ứng với thang đo trong Python)
            // MBTI, Holland, Big5, DISC, và điểm các môn học
            // Đây là ví dụ, bạn cần ánh xạ đúng key từ Firestore vào tham số hàm Python
            String mbti = (String) scores.getOrDefault("MBTI", "INTJ");
            String holland = (String) scores.getOrDefault("Holland", "R");
            float big5_o = getFloat(scores, "Big5_O");
            float big5_c = getFloat(scores, "Big5_C");
            float big5_e = getFloat(scores, "Big5_E");
            float big5_a = getFloat(scores, "Big5_A");
            float big5_n = getFloat(scores, "Big5_N");
            String disc = (String) scores.getOrDefault("DISC", "D");
            
            float toan = getFloat(scores, "Toan");
            float ly = getFloat(scores, "Ly");
            float hoa = getFloat(scores, "Hoa");
            float sinh = getFloat(scores, "Sinh");
            float van = getFloat(scores, "Van");
            float anh = getFloat(scores, "Anh");
            float tin = getFloat(scores, "Tin");
            float dia = getFloat(scores, "Dia");
            float su = getFloat(scores, "Su");

            PyObject result = pyModule.callAttr("predict_career", 
                    mbti, holland, big5_o, big5_c, big5_e, big5_a, big5_n, disc,
                    toan, ly, hoa, sinh, van, anh, tin, dia, su);

            String predictedCareer = result.toString();
            updateUI(predictedCareer);

        } catch (Exception e) {
            Log.e("PYTHON_ERROR", "Error: " + e.getMessage());
            tvResultDescription.setText("Lỗi dự đoán AI: " + e.getMessage());
        }
    }

    private float getFloat(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).floatValue();
        return 0.0f;
    }

    private void updateUI(String career) {
        tvResultDescription.setText("Dựa trên phân tích AI, nghề nghiệp phù hợp nhất với bạn là: " + career + 
                "\n\nBạn có những tố chất rất phù hợp để phát triển trong lĩnh vực này.");
        
        // Tải danh sách công việc liên quan từ Firestore
        db.collection("CongViec")
                .whereEqualTo("TenCongViec", career) // Hoặc tìm kiếm theo lĩnh vực
                .limit(3)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    jobList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        jobList.add(doc.toObject(CongViec.class));
                    }
                    jobAdapter.notifyDataSetChanged();
                });
    }
}
