package com.example.servermonitor.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class LocalFileOperations {
    public static File localFileToCopy = null;
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
}
