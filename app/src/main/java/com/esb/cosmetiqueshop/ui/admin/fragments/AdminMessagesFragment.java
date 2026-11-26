package com.esb.cosmetiqueshop.ui.admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.adapter.AdminMessageAdapter;
import com.esb.cosmetiqueshop.data.repository.MessageRepository;
import com.esb.cosmetiqueshop.databinding.FragmentAdminListBinding;
import com.esb.cosmetiqueshop.ui.messages.ConversationActivity;

/** Admin — liste des messages sous forme de cartes détaillées (objet, client, aperçu). */
public class AdminMessagesFragment extends Fragment {

    private FragmentAdminListBinding binding;
    private MessageRepository repo;
    private AdminMessageAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminListBinding.inflate(inflater, container, false);
        binding.fabAdd.setVisibility(View.GONE);
        repo = new MessageRepository(requireContext());
        adapter = new AdminMessageAdapter(c ->
                startActivity(ConversationActivity.intent(requireContext(), c.getId())));
        binding.recyclerAdmin.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerAdmin.setAdapter(adapter);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        if (binding == null || repo == null) {
            return;
        }
        repo.listAllAdmin(conversations -> {
            if (binding == null || !isAdded()) {
                return;
            }
            adapter.submit(conversations);
            boolean empty = conversations == null || conversations.isEmpty();
            binding.txtEmptyAdmin.setVisibility(empty ? View.VISIBLE : View.GONE);
            if (empty) {
                binding.txtEmptyAdmin.setText(R.string.messages_empty);
            }
            binding.recyclerAdmin.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
