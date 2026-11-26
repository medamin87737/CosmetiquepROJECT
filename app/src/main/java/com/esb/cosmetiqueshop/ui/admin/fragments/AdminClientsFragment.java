package com.esb.cosmetiqueshop.ui.admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.adapter.AdminUserAdapter;
import com.esb.cosmetiqueshop.data.repository.UserRepository;
import com.esb.cosmetiqueshop.databinding.FragmentAdminListBinding;
import com.esb.cosmetiqueshop.model.User;
import com.esb.cosmetiqueshop.ui.admin.AdminUserEditActivity;

/**
 * CRUD utilisateurs : lister, créer, modifier, désactiver.
 */
public class AdminClientsFragment extends Fragment {

    private UserRepository userRepository;
    private FragmentAdminListBinding binding;
    private AdminUserAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminListBinding.inflate(inflater, container, false);
        userRepository = new UserRepository(requireContext());
        binding.recyclerAdmin.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AdminUserAdapter(new AdminUserAdapter.Listener() {
            @Override
            public void onEdit(User user) {
                startActivity(AdminUserEditActivity.intent(requireContext(), user.getId()));
            }

            @Override
            public void onToggleActive(User user) {
                userRepository.setUserActive(user.getId(), !user.isActive(),
                        new UserRepository.AuthCallback() {
                            @Override
                            public void onSuccess(User updated) {
                                Toast.makeText(requireContext(),
                                        updated.isActive() ? R.string.btn_activate : R.string.btn_deactivate,
                                        Toast.LENGTH_SHORT).show();
                                load();
                            }

                            @Override
                            public void onFailure(String message) {
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        binding.recyclerAdmin.setAdapter(adapter);
        binding.fabAdd.setVisibility(View.VISIBLE);
        binding.fabAdd.setOnClickListener(v ->
                startActivity(AdminUserEditActivity.intent(requireContext(), -1)));
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        userRepository.listAllUsers(users -> {
            if (binding == null) {
                return;
            }
            adapter.submit(users);
            boolean empty = users == null || users.isEmpty();
            binding.txtEmptyAdmin.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.recyclerAdmin.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
