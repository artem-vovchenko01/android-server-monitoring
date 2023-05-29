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
import android.widget.Button;

import com.example.servermonitor.R;
import com.example.servermonitor.databinding.FragmentEditServerBinding;
import com.example.servermonitor.model.ServerModel;

public class EditServerFragment extends Fragment {
    private FragmentEditServerBinding binding;

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
       return binding.getRoot();
    }

    public Bundle getResultBundle() {
        String serverName = binding.etServerName.getText().toString();
        String hostIp = binding.etIp.getText().toString();
        int port = Integer.parseInt(binding.etPort.getText().toString());
        String userName = binding.etUsername.getText().toString();
        String password = binding.etPassword.getText().toString();
        String privateKey = binding.etPrivateKey.getText().toString();
        Bundle bundle = new Bundle();
        ServerModel serverModel = new ServerModel(0, serverName, hostIp, port, userName, password, privateKey, false, 0, 0, 0, 0, 0, 0);
        bundle.putParcelable("serverModel", serverModel);
        return bundle;
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