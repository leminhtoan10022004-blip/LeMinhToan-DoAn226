package com.chaquo.python.console;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Kiểm tra xem đã nạp dữ liệu lần nào chưa
        SharedPreferences prefs = getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
        boolean isDataImported = prefs.getBoolean("isDataImported", false);

        if (!isDataImported) {
            uploadAllTables();
        } else {
            Log.d("FIREBASE", "Dữ liệu đã được nạp trước đó, chuyển hướng sang Login...");
            chuyenSangLogin();
        }
    }

    private void uploadAllTables() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        try {
            InputStream is = getAssets().open("ERD.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String jsonString = new String(buffer, "UTF-8");
            JSONObject root = new JSONObject(jsonString);

            Iterator<String> tableNames = root.keys();
            int totalOperationCount = 0;

            while (tableNames.hasNext()) {
                String tableName = tableNames.next();
                JSONObject tableData = root.getJSONObject(tableName);
                Iterator<String> docIds = tableData.keys();

                while (docIds.hasNext()) {
                    String docId = docIds.next();
                    Object docValue = tableData.get(docId);

                    Map<String, Object> dataMap;
                    if (docValue instanceof JSONObject) {
                        dataMap = jsonObjectToMap((JSONObject) docValue);
                    } else if (docValue instanceof JSONArray) {
                        dataMap = new HashMap<>();
                        dataMap.put("data", jsonArrayToList((JSONArray) docValue));
                    } else {
                        continue;
                    }

                    batch.set(db.collection(tableName).document(docId), dataMap);
                    totalOperationCount++;
                    
                    if (totalOperationCount >= 450) {
                         commitBatch(batch, totalOperationCount, false);
                         batch = db.batch();
                         totalOperationCount = 0;
                    }
                }
            }

            if (totalOperationCount > 0) {
                commitBatch(batch, totalOperationCount, true);
            } else {
                chuyenSangLogin();
            }

        } catch (Exception e) {
            Log.e("FIREBASE_ERROR", "Lỗi xử lý dữ liệu", e);
            chuyenSangLogin();
        }
    }

    private void commitBatch(WriteBatch batch, int count, boolean isLastBatch) {
        batch.commit().addOnSuccessListener(aVoid -> {
            Log.d("FIREBASE_OK", "Đã nạp thành công " + count + " bản ghi!");
            if (isLastBatch) {
                // Đánh dấu đã nạp xong để lần sau không nạp lại
                SharedPreferences prefs = getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
                prefs.edit().putBoolean("isDataImported", true).apply();
                
                Toast.makeText(this, "Nạp dữ liệu gốc thành công!", Toast.LENGTH_SHORT).show();
                chuyenSangLogin();
            }
        }).addOnFailureListener(e -> {
            Log.e("FIREBASE_ERROR", "Lỗi khi nạp dữ liệu: " + e.getMessage());
            if (isLastBatch) chuyenSangLogin();
        });
    }

    private void chuyenSangLogin() {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        finish();
    }

    private Map<String, Object> jsonObjectToMap(JSONObject json) throws JSONException {
        Map<String, Object> map = new HashMap<>();
        Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = json.get(key);
            if (value instanceof JSONObject) {
                value = jsonObjectToMap((JSONObject) value);
            } else if (value instanceof JSONArray) {
                value = jsonArrayToList((JSONArray) value);
            }
            map.put(key, value);
        }
        return map;
    }

    private List<Object> jsonArrayToList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONObject) {
                value = jsonObjectToMap((JSONObject) value);
            } else if (value instanceof JSONArray) {
                value = jsonArrayToList((JSONArray) value);
            }
            list.add(value);
        }
        return list;
    }
}