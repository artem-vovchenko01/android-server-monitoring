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

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
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
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (MainActivity) getActivity();
        activity.getSupportActionBar().setTitle(R.string.fragment_ssh_keys_title);
    }
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
            binding.rvSshKeys.setAdapter(adapter);
        }).start();
        Bundle args = getArguments();
        if (args != null) {
            if (args.getInt("success") == 1) {
                SshKeyModel sshKey = args.getParcelable("sshKeyModel");
                addNewSshKey(sshKey);
            }
            getArguments().clear();
        }
        return binding.getRoot();
    }
    public void addNewSshKey(SshKeyModel sshKey) {
        new Thread(() -> {
            if (sshKey.getId() == 0) {
                int id = (int) sshKeyService.addSshKey(sshKey);
                sshKey.setId(id);
                sshKeys.add(sshKey);
            } else {
                sshKeyService.updateSshKey(sshKey);
                int pos = sshKeys.indexOf(sshKeys.stream().filter(k -> k.getId() == sshKey.getId()).findFirst().get());
                sshKeys.set(pos, sshKey);
            }
            activity.runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int pposition = adapter.selectedItemPosition;

        switch (item.getTitle().toString()) {
            case "Edit":
                NavController controller = Navigation.findNavController(binding.getRoot());
                Bundle bundle = new Bundle();
                bundle.putInt("edit", 1);
                bundle.putParcelable("sshKeyModel", sshKeys.get(pposition));
                controller.navigate(R.id.action_sshKeysFragment_to_editSshKeyFragment, bundle);
                break;
            case "Delete":
                new Thread(() -> {
                    sshKeyService.deleteSshKey(sshKeys.get(pposition));
                    sshKeys.remove(pposition);
                    activity.runOnUiThread(() -> adapter.notifyItemRemoved(pposition));
                }).start();
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = activity.getMenuInflater();
        inflater.inflate(R.menu.ssh_key_context_menu, menu);
    }
}