package com.chaquo.python.console;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        holder.tvStepNumber.setText("Bước " + step.getBuocSo());
        holder.tvStepTitle.setText(step.getTenBuoc());
        holder.tvStepDescription.setText(step.getMoTa());

        // Hide top line for the first item
        holder.lineTop.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
        // Hide bottom line for the last item
        holder.lineBottom.setVisibility(position == roadmapSteps.size() - 1 ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return roadmapSteps != null ? roadmapSteps.size() : 0;
    }

    public static class RoadmapViewHolder extends RecyclerView.ViewHolder {
        TextView tvStepNumber, tvStepTitle, tvStepDescription;
        View lineTop, lineBottom;

        public RoadmapViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStepNumber = itemView.findViewById(R.id.tvStepNumber);
            tvStepTitle = itemView.findViewById(R.id.tvStepTitle);
            tvStepDescription = itemView.findViewById(R.id.tvStepDescription);
            lineTop = itemView.findViewById(R.id.lineTop);
            lineBottom = itemView.findViewById(R.id.lineBottom);
        }
    }
}