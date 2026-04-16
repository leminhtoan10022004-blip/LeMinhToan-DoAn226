package com.chaquo.python.console;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chaquo.python.model.BaiTest;

import java.util.List;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizViewHolder> {

    private List<BaiTest> quizList;
    private Context context;

    public QuizAdapter(Context context, List<BaiTest> quizList) {
        this.context = context;
        this.quizList = quizList;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        BaiTest quiz = quizList.get(position);
        holder.tvTitle.setText(quiz.getTieuDe());
        holder.tvDescription.setText(quiz.getMoTaLoai());
        holder.tvTime.setText("⏱ " + quiz.getThoiGian() + " phút");
        holder.tvQuestionCount.setText("📋 " + quiz.getSoLuongCauHoi() + " câu hỏi");

        // Xử lý hình ảnh tĩnh theo tên từ dữ liệu (Firestore)
        String imageName = quiz.getHinhAnh(); // Ví dụ: "mbti_icon"
        if (imageName != null && !imageName.isEmpty()) {
            int resId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
            if (resId != 0) {
                holder.imgQuiz.setImageResource(resId);
            } else {
                // Mặc định nếu không tìm thấy
                holder.imgQuiz.setImageResource(R.drawable.mbti_icon);
            }
        } else {
            holder.imgQuiz.setImageResource(R.drawable.mbti_icon);
        }
    }

    @Override
    public int getItemCount() {
        return quizList != null ? quizList.size() : 0;
    }

    public static class QuizViewHolder extends RecyclerView.ViewHolder {
        ImageView imgQuiz;
        TextView tvTitle, tvDescription, tvTime, tvQuestionCount;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            imgQuiz = itemView.findViewById(R.id.imgQuiz);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvQuestionCount = itemView.findViewById(R.id.tvQuestionCount);
        }
    }
}