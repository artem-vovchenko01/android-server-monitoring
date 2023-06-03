package com.example.servermonitor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.room.Room;

import android.os.Bundle;

import com.example.servermonitor.adapter.ServerAdapter;
import com.example.servermonitor.databinding.ActivityMainBinding;
import com.example.servermonitor.db.Converters;
import com.example.servermonitor.db.ServerDatabase;
import com.example.servermonitor.db.entity.MonitoringRecordEntity;
import com.example.servermonitor.model.ServerModel;
import com.example.servermonitor.model.SshKeyModel;
import com.example.servermonitor.service.ServerService;
import com.example.servermonitor.service.SshKeyService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final int MONITORING_INTERVAL = 3;
    public static ServerDatabase database;
    public ServerService serverService;
    public ServerAdapter serverAdapter;
    public ArrayList<ServerModel> serverModels;
    public HashMap<ServerModel, ScheduledFuture> scheduledJobs;
    public HashMap<ServerModel, ExecutorService> executors;
    public HashMap<ServerModel, SshSessionWorker> serverSessions;
    private ActivityMainBinding binding;
    private SshKeyService sshKeyService;

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
        sshKeyService = new SshKeyService(database);
        serverSessions = new HashMap<>();
        scheduledJobs = new HashMap<>();
        executors = new HashMap<>();
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
                new AppBarConfiguration.Builder(R.id.serversFragment, R.id.sshKeysFragment, R.id.shellScriptsFragment, R.id.alertsFragment, R.id.localFilesFragment).setDrawerLayout(binding.drawerLayout).build();
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

    public void addNewServer(ServerModel serverModel) {
        new Thread(() -> {
            if (serverModel.getId() != 0) {
                int pos = serverModels.indexOf(serverModels.stream().filter(m -> m.getId() == serverModel.getId()).findFirst().get());
                serverService.updateServer(serverModel);
                ServerModel previousServer = serverModels.get(pos);
                stopJobForServer(previousServer);
                serverModels.set(pos, serverModel);
                addWorkerForNewServer(serverModel, pos);
            } else {
                long id = serverService.addServer(serverModel);
                serverModel.setId((int) id);
                serverModels.add(serverModel);
                addWorkerForNewServer(serverModel, serverModels.size() - 1);
            }
            runOnUiThread(() -> serverAdapter.notifyDataSetChanged());
        }).start();
    }
    public void stopJobForServer(ServerModel serverModel) {
        ScheduledFuture<?> future = scheduledJobs.get(serverModel);
        ExecutorService executor = executors.get(serverModel);
        future.cancel(true);
        executor.shutdown();
        scheduledJobs.remove(serverModel);
        executors.remove(serverModel);
    }

    private void addWorkerForNewServer(ServerModel serverModel, int position) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        ScheduledFuture<?> future = executor.scheduleAtFixedRate(() -> {
            Optional<SshKeyModel> sshKeyModel = Optional.of(sshKeyService.getSshKeyById(serverModel.getPrivateKeyId()));
            SshSessionWorker worker = serverSessions.getOrDefault(serverModel, null);
            if (worker == null) {
                try {
                    worker = new SshSessionWorker(getApplicationContext(), serverModel, sshKeyModel);
                    serverSessions.put(serverModel, worker);
                } catch (Exception e) {
                    serverModel.setConnected(false);
                    serverModel.setServerStatusImg(R.drawable.redcircle);
                    runOnUiThread(() -> {
                        if (serverAdapter != null)
                            serverAdapter.notifyItemChanged(position);
                    });
                    return;
                }
            }
            MonitoringRecordEntity monitoringRecordEntity = worker.getMonitoringStats();
            updateServerModel(position, serverModel, monitoringRecordEntity);
            runOnUiThread(() -> {
                if (serverAdapter != null)
                    serverAdapter.notifyItemChanged(position);
            });
        }, 0, MONITORING_INTERVAL, TimeUnit.SECONDS);
        scheduledJobs.put(serverModel, future);
        executors.put(serverModel, executor);
    }
    private void updateServerModel(int position, ServerModel serverModel, MonitoringRecordEntity monitoringRecord) {
        if (monitoringRecord == null) {
            serverModel.setConnected(false);
            serverModel.setServerStatusImg(R.drawable.redcircle);
            return;
        }
        serverModel.setMemoryUsedMb(monitoringRecord.memoryUsedMb);
        serverModel.setMemoryTotalMb(monitoringRecord.memoryTotalMb);
        serverModel.setDiskUsedMb(monitoringRecord.diskUsedMb);
        serverModel.setDiskTotalMb(monitoringRecord.diskTotalMb);
        serverModel.setCpuUsagePercent(monitoringRecord.cpuUsagePercent);
        serverModel.setConnected(true);
        serverModel.setServerStatusImg(R.drawable.greencircle);
        if (serverModel.getMonitoringSessionId() != -1) {
            monitoringRecord.monitoringSessionId = serverModel.getMonitoringSessionId();
            monitoringRecord.timeRecorded = Converters.dateToTimestamp(Calendar.getInstance().getTime());
            database.getMonitoringRecordDao().addMonitoringRecord(monitoringRecord);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        serverModels = null;
    }
}