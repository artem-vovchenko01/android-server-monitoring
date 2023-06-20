package com.example.servermonitor.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.R;
import com.example.servermonitor.databinding.FragmentEditServerBinding;
import com.example.servermonitor.model.ServerModel;
import com.example.servermonitor.model.SshKeyModel;
import com.example.servermonitor.service.SshKeyService;

import java.util.ArrayList;
import java.util.Optional;

public class EditServerFragment extends Fragment {
    private FragmentEditServerBinding binding;
    private ArrayList<SshKeyModel> sshKeys;
    private SshKeyService sshKeyService;
    private MainActivity activity;
    private ServerModel serverModel;
    private Context context;
    private ArrayAdapter<SshKeyModel> sshKeysAdapter;

    public EditServerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       binding = FragmentEditServerBinding.inflate(inflater, container, false);
        activity = (MainActivity) getActivity();
        sshKeyService = new SshKeyService(MainActivity.database);
        serverModel = new ServerModel();
        context = activity.getApplicationContext();
       Bundle args = getArguments();
       setupListeners();
       fetchData(args);
        activity.getSupportActionBar().setTitle(R.string.fragment_edit_server_create_title);
       return binding.getRoot();
    }
    private void setupListeners() {
        binding.spinPrivateKey.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                serverModel.setPrivateKeyId(sshKeys.get(position).getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                serverModel.setPrivateKeyId(0);
            }
        });
    }
    private void fetchData(Bundle args) {
        new Thread(() -> {
            sshKeys = sshKeyService.getAllSshKeys();
            sshKeysAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, sshKeys);
            sshKeysAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinPrivateKey.setAdapter(sshKeysAdapter);
            activity.runOnUiThread(() -> {
                if (args != null) {
                    if (args.getInt("edit") == 1) {
                        fillDataOfExistingServer(args.getParcelable("serverModel"));
                        activity.getSupportActionBar().setTitle(getString(R.string.fragment_edit_server_edit_title) + " " + serverModel.getName());
                    }
                    args.clear();
                }
            });
        }).start();
    }

    public Bundle getResultBundle() {
        String serverName = binding.etServerName.getText().toString();
        String hostIp = binding.etIp.getText().toString();
        int port = Integer.parseInt(binding.etPort.getText().toString());
        String userName = binding.etUsername.getText().toString();
        String password = binding.etPassword.getText().toString();
        int privateKeyId = serverModel.getPrivateKeyId();
        Bundle bundle = new Bundle();
        ServerModel serverModel = new ServerModel(this.serverModel.getId(), serverName, hostIp, port, userName, password, privateKeyId, false, 0, 0, 0, 0, 0, 0);
        bundle.putParcelable("serverModel", serverModel);
        return bundle;
    }
    private void fillDataOfExistingServer(ServerModel serverModel) {
        this.serverModel  = serverModel;
       binding.etServerName.setText(serverModel.getName());
       binding.etIp.setText(serverModel.getHostIp());
       binding.etPort.setText("" + serverModel.getPort());
       binding.etUsername.setText(serverModel.getUserName());
       binding.etPassword.setText(serverModel.getPassword());
       Optional<SshKeyModel> sshKeyMaybe = sshKeys.stream().filter(k -> k.getId() == serverModel.getPrivateKeyId()).findFirst();
        sshKeyMaybe.ifPresent(sshKeyModel -> {
            int keyId = sshKeyModel.getId();
            sshKeys.stream().filter(k -> k.getId() == keyId).findFirst().ifPresent(k -> {
                binding.spinPrivateKey.setSelection(sshKeys.indexOf(k));
            });
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnApply.setOnClickListener(v -> {
            NavController controller = Navigation.findNavController(binding.getRoot());
            Bundle args = getResultBundle();
            args.putInt("success", 1);
            controller.navigate(R.id.serversFragment, args);
        });
        binding.btnCancel.setOnClickListener(v -> {
            NavController controller = Navigation.findNavController(binding.getRoot());
            Bundle args = new Bundle();
            args.putInt("success", 0);
            controller.navigate(R.id.serversFragment, args);
        });
    }
}