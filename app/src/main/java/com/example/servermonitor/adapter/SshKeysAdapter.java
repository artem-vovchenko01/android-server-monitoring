package com.example.servermonitor.adapter;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.R;
import com.example.servermonitor.model.SshKeyModel;

import java.util.ArrayList;

public class SshKeysAdapter extends RecyclerView.Adapter<SshKeysAdapter.SshKeysViewHolder> {
    private ArrayList<SshKeyModel> sshKeys;
    private Context context;
    private MainActivity activity;
    public int selectedItemPosition;

    public SshKeysAdapter(Context context, ArrayList<SshKeyModel> sshKeys, MainActivity activity) {
        this.context = context;
        this.activity = activity;
        this.sshKeys = sshKeys;
    }

    @NonNull
    @Override
    public SshKeysViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.ssh_key_item_layout, parent, false);
        return new SshKeysViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SshKeysViewHolder holder, int position) {
        SshKeyModel sshKey = sshKeys.get(position);
        holder.tvSshKeyName.setText(sshKey.getName());
        holder.itemView.setOnLongClickListener((v) -> {
            selectedItemPosition = position;
            return false;
        });
        activity.registerForContextMenu(holder.itemView);
    }

    @Override
    public int getItemCount() {
        return sshKeys.size();
    }

    public static class SshKeysViewHolder extends RecyclerView.ViewHolder {
        public TextView tvSshKeyName;
        public SshKeysViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSshKeyName = itemView.findViewById(R.id.tvSshKeyName);
        }
    }
}
