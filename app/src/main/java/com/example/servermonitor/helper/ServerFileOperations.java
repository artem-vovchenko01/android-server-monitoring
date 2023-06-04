package com.example.servermonitor.helper;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ServerFileOperations {
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
}
