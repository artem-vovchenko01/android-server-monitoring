package com.example.servermonitor.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.R;
import com.example.servermonitor.databinding.FragmentServerBinding;
import com.example.servermonitor.db.Converters;
import com.example.servermonitor.db.ServerDatabase;
import com.example.servermonitor.db.entity.MonitoringRecordEntity;
import com.example.servermonitor.model.MonitoringSessionModel;
import com.example.servermonitor.model.ServerModel;
import com.example.servermonitor.service.LineChartStyler;
import com.example.servermonitor.service.MonitoringSessionService;
import com.example.servermonitor.service.PieChartStyler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerFragment extends Fragment {
    private static int COLOR_LIGHT_YELLOW;
    private static int COLOR_GREEN;
    private FragmentServerBinding binding;
    private MainActivity activity;
    private Context context;
    private ServerDatabase database;
    private ServerModel serverModel;
    private MonitoringSessionService monitoringSessionService;
    private PieChartStyler pieChartStyler;
    private LineChartStyler lineChartStyler;

    public ServerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentServerBinding.inflate(inflater, container, false);
        activity = (MainActivity) getActivity();
        activity.serverFragment = this;
        database = MainActivity.database;
        monitoringSessionService = new MonitoringSessionService(database);
        activity.getSupportActionBar().setTitle("Server");
        context = activity.getApplicationContext();
        COLOR_LIGHT_YELLOW = ContextCompat.getColor(context, R.color.light_yellow);
        COLOR_GREEN = ContextCompat.getColor(context, R.color.pale_green);
        return binding.getRoot();
    }
    private void getServerModel(Bundle args) {
        serverModel = args.getParcelable("serverModel");
        activity.getSupportActionBar().setTitle("Server " + serverModel.getName());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pieChartStyler = new PieChartStyler("used", "total", COLOR_LIGHT_YELLOW, COLOR_GREEN);
        lineChartStyler = new LineChartStyler();
        getServerModel(getArguments());
        updateMonitoringUiComponents();
        setupOnClickListeners();
    }

    public void setupOnClickListeners() {
        binding.btnChangeMonitoringState.setOnClickListener(v -> {
            if (serverModel.getMonitoringSessionId() != -1) {
                binding.btnChangeMonitoringState.setText("Start monitoring session");
                saveMonitoringSessionToDb();
            } else {
                binding.btnChangeMonitoringState.setText("Stop monitoring session");
                startNewMonitoringSession();
            }
        });

        binding.btnOpenTerminal.setOnClickListener(v -> {
            TerminalFragment.serverModel = serverModel;
            NavController controller = Navigation.findNavController(binding.getRoot());
            controller.navigate(R.id.terminalFragment);
        });

        binding.btnBrowseFiles.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putParcelable("serverModel", serverModel);
            NavController controller = Navigation.findNavController(binding.getRoot());
            controller.navigate(R.id.browseServerFilesFragment, args);
        });

        binding.btnMonitoringHistory.setOnClickListener(v -> {
            NavController controller = Navigation.findNavController(binding.getRoot());
            Bundle args = new Bundle();
            args.putParcelable("serverModel", serverModel);
            controller.navigate(R.id.action_serverFragment_to_monitoringSessionsFragment, args);
        });

        binding.btnViewServices.setOnClickListener(v -> {
            NavController controller = Navigation.findNavController(binding.getRoot());
            Bundle args = new Bundle();
            args.putParcelable("serverModel", serverModel);
            controller.navigate(R.id.action_serverFragment_to_systemdServicesFragment, args);
        });
    }
    public void updateMonitoringUiComponents() {
        if (serverModel.getMonitoringSessionId() == -1) {
            binding.btnChangeMonitoringState.setText("Start monitoring session");
        } else {
            binding.btnChangeMonitoringState.setText("Stop monitoring session");
        }
        new Thread(() -> {
            updatePieCharts();
            updateLineCharts();
        }).start();
    }
    public void saveMonitoringSessionToDb() {
        new Thread(() -> {
            int sessionId = serverModel.getMonitoringSessionId();
            serverModel.setMonitoringSessionId(-1);
            MonitoringSessionModel session = monitoringSessionService.getMonitoringSessionModelById(sessionId);
            session.setDateEnded(Calendar.getInstance().getTime());
            monitoringSessionService.updateMonitoringSession(session);
            updateLineCharts();
        }).start();
    }
    public void startNewMonitoringSession() {
        new Thread(() -> {
            MonitoringSessionModel session = new MonitoringSessionModel();
            Date currentTime = Calendar.getInstance().getTime();
            session.setName("session "
                    + currentTime.getHours()
                    + ":" + currentTime.getMinutes()
                    + " " + currentTime.getDay()
                    + "." + currentTime.getMonth()
                    + "." + currentTime.getYear());
            session.setDateStarted(currentTime);
            session.setServerId(serverModel.getId());
            monitoringSessionService.addMonitoringSession(session);
            session = monitoringSessionService.getMonitoringSessionByStartTime(currentTime);
            serverModel.setMonitoringSessionId(session.getId());
            activity.runOnUiThread(() -> {
                setLineChartsVisible();
            });
        }).start();
    }

    public void updatePieCharts() {
        float memoryUsed = serverModel.getMemoryUsedMb();
        float memoryTotal = serverModel.getMemoryTotalMb();
        float diskUsed = (float) serverModel.getDiskUsedMb();
        float diskTotal = (float) serverModel.getDiskTotalMb();
        float cpuUsed = (float) serverModel.getCpuUsagePercent();
        float cpuTotal = 100f;
        if (memoryTotal == 0 || cpuUsed == -1 || diskTotal == 0) {
            activity.runOnUiThread(() -> setPieChartsHidden());
        } else {
            activity.runOnUiThread(() -> {
                pieChartStyler.stylePieChart(binding.pcMemory, memoryUsed, memoryTotal);
                pieChartStyler.stylePieChart(binding.pcCpu, cpuUsed, cpuTotal);
                pieChartStyler.stylePieChart(binding.pcDisk, diskUsed, diskTotal);
                setPieChartsVisible();
            });
        }
    }
    public void updateLineCharts() {
        int sessionId = serverModel.getMonitoringSessionId();
        if (sessionId == -1) {
            activity.runOnUiThread(() -> {
                setLineChartsHidden();
                lineChartStyler.styleLineChart(binding.lcMemory, new ArrayList<>(), LineChartStyler.LineChartDataType.DATA_MEMORY, null);
                lineChartStyler.styleLineChart(binding.lcCpu, new ArrayList<>(), LineChartStyler.LineChartDataType.DATA_CPU, null);
                lineChartStyler.styleLineChart(binding.lcStorage, new ArrayList<>(), LineChartStyler.LineChartDataType.DATA_DISK, null);
            });
        } else {
            MonitoringSessionModel session = monitoringSessionService.getMonitoringSessionModelById(sessionId);
            ArrayList<MonitoringRecordEntity> monitoringRecords = new ArrayList<>(database.getMonitoringRecordDao().getAllByMonitoringSessionId(sessionId));
            activity.runOnUiThread(() -> {
                lineChartStyler.styleLineChart(binding.lcMemory, monitoringRecords, LineChartStyler.LineChartDataType.DATA_MEMORY, session);
                lineChartStyler.styleLineChart(binding.lcCpu, monitoringRecords, LineChartStyler.LineChartDataType.DATA_CPU, session);
                lineChartStyler.styleLineChart(binding.lcStorage, monitoringRecords, LineChartStyler.LineChartDataType.DATA_DISK, session);
                setLineChartsVisible();
            });
        }
    }

    public void setLineChartsVisible() {
        binding.lcMemory.invalidate();
        binding.lcCpu.invalidate();
        binding.lcStorage.invalidate();
        binding.lcMemory.setVisibility(View.VISIBLE);
        binding.lcCpu.setVisibility(View.VISIBLE);
        binding.lcStorage.setVisibility(View.VISIBLE);
        binding.tvForLcCpu.setVisibility(View.VISIBLE);
        binding.tvForLcMemory.setVisibility(View.VISIBLE);
        binding.tvForLcDisk.setVisibility(View.VISIBLE);
    }
    public void setLineChartsHidden() {
        binding.lcMemory.setVisibility(View.GONE);
        binding.lcCpu.setVisibility(View.GONE);
        binding.lcStorage.setVisibility(View.GONE);
        binding.tvForLcCpu.setVisibility(View.GONE);
        binding.tvForLcMemory.setVisibility(View.GONE);
        binding.tvForLcDisk.setVisibility(View.GONE);
    }
    public void setPieChartsVisible() {
        binding.pcMemory.invalidate();
        binding.pcCpu.invalidate();
        binding.pcDisk.invalidate();
        binding.pcMemory.setVisibility(View.VISIBLE);
        binding.tvLabelMemoryNoData.setVisibility(View.GONE);
        binding.pcCpu.setVisibility(View.VISIBLE);
        binding.tvLabelCpuNoData.setVisibility(View.GONE);
        binding.pcDisk.setVisibility(View.VISIBLE);
        binding.tvLabelDiskNoData.setVisibility(View.GONE);
    }
    public void setPieChartsHidden() {
        binding.pcMemory.setVisibility(View.GONE);
        binding.tvLabelMemoryNoData.setVisibility(View.VISIBLE);
        binding.pcCpu.setVisibility(View.GONE);
        binding.tvLabelCpuNoData.setVisibility(View.VISIBLE);
        binding.pcDisk.setVisibility(View.GONE);
        binding.tvLabelDiskNoData.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();
        activity.serverFragment = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        activity.serverFragment = this;
    }
}