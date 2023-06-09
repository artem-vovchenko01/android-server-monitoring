package com.example.servermonitor.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.R;
import com.example.servermonitor.model.ServerModel;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class UiHelper {
    private static final int REQUEST_CODE_CREATE_DOCUMENT = 25;
    private static final int REQUEST_CODE_OPEN_DOCUMENT = 1;
    public static void actOnStringInputFromDialog(Context context, View dialogView, String title, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView)
                .setTitle(title)
                .setPositiveButton("OK", listener)
                .setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void verifyDialogInputAndAct(Context context, Consumer<String> actor, Predicate<String> verifier, String dialogTitle, String errorMessage) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.single_string_dialog_layout, null);
        actOnStringInputFromDialog(context, dialogView, dialogTitle, (dialog, which) -> {
            EditText inputField = dialogView.findViewById(R.id.editText);
            String result = inputField.getText().toString();
            if (!verifier.test(result)) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                return;
            }
            actor.accept(result);
        });
    }

    public static void createDirectoryAfterDialog(Context context, Consumer<String> directoryCreator) {
        verifyDialogInputAndAct(context, directoryCreator, FileOperations::verifyFilename, "Directory name", "Directory name is invalid");
    }

    public static void createFileAfterDialog(Context context, Consumer<String> fileCreator) {
        verifyDialogInputAndAct(context, fileCreator, FileOperations::verifyFilename, "File name", "File name is invalid");
    }

    public static Handler showProgressDialog(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Loading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.setProgress(0);
        progressDialog.setCancelable(false);
        progressDialog.show();

        @SuppressLint("HandlerLeak") final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                int newProgress = (int)Double.parseDouble(msg.obj.toString());
                progressDialog.setProgress(newProgress);
                if (newProgress >= progressDialog.getMax()) {
                    progressDialog.dismiss();
                }
            }
        };
        return handler;
    }
    public static Boolean serverStillExists(MainActivity activity, ServerModel server) {
        if (server == null) return true;
        return activity.serverModels.stream().anyMatch(s -> s.getId() == server.getId());
    }

    public static void monitorProgress(Context context, Activity activity, FileLoadingProgressMonitor monitor, Supplier function) {
        activity.runOnUiThread(() -> {
            Handler progressHandler = UiHelper.showProgressDialog(context);
            new Thread(() -> {
                while (true) {
                    Message msg = Message.obtain();
                    msg.obj = monitor.getProgressPercents();
                    msg.setTarget(progressHandler);
                    msg.sendToTarget();
                    if (monitor.getProgress() == 1) break;
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                function.get();
            }).start();
        });
    }

    public static void createFilePicker(Fragment fragment, String type, String fileName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(type);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        fragment.startActivityForResult(intent, REQUEST_CODE_CREATE_DOCUMENT);
    }
    public static void openFilePicker(Fragment fragment) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        fragment.startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT);
    }

    public static void displayError(Activity activity, String text) {
        activity.runOnUiThread(() -> {
            Toast.makeText(activity.getApplicationContext(), text, Toast.LENGTH_LONG).show();
        });
    }
}