package com.example.servermonitor.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.R;
import com.example.servermonitor.adapter.ShellScriptAdapter;
import com.example.servermonitor.databinding.FragmentShellScriptsBinding;
import com.example.servermonitor.model.ShellScriptModel;
import com.example.servermonitor.service.ShellScriptService;

import java.util.ArrayList;

public class ShellScriptsFragment extends Fragment {
    private FragmentShellScriptsBinding binding;
    private ShellScriptAdapter adapter;
    private MainActivity activity;
    private Context context;
    private ShellScriptService shellScriptService;
    private ArrayList<ShellScriptModel> shellScripts;

    public ShellScriptsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentShellScriptsBinding.inflate(inflater, container, false);
        activity = (MainActivity) getActivity();
        context = activity.getApplicationContext();
        shellScriptService = new ShellScriptService(MainActivity.database);
        setupUiComponents();
        setupOnClickListeners();
        new Thread(() -> {
            shellScripts = shellScriptService.getAllShellScripts();
            adapter = new ShellScriptAdapter(context, shellScripts, activity);
            activity.runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
                binding.rvShellScripts.setAdapter(adapter);
            });
        }).start();
        Bundle args = getArguments();
        if (args != null) {
            if (args.getInt("success") == 1) {
                ShellScriptModel shellScriptModel = args.getParcelable("shellScriptModel");
                addNewShellScript(shellScriptModel);
            }
            getArguments().clear();
        }
        return binding.getRoot();
    }
    public void addNewShellScript(ShellScriptModel shellScriptModel) {
        new Thread(() -> {
            shellScriptService.addShellScript(shellScriptModel);
            shellScripts.add(shellScriptModel);
            activity.runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        registerForContextMenu(binding.rvShellScripts);
    }

    public void setupUiComponents() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        binding.rvShellScripts.setLayoutManager(layoutManager);
        binding.rvShellScripts.setItemAnimator(new DefaultItemAnimator());
    }

    private void setupOnClickListeners() {
        binding.fabAddShellScript.setOnClickListener((v) -> {
            NavController controller = Navigation.findNavController(binding.getRoot());
            controller.navigate(R.id.editShellScriptFragment);
        });
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int pposition = ((ShellScriptAdapter)binding.rvShellScripts.getAdapter()).selectedItemPosition;

        switch (item.getTitle().toString()) {
            case "Edit":
                NavController controller = Navigation.findNavController(binding.getRoot());
                Bundle bundle = new Bundle();
                bundle.putInt("edit", 1);
                bundle.putParcelable("shellScriptModel", shellScripts.get(pposition));
                controller.navigate(R.id.action_shellScriptsFragment_to_editShellScriptFragment, bundle);
                break;
            case "Delete":
                new Thread(() -> {
                    shellScriptService.deleteShellScript(shellScripts.get(pposition));
                    shellScripts.remove(pposition);
                    activity.runOnUiThread(() -> adapter.notifyItemRemoved(pposition));
                }).start();
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = activity.getMenuInflater();
        inflater.inflate(R.menu.server_context_menu, menu);
    }
}
