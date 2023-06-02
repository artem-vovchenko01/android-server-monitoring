package com.example.servermonitor;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.servermonitor.db.entity.MonitoringRecordEntity;
import com.example.servermonitor.model.ServerModel;
import com.example.servermonitor.model.SshKeyModel;
import com.example.servermonitor.service.SshKeyService;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Optional;

public class SshSessionWorker implements AutoCloseable {
    private static final String TAG = "sshSessionWorker";
    private Session session;
    private Context context;
    private Optional<SshKeyModel> sshKey;
    private ServerModel server;
    private JSch jsch;
    private ArrayList<File> tempFiles;
    private static String MEMORY_USED_MB_COMMAND = "free -m | grep Mem | awk '{print $3}';";
    private static String MEMORY_TOTAL_MB_COMMAND = "free -m | grep Mem | awk '{print $2}';";
    private static String DISK_USED_MB_COMMAND = "df -mP / | tail -n -1 | awk '{print $3}';";
    private static String DISK_TOTAL_MB_COMMAND = "df -mP / | tail -n -1 | awk '{print $2}';";
    private static String CPU_USAGE_COMMAND = "top -bn 1 | grep '%Cpu' | awk '{print $2 + $4}';";
    public SshSessionWorker(Context context, ServerModel server, Optional<SshKeyModel> sshKey) throws Exception {
        this.context = context;
        this.server = server;
        this.sshKey = sshKey;
        this.jsch = new JSch();
        this.tempFiles = new ArrayList<>();
        if (!createSshSession()) {
            throw new Exception("Couldn't establish session with a server");
        }
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

    public MonitoringRecordEntity getMonitoringStats() {
        String output = "";
        String commandList =
                MEMORY_USED_MB_COMMAND +
                MEMORY_TOTAL_MB_COMMAND +
                DISK_USED_MB_COMMAND +
                DISK_TOTAL_MB_COMMAND +
                CPU_USAGE_COMMAND;
        output = executeSingleCommand(commandList);
        return parseMonitoringOutput(output);
    }
    private MonitoringRecordEntity parseMonitoringOutput(String output) {
        MonitoringRecordEntity monitoringRecordEntity = new MonitoringRecordEntity();
        try {
           String[] split = output.split("\n");
           monitoringRecordEntity.id = 0;
           monitoringRecordEntity.monitoringSessionId = -1;
           monitoringRecordEntity.memoryUsedMb = Integer.parseInt(split[0]);
           monitoringRecordEntity.memoryTotalMb = Integer.parseInt(split[1]);
           monitoringRecordEntity.diskUsedMb = Integer.parseInt(split[2]);
           monitoringRecordEntity.diskTotalMb = Integer.parseInt(split[3]);
           monitoringRecordEntity.cpuUsagePercent = Double.parseDouble(split[4]);
       } catch (Exception e) {
            Log.d(TAG, "Exception occurred while filling monitoring data from output: " + e.getMessage());
            Log.d(TAG, Log.getStackTraceString(e));
           return null;
       }
       return monitoringRecordEntity;
    }
    public String executeShellScript(String scriptContents) {
        String homeDirPath = executeSingleCommand("pwd").replace("\n", "");
        String serverFileName = homeDirPath + "/scriptToExecute.sh";
        putFileFromText(scriptContents, serverFileName);
        executeSingleCommand("chmod +x " + serverFileName);
        String output = executeSingleCommand(serverFileName);
        executeSingleCommand("rm " + serverFileName);
        return output;
    }
    public Boolean putFileFromText(String text, String serverPath) {
        File file = null;
        try {
            file = getTemporaryFile(text);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Exception occurred when trying to place text in a temporary file");
            return false;
        }
        return putFile(file, serverPath);
    }
    public Boolean putFile(File file, String serverPath) {
        ChannelSftp channelSftp = null;
        try {
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
        } catch (JSchException e) {
            e.printStackTrace();
            Log.d(TAG, "JSch exception during SFTP connection occurred.");
            return false;
        }
        try {
            channelSftp.put(file.getAbsolutePath(), serverPath);
        } catch (SftpException e) {
            e.printStackTrace();
            Log.d(TAG, "sftp exception occurred when trying to put file to the server");
            return false;
        }
        channelSftp.disconnect();
        return true;
    }

    private String executeSingleCommand(String command) {
        String result = "";
        try {
            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command);
            channelExec.connect();
            result = readChannelOutput(channelExec);
            channelExec.disconnect();
        } catch (JSchException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private String readChannelOutput(Channel channel){
        StringBuilder result = new StringBuilder();
        byte[] buffer = new byte[1024];
        try{
            InputStream in = channel.getInputStream();
            String line = "";
            while (true){
                while (in.available() > 0) {
                    int i = in.read(buffer, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    line = new String(buffer, 0, i);
                    result.append(line);
                }

                if (channel.isClosed()){
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee){}
            }
        }catch(Exception e){
        }
        return result.toString();
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
        session.disconnect();
        for (File file : tempFiles) {
            file.delete();
        }
    }
}
