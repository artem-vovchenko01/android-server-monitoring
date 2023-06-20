package com.example.servermonitor.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.R;
import com.example.servermonitor.helper.UiHelper;
import com.example.servermonitor.model.SystemdServiceModel;
import com.example.servermonitor.service.SshSessionWorker;

public class ShowSystemdServiceLogDialog extends DialogFragment {
    private ScrollView scrollView;
    private TextView tvLogOutput;
    private TextView tvLoading;
    private Button btnClose;
    private Button btnCopy;
    private SshSessionWorker sshSessionWorker;
    private MainActivity activity;
    private SystemdServiceModel systemdServiceModel;

    public ShowSystemdServiceLogDialog(SshSessionWorker sshSessionWorker, SystemdServiceModel systemdServiceModel) {
        this.sshSessionWorker = sshSessionWorker;
        this.systemdServiceModel = systemdServiceModel;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.script_output_layout, container, false);
        tvLogOutput = rootView.findViewById(R.id.tvOutput);
        tvLoading = rootView.findViewById(R.id.tvLoading);
        btnClose = rootView.findViewById(R.id.btnClose);
        btnCopy = rootView.findViewById(R.id.btnCopy);
        scrollView = rootView.findViewById(R.id.svScriptOutput);
        setupListeners();
        activity = (MainActivity) getActivity();
        new Thread(() -> {
            activity.runOnUiThread(() -> {
                tvLoading.setVisibility(View.VISIBLE);
            });
            String output = null;
            try {
                output = sshSessionWorker.executeSingleCommand("journalctl --no-pager -u "
                    + systemdServiceModel.serviceName);
            } catch (Exception e) {
                UiHelper.displayError(activity, "Failed to fetch log output");
                return;
            }
            String finalOutput = output;
            activity.runOnUiThread(() -> {
                tvLoading.setVisibility(View.GONE);
                tvLogOutput.setText(finalOutput);
                scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
            });
        }).start();
        return rootView;
    }

    private void setupListeners() {
        btnClose.setOnClickListener((v) -> {
            dismiss();
        });
        btnCopy.setOnClickListener((v) -> {

        });
    }
}
