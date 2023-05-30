package com.example.servermonitor.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.servermonitor.R;
import com.example.servermonitor.databinding.FragmentEditSshKeyBinding;
import com.example.servermonitor.model.SshKeyModel;

public class EditSshKeyFragment extends Fragment {
    private FragmentEditSshKeyBinding binding;

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
        return binding.getRoot();
    }

    public Bundle getResultBundle() {
        String keyName = binding.etSshKeyName.getText().toString();
        String keyData = binding.etKeyData.getText().toString();
        Bundle bundle = new Bundle();
        SshKeyModel model = new SshKeyModel(keyName, keyData);
        bundle.putParcelable("sshKeyModel", model);
        return bundle;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnApply.setOnClickListener(v -> {
            NavController controller = Navigation.findNavController(binding.getRoot());
            Bundle args = getResultBundle();
            args.putInt("success", 1);
            controller.navigate(R.id.sshKeysFragment, args);
        });
        binding.btnCancel.setOnClickListener(v -> {
            NavController controller = Navigation.findNavController(binding.getRoot());
            Bundle args = new Bundle();
            args.putInt("success", 0);
            controller.navigate(R.id.sshKeysFragment, args);
        });
    }
}