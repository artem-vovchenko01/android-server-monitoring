package com.example.servermonitor.helper;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.model.ServerModel;
import com.example.servermonitor.model.SshKeyModel;
import com.example.servermonitor.service.SshKeyService;
import com.example.servermonitor.service.SshShellSessionWorker;
import com.jcraft.jsch.SftpException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class FileOperations {
    private static String remoteFileToCopy = null;
    private static String remoteFileShortName = null;
    private static File localFileToCopy = null;
    public static ServerModel serverModelLastBrowsedFiles = null;
    private static ServerModel serverModelWantToCopyFrom = null;
    public static boolean existsFileToCopy() {
        return localFileToCopy != null || remoteFileToCopy != null;
    }
    public static boolean existsRemoteFileToCopy() {
        return remoteFileToCopy != null;
    }
    public static String fileToCopyName() {
        if (localFileToCopy != null) {
            return localFileToCopy.getName();
        }
        if (remoteFileToCopy != null) {
            return remoteFileShortName;
        }
        return "";
    }
    public static ServerModel getServerModelWantToCopyFrom() {
        return serverModelWantToCopyFrom;
    }
    public static String getShortFileNameToCopy() {
        if (remoteFileToCopy != null) {
            return remoteFileShortName;
        }
        if (localFileToCopy != null) {
            return localFileToCopy.getName();
        }
        return "";
    }
    public static void copyFileToCopy(Activity activity, Context showProgressContext, ServerModel destinationServer, String destinationPath, Supplier functionAfterCopy) {
        SshKeyService sshKeyService = new SshKeyService(MainActivity.database);
        Context context = activity.getApplicationContext();
        if (destinationServer == null) {
            if (localFileToCopy != null) {
                // local to local
                File from = localFileToCopy;
                File to = new File(destinationPath + "/" + from.getName());
                InputStream is = null;
                try {
                    is = new FileInputStream(from);
                } catch (FileNotFoundException e) {
                    UiHelper.displayError(activity, "Can't get input stream from source file");
                    return;
                }
                OutputStream os = null;
                try {
                    os = new FileOutputStream(to);
                } catch (FileNotFoundException e) {
                    UiHelper.displayError(activity, "Can't get output stream on destination file");
                    return;
                }
                try {
                    copyDataBetweenStreams(is, os);
                    functionAfterCopy.get();
                } catch (IOException e) {
                    UiHelper.displayError(activity, "Error when copying local file to local storage");
                    return;
                }
            }
            else {
                // server to local
                File to = new File(destinationPath + "/" + remoteFileShortName);
                OutputStream os = null;
                try {
                    os = new FileOutputStream(to);
                } catch (FileNotFoundException e) {
                    UiHelper.displayError(activity, "Can't get output stream on destination file");
                    return;
                }
                ServerModel server = serverModelWantToCopyFrom;
                Optional<SshKeyModel> sshKey = sshKeyService.getSshKeyForServer(server);
                SshShellSessionWorker shellSessionWorker = null;
                try {
                    shellSessionWorker = new SshShellSessionWorker(context, server, sshKey);
                } catch (Exception e) {
                    UiHelper.displayError(activity, "Error while connecting to server");
                    return;
                }
                List<Object> results = shellSessionWorker.copyFromServerUsingStreams(remoteFileToCopy, os);
                if ((boolean) results.get(0)) {
                    FileLoadingProgressMonitor monitor = (FileLoadingProgressMonitor) results.get(1);
                    UiHelper.monitorProgress(showProgressContext, activity, monitor, functionAfterCopy::get);
                }
            }
        } else {
            if (localFileToCopy != null) {
                // local to server
                File from = localFileToCopy;
                long fromSize = from.length();
                InputStream is = null;
                try {
                    is = new FileInputStream(from);
                } catch (FileNotFoundException e) {
                    UiHelper.displayError(activity, "Can't get input stream from source file");
                }
                ServerModel server = destinationServer;
                Optional<SshKeyModel> sshKey = sshKeyService.getSshKeyForServer(server);
                SshShellSessionWorker shellSessionWorker = null;
                try {
                    shellSessionWorker = new SshShellSessionWorker(context, server, sshKey);
                } catch (Exception e) {
                    UiHelper.displayError(activity, "Error while connecting to server");
                    return;
                }
                List<Object> results = shellSessionWorker.copyFromLocalUsingStreams(destinationPath + "/"  + from.getName(), is, fromSize);
                if ((boolean) results.get(0)) {
                    FileLoadingProgressMonitor monitor = (FileLoadingProgressMonitor) results.get(1);
                    UiHelper.monitorProgress(showProgressContext, activity, monitor, functionAfterCopy::get);
                }
            } else {
                // server to server
                ServerModel server = serverModelWantToCopyFrom;
                Optional<SshKeyModel> sshKey = sshKeyService.getSshKeyForServer(server);
                SshShellSessionWorker shellSessionWorker = null;
                try {
                    shellSessionWorker = new SshShellSessionWorker(context, server, sshKey);
                } catch (Exception e) {
                    UiHelper.displayError(activity, "Error while connecting to server");
                    return;
                }
                List<Object> results = null;
                try {
                    results = shellSessionWorker.copyFromServerGetInputStream(remoteFileToCopy);
                } catch (SftpException e) {
                    UiHelper.displayError(activity, "Error while getting input stream");
                    return;
                }
                if ((boolean) results.get(0)) {
                    FileLoadingProgressMonitor monitor = (FileLoadingProgressMonitor) results.get(1);
                    Optional<SshKeyModel> sshKeyDestination = sshKeyService.getSshKeyForServer(destinationServer);
                    SshShellSessionWorker shellSessionWorkerDestination = null;
                    try {
                        shellSessionWorkerDestination = new SshShellSessionWorker(context, destinationServer, sshKeyDestination);
                    } catch (Exception e) {
                        UiHelper.displayError(activity, "Error while connecting to server");
                        return;
                    }
                    InputStream is = (InputStream) results.get(2);
                    long fileSize = (long) results.get(3);
                    shellSessionWorkerDestination.copyFromLocalUsingStreams(destinationPath + "/" + remoteFileShortName, is, fileSize);
                    UiHelper.monitorProgress(showProgressContext, activity, monitor, functionAfterCopy::get);
                }
            }
        }
    }
    public static void setRemoteFileForCopy(ServerModel server, String fullPath, String shortName) {
        serverModelWantToCopyFrom = server;
        remoteFileToCopy = fullPath;
        remoteFileShortName = shortName;
        localFileToCopy = null;
    }
    public static void setLocalFileForCopy(File file) {
        localFileToCopy = file;
        remoteFileToCopy = null;
        remoteFileShortName = null;
        serverModelWantToCopyFrom = null;
    }
    public static Boolean removeDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    removeDirectory(file);
                }
            }
        }
        return directory.delete();
    }

    public static Boolean verifyFilename(String fileName) {
        String disallowedChars = " \t\n\r\\/:?\"<>|";
        for (char c : disallowedChars.toCharArray()) {
            if (fileName.contains(Character.toString(c))) {
                return false;
            }
        }
        if (fileName.equals(".") || fileName.equals("..") || fileName.equals("")) return false;
        return true;
    }

    public static File saveFileToLocalStorage(Context context, InputStream inputStream, String fileName) throws IOException {
        File directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File file = new File(directory, fileName);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            return file;
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
            inputStream.close();
        }
    }

    public static void copyDataBetweenStreams(InputStream is, OutputStream os) throws IOException {
        try {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        }
        finally {
            if (os != null) {
                os.close();
            }
            is.close();
        }
    }
}
