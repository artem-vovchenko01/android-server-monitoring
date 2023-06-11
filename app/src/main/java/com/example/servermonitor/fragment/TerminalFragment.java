package com.example.servermonitor.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.databinding.FragmentTerminalBinding;
import com.example.servermonitor.model.ServerModel;
import com.example.servermonitor.model.SshKeyModel;
import com.example.servermonitor.service.SshKeyService;
import com.example.servermonitor.service.SshShellSessionWorker;

import java.util.Optional;

public class TerminalFragment extends Fragment {
    private static final int OUTPUT_SIZE_LIMIT = 100000;
    private int currentOutputSize = 0;
    private FragmentTerminalBinding binding;
    private SshKeyService sshKeyService;
    private MainActivity activity;
    public static ServerModel serverModel;
    private SshShellSessionWorker shellSessionWorker;
    private Context context;

    public TerminalFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTerminalBinding.inflate(inflater, container, false);
        activity = (MainActivity) getActivity();
        activity.getSupportActionBar().setTitle("Terminal");
        context = activity.getApplicationContext();
        sshKeyService = new SshKeyService(MainActivity.database);
        binding.userInput.setActivated(true);
        runTerminalSession();
        setupListeners();
        return binding.getRoot();
    }
    private void setupListeners() {
        binding.btnRunCommand.setOnClickListener((v) -> {
            executeCommand(binding.userInput.getText().toString());
            binding.userInput.setText("");
        });
    }
    private void runTerminalSession() {
        new Thread(() -> {
            Optional<SshKeyModel> sshKeyModel = Optional.of(sshKeyService.getSshKeyById(serverModel.getPrivateKeyId()));
            try {
                shellSessionWorker = new SshShellSessionWorker(context, serverModel, sshKeyModel);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            fetchOutput();
        }).start();
    }
    private void executeCommand(String command) {
        shellSessionWorker.executeCommand(command);
    }

    private void fetchOutput() throws RuntimeException {
        Thread thread = new Thread(() -> {
            while (true) {
                if (shellSessionWorker == null) break;
                String result = shellSessionWorker.tryFetchNewOutput();
                if (result != "")
                    activity.runOnUiThread(() -> updateTerminalWithNewText(result));
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        try {
            thread.start();
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateTerminalWithNewText(String newPart) {
        binding.terminalOutput.append(newPart);
        currentOutputSize += newPart.length();
        if (currentOutputSize > OUTPUT_SIZE_LIMIT) {
            String text = binding.terminalOutput.getText().toString();
            String newText = text.substring(text.length() -  (int)(OUTPUT_SIZE_LIMIT * 0.8));
            binding.terminalOutput.setText(newText);
            currentOutputSize = newText.length();
        }
        binding.terminalScrollView.fullScroll(View.FOCUS_DOWN);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        shellSessionWorker = null;
    }
}
