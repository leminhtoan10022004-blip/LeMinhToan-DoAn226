package com.chaquo.python.console;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chaquo.python.model.RoadmapStep;

import java.util.List;

public class RoadmapAdapter extends RecyclerView.Adapter<RoadmapAdapter.RoadmapViewHolder> {

    private List<RoadmapStep> roadmapSteps;

    public RoadmapAdapter(List<RoadmapStep> roadmapSteps) {
        this.roadmapSteps = roadmapSteps;
    }

    @NonNull
    @Override
    public RoadmapViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_roadmap_step, parent, false);
        return new RoadmapViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoadmapViewHolder holder, int position) {
        RoadmapStep step = roadmapSteps.get(position);
        
        holder.tvStepCircleNumber.setText(String.valueOf(position + 1));
        holder.tvStepNumberLabel.setText("BƯỚC " + (position + 1));
        holder.tvStepTitle.setText(step.getTenBuoc());
        holder.tvStepDescription.setText(step.getMoTa());

        if (step.getThoiGian() != null && !step.getThoiGian().isEmpty()) {
            holder.tvDuration.setVisibility(View.VISIBLE);
            holder.tvDuration.setText("⏱ " + step.getThoiGian());
        } else {
            holder.tvDuration.setVisibility(View.GONE);
        }

        if (step.getKyNang() != null && !step.getKyNang().isEmpty()) {
            holder.layoutSkillsContainer.setVisibility(View.VISIBLE);
            StringBuilder skillsText = new StringBuilder();
            for (String skill : step.getKyNang()) {
                skillsText.append("• ").append(skill).append("\n");
            }
            holder.tvSkills.setText(skillsText.toString().trim());
        } else {
            holder.layoutSkillsContainer.setVisibility(View.GONE);
        }

        if (step.getHinhAnh() != null && !step.getHinhAnh().isEmpty()) {
            holder.ivStepImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(step.getHinhAnh())
                    .placeholder(R.drawable.background)
                    .into(holder.ivStepImage);
        } else {
            holder.ivStepImage.setVisibility(View.GONE);
        }

        holder.lineTop.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
        holder.lineBottom.setVisibility(position == roadmapSteps.size() - 1 ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return roadmapSteps != null ? roadmapSteps.size() : 0;
    }

    public static class RoadmapViewHolder extends RecyclerView.ViewHolder {
        TextView tvStepCircleNumber, tvStepNumberLabel, tvStepTitle, tvStepDescription, tvDuration, tvSkills;
        ImageView ivStepImage;
        View lineTop, lineBottom;
        LinearLayout layoutSkillsContainer;

        public RoadmapViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStepCircleNumber = itemView.findViewById(R.id.tvStepCircleNumber);
            tvStepNumberLabel = itemView.findViewById(R.id.tvStepNumberLabel);
            tvStepTitle = itemView.findViewById(R.id.tvStepTitle);
            tvStepDescription = itemView.findViewById(R.id.tvStepDescription);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvSkills = itemView.findViewById(R.id.tvSkills);
            ivStepImage = itemView.findViewById(R.id.ivStepImage);
            lineTop = itemView.findViewById(R.id.lineTop);
            lineBottom = itemView.findViewById(R.id.lineBottom);
            layoutSkillsContainer = itemView.findViewById(R.id.layoutSkillsContainer);
        }
    }
}
