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

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.R;
import com.example.servermonitor.databinding.FragmentEditSshKeyBinding;
import com.example.servermonitor.model.SshKeyModel;

public class EditSshKeyFragment extends Fragment {
    private FragmentEditSshKeyBinding binding;
    private SshKeyModel sshKeyModel;
    private MainActivity activity;
    private Context context;

    public EditSshKeyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditSshKeyBinding.inflate(inflater, container, false);
        sshKeyModel = new SshKeyModel();
        activity = (MainActivity) getActivity();
        activity.getSupportActionBar().setTitle(R.string.fragment_edit_ssh_key_create_title);
        context = activity.getApplicationContext();
        Bundle args = getArguments();
        fetchData(args);
        return binding.getRoot();
    }

    public Bundle getResultBundle() {
        String keyName = binding.etSshKeyName.getText().toString();
        String keyData = binding.etKeyData.getText().toString();
        Bundle bundle = new Bundle();
        SshKeyModel model = new SshKeyModel(sshKeyModel.getId(), keyName, keyData);
        bundle.putParcelable("sshKeyModel", model);
        return bundle;
    }
    private void fillDataOfExistingSshKey(SshKeyModel sshKeyModel) {
        this.sshKeyModel = sshKeyModel;
        binding.etSshKeyName.setText(sshKeyModel.getName());
        binding.etKeyData.setText(sshKeyModel.getKeyData());
    }
    private void fetchData(Bundle args) {
        new Thread(() -> {
            activity.runOnUiThread(() -> {
                if (args != null) {
                    if (args.getInt("edit") == 1) {
                        fillDataOfExistingSshKey(args.getParcelable("sshKeyModel"));
                        activity.getSupportActionBar().setTitle(getString(R.string.fragment_edit_ssh_key_edit_title) + " " + sshKeyModel.getName());
                    }
                    args.clear();
                }
            });
        }).start();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnApply.setOnClickListener(v -> {
            NavController controller = Navigation.findNavController(binding.getRoot());
            Bundle args = getResultBundle();
            args.putInt("success", 1);
            controller.navigate(R.id.action_editSshKeyFragment_to_sshKeysFragment, args);
        });
        binding.btnCancel.setOnClickListener(v -> {
            NavController controller = Navigation.findNavController(binding.getRoot());
            Bundle args = new Bundle();
            args.putInt("success", 0);
            controller.navigate(R.id.action_editSshKeyFragment_to_sshKeysFragment, args);
        });
    }
}