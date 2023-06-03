package com.example.servermonitor.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.R;
import com.example.servermonitor.fragment.LocalFilesFragment;

import java.io.File;

public class LocalFilesAdapter extends RecyclerView.Adapter<LocalFilesAdapter.LocalFilesViewHolder> {
    private File[] files;
    private Context context;
    private MainActivity activity;
    public int selectedItemPosition;
    private LocalFilesFragment fragment;

    public LocalFilesAdapter(Context context, File[] files, File currentDirectory, MainActivity activity, LocalFilesFragment fragment) {
        this.context = context;
        this.activity = activity;
        setFiles(files, currentDirectory);
        this.fragment = fragment;
    }
    public void setFiles(File[] files, File currentDirectory) {
        this.files = new File[files.length + 2];
        this.files[1] = currentDirectory;
        this.files[0] = this.files[1].getParentFile();
        System.arraycopy(files, 0, this.files, 2, files.length);
    }

    @NonNull
    @Override
    public LocalFilesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.file_item_layout, parent, false);
        return new LocalFilesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull LocalFilesViewHolder holder, int position) {
        File file = files[position];
        holder.tvFileName.setText(file.getName());
        if (position == 0) holder.tvFileName.setText("..");
        if (position == 1) holder.tvFileName.setText(".");
        holder.itemView.setOnLongClickListener((v) -> {
            selectedItemPosition = position;
            return false;
        });
        holder.itemView.setOnClickListener(v -> {
            if (position == 0 && fragment.currentPath.equals(fragment.homePath)) {
                fragment.goToPath(files[1]);
                return;
            }
            if (file.isDirectory())
                fragment.goToPath(file);
        });
        if (file.isDirectory()) {
            holder.fileIcon.setImageResource(R.drawable.baseline_folder_24);
        } else {
            holder.fileIcon.setImageResource(R.drawable.baseline_insert_drive_file_24);
        }
    }

    @Override
    public int getItemCount() {
        return files.length;
    }

    public static class LocalFilesViewHolder extends RecyclerView.ViewHolder {
        public TextView tvFileName;
        public ImageView fileIcon;
        public LocalFilesViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            fileIcon = itemView.findViewById(R.id.fileIcon);
        }
    }
}
