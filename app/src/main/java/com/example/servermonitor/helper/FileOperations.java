package com.example.servermonitor.helper;

import android.content.Context;
import android.os.Environment;

import com.example.servermonitor.model.ServerModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileOperations {
    public static String remoteFileToCopy = null;
    public static String remoteFileShortName = null;
    public static File localFileToCopy = null;
    public static ServerModel serverModelLastBrowsedFiles = null;
    public static ServerModel serverModelWantToCopyFrom = null;
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
