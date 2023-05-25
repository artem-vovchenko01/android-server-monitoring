package com.example.servermonitor.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.servermonitor.R;
import com.example.servermonitor.SshSessionWorker;
import com.example.servermonitor.model.ServerModel;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class TerminalActivity extends AppCompatActivity {
    private static final int OUTPUT_SIZE_LIMIT = 100000;
    public static ServerModel serverModel;
    private TextView terminalOutput;
    private EditText userInput;
    private JSch jsch;
    private Session session;
    private ChannelShell channel;
    private OutputStream outputStream;
    private InputStream inputStream;
    private ScrollView scrollView;
    private int currentOutputSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);
        terminalOutput = findViewById(R.id.terminalOutput);
        userInput = findViewById(R.id.userInput);
        scrollView = findViewById(R.id.terminalScrollView);
        jsch = new JSch();
        connectSSH();

        userInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String command = userInput.getText().toString().trim();
                    Log.d("myapp", "command: " + command);
                    executeCommand(command);
                    userInput.setText("");
                    return true;
                }
                return false;
            }
        });
    }

    private void connectSSH() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    session = SshSessionWorker.createSshSession(jsch, getApplicationContext(), serverModel);
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            userInput.setActivated(true);
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
                    runOnUiThread(new Runnable() {
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
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        terminalOutput.append(finalResult);
                                        currentOutputSize += finalResult.length();
                                        if (currentOutputSize > OUTPUT_SIZE_LIMIT) {
                                            String text = terminalOutput.getText().toString();
                                            String newText = text.substring(text.length() -  (int)(OUTPUT_SIZE_LIMIT * 0.8));
                                            terminalOutput.setText(newText);
                                            currentOutputSize = newText.length();
                                        }
                                        scrollView.fullScroll(View.FOCUS_DOWN);
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
    protected void onDestroy() {
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