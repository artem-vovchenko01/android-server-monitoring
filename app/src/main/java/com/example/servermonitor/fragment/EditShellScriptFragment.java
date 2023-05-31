package com.example.servermonitor.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.servermonitor.R;
import com.example.servermonitor.databinding.FragmentEditShellScriptBinding;
import com.example.servermonitor.model.ShellScriptModel;

public class EditShellScriptFragment extends Fragment {
    private FragmentEditShellScriptBinding binding;

    public EditShellScriptFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditShellScriptBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public Bundle getResultBundle() {
        String shellScriptName = binding.etShellScriptName.getText().toString();
        String shellScriptData = binding.etShellScriptData.getText().toString();
        Bundle bundle = new Bundle();
        ShellScriptModel model = new ShellScriptModel(0, shellScriptName, shellScriptData);
        bundle.putParcelable("shellScriptModel", model);
        return bundle;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnApply.setOnClickListener(v -> {
            NavController controller = Navigation.findNavController(binding.getRoot());
            Bundle args = getResultBundle();
            args.putInt("success", 1);
            controller.navigate(R.id.shellScriptsFragment, args);
        });
        binding.btnCancel.setOnClickListener(v -> {
            NavController controller = Navigation.findNavController(binding.getRoot());
            Bundle args = new Bundle();
            args.putInt("success", 0);
            controller.navigate(R.id.shellScriptsFragment, args);
        });
    }
}
