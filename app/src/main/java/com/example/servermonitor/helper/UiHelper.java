package com.example.servermonitor.helper;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.servermonitor.R;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class UiHelper {
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
            if (! verifier.test(result)) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                return;
            }
            actor.accept(result);
        });
    }
    public static void createDirectoryAfterDialog(Context context, Consumer<String> directoryCreator) {
        verifyDialogInputAndAct(context, directoryCreator, LocalFileOperations::verifyFilename, "Directory name", "Directory name is invalid");
    }

    public static void createFileAfterDialog(Context context, Consumer<String> fileCreator) {
        verifyDialogInputAndAct(context, fileCreator, LocalFileOperations::verifyFilename, "File name", "File name is invalid");
    }
}