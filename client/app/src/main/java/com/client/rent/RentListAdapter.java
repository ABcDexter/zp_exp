package com.client.rent;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.client.R;
import com.client.ride.RideListData;

import java.util.List;

public class RentListAdapter extends RecyclerView.Adapter<RentListAdapter.ViewHolder> {
    private List<RideListData> rent_list_data;
    private Context context;

    public RentListAdapter(List<RideListData> rent_list_data, Context context) {
        this.rent_list_data = rent_list_data;
        this.context = context;
    }

    @NonNull
    @Override
    public RentListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.delivery_data_list, parent, false);

        return new RentListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RentListAdapter.ViewHolder holder, int position) {

        RideListData listData = rent_list_data.get(position);
        holder.rentID.setText(listData.getRideID());
        holder.rentTxt.setText(R.string.date);
        holder.rentTxt2.setText(R.string.vehicle);
        holder.rentStatus.setText(listData.getRideStatus());
        holder.rentDate.setText(listData.getRideDate());
        //holder.rentVType.setText(listData.getRideVtype());
        String vtype = listData.getRideVtype();

        switch (vtype) {
            case "0":
                holder.rentVType.setText(R.string.e_cycle);
                break;
            case "1":
                holder.rentVType.setText(R.string.e_scooty);
                break;
            case "2":
                holder.rentVType.setText(R.string.e_bike);
                break;
            case "3":
                holder.rentVType.setText(R.string.zbee);
                break;
            default:
                holder.rentVType.setText(vtype);
                break;
        }
        holder.rentDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String valueTid = listData.getRideID();
                Intent details = new Intent(context, ActivityRentSummery.class);
                details.putExtra("TID", valueTid);
                context.startActivity(details);
            }
        });
    }


    @Override
    public int getItemCount() {
        return rent_list_data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView rentID, rentStatus,  rentVType, rentDate, rentTxt,rentTxt2;

        public ViewHolder(View itemView) {
            super(itemView);
            rentTxt2 = itemView.findViewById(R.id.delText2);
            rentTxt = itemView.findViewById(R.id.delText);
            rentID = itemView.findViewById(R.id.delivery_id);
            rentStatus = itemView.findViewById(R.id.delivery_status);
            rentDate = itemView.findViewById(R.id.delivery_itype);
            rentVType = itemView.findViewById(R.id.delivery_price);

        }
    }
}
