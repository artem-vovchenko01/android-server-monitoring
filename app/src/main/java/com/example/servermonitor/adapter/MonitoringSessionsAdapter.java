package com.example.servermonitor.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.R;
import com.example.servermonitor.model.MonitoringSessionModel;
import com.example.servermonitor.model.SshKeyModel;

import java.util.ArrayList;

public class MonitoringSessionsAdapter extends RecyclerView.Adapter<MonitoringSessionsAdapter.MonitoringSessionViewHolder> {
    private ArrayList<MonitoringSessionModel> monitoringSessions;
    private Context context;
    private MainActivity activity;
    public int selectedItemPosition;

    public MonitoringSessionsAdapter(Context context, ArrayList<MonitoringSessionModel> monitoringSessions, MainActivity activity) {
        this.context = context;
        this.activity = activity;
        this.monitoringSessions = monitoringSessions;
    }

    @NonNull
    @Override
    public MonitoringSessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.monitoring_session_item_layout, parent, false);
        return new MonitoringSessionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MonitoringSessionViewHolder holder, int position) {
        MonitoringSessionModel monitoringSession = monitoringSessions.get(position);
        holder.tvMonitoringSessionName.setText(monitoringSession.getName());
        holder.itemView.setOnLongClickListener((v) -> {
            selectedItemPosition = position;
            return false;
        });
        holder.itemView.setOnClickListener(v -> {
            NavController controller = Navigation.findNavController(holder.itemView);
            Bundle args = new Bundle();
            args.putParcelable("monitoringSessionModel", monitoringSession);
            controller.navigate(R.id.action_monitoringSessionsFragment_to_monitoringSessionFragment, args);
        });
    }

    @Override
    public int getItemCount() {
        return monitoringSessions.size();
    }

    public static class MonitoringSessionViewHolder extends RecyclerView.ViewHolder {
        public TextView tvMonitoringSessionName;
        public MonitoringSessionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMonitoringSessionName = itemView.findViewById(R.id.tvMonitoringSessionName);
        }
    }
}
