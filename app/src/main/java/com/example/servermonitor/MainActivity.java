package com.example.servermonitor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Delete;
import androidx.room.Room;
import androidx.work.Data;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.servermonitor.adapter.ServerAdapter;
import com.example.servermonitor.databinding.ActivityMainBinding;
import com.example.servermonitor.db.Converters;
import com.example.servermonitor.db.ServerDatabase;
import com.example.servermonitor.db.entity.MonitoringRecordEntity;
import com.example.servermonitor.db.entity.ServerEntity;
import com.example.servermonitor.mapper.ServerMapper;
import com.example.servermonitor.model.ServerModel;
import com.example.servermonitor.service.ServerService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.NavigableMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final String DIALOG_TAG = "CreateServerDialogFragment";
    private static final int MONITORING_INTERVAL = 7;
    private int selectedItemId;
    public static ServerDatabase database;
    public ServerService serverService;
    public ServerAdapter serverAdapter;
    public ArrayList<ServerModel> serverModels;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupUiComponents();
        setupOnClickListeners();
        database = Room.databaseBuilder(
                getApplicationContext(),
                ServerDatabase.class,
                "ServerDB")
                .fallbackToDestructiveMigration()
                .build();
        serverService = new ServerService(database);
        new Thread(() -> {
            serverModels = serverService.getAllServers();
            addPreviousServers(serverModels);
        }).start();
    }
    public void setupUiComponents() {
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.navHostFragment);

        NavController navController = navHostFragment.getNavController();
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(R.id.serversFragment, R.id.sshKeysFragment).setDrawerLayout(binding.drawerLayout).build();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        NavigationUI.setupWithNavController(
                toolbar, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }
    public void setupOnClickListeners() {
    }

    public void addPreviousServers(ArrayList<ServerModel> serverModels) {
        for (int i = 0; i < serverModels.size(); i++) {
            ServerModel model = serverModels.get(i);
            model.setServerStatusImg(R.drawable.redcircle);
            model.setConnected(false);
            addWorkerForNewServer(model, i);
        }
    }

    public void showDialog() {
        CreateServerDialogFragment dialogFragment = new CreateServerDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), DIALOG_TAG);
    }

    public void onDialogResult(String serverName, String hostIp, int port, String userName, String password, String privateKey) {
        ServerModel server = new ServerModel(0, serverName, hostIp, port, userName, password, privateKey, false, 0, 0, -1, 0, 0, R.drawable.redcircle);
        serverModels.add(server);
        database.getServerDao().addServer(ServerMapper.serverModelToEntity(server));
        //serverAdapter.notifyItemInserted(serverModels.size() - 1);
        serverAdapter.notifyDataSetChanged();
        addWorkerForNewServer(server, serverModels.size() - 1);
    }

    private void addWorkerForNewServer(ServerModel serverModel, int position) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Handler handler = new Handler(Looper.getMainLooper());
        executor.scheduleAtFixedRate(() -> {
            MonitoringRecordEntity monitoringRecordEntity = SshSessionWorker.monitorServer(getApplicationContext(), serverModel);
            if (monitoringRecordEntity == null) {
                serverModel.setConnected(false);
                serverModel.setServerStatusImg(R.drawable.redcircle);
                handler.post(() -> serverAdapter.notifyItemChanged(position));
                return;
            }
            serverModel.setMemoryUsedMb(monitoringRecordEntity.memoryUsedMb);
            serverModel.setMemoryTotalMb(monitoringRecordEntity.memoryTotalMb);
            serverModel.setDiskUsedMb(monitoringRecordEntity.diskUsedMb);
            serverModel.setDiskTotalMb(monitoringRecordEntity.diskTotalMb);
            serverModel.setCpuUsagePercent(monitoringRecordEntity.cpuUsagePercent);
            serverModel.setConnected(true);
            serverModel.setServerStatusImg(R.drawable.greencircle);
            if (serverModel.getMonitoringSessionId() != -1) {
                monitoringRecordEntity.monitoringSessionId = serverModel.getMonitoringSessionId();
                monitoringRecordEntity.timeRecorded = Converters.dateToTimestamp(Calendar.getInstance().getTime());
                database.getMonitoringRecordDao().addMonitoringRecord(monitoringRecordEntity);
            }
            handler.post(() ->
                {
                    if (serverAdapter != null) {
                        serverAdapter.notifyItemChanged(position);
                    }
                }
            );
        }, 0, MONITORING_INTERVAL, TimeUnit.SECONDS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        serverModels = null;
    }

}