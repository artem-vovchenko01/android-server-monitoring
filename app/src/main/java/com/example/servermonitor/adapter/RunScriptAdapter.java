package com.example.servermonitor.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.R;
import com.example.servermonitor.model.ServerModel;

import java.util.ArrayList;
import java.util.HashMap;

public class RunScriptAdapter extends RecyclerView.Adapter<RunScriptAdapter.ServerViewHolder>  {
    private ArrayList<ServerModel> servers;
    public HashMap<Integer, ServerModel> chosenServers;
    private Context context;
    private MainActivity mainActivity;

    public RunScriptAdapter(Context context, ArrayList<ServerModel> servers, MainActivity mainActivity) {
        this.context = context;
        this.mainActivity = mainActivity;
        this.servers = servers;
        chosenServers = new HashMap<>();
    }

    @NonNull
    @Override
    public ServerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.run_script_server_item_layout, parent, false);
        return new ServerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ServerViewHolder holder, int position) {
        ServerModel serverModel = servers.get(position);
        holder.tvServerName.setText(serverModel.getName());
        holder.cbServerChosen.setOnClickListener(v -> {
            boolean isSelected = holder.cbServerChosen.isSelected();
            holder.cbServerChosen.setSelected(! isSelected);
            if (holder.cbServerChosen.isSelected()) {
                chosenServers.put(position, servers.get(position));
            } else {
                chosenServers.remove(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return servers.size();
    }

    public static class ServerViewHolder extends RecyclerView.ViewHolder {
        public Button btnShowOutput;
        public CheckBox cbServerChosen;
        public TextView tvServerName;
        public ServerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServerName = itemView.findViewById(R.id.tvServerName);
            cbServerChosen = itemView.findViewById(R.id.cbServerChosen);
            btnShowOutput = itemView.findViewById(R.id.btnShowOutput);
        }
    }
}
