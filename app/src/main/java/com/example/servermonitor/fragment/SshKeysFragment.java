package com.example.servermonitor.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.R;
import com.example.servermonitor.adapter.SshKeysAdapter;
import com.example.servermonitor.databinding.FragmentSshKeysBinding;
import com.example.servermonitor.model.SshKeyModel;
import com.example.servermonitor.service.SshKeyService;

import java.util.ArrayList;

public class SshKeysFragment extends Fragment {
    private FragmentSshKeysBinding binding;
    private SshKeysAdapter adapter;
    private MainActivity activity;
    private Context context;
    private SshKeyService sshKeyService;
    private ArrayList<SshKeyModel> sshKeys;
    public SshKeysFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSshKeysBinding.inflate(inflater, container, false);
        activity = (MainActivity) getActivity();
        context = activity.getApplicationContext();
        sshKeyService = new SshKeyService(MainActivity.database);
        setupUiComponents();
        setupOnClickListeners();
        new Thread(() -> {
            sshKeys = sshKeyService.getAllSshKeys();
            adapter = new SshKeysAdapter(context, sshKeys, activity);
        }).start();
        Bundle args = getArguments();
        if (args != null) {
            if (args.getInt("success") == 1) {
                SshKeyModel sshKey = args.getParcelable("sshKeyModel");
                addNewSshKey(sshKey);
            }
            getArguments().clear();
        }
        Toast.makeText(context, "oncreateview", Toast.LENGTH_SHORT).show();
        return binding.getRoot();
    }
    public void addNewSshKey(SshKeyModel sshKey) {
        new Thread(() -> {
            sshKeyService.addSshKey(sshKey);
            sshKeys.add(sshKey);
            activity.runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.rvSshKeys.setAdapter(adapter);
        registerForContextMenu(binding.rvSshKeys);
    }

    public void setupUiComponents() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        binding.rvSshKeys.setLayoutManager(layoutManager);
        binding.rvSshKeys.setItemAnimator(new DefaultItemAnimator());
    }

    private void setupOnClickListeners() {
        binding.fabAddSshKey.setOnClickListener((v) -> {
            NavController controller = Navigation.findNavController(binding.getRoot());
            controller.navigate(R.id.editSshKeyFragment);
        });
    }
}