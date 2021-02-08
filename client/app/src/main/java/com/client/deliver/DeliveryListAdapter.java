package com.client.deliver;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.client.R;

import java.util.List;

public class DeliveryListAdapter extends RecyclerView.Adapter<DeliveryListAdapter.ViewHolder> {
    private List<DeliveryListData> delivery_list_data;
    private Context context;
    private int intentValue;

    public DeliveryListAdapter(List<DeliveryListData> delivery_list_data, Context context) {
        this.delivery_list_data = delivery_list_data;
        this.context = context;
    }

    @NonNull
    @Override
    public DeliveryListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.delivery_data_list, parent, false);

        return new DeliveryListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DeliveryListAdapter.ViewHolder holder, int position) {

        DeliveryListData listData = delivery_list_data.get(position);
        holder.txtID.setText(listData.getDeliveryID());
        holder.txtStatus.setText(listData.getDeliveryStatus());
        holder.txtPrice.setText(listData.getDeliveryPrice());
        holder.txtIType.setText(listData.getDeliveryIType());
        holder.delText2.setText(R.string.price);

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String valueScid = listData.getDeliveryID();
                Log.d("DeliveryListAdapter", "itype" + valueScid);
                Intent details = new Intent(context, ActivityDeliverySummery.class);
                details.putExtra("SCID", valueScid);
                context.startActivity(details);
                //((Activity) context).finish();
            }
        });
    }


    @Override
    public int getItemCount() {
        return delivery_list_data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final private TextView txtID, txtStatus, txtPrice, txtIType, delText2;
        final private LinearLayout layout;

        public ViewHolder(View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout);
            delText2 = itemView.findViewById(R.id.delText2);
            txtID = itemView.findViewById(R.id.delivery_id);
            txtStatus = itemView.findViewById(R.id.delivery_status);
            txtPrice = itemView.findViewById(R.id.delivery_price);
            txtIType = itemView.findViewById(R.id.delivery_itype);

        }
    }
}
