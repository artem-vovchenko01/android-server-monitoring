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
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;

public class SshSessionWorker {
    public static String MEMORY_USED_MB_COMMAND = "free -m | grep Mem | awk '{print $3}';";
    public static String MEMORY_TOTAL_MB_COMMAND = "free -m | grep Mem | awk '{print $2}';";
    public static String DISK_USED_MB_COMMAND = "df -mP / | tail -n -1 | awk '{print $3}';";
    public static String DISK_TOTAL_MB_COMMAND = "df -mP / | tail -n -1 | awk '{print $2}';";
    public static String CPU_USAGE_COMMAND = "top -bn 1 | grep '%Cpu' | awk '{print $2 + $4}';";
    private static final String LOGGING_TAG = "myapp";

    public static MonitoringRecordEntity monitorServer(Context context, ServerModel serverModel) {
        MonitoringRecordEntity result = collectMonitoringStatistics(context, serverModel);
        return result;
    }

    public static Session createSshSession(JSch jsch, Context context, ServerModel serverModel) throws IOException, JSchException {
        String user = serverModel.getUserName();
        String host = serverModel.getHostIp();
        String password = serverModel.getPassword();
        String privateKey = serverModel.getPrivateKey();
        int port = serverModel.getPort();
        if (privateKey != null) {
            File tempFile = getTemporaryFile(context, privateKey);
            String privateKeyPath = tempFile.getAbsolutePath();
            jsch.addIdentity(privateKeyPath);
            tempFile.delete();
        }
        Session session = jsch.getSession(user, host, port);
        if (password != null) {
            session.setPassword(password);
        }
        session.setConfig("StrictHostKeyChecking", "no");
        session.setTimeout(5000);
        session.connect();
        return session;
    }

    public static MonitoringRecordEntity collectMonitoringStatistics(Context context, ServerModel serverModel) {
        String output = "";
        try {
            JSch jsch = new JSch();
            Session session = createSshSession(jsch, context, serverModel);
            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
            String commandList =
                    MEMORY_USED_MB_COMMAND +
                    MEMORY_TOTAL_MB_COMMAND +
                    DISK_USED_MB_COMMAND +
                    DISK_TOTAL_MB_COMMAND +
                    CPU_USAGE_COMMAND;
            channelExec.setCommand(commandList);
            channelExec.connect();
            output = readChannelOutput(channelExec);
            channelExec.disconnect();
        }
        catch(JSchException e){
            Log.d(LOGGING_TAG, "Jsch Exception occurred: " + e.getMessage());
            Log.d(LOGGING_TAG, Log.getStackTraceString(e));
            return null;
        } catch (IOException e) {
            Log.d(LOGGING_TAG, "Exception occurred during process of working with private key file: " + e.getMessage());
            Log.d(LOGGING_TAG, Log.getStackTraceString(e));
            return null;
        }
        return parseMonitoringOutput(output);
    }
    private static MonitoringRecordEntity parseMonitoringOutput(String output) {
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
            Log.d(LOGGING_TAG, "Exception occurred while filling monitoring data from output: " + e.getMessage());
            Log.d(LOGGING_TAG, Log.getStackTraceString(e));
           return null;
       }
       return monitoringRecordEntity;
    }

    private static String readChannelOutput(Channel channel){
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

    private static File getTemporaryFile(Context context, String key) throws IOException {
        File tempFile = File.createTempFile("prefix_", ".txt", context.getCacheDir());
        writeStringToFile(tempFile, key);
        return tempFile;
    }

    private static void writeStringToFile(File file, String content) {
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
