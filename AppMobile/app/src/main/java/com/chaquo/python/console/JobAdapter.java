package com.chaquo.python.console;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chaquo.python.model.CongViec;
import com.google.android.material.chip.Chip;

import java.util.List;

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
        String salaryRange = formatSalary(job.getLuongToiThieu()) + " - " + formatSalary(job.getLuongToiDa());
        holder.tvSalary.setText(salaryRange);
        holder.chipHot.setText(job.getDoHot());

        if (job.getHinhAnh() != null && !job.getHinhAnh().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(job.getHinhAnh())
                    .placeholder(R.drawable.background)
                    .error(R.drawable.background)
                    .into(holder.ivJobIcon);
        }

        View.OnClickListener listener = v -> {
            Intent intent = new Intent(v.getContext(), JobDetail.class);
            intent.putExtra("jobCode", job.getMaCongViec());
            v.getContext().startActivity(intent);
        };

        holder.itemView.setOnClickListener(listener);
        holder.tvSeeDetails.setOnClickListener(listener);
    }

    private String formatSalary(long salary) {
        if (salary >= 1000000) return (salary / 1000000) + " Tr";
        return String.valueOf(salary);
    }

    @Override
    public int getItemCount() {
        return jobList != null ? jobList.size() : 0;
    }

    public static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView tvJobTitle, tvSalary, tvEducation, tvSeeDetails;
        ImageView ivJobIcon;
        Chip chipHot;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvSalary = itemView.findViewById(R.id.tvSalary);
            tvEducation = itemView.findViewById(R.id.tvEducation);
            tvSeeDetails = itemView.findViewById(R.id.tvSeeDetails);
            ivJobIcon = itemView.findViewById(R.id.ivJobIcon);
            chipHot = itemView.findViewById(R.id.chipHot);
        }
    }
}
