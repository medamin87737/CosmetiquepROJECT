package com.esb.cosmetiqueshop.ui.client.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.esb.cosmetiqueshop.data.repository.UserRepository;
import com.esb.cosmetiqueshop.databinding.FragmentProfileBinding;
import com.esb.cosmetiqueshop.ui.NavigationHelper;
import com.esb.cosmetiqueshop.ui.assistant.BeautyAssistantActivity;
import com.esb.cosmetiqueshop.ui.auth.ChangePasswordActivity;
import com.esb.cosmetiqueshop.ui.messages.MessagesActivity;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private UserRepository userRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        userRepository = new UserRepository(requireContext());

        userRepository.getCurrentUser(user -> {
            if (user != null && binding != null) {
                binding.txtEmail.setText(user.getEmail());
                binding.edtFullName.setText(user.getFullName());
                binding.edtPhone.setText(user.getPhone());
            }
        });

        binding.btnSaveProfile.setOnClickListener(v -> userRepository.updateProfile(
                userRepository.getSessionManager().getUserId(),
                text(binding.edtFullName), text(binding.edtPhone),
                new UserRepository.AuthCallback() {
                    @Override
                    public void onSuccess(com.esb.cosmetiqueshop.model.User user) {
                        Toast.makeText(requireContext(), com.esb.cosmetiqueshop.R.string.btn_edit_profile,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String message) {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }
                }));

        binding.btnBeautyAssistant.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), BeautyAssistantActivity.class)));
        binding.btnMessages.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), MessagesActivity.class)));
        binding.btnChangePassword.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ChangePasswordActivity.class)));
        binding.btnLogout.setOnClickListener(v -> {
            userRepository.logout();
            NavigationHelper.openPublic(requireContext());
        });
        return binding.getRoot();
    }

    private static String text(com.google.android.material.textfield.TextInputEditText edt) {
        return edt.getText() != null ? edt.getText().toString() : "";
    }
}