package com.chaquo.python.console;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FirestoreImporter {
    private static final String TAG = "FirestoreImporter";

    public static void importData(Context context) {
        String json = loadJSONFromAsset(context, "ERD.json");
        if (json == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            JSONObject root = new JSONObject(json);
            
            // Chỉ định danh sách các collection muốn import
            String[] targetCollections = {"Sach", "TroChoi"};

            for (String collectionName : targetCollections) {
                if (!root.has(collectionName)) {
                    Log.w(TAG, "Không tìm thấy collection: " + collectionName + " trong file JSON");
                    continue;
                }

                JSONObject documents = root.getJSONObject(collectionName);
                Iterator<String> docIds = documents.keys();

                WriteBatch batch = db.batch();
                int count = 0;

                while (docIds.hasNext()) {
                    String docId = docIds.next();
                    Object docData = documents.get(docId);

                    if (docData instanceof JSONObject) {
                        Map<String, Object> map = jsonToMap((JSONObject) docData);
                        // Sử dụng SetOptions.merge() để không ghi đè mất dữ liệu cũ nếu trùng ID
                        batch.set(db.collection(collectionName).document(docId), map, SetOptions.merge());
                        count++;
                    } else if (docData instanceof JSONArray) {
                        Map<String, Object> wrapper = new HashMap<>();
                        wrapper.put("steps", jsonToList((JSONArray) docData));
                        batch.set(db.collection(collectionName).document(docId), wrapper, SetOptions.merge());
                        count++;
                    }

                    if (count >= 400) {
                        batch.commit();
                        batch = db.batch();
                        count = 0;
                    }
                }
                
                batch.commit().addOnSuccessListener(aVoid -> 
                    Log.d(TAG, "Import thành công collection: " + collectionName)
                ).addOnFailureListener(e -> 
                    Log.e(TAG, "Lỗi khi import " + collectionName, e)
                );
            }
        } catch (JSONException e) {
            Log.e(TAG, "Lỗi parse JSON", e);
        }
    }

    private static String loadJSONFromAsset(Context context, String fileName) {
        String json;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Log.e(TAG, "Không tìm thấy file " + fileName, ex);
            return null;
        }
        return json;
    }

    private static Map<String, Object> jsonToMap(JSONObject jsonObject) throws JSONException {
        Map<String, Object> map = new HashMap<>();
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                value = jsonToMap((JSONObject) value);
            } else if (value instanceof JSONArray) {
                value = jsonToList((JSONArray) value);
            }
            map.put(key, value);
        }
        return map;
    }

    private static List<Object> jsonToList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONObject) {
                value = jsonToMap((JSONObject) value);
            } else if (value instanceof JSONArray) {
                value = jsonToList((JSONArray) value);
            }
            list.add(value);
        }
        return list;
    }
}
