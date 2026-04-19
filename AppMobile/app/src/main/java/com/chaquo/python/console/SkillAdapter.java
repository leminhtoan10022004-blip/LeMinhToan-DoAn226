package com.chaquo.python.console;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class SkillAdapter extends RecyclerView.Adapter<SkillAdapter.SkillViewHolder> {

    private List<Map<String, Object>> skillList;

    public SkillAdapter(List<Map<String, Object>> skillList) {
        this.skillList = skillList;
    }

    @NonNull
    @Override
    public SkillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_skill_card, parent, false);
        return new SkillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SkillViewHolder holder, int position) {
        Map<String, Object> skill = skillList.get(position);
        
        holder.tvSkillName.setText((String) skill.get("TenKyNang"));
        holder.tvSkillType.setText((String) skill.get("Loai"));
        holder.tvSkillDescription.setText((String) skill.get("MoTa"));
    }

    @Override
    public int getItemCount() {
        return skillList != null ? skillList.size() : 0;
    }

    public static class SkillViewHolder extends RecyclerView.ViewHolder {
        TextView tvSkillName, tvSkillType, tvSkillDescription;

        public SkillViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSkillName = itemView.findViewById(R.id.tvSkillName);
            tvSkillType = itemView.findViewById(R.id.tvSkillType);
            tvSkillDescription = itemView.findViewById(R.id.tvSkillDescription);
        }
    }
}