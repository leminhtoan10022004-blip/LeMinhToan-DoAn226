package com.chaquo.python.console;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.chaquo.python.model.CongViec;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class JobListByCategoryActivity extends AppCompatActivity {

    private RecyclerView rvJobs;
    private JobAdapter adapter;
    private List<CongViec> jobList;
    private FirebaseFirestore db;
    private String categoryId;
    private String categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_list_by_category);

        categoryId = getIntent().getStringExtra("CATEGORY_ID");
        categoryName = getIntent().getStringExtra("CATEGORY_NAME");
        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbarJobList);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(categoryName != null ? categoryName : "Việc làm");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvJobs = findViewById(R.id.rvJobsByCategory);
        jobList = new ArrayList<>();
        adapter = new JobAdapter(jobList);
        rvJobs.setLayoutManager(new LinearLayoutManager(this));
        rvJobs.setAdapter(adapter);

        loadJobsByCategory();
    }

    private void loadJobsByCategory() {
        if (categoryId == null) return;

        db.collection("CongViec")
                .whereEqualTo("MaNganh", categoryId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    jobList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        CongViec job = doc.toObject(CongViec.class);
                        job.setMaCongViec(doc.getId());
                        jobList.add(job);
                    }
                    adapter.notifyDataSetChanged();
                    if (jobList.isEmpty()) {
                        Toast.makeText(this, "Không có việc làm nào cho ngành này", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
