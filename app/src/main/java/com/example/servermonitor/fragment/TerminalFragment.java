package com.example.servermonitor.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.R;
import com.example.servermonitor.SshSessionWorker;
import com.example.servermonitor.databinding.FragmentTerminalBinding;
import com.example.servermonitor.mapper.ServerMapper;
import com.example.servermonitor.model.ServerModel;
import com.example.servermonitor.model.SshKeyModel;
import com.example.servermonitor.service.SshKeyService;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Optional;

public class TerminalFragment extends Fragment {
    private static final int OUTPUT_SIZE_LIMIT = 100000;
    private int currentOutputSize = 0;
    private FragmentTerminalBinding binding;
    private Session session;
    private SshKeyService sshKeyService;
    private OutputStream outputStream;
    private InputStream inputStream;
    private ChannelShell channel;
    private JSch jsch;
    private MainActivity activity;
    public static ServerModel serverModel;

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
        jsch = new JSch();
        activity = (MainActivity) getActivity();
        sshKeyService = new SshKeyService(MainActivity.database);
        return binding.getRoot();
    }

    private void connectSSH() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Optional<SshKeyModel> sshKeyModel = Optional.of(sshKeyService.getSshKeyById(serverModel.getPrivateKeyId()));
                    session = SshSessionWorker.createSshSession(jsch, activity, serverModel, sshKeyModel);
                    channel = (ChannelShell) session.openChannel("shell");
                    outputStream = channel.getOutputStream();
                    channel.connect();
                    inputStream = channel.getInputStream();
                    try {
                        fetchOutput();
                        executeCommand("unset LS_COLORS; export TERM=vt220");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.userInput.setActivated(true);
                        }
                    });
                } catch (JSchException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private void executeCommand(final String command) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PrintStream printStream = new PrintStream(outputStream, true);
                    printStream.print(command + "\n");
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //terminalOutput.append(command + "\n");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void fetchOutput() throws RuntimeException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int SIZE = 2048;
                byte[] tmp = new byte[SIZE];
                String result;
                while (true) {
                    try {
                        inputStream = channel.getInputStream();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        while (true) {
                            int i = inputStream.read(tmp, 0, SIZE);
                            if (i < 0)
                                break;
                            result = new String(tmp, 0, i);
                            String finalResult = result
                                    .replace("\u001B[?2004l","")
                                    .replace("\u001B[?2004h","");
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    binding.terminalOutput.append(finalResult);
                                    currentOutputSize += finalResult.length();
                                    if (currentOutputSize > OUTPUT_SIZE_LIMIT) {
                                        String text = binding.terminalOutput.getText().toString();
                                        String newText = text.substring(text.length() -  (int)(OUTPUT_SIZE_LIMIT * 0.8));
                                        binding.terminalOutput.setText(newText);
                                        currentOutputSize = newText.length();
                                    }
                                    binding.terminalScrollView.fullScroll(View.FOCUS_DOWN);
                                }
                            });
                        }
                    } catch (IOException ignored) {}
                    if(channel.isClosed())
                    {
                        // System.out.println("exit-status: " + channel.getExitStatus());
                        break;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (channel != null) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
