package com.chaquo.python.console;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class MLPSampleActivity extends AppCompatActivity {

    private EditText etMbti, etHolland, etDisc;
    private EditText etO, etC, etE, etA, etN;
    private EditText etToan, etLy, etHoa, etSinh, etVan, etAnh, etTin, etDia, etSu;
    private Button btnPredict;
    private TextView tvResult;
    private LinearLayout layoutLoading;
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mlp_sample);

        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();

        btnPredict.setOnClickListener(v -> {
            try {
                performPrediction();
            } catch (Exception e) {
                Log.e("MLPSample", "Error in prediction", e);
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews() {
        etMbti = findViewById(R.id.etMbti);
        etHolland = findViewById(R.id.etHolland);
        etDisc = findViewById(R.id.etDisc);
        
        etO = findViewById(R.id.etO);
        etC = findViewById(R.id.etC);
        etE = findViewById(R.id.etE);
        etA = findViewById(R.id.etA);
        etN = findViewById(R.id.etN);
        
        etToan = findViewById(R.id.etToan);
        etLy = findViewById(R.id.etLy);
        etHoa = findViewById(R.id.etHoa);
        etSinh = findViewById(R.id.etSinh);
        etVan = findViewById(R.id.etVan);
        etAnh = findViewById(R.id.etAnh);
        etTin = findViewById(R.id.etTin);
        etDia = findViewById(R.id.etDia);
        etSu = findViewById(R.id.etSu);
        
        btnPredict = findViewById(R.id.btnPredict);
        tvResult = findViewById(R.id.tvResult);
        layoutLoading = findViewById(R.id.layoutLoading);

        // Giá trị mặc định
        etMbti.setText("INTJ");
        etHolland.setText("R");
        etDisc.setText("D");
        etO.setText("0.8"); etC.setText("0.7"); etE.setText("0.3"); etA.setText("0.5"); etN.setText("0.2");
        etToan.setText("9.0"); etLy.setText("8.5"); etHoa.setText("8.0"); 
        etSinh.setText("7.0"); etVan.setText("6.5"); etAnh.setText("8.0"); 
        etTin.setText("9.5"); etDia.setText("6.0"); etSu.setText("5.5");
    }

    private void performPrediction() {
        // Hiện màn hình load ngay lập tức trên UI thread
        layoutLoading.setVisibility(View.VISIBLE);
        layoutLoading.bringToFront(); // Đưa lên trên cùng
        btnPredict.setEnabled(false);

        Python py = Python.getInstance();
        PyObject pyModule = py.getModule("career_mlp");

        new Thread(() -> {
            try {
                pyModule.callAttr("init_model");

                // Lấy dữ liệu từ UI
                String mbti = etMbti.getText().toString();
                String holland = etHolland.getText().toString();
                String disc = etDisc.getText().toString();
                
                float o = Float.parseFloat(etO.getText().toString());
                float c = Float.parseFloat(etC.getText().toString());
                float e = Float.parseFloat(etE.getText().toString());
                float a = Float.parseFloat(etA.getText().toString());
                float n = Float.parseFloat(etN.getText().toString());
                
                float toan = Float.parseFloat(etToan.getText().toString());
                float ly = Float.parseFloat(etLy.getText().toString());
                float hoa = Float.parseFloat(etHoa.getText().toString());
                float sinh = Float.parseFloat(etSinh.getText().toString());
                float van = Float.parseFloat(etVan.getText().toString());
                float anh = Float.parseFloat(etAnh.getText().toString());
                float tin = Float.parseFloat(etTin.getText().toString());
                float dia = Float.parseFloat(etDia.getText().toString());
                float su = Float.parseFloat(etSu.getText().toString());

                // AI xử lý
                PyObject top5Results = pyModule.callAttr("predict_career_top_5", 
                        mbti, holland, o, c, e, a, n, disc,
                        toan, ly, hoa, sinh, van, anh, tin, dia, su);

                List<PyObject> resultList = top5Results.asList();
                
                // Chuẩn bị kết quả
                Map<String, Integer> careerProbabilities = new HashMap<>();
                StringBuilder sb = new StringBuilder();
                sb.append("GỢI Ý TOP 5 NGHỀ NGHIỆP PHÙ HỢP:\n\n");

                for (int i = 0; i < resultList.size(); i++) {
                    PyObject item = resultList.get(i);
                    String career = item.get("career").toString();
                    double prob = item.get("probability").toDouble();
                    careerProbabilities.put(career, (int)prob);
                    sb.append(String.format(Locale.getDefault(), "%d. %s: %.1f%%\n", (i + 1), career, prob));
                }

                String finalResultText = sb.toString();
                String topCareer = resultList.get(0).get("career").toString();

                // Lưu vào Firebase
                saveResultToFirebase(topCareer, careerProbabilities, mbti, holland, disc);

                runOnUiThread(() -> {
                    tvResult.setText(finalResultText);
                    layoutLoading.setVisibility(View.GONE);
                    btnPredict.setEnabled(true);
                });

            } catch (Exception ex) {
                runOnUiThread(() -> {
                    tvResult.setText("Lỗi: " + ex.getMessage());
                    layoutLoading.setVisibility(View.GONE);
                    btnPredict.setEnabled(true);
                });
            }
        }).start();
    }

    private void saveResultToFirebase(String topCareer, Map<String, Integer> probabilities, String mbti, String holland, String disc) {
        if (mAuth.getCurrentUser() == null) return;
        
        String userId = mAuth.getCurrentUser().getUid();
        String resultId = UUID.randomUUID().toString();
        
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("MaKetQua", resultId);
        resultData.put("MaNguoiDung", userId);
        resultData.put("MaTest", "MLP_AI_ANALYSIS");
        resultData.put("MaNganhPhuHop", topCareer);
        resultData.put("NgayThucHien", Timestamp.now());
        resultData.put("KetQuaChiTiet", probabilities);
        
        Map<String, String> inputs = new HashMap<>();
        inputs.put("MBTI", mbti);
        inputs.put("Holland", holland);
        inputs.put("DISC", disc);
        resultData.put("DuLieuDauVao", inputs);

        db.collection("KetQuaPhanTich").document(resultId).set(resultData);
    }
}
