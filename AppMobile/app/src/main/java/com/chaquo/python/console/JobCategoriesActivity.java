package com.chaquo.python.console;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.chaquo.python.model.Nganh;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class JobCategoriesActivity extends AppCompatActivity {

    private RecyclerView rvCategories;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_categories);

        db = FirebaseFirestore.getInstance();
        userId = getIntent().getStringExtra("USER_ID");

        Toolbar toolbar = findViewById(R.id.toolbarCategories);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Danh mục ngành nghề");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvCategories = findViewById(R.id.rvCategories);
        rvCategories.setLayoutManager(new LinearLayoutManager(this));

        loadCategories();
    }

    private void loadCategories() {
        db.collection("Nganh")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Nganh> categories = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Nganh nganh = doc.toObject(Nganh.class);
                        nganh.setMaNganh(doc.getId());
                        categories.add(nganh);
                    }
                    if (categories.isEmpty()) {
                        Toast.makeText(this, "Không có dữ liệu ngành nghề", Toast.LENGTH_SHORT).show();
                    }
                    rvCategories.setAdapter(new CategoryAdapter(categories));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
        List<Nganh> categories;
        CategoryAdapter(List<Nganh> categories) { this.categories = categories; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_jod_category_card, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Nganh cat = categories.get(position);
            holder.tvTitle.setText(cat.getTenNganh());
            holder.tvDescription.setText(cat.getMoTa());
            
            // Ẩn phần chỉ số (Vị trí tuyển, Tăng trưởng, Lương) trong màn hình Danh mục
            holder.layoutStats.setVisibility(View.GONE);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(JobCategoriesActivity.this, JobListByCategoryActivity.class);
                intent.putExtra("CATEGORY_ID", cat.getMaNganh());
                intent.putExtra("CATEGORY_NAME", cat.getTenNganh());
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return categories.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDescription;
            View layoutStats;
            ViewHolder(View v) { 
                super(v); 
                tvTitle = v.findViewById(R.id.tvCategoryTitle);
                tvDescription = v.findViewById(R.id.tvCategoryDescription);
                layoutStats = v.findViewById(R.id.layoutStats);
            }
        }
    }
}
