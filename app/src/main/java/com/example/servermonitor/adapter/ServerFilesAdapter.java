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
import com.example.servermonitor.fragment.BrowseServerFilesFragment;
import com.example.servermonitor.model.SshKeyModel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;

import java.util.ArrayList;
import java.util.Vector;

public class ServerFilesAdapter extends RecyclerView.Adapter<ServerFilesAdapter.ServerFilesViewHolder> {
    public Vector<ChannelSftp.LsEntry> lsEntries;
    private Context context;
    private MainActivity activity;
    public int selectedItemPosition;
    private BrowseServerFilesFragment fragment;

    public ServerFilesAdapter(Context context, Vector<ChannelSftp.LsEntry> lsEntries, MainActivity activity, BrowseServerFilesFragment fragment) {
        this.context = context;
        this.activity = activity;
        this.lsEntries = lsEntries;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public ServerFilesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.file_item_layout, parent, false);
        return new ServerFilesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ServerFilesViewHolder holder, int position) {
        ChannelSftp.LsEntry entry = lsEntries.get(position);
        holder.tvFileName.setText(entry.getFilename());
        holder.itemView.setOnLongClickListener((v) -> {
            selectedItemPosition = position;
            return false;
        });
        holder.itemView.setOnClickListener(v -> {
            if (entry.getAttrs().isDir())
                fragment.goToPath(entry.getFilename());
        });
        if (entry.getAttrs().isDir()) {
           holder.fileIcon.setImageResource(R.drawable.baseline_folder_24);
        } else {
            holder.fileIcon.setImageResource(R.drawable.baseline_insert_drive_file_24);
        }
        activity.registerForContextMenu(holder.itemView);
    }

    @Override
    public int getItemCount() {
        return lsEntries.size();
    }

    public static class ServerFilesViewHolder extends RecyclerView.ViewHolder {
        public TextView tvFileName;
        public ImageView fileIcon;
        public ServerFilesViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            fileIcon = itemView.findViewById(R.id.fileIcon);
        }
    }
}
