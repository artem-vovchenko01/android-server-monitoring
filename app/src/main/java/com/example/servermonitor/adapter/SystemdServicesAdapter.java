package com.example.servermonitor.adapter;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.R;
import com.example.servermonitor.dialog.ShowScriptOutputDialog;
import com.example.servermonitor.dialog.ShowSystemdServiceLogDialog;
import com.example.servermonitor.fragment.SystemdServicesFragment;
import com.example.servermonitor.model.SshKeyModel;
import com.example.servermonitor.model.SystemdServiceModel;

import java.util.ArrayList;

public class SystemdServicesAdapter extends RecyclerView.Adapter<SystemdServicesAdapter.SystemdServicesViewHolder> {
    private ArrayList<SystemdServiceModel> systemdServices;
    private Context context;
    private MainActivity activity;
    private SystemdServicesFragment fragment;
    public int selectedItemPosition;

    public SystemdServicesAdapter(Context context, ArrayList<SystemdServiceModel> systemdServices, MainActivity activity, SystemdServicesFragment fragment) {
        this.context = context;
        this.activity = activity;
        this.systemdServices = systemdServices;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public SystemdServicesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.systemd_service_item_layout, parent, false);
        return new SystemdServicesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SystemdServicesViewHolder holder, int position) {
        SystemdServiceModel systemdService = systemdServices.get(position);
        holder.tvServiceName.setText(systemdService.serviceName);
        holder.tvServiceLongName.setText(systemdService.serviceLongName);
        switch (systemdService.serviceStatus) {
            case RUNNING:
                holder.imvServiceStatus.setImageResource(R.drawable.greencircle);
                break;
            case EXITED:
                holder.imvServiceStatus.setImageResource(R.drawable.redcircle);
                break;
            case OTHER:
                holder.imvServiceStatus.setImageResource(R.drawable.redcircle);
                break;
        }
        holder.itemView.setOnLongClickListener((v) -> {
            selectedItemPosition = position;
            return false;
        });
        holder.itemView.setOnClickListener(v -> {
            ShowSystemdServiceLogDialog dialog = new ShowSystemdServiceLogDialog(fragment.sshSessionWorker, systemdService);
            dialog.show(fragment.getChildFragmentManager(), systemdService.serviceName + " log");
        });
    }

    @Override
    public int getItemCount() {
        return systemdServices.size();
    }

    public static class SystemdServicesViewHolder extends RecyclerView.ViewHolder {
        public TextView tvServiceName;
        public TextView tvServiceLongName;
        public ImageView imvServiceStatus;
        public SystemdServicesViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvServiceLongName = itemView.findViewById(R.id.tvServiceLongName);
            imvServiceStatus = itemView.findViewById(R.id.imvServiceStatus);
        }
    }
}
