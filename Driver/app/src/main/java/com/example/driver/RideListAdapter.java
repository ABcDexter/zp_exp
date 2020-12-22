package com.example.driver;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RideListAdapter extends RecyclerView.Adapter<RideListAdapter.ViewHolder> {
    private List<RideListData> ride_list_data;
    private Context context;

    public RideListAdapter(List<RideListData> ride_list_data, Context context) {
        this.ride_list_data = ride_list_data;
        this.context = context;
    }

    @NonNull
    @Override
    public RideListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.delivery_data_list, parent, false);

        return new RideListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RideListAdapter.ViewHolder holder, int position) {

        RideListData listData = ride_list_data.get(position);
        holder.rideID.setText(listData.getRideID());
        holder.rideTxt.setText(R.string.date);
        holder.rideTxt2.setText(R.string.vehicle);
        holder.rideStatus.setText(listData.getRideStatus());
        holder.rideDate.setText(listData.getRideDate());
        String vtype = listData.getRideVtype();

        switch (vtype) {
            case "0":
                holder.rideVType.setText(R.string.e_cycle);
                break;
            case "1":
                holder.rideVType.setText(R.string.e_scooty);
                break;
            case "2":
                holder.rideVType.setText(R.string.e_bike);
                break;
            case "3":
                holder.rideVType.setText(R.string.zbee);
                break;
            default:
                holder.rideVType.setText(vtype);
                break;
        }
        holder.rideDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String valueTid = listData.getRideID();
                Intent details = new Intent(context, ActivityRideSummery.class);
                details.putExtra("TID",valueTid);
                context.startActivity(details);
            }
        });
    }


    @Override
    public int getItemCount() {
        return ride_list_data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView rideID, rideStatus, rideDate, rideVType, rideTxt, rideTxt2;

        public ViewHolder(View itemView) {
            super(itemView);
            rideTxt2 = itemView.findViewById(R.id.delText2);
            rideTxt = itemView.findViewById(R.id.delText);
            rideID = itemView.findViewById(R.id.delivery_id);
            rideStatus = itemView.findViewById(R.id.delivery_status);
            rideDate = itemView.findViewById(R.id.delivery_itype);
            rideVType = itemView.findViewById(R.id.delivery_price);

        }
    }
}
