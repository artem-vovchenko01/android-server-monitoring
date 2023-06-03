package com.example.servermonitor.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.R;
import com.example.servermonitor.model.AlertModel;

import java.util.ArrayList;

public class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.AlertViewHolder> {
    private ArrayList<AlertModel> alerts;
    private MainActivity activity;
    private Context context;
    public int selectedItemPosition;
    public AlertsAdapter(MainActivity activity, ArrayList<AlertModel> alerts) {
        this.alerts = alerts;
        this.activity = activity;
        this.context = activity.getApplicationContext();
    }
    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.alert_item_layout, parent, false);
        return new AlertViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        AlertModel alert = alerts.get(position);
        holder.itemView.setOnLongClickListener(v -> {
            selectedItemPosition = position;
            return false;
        });
        holder.itemView.setOnClickListener(v -> {
            NavController controller = Navigation.findNavController(holder.itemView);
            Bundle args = new Bundle();
            args.putParcelable("alertModel", alert);
            controller.navigate(R.id.action_alertsFragment_to_editAlertFragment, args);
        });
        holder.tvAlertName.setText(alert.getName());
        int imResourceId = 0;
        switch (alert.getAlertType()) {
            case TYPE_CPU:
                imResourceId = R.drawable.cpu;
                break;
            case TYPE_MEMORY:
                imResourceId = R.drawable.cpu;
                break;
            case TYPE_IO_READ:
                imResourceId = R.drawable.cpu;
                break;
            case TYPE_IO_WRITE:
                imResourceId = R.drawable.cpu;
                break;
            case TYPE_NETWORK_DL:
                imResourceId = R.drawable.cpu;
                break;
            case TYPE_NETWORK_UL:
                imResourceId = R.drawable.cpu;
                break;
            case TYPE_STORAGE:
                imResourceId = R.drawable.cpu;
                break;
        }
        holder.imvAlertIcon.setImageResource(imResourceId);
    }

    @Override
    public int getItemCount() {
        return alerts.size();
    }

    public class AlertViewHolder extends RecyclerView.ViewHolder {
        private TextView tvAlertName;
        private ImageView imvAlertIcon;
        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAlertName = itemView.findViewById(R.id.tvAlertName);
            imvAlertIcon = itemView.findViewById(R.id.imvAlertIcon);
        }
    }
}
