package com.example.servermonitor.service;

import android.content.Context;
import android.util.Log;

import com.example.servermonitor.model.ServerModel;
import com.example.servermonitor.model.SshKeyModel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Optional;

public class SshShellSessionWorker implements AutoCloseable {
    private static final String TAG = "sshSessionWorker";
    private OutputStream outputStream;
    private InputStream inputStream;
    private ChannelShell channel;
    private Optional<SshKeyModel> sshKey;
    private ServerModel server;
    private Session session;
    private JSch jsch;
    private ArrayList<File> tempFiles;
    private Context context;

    public SshShellSessionWorker(Context context, ServerModel server, Optional<SshKeyModel> sshKey) throws Exception {
        this.context = context;
        this.server = server;
        this.sshKey = sshKey;
        this.jsch = new JSch();
        this.tempFiles = new ArrayList<>();
        if (!createSshSession()) {
            throw new Exception("Couldn't establish session with a server");
        }
        establishShell();
    }

    public String tryFetchNewOutput() {
        StringBuilder result = new StringBuilder();
        int SIZE = 2048;
        byte[] tmp = new byte[SIZE];
        while (true) {
            int i = 0;
            try {
                if (inputStream.available() == 0) break;
                i = inputStream.read(tmp, 0, SIZE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (i < 0)
                break;
            result.append(new String(tmp, 0, i));
        }
        return result.toString()
            .replace("\u001B[?2004l","")
            .replace("\u001B[?2004h","");
    }

    private Boolean createSshSession() {
        String user = server.getUserName();
        String host = server.getHostIp();
        String password = server.getPassword();
        int port = server.getPort();
        try {
            if (sshKey.isPresent()) {
                String privateKey = sshKey.get().getKeyData();
                File tempFile = getTemporaryFile(privateKey);
                String privateKeyPath = tempFile.getAbsolutePath();
                jsch.addIdentity(privateKeyPath);
            }
            session = jsch.getSession(user, host, port);
            if (password != null) {
                session.setPassword(password);
            }
            session.setConfig("StrictHostKeyChecking", "no");
            session.setTimeout(10000);
            session.connect();
        } catch (JSchException e) {
            e.printStackTrace();
            Log.d(TAG, "JSch exception occurred while getting the session");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "IO exception occurred when working with SSH key and trying to establish ssh session");
            return false;
        }
        return true;
    }

    private void establishShell() {
        try {
            channel = (ChannelShell) session.openChannel("shell");
            outputStream = channel.getOutputStream();
            channel.connect();
            inputStream = channel.getInputStream();
            outputStream = channel.getOutputStream();
            executeCommand("unset LS_COLORS; export TERM=vt220");
        } catch (JSchException | IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Exception occurred when establishing SSH shell");
        }
    }

    public void executeCommand(String command) {
        new Thread(() -> {
            try {
                PrintStream printStream = new PrintStream(outputStream, true);
                printStream.print(command + "\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private File getTemporaryFile(String content) throws IOException {
        File tempFile = File.createTempFile("file_" + content.hashCode(), ".txt", context.getCacheDir());
        writeStringToFile(tempFile, content);
        tempFiles.add(tempFile);
        return tempFile;
    }

    private void writeStringToFile(File file, String content) {
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void close() throws Exception {
        inputStream.close();
        outputStream.close();
        channel.disconnect();
        session.disconnect();
        for (File file : tempFiles) {
            file.delete();
        }
    }
}
