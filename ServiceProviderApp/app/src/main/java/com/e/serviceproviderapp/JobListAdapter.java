package com.e.serviceproviderapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class JobListAdapter extends RecyclerView.Adapter<JobListAdapter.ViewHolder> {
    private List<JobListData> ride_list_data;
    private Context context;
    private final int intentValue;

    public JobListAdapter(List<JobListData> ride_list_data, Context context, int i) {
        this.ride_list_data = ride_list_data;
        this.context = context;
        this.intentValue = i;
    }

    @NonNull
    @Override
    public JobListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.job_data_list, parent, false);

        return new JobListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(JobListAdapter.ViewHolder holder, int position) {

        JobListData listData = ride_list_data.get(position);
        holder.jobID.setText(listData.getJobID());
        holder.jobTime.setText(listData.getJobTime());
        holder.jobDate.setText(listData.getJobDate());
        holder.jobType.setText(listData.getJobType());
        holder.jobEarn.setText(listData.getJobEarn());

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (intentValue == 1) {
                    String valueTid = listData.getJobID();
                    Intent details = new Intent(context, ActivityNewJobs.class);
                    details.putExtra("BID", valueTid);
                    context.startActivity(details);
                    ((Activity) context).finish();
                }
                if (intentValue == 2) {
                    String valueTid = listData.getJobID();
                    Intent details = new Intent(context, ActivityOTP.class);
                    details.putExtra("BID", valueTid);
                    details.putExtra("CALL", "2");
                    context.startActivity(details);
                    ((Activity) context).finish();
                }if (intentValue == 3) {
                    String valueTid = listData.getJobID();
                    Intent details = new Intent(context, ActivityOTP.class);
                    details.putExtra("BID", valueTid);
                    details.putExtra("CALL", "3");
                    context.startActivity(details);
                    ((Activity) context).finish();
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return ride_list_data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final private TextView jobID, jobTime, jobDate, jobType, jobEarn;
        final private LinearLayout layout;

        public ViewHolder(View itemView) {
            super(itemView);
            jobEarn = itemView.findViewById(R.id.txt_jobEarn);
            jobID = itemView.findViewById(R.id.job_id);
            jobTime = itemView.findViewById(R.id.txt_jobTime);
            jobDate = itemView.findViewById(R.id.txt_jobDate);
            jobType = itemView.findViewById(R.id.txt_jobType);
            layout = itemView.findViewById(R.id.layout);

        }
    }
}
