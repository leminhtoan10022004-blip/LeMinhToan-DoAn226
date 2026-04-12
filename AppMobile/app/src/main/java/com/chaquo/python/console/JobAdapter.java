package com.chaquo.python.console;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chaquo.python.model.CongViec;
import com.google.android.material.chip.Chip;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private List<CongViec> jobList;

    public JobAdapter(List<CongViec> jobList) {
        this.jobList = jobList;
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job_card, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        CongViec job = jobList.get(position);
        holder.tvJobTitle.setText(job.getTenCongViec());
        holder.tvEducation.setText(job.getYeuCauDaoTao());
        
        // Định dạng lương: 15.000.000 -> 15 Triệu
        String salaryRange = formatSalary(job.getLuongToiThieu()) + " - " + formatSalary(job.getLuongToiDa());
        holder.tvSalary.setText(salaryRange);

        // Hiển thị Độ Hot
        holder.chipHot.setText(job.getDoHot());
        if ("Rất Cao".equals(job.getDoHot())) {
            holder.chipHot.setChipBackgroundColorResource(android.R.color.holo_red_light);
        } else if ("Cao".equals(job.getDoHot())) {
            holder.chipHot.setChipBackgroundColorResource(android.R.color.holo_orange_light);
        } else {
            holder.chipHot.setChipBackgroundColorResource(android.R.color.holo_blue_light);
        }
    }

    private String formatSalary(long salary) {
        if (salary >= 1000000) {
            return (salary / 1000000) + " Triệu";
        }
        return String.valueOf(salary);
    }

    @Override
    public int getItemCount() {
        return jobList != null ? jobList.size() : 0;
    }

    public static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView tvJobTitle, tvSalary, tvEducation;
        Chip chipHot;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvSalary = itemView.findViewById(R.id.tvSalary);
            tvEducation = itemView.findViewById(R.id.tvEducation);
            chipHot = itemView.findViewById(R.id.chipHot);
        }
    }
}