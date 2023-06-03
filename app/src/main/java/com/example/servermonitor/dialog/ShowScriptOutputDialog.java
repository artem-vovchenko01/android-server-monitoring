package com.example.servermonitor.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.servermonitor.R;

public class ShowScriptOutputDialog extends DialogFragment {
    private TextView tvScriptOutput;
    private Button btnClose;
    private Button btnCopy;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.script_output_layout, container, false);
        tvScriptOutput = rootView.findViewById(R.id.tvOutput);
        btnClose = rootView.findViewById(R.id.btnClose);
        btnCopy = rootView.findViewById(R.id.btnCopy);
        setupListeners();
        return rootView;
    }
    public void setOutputText(String text) {
        new Thread(() -> {
            while (tvScriptOutput == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            getActivity().runOnUiThread(() -> {
                tvScriptOutput.setText(text);
            });
        }).start();
    }

    private void setupListeners() {
        btnClose.setOnClickListener((v) -> {
           dismiss();
        });
        btnCopy.setOnClickListener((v) -> {

        });
    }
}
