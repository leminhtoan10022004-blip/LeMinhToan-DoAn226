package com.chaquo.python.console;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chaquo.python.model.BaiTest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class QuestionBank extends AppCompatActivity {

    private RecyclerView rvQuizzes;
    private QuizAdapter quizAdapter;
    private List<BaiTest> quizList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_question_bank);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        loadDataFromFirestore();
    }

    private void initViews() {
        rvQuizzes = findViewById(R.id.rvQuizzes);
        quizList = new ArrayList<>();
        quizAdapter = new QuizAdapter(this, quizList);
        
        rvQuizzes.setLayoutManager(new GridLayoutManager(this, 2));
        rvQuizzes.setAdapter(quizAdapter);
        
        db = FirebaseFirestore.getInstance();
    }

    private void loadDataFromFirestore() {
        db.collection("BaiTest")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        quizList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            BaiTest quiz = document.toObject(BaiTest.class);
                            quizList.add(quiz);
                        }
                        quizAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("QuestionBank", "Error getting documents: ", task.getException());
                        Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}