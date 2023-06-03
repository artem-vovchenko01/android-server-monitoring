package com.example.servermonitor.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.R;
import com.example.servermonitor.fragment.ServerFragment;
import com.example.servermonitor.model.ServerModel;

import java.util.ArrayList;

public class ServerAdapter extends RecyclerView.Adapter<ServerAdapter.ServerViewHolder>  {
    private ArrayList<ServerModel> servers;
    private Context context;
    private MainActivity mainActivity;
    public int selectedItemPosition;

    public ServerAdapter(Context context, ArrayList<ServerModel> servers, MainActivity mainActivity) {
        this.context = context;
        this.mainActivity = mainActivity;
        this.servers = servers;
    }

    @NonNull
    @Override
    public ServerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.server_item_layout, parent, false);
        return new ServerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ServerViewHolder holder, int position) {
        ServerModel serverModel = servers.get(position);
        holder.tvServerName.setText(serverModel.getName());
        holder.tvMemoryUsed.setText(String.valueOf(serverModel.getMemoryUsedMb()) + "MB");
        holder.tvMemoryTotal.setText(String.valueOf(serverModel.getMemoryTotalMb()) + "MB");
        holder.tvDiskUsed.setText((int)serverModel.getDiskUsedMb() + "MB");
        holder.tvDiskTotal.setText((int)serverModel.getDiskTotalMb() + "MB");
        holder.imvServerStatus.setImageResource(serverModel.getServerStatusImg());
        holder.tvCpuUsage.setText(serverModel.getCpuUsagePercent() + "%");
        holder.itemView.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(mainActivity, R.id.navHostFragment);
            Bundle fragmentData = new Bundle();
            fragmentData.putParcelable("serverModel", servers.get(position));
            navController.navigate(R.id.action_serversFragment_to_serverFragment, fragmentData);
        });
        holder.itemView.setOnLongClickListener(v -> {
            selectedItemPosition = position;
            return false;
        });
        mainActivity.registerForContextMenu(holder.itemView);
    }

    @Override
    public int getItemCount() {
        return servers.size();
    }

    public static class ServerViewHolder extends RecyclerView.ViewHolder {
        public Button btnEdit;
        public Button btnDelete;
        public TextView tvServerName;
        public TextView tvMemoryUsed;
        public TextView tvMemoryTotal;
        public TextView tvDiskUsed;
        public TextView tvDiskTotal;
        public TextView tvCpuUsage;
        public ImageView imvServerStatus;
        public ServerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServerName = itemView.findViewById(R.id.tvServerName);
            tvMemoryUsed = itemView.findViewById(R.id.tvMemoryUsed);
            tvMemoryTotal = itemView.findViewById(R.id.tvMemoryTotal);
            tvDiskUsed = itemView.findViewById(R.id.tvDiskUsed);
            tvDiskTotal = itemView.findViewById(R.id.tvDiskTotal);
            tvCpuUsage = itemView.findViewById(R.id.tvCpuUsage);
            imvServerStatus = itemView.findViewById(R.id.imvServerStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
