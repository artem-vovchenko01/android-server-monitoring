package com.example.servermonitor.service;

import android.content.Context;
import android.util.Log;

import com.example.servermonitor.fragment.BrowseServerFilesFragment;
import com.example.servermonitor.helper.FileLoadingProgressMonitor;
import com.example.servermonitor.model.ServerModel;
import com.example.servermonitor.model.SshKeyModel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Vector;

public class SshShellSessionWorker implements AutoCloseable {
    private static final String TAG = "sshSessionWorker";
    private OutputStream outputStream;
    private InputStream inputStream;
    private ChannelShell channel;
    private ChannelSftp channelSftp;
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
            Log.d(TAG, "JSch exception occurred while getting the session");
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IO exception occurred when working with SSH key and trying to establish ssh session");
            return false;
        }
        return true;
    }
    public Vector<ChannelSftp.LsEntry> listDir(String dir, BrowseServerFilesFragment fragment) {
        Vector<ChannelSftp.LsEntry> lsEntries = null;
        try {
            if (channelSftp == null) {
                channelSftp = (ChannelSftp) session.openChannel("sftp");
                channelSftp.connect();
            }
            channelSftp.cd(dir);
            fragment.currentPath = channelSftp.pwd();
            lsEntries = channelSftp.ls(".");
            lsEntries.sort(lsEntryComparator);
        } catch(JSchException | SftpException e) {
            channelSftp.disconnect();
            try {
                channelSftp = (ChannelSftp) session.openChannel("sftp");
                channelSftp.connect();
            } catch (JSchException ex) {
                return null;
            }
            return null;
        }
        return lsEntries;
    }

    Comparator<ChannelSftp.LsEntry> lsEntryComparator = (entry1, entry2) -> {
        String name1 = entry1.getFilename();
        String name2 = entry2.getFilename();
        if (name1.equals(".") && ! name2.equals("..")) {
            return -1;
        } else if (name1.equals("..") && !name2.equals(".")) {
            return -1;
        }  else if (name1.equals("..") && name2.equals(".")) {
            return -1;
        } else {
            return name1.compareToIgnoreCase(name2);
        }
    };
    private void establishShell() {
        try {
            channel = (ChannelShell) session.openChannel("shell");
            outputStream = channel.getOutputStream();
            channel.connect();
            inputStream = channel.getInputStream();
            outputStream = channel.getOutputStream();
            executeCommand("unset LS_COLORS; export TERM=vt220");
        } catch (JSchException | IOException e) {
            Log.d(TAG, "Exception occurred when establishing SSH shell");
        }
    }

    public void executeCommand(String command) {
        new Thread(() -> {
            try {
                PrintStream printStream = new PrintStream(outputStream, true);
                printStream.print(command + "\n");
            } catch (Exception e) {
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
            Log.d(TAG, "IOException");
        }
    }

    public Boolean executeSingleCommand(String command) {
        try {
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.connect();
            for (int i = 0; i < 10; i++) {
                if (channel.isClosed())
                    break;
                Thread.sleep(100);
                if (i == 9) return false;
            }
            int exitStatus = channel.getExitStatus();
            if (exitStatus == 0) {
                return true;
            }
        } catch (JSchException | InterruptedException ee) {
            return false;
        }
        return false;
    }

    public Boolean sftpRm(String path) {
        SftpATTRS entry = null;
        try {
            entry = channelSftp.stat(path);
            if (!entry.isDir()) {
                channelSftp.rm(path);
                return true;
            }
            else {
                channelSftp.rmdir(path);
                return true;
            }
        } catch (SftpException e) {
            if (entry.isDir()) {
                String pwd = null;
                try {
                    pwd = channelSftp.pwd();
                    return executeSingleCommand("rm -r " + pwd + "/" + path);
                } catch (SftpException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        return false;
    }
    public List<Object> copyFromLocal(String localFullPath, String localFileName, String remoteDirectory) {
        ArrayList<Object> results = new ArrayList<>();
        FileLoadingProgressMonitor monitor = new FileLoadingProgressMonitor();
        new Thread(() -> {
            try {
                channelSftp.put(localFullPath, remoteDirectory + "/" + localFileName, monitor);
            } catch (SftpException e) {}
        }).start();
        results.add(true);
        results.add(monitor);
        return results;
    }
    public List<Object> copyFromServer(String remotePath, String remoteName, String localDirectory) {
        ArrayList<Object> results = new ArrayList<>();
        if (channelSftp == null) {
            try {
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            } catch (JSchException e) {
                results.add(false);
                return results;
            }
        }
        FileLoadingProgressMonitor monitor = new FileLoadingProgressMonitor();
        new Thread(() -> {
            try {
                channelSftp.get(remotePath, localDirectory + "/" + remoteName, monitor);
            } catch (SftpException e) {}
        }).start();
        results.add(true);
        results.add(monitor);
        return results;
    }
    public List<Object> downloadFile(String path) {
        ArrayList<Object> results = new ArrayList<>();
        FileLoadingProgressMonitor monitor = new FileLoadingProgressMonitor();
        InputStream fileStream = null;
        try {
             fileStream = channelSftp.get(path, monitor);
        } catch (SftpException e) {
            throw new RuntimeException(e);
        }
        results.add(fileStream);
        results.add(monitor);
        return results;
    }
    public Boolean mkdir(String directory) {
        try {
            channelSftp.mkdir(directory);
        } catch (SftpException e) {
            return false;
        }
        return true;
    }
    public Boolean touch(String file) {
        try {
            String pwd = channelSftp.pwd();
            return executeSingleCommand("touch " + pwd + "/" + file);
        } catch (SftpException e) {
            return false;
        }
    }
    @Override
    public void close() throws Exception {
        inputStream.close();
        outputStream.close();
        channel.disconnect();
        session.disconnect();
        if (channelSftp != null)
            channelSftp.disconnect();
        for (File file : tempFiles) {
            file.delete();
        }
    }
}
