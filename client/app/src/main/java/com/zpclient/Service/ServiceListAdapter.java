package com.zpclient.Service;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zpclient.R;
import com.zpclient.ride.RideListData;

import java.util.List;

public class ServiceListAdapter extends RecyclerView.Adapter<ServiceListAdapter.ViewHolder> {
    private final List<RideListData> ride_list_data;
    private final Context context;

    public ServiceListAdapter(List<RideListData> ride_list_data, Context context) {
        this.ride_list_data = ride_list_data;
        this.context = context;
    }

    @NonNull
    @Override
    public ServiceListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.delivery_data_list, parent, false);

        return new ServiceListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ServiceListAdapter.ViewHolder holder, int position) {

        RideListData listData = ride_list_data.get(position);
        holder.rideID.setText(listData.getRideID());
        holder.rideTxt.setText(R.string.date);
        holder.rideTxt2.setText(R.string.vehicle);
        holder.rideStatus.setText(listData.getRideStatus());
        holder.rideDate.setText(listData.getRideDate());
        holder.srcTxt.setText(listData.getRideSrc());
        holder.dstTxt.setText(listData.getRideDst());
        //holder.rideVType.setText(listData.getRideVtype());
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
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String valueTid = listData.getRideID();
                Intent details = new Intent(context, ActivityServiceSummery.class);
                details.putExtra("TID", valueTid);
                context.startActivity(details);
            }
        });
    }


    @Override
    public int getItemCount() {
        return ride_list_data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView rideID;
        private final TextView rideStatus;
        private final TextView rideDate;
        private final TextView rideVType;
        private final TextView rideTxt;
        private final TextView rideTxt2;
        private final TextView srcTxt;
        private final TextView dstTxt;
        private final LinearLayout llSrc;
        private final LinearLayout llDst;
        private final LinearLayout layout;

        public ViewHolder(View itemView) {
            super(itemView);
            llSrc = itemView.findViewById(R.id.ll_src);
            layout = itemView.findViewById(R.id.layout);
            llDst = itemView.findViewById(R.id.ll_dst);
            llSrc.setVisibility(View.VISIBLE);
            llDst.setVisibility(View.VISIBLE);
            rideTxt2 = itemView.findViewById(R.id.delText2);
            rideTxt = itemView.findViewById(R.id.delText);
            rideID = itemView.findViewById(R.id.delivery_id);
            rideStatus = itemView.findViewById(R.id.delivery_status);
            rideDate = itemView.findViewById(R.id.delivery_itype);
            rideVType = itemView.findViewById(R.id.delivery_price);
            srcTxt = itemView.findViewById(R.id.src_point);
            dstTxt = itemView.findViewById(R.id.dst_point);

        }
    }
}
