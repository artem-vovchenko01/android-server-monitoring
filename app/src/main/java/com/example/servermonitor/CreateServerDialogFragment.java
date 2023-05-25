package com.example.servermonitor;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class CreateServerDialogFragment extends DialogFragment {
    private EditText etServerName;
    private EditText etHostIp;
    private EditText etPort;
    private EditText etUsername;
    private EditText etPassword;
    private EditText etPrivateKey;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Enter Text");

        // Inflate the layout for the dialog
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_server_dialog, null);
        builder.setView(dialogView);

        // Get reference to the EditText field in the dialog layout
        etServerName = dialogView.findViewById(R.id.etServerName);
        etHostIp = dialogView.findViewById(R.id.etIp);
        etPort = dialogView.findViewById(R.id.etPort);
        etUsername = dialogView.findViewById(R.id.etUsername);
        etPassword = dialogView.findViewById(R.id.etPassword);
        etPrivateKey = dialogView.findViewById(R.id.etPrivateKey);

        // Set positive and negative buttons for the dialog
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle the OK button click
                String serverName = etServerName.getText().toString();
                String hostIp = etHostIp.getText().toString();
                int port = Integer.parseInt(etPort.getText().toString());
                String userName = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                String privateKey = etPrivateKey.getText().toString();

                // Pass the enteredText to the activity
                ((MainActivity) requireActivity()).onDialogResult(serverName, hostIp, port, userName, password, privateKey);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle the Cancel button click
                dismiss(); // Dismiss the dialog
            }
        });

        return builder.create();
    }
}
