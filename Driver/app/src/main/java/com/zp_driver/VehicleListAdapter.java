package com.zp_driver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VehicleListAdapter extends RecyclerView.Adapter<VehicleListAdapter.ViewHolder> {
    private List<VehicleListData> vehicle_list_data;
    private Context context;


    public VehicleListAdapter(List<VehicleListData> vehicle_list_data, Context context) {
        this.vehicle_list_data = vehicle_list_data;
        this.context = context;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vehicle_data_list, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VehicleListAdapter.ViewHolder holder, int position) {

        VehicleListData listData = vehicle_list_data.get(position);
        holder.vehicleRegn.setText(listData.getRegnVehicle());
        holder.vehicleType.setText(listData.getTypeVehicle());
        holder.vehicleAn.setText(listData.getAnVehicle());
        String strAn = listData.getAnVehicle();

        holder.vehicleRegn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*SharedPreferences directoryLocation = v.getContext().getSharedPreferences(VEHICLE_DETAILS, Context.MODE_PRIVATE);
                directoryLocation.edit().putString(VEHICLE_ID, strID).apply();*//*
                    Intent intent = new Intent(context, ActivityHome.class);
                    intent.putExtra("VID", strRegn);
                    context.startActivity(intent);

                    Log.d("VehicleListAdapter", "Vehicle ID " + strRegn );
                    ((Activity) context).finish();*/
                Intent intent = new Intent(context, VehicleList.class);
                intent.putExtra("VAN", strAn);
                context.startActivity(intent);

                Log.d("VehicleListAdapter", "Vehicle AN " + strAn);
                ((Activity) context).finish();
            }

        });
    }

    @Override
    public int getItemCount() {
        return vehicle_list_data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView vehicleRegn, vehicleType, vehicleAn;

        public ViewHolder(View itemView) {
            super(itemView);
            vehicleRegn = itemView.findViewById(R.id.vRegn);
            vehicleType = itemView.findViewById(R.id.vType);
            vehicleAn = itemView.findViewById(R.id.vAn);

        }
    }
}
