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
import com.example.servermonitor.helper.UiHelper;
import com.example.servermonitor.service.SshSessionWorker;
import com.example.servermonitor.adapter.ServerAdapter;
import com.example.servermonitor.databinding.FragmentServersBinding;
import com.example.servermonitor.model.ServerModel;
import com.example.servermonitor.service.ServerService;

public class ServersFragment extends Fragment {
    private FragmentServersBinding binding;
    private MainActivity activity;
    private Context context;
    private ServerService serverService;
    private ServerAdapter serverAdapter;

    public ServersFragment() {
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
        binding = FragmentServersBinding.inflate(inflater, container, false);
        activity = (MainActivity) getActivity();
        activity.getSupportActionBar().setTitle("Servers");
        context = activity.getApplicationContext();
        serverService = new ServerService(MainActivity.database);
        Bundle args = getArguments();
        if (args != null) {
            if (args.getInt("success") == 1) {
                ServerModel newServerModel = args.getParcelable("serverModel");
                activity.addNewServer(newServerModel);
            }
            getArguments().clear();
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupUiComponents();
        setupOnClickListeners();
        registerForContextMenu(binding.rvServers);
        new Thread(() -> {
            while (activity.serverModels == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            serverAdapter = new ServerAdapter(context, activity.serverModels, activity);
            activity.serverAdapter = serverAdapter;
            activity.runOnUiThread(() -> {
                binding.rvServers.setAdapter(serverAdapter);
            });
        }).start();
    }

    public void setupUiComponents() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        binding.rvServers.setLayoutManager(layoutManager);
        binding.rvServers.setItemAnimator(new DefaultItemAnimator());
    }

    public void setupOnClickListeners() {
        binding.fabAddServer.setOnClickListener(v -> {
            NavController controller = Navigation.findNavController(binding.getRoot());
            controller.navigate(R.id.action_serversFragment_to_editServerFragment);
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (activity != null)
            activity.serverAdapter = null;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int pposition = serverAdapter.selectedItemPosition;
        ServerModel server = activity.serverModels.get(pposition);

        switch (item.getTitle().toString()) {
            case "Edit":
                NavController controller = Navigation.findNavController(binding.getRoot());
                Bundle bundle = new Bundle();
                bundle.putInt("edit", 1);
                bundle.putParcelable("serverModel", server);
                controller.navigate(R.id.action_serversFragment_to_editServerFragment, bundle);
                break;
            case "Delete":
                new Thread(() -> {
                    serverService.deleteServer(server);
                    activity.serverModels.remove(pposition);
                    activity.stopJobForServer(server);
                    activity.runOnUiThread(() -> serverAdapter.notifyDataSetChanged());
                }).start();
                break;
            case "Reboot":
                new Thread(() -> {
                    SshSessionWorker worker = activity.serverSessions.get(server);
                    try {
                        worker.executeSingleCommand("sudo reboot");
                    } catch (Exception e) {
                        UiHelper.displayError(activity, "Reboot failed");
                    }
                }).start();
                break;
            case "Shutdown":
                new Thread(() -> {
                    SshSessionWorker worker = activity.serverSessions.get(server);
                    try {
                        worker.executeSingleCommand("sudo shutdown now");
                    } catch (Exception e) {
                        UiHelper.displayError(activity, "Shutdown failed");
                    }
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