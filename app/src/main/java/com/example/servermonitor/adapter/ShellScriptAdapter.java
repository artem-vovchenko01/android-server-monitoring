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
import com.example.servermonitor.model.ShellScriptModel;

import java.util.ArrayList;

public class ShellScriptAdapter extends RecyclerView.Adapter<ShellScriptAdapter.ShellScriptViewHolder> {
    private ArrayList<ShellScriptModel> shellScripts;
    private Context context;
    private MainActivity activity;
    public int selectedItemPosition;

    public ShellScriptAdapter(Context context, ArrayList<ShellScriptModel> shellScripts, MainActivity activity) {
        this.context = context;
        this.activity = activity;
        this.shellScripts = shellScripts;
    }

    @NonNull
    @Override
    public ShellScriptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.shell_script_item_layout, parent, false);
        return new ShellScriptViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ShellScriptViewHolder holder, int position) {
        ShellScriptModel sshKey = shellScripts.get(position);
        holder.tvShellScriptName.setText(sshKey.getName());
        holder.itemView.setOnLongClickListener((v) -> {
            selectedItemPosition = position;
            return false;
        });
        activity.registerForContextMenu(holder.itemView);
    }

    @Override
    public int getItemCount() {
        return shellScripts.size();
    }

    public static class ShellScriptViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        public TextView tvShellScriptName;
        public ShellScriptViewHolder(@NonNull View itemView) {
            super(itemView);
            tvShellScriptName = itemView.findViewById(R.id.tvShellScriptName);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add("Edit");
            menu.add("Delete");
        }
    }
}
