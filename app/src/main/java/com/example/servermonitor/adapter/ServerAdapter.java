package com.example.servermonitor.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.R;
import com.example.servermonitor.activity.ServerActivity;
import com.example.servermonitor.model.ServerModel;

import java.util.ArrayList;

public class ServerAdapter extends RecyclerView.Adapter<ServerAdapter.ServerViewHolder> {
    private ArrayList<ServerModel> servers;
    private Context context;
    private MainActivity mainActivity;

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
        holder.tvMemoryUsed.setText(String.valueOf(serverModel.getMemoryUsedMb()));
        holder.tvMemoryTotal.setText(String.valueOf(serverModel.getMemoryTotalMb()));
        holder.tvDiskUsed.setText(Double.toString(serverModel.getDiskUsedMb()));
        holder.tvDiskTotal.setText(Double.toString(serverModel.getDiskTotalMb()));
        holder.imvServerStatus.setImageResource(serverModel.getServerStatusImg());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serverActivity = new Intent(
                            mainActivity.getApplicationContext(),
                            ServerActivity.class
                        );
                ServerActivity.serverModel = serverModel;
                mainActivity.startActivity(serverActivity);
            }
        });
    }

    @Override
    public int getItemCount() {
        return servers.size();
    }

    public static class ServerViewHolder extends RecyclerView.ViewHolder {
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
        }
    }
}
