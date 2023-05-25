package com.example.servermonitor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.work.Data;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import com.example.servermonitor.adapter.ServerAdapter;
import com.example.servermonitor.db.ServerDatabase;
import com.example.servermonitor.db.entity.MonitoringRecordEntity;
import com.example.servermonitor.db.entity.ServerEntity;
import com.example.servermonitor.mapper.ServerMapper;
import com.example.servermonitor.model.ServerModel;
import com.example.servermonitor.service.ServerService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final String DIALOG_TAG = "CreateServerDialogFragment";
    private ServerAdapter serverAdapter;
    private ServerDatabase database;
    RecyclerView rvServers;
    ArrayList<ServerModel> serverModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = Room.databaseBuilder(
                getApplicationContext(),
                ServerDatabase.class,
                "ServerDB")
                .allowMainThreadQueries()
                .build();

        FloatingActionButton fabAddServer = findViewById(R.id.fabAddServer);
        rvServers = findViewById(R.id.rvServers);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        rvServers.setLayoutManager(layoutManager);
        rvServers.setItemAnimator(new DefaultItemAnimator());
        serverModels = new ArrayList<>();
        serverModels.addAll(ServerService.mapServers(database.getServerDao().getAllServers()));
        addPreviousServers(serverModels);
        serverAdapter = new ServerAdapter(getApplicationContext(), serverModels, this);
        rvServers.setAdapter(serverAdapter);

        fabAddServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
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
        ServerModel server = new ServerModel(-1, serverName, hostIp, port, userName, password, privateKey, false, 0, 0, 0, 0, 0, R.drawable.redcircle);
        serverModels.add(server);
        database.getServerDao().addServer(ServerMapper.serverModelToEntity(server));
        serverAdapter.notifyItemInserted(serverModels.size() - 1);
        addWorkerForNewServer(server, serverModels.size() - 1);
    }

    private void addWorkerForNewServer(ServerModel serverModel, int position) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Handler handler = new Handler(Looper.getMainLooper());
        executor.scheduleAtFixedRate(() -> {
            MonitoringRecordEntity monitoringRecordEntity = SshSessionWorker.monitorServer(getApplicationContext(), serverModel);
            if (monitoringRecordEntity == null) {
                serverModel.setConnected(true);
                serverModel.setServerStatusImg(R.drawable.redcircle);
                handler.post(() -> {
                    Toast.makeText(getApplicationContext(),
                        "Error occurred during fetching data from server " + serverModel.getName(),
                        Toast.LENGTH_SHORT).show();
                        serverAdapter.notifyItemChanged(position);
                });
                return;
            }
            serverModel.setMemoryUsedMb(monitoringRecordEntity.memoryUsedMb);
            serverModel.setMemoryTotalMb(monitoringRecordEntity.memoryTotalMb);
            serverModel.setDiskUsedMb(monitoringRecordEntity.diskUsedMb);
            serverModel.setDiskTotalMb(monitoringRecordEntity.diskTotalMb);
            serverModel.setCpuUsagePercent(monitoringRecordEntity.cpuUsagePercent);
            serverModel.setConnected(true);
            serverModel.setServerStatusImg(R.drawable.greencircle);
            handler.post(() -> serverAdapter.notifyItemChanged(position));
        }, 0, 15, TimeUnit.SECONDS);
    }
}