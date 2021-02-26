package com.client;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.client.rent.ActivityRentHome;
import com.client.rent.ActivityUpdateInfo;
import com.client.ride.ActivityRideHome;

import java.util.List;

public class MyHubListAdapter extends RecyclerView.Adapter<MyHubListAdapter.ViewHolder> {
    private final List<HubListData> hub_list_data;
    private final Context context;
    private final int intentValue;
    public static final String PREFS_LOCATIONS = "com.client.ride.Locations";
    public static final String LOCATION_PICK = "PickLocation";
    public static final String LOCATION_DROP = "DropLocation";
    public static final String LOCATION_PICK_ID = "PickLocationID";
    public static final String LOCATION_DROP_ID = "DropLocationID";
    public static final String LOCATION_PICK_LAT = "PickLocationLat";
    public static final String LOCATION_PICK_LNG = "PickLocationLng";
    public static final String LOCATION_DROP_LAT = "DropLocationLat";
    public static final String LOCATION_DROP_LNG = "DropLocationLng";

    public MyHubListAdapter(List<HubListData> hub_list_data, Context context, int i) {
        this.hub_list_data = hub_list_data;
        this.context = context;
        this.intentValue = i;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hub_data_list, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyHubListAdapter.ViewHolder holder, int position) {

        HubListData listData = hub_list_data.get(position);
        holder.txtName.setText(listData.getHubName());
        holder.txtID.setText(listData.getIdPlace());
        holder.txtLat.setText(listData.getLatitude());
        holder.txtLng.setText(listData.getLongitude());
        String strValue = listData.getHubName();
        String strID = listData.getIdPlace();
        String strLat = listData.getLatitude();
        String strLng = listData.getLongitude();

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences directoryLocation = v.getContext().getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);

                if (intentValue == 1) {

                    directoryLocation.edit().putString(LOCATION_PICK, strValue).apply();
                    directoryLocation.edit().putString(LOCATION_PICK_ID, strID).apply();
                    directoryLocation.edit().putString(LOCATION_PICK_LAT, strLat).apply();
                    directoryLocation.edit().putString(LOCATION_PICK_LNG, strLng).apply();
                    Log.d("MyHubListAdapter", "Pick Location Saved" + directoryLocation);

                    Intent intent = new Intent(context, ActivityRideHome.class);
                    context.startActivity(intent);

                    Log.d("MyHubListAdapter", "Pick Location  " + strValue + "  Send from  MyHubListAdapter");
                    ((Activity) context).finish();
                }
                if (intentValue == 2) {
                    directoryLocation.edit().putString(LOCATION_DROP, strValue).apply();
                    directoryLocation.edit().putString(LOCATION_DROP_ID, strID).apply();
                    directoryLocation.edit().putString(LOCATION_DROP_LAT, strLat).apply();
                    directoryLocation.edit().putString(LOCATION_DROP_LNG, strLng).apply();
                    Log.d("MyHubListAdapter", "Drop Location Saved" + directoryLocation);

                    Intent intent = new Intent(context, ActivityRideHome.class);
                    context.startActivity(intent);
                    Log.d("MyHubListAdapter", "Drop Location " + strValue + " Send from  MyHubListAdapter");
                    ((Activity) context).finish();
                }
                if (intentValue == 3) {
                    directoryLocation.edit().putString(LOCATION_PICK, strValue).apply();
                    directoryLocation.edit().putString(LOCATION_PICK_ID, strID).apply();
                    directoryLocation.edit().putString(LOCATION_PICK_LAT, strLat).apply();
                    directoryLocation.edit().putString(LOCATION_PICK_LNG, strLng).apply();
                    Log.d("MyHubListAdapter", "Rental Pick Location Saved" + directoryLocation);

                    Intent intent = new Intent(context, ActivityRentHome.class);
                    context.startActivity(intent);

                    Log.d("MyHubListAdapter", "Rental Pick Location  " + strValue + "  Send from  MyHubListAdapter");
                    ((Activity) context).finish();
                }
                if (intentValue == 4) {
                    directoryLocation.edit().putString(LOCATION_DROP, strValue).apply();
                    directoryLocation.edit().putString(LOCATION_DROP_ID, strID).apply();
                    directoryLocation.edit().putString(LOCATION_DROP_LAT, strLat).apply();
                    directoryLocation.edit().putString(LOCATION_DROP_LNG, strLng).apply();
                    Log.d("MyHubListAdapter", "Rental Drop Location Saved" + directoryLocation);

                    Intent intent = new Intent(context, ActivityRentHome.class);
                    context.startActivity(intent);
                    Log.d("MyHubListAdapter", "Rental Drop Location " + strValue + " Send from  MyHubListAdapter");
                    ((Activity) context).finish();
                }
                if (intentValue == 5) {
                    directoryLocation.edit().putString(LOCATION_DROP, strValue).apply();
                    directoryLocation.edit().putString(LOCATION_DROP_ID, strID).apply();
                    directoryLocation.edit().putString(LOCATION_DROP_LAT, strLat).apply();
                    directoryLocation.edit().putString(LOCATION_DROP_LNG, strLng).apply();
                    Log.d("MyHubListAdapter", "Rental Drop Location Saved" + directoryLocation);

                    Intent intent = new Intent(context, ActivityUpdateInfo.class);
                    context.startActivity(intent);
                    Log.d("MyHubListAdapter", "Rental Drop Location " + strValue + " Send from  MyHubListAdapter");
                    ((Activity) context).finish();
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return hub_list_data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtName;
        private final TextView txtLat;
        private final TextView txtLng;
        private final TextView txtID;
        private final LinearLayout layout;

        public ViewHolder(View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout_hub);
            txtName = itemView.findViewById(R.id.text_name);
            txtID = itemView.findViewById(R.id.pidName);
            txtLat = itemView.findViewById(R.id.text_lat);
            txtLng = itemView.findViewById(R.id.text_lng);

        }
    }
}
