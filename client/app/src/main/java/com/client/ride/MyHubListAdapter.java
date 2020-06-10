package com.client.ride;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.client.ActivityRideHome;
import com.client.R;
import com.client.rental.RentalHome;

import java.util.List;

public class MyHubListAdapter extends RecyclerView.Adapter<MyHubListAdapter.ViewHolder> {
    private List<HubListData> hub_list_data;
    private Context context;
    private int intentValue;

    public static final String PREFS_LOCATIONS = "com.client.ride.Locations";
    public static final String LOCATION_PICK = "PickLocation";
    public static final String LOCATION_DROP = "DropLocation";
    public static final String LOCATION_PICK_ID = "PickLocationID";
    public static final String LOCATION_DROP_ID = "DropLocationID";


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
        String strValue = listData.getHubName();
        String strID = listData.getIdPlace();


        holder.txtName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences directoryLocation = v.getContext().getSharedPreferences(PREFS_LOCATIONS, Context.MODE_PRIVATE);

                if (intentValue == 1) {

                    directoryLocation.edit().putString(LOCATION_PICK, strValue).apply();
                    directoryLocation.edit().putString(LOCATION_PICK_ID, strID).apply();
                    Log.d("MyHubListAdapter", "Pick Location Saved" + directoryLocation);

                    Intent intent = new Intent(context, ActivityRideHome.class);
                    context.startActivity(intent);

                    Log.d("MyHubListAdapter", "Pick Location  " + strValue + "  Send from  MyHubListAdapter");
                    ((Activity) context).finish();
                }
                if (intentValue == 2) {
                    directoryLocation.edit().putString(LOCATION_DROP, strValue).apply();
                    directoryLocation.edit().putString(LOCATION_DROP_ID, strID).apply();
                    Log.d("MyHubListAdapter", "Drop Location Saved" + directoryLocation);

                    Intent intent = new Intent(context, ActivityRideHome.class);
                    context.startActivity(intent);
                    Log.d("MyHubListAdapter", "Drop Location " + strValue + " Send from  MyHubListAdapter");
                    ((Activity) context).finish();
                }
                if (intentValue == 3) {
                    directoryLocation.edit().putString(LOCATION_PICK, strValue).apply();
                    directoryLocation.edit().putString(LOCATION_PICK_ID, strID).apply();
                    Log.d("MyHubListAdapter", "Rental Pick Location Saved" + directoryLocation);

                    Intent intent = new Intent(context, RentalHome.class);
                    context.startActivity(intent);

                    Log.d("MyHubListAdapter", "Rental Pick Location  " + strValue + "  Send from  MyHubListAdapter");
                    ((Activity) context).finish();
                }
                if (intentValue == 4) {
                    directoryLocation.edit().putString(LOCATION_DROP, strValue).apply();
                    directoryLocation.edit().putString(LOCATION_DROP_ID, strID).apply();
                    Log.d("MyHubListAdapter", "Rental Drop Location Saved" + directoryLocation);

                    Intent intent = new Intent(context, RentalHome.class);
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
        private TextView txtName, txtLat, txtLng, txtID;

        public ViewHolder(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.text_name);
            txtID = itemView.findViewById(R.id.pidName);

        }
    }
}
