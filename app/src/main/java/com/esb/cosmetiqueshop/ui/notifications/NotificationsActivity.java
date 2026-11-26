package com.esb.cosmetiqueshop.ui.notifications;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.adapter.NotificationAdapter;
import com.esb.cosmetiqueshop.data.repository.NotificationRepository;
import com.esb.cosmetiqueshop.databinding.ActivitySimpleListBinding;
import com.esb.cosmetiqueshop.utils.SessionManager;
import com.esb.cosmetiqueshop.utils.ToolbarBackHelper;

public class NotificationsActivity extends AppCompatActivity {

    private ActivitySimpleListBinding binding;
    private NotificationRepository repo;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySimpleListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.toolbar.setTitle(R.string.notifications_title);
        ToolbarBackHelper.enableFinish(binding.toolbar, this);

        userId = new SessionManager(this).getUserId();
        repo = new NotificationRepository(this);
        load();
    }

    private void load() {
        repo.loadForUser(userId, items -> {
            if (items == null || items.isEmpty()) {
                binding.stateEmpty.getRoot().setVisibility(View.VISIBLE);
                binding.stateEmpty.txtEmpty.setText(R.string.notifications_empty);
                binding.recycler.setAdapter(null);
                return;
            }
            binding.stateEmpty.getRoot().setVisibility(View.GONE);
            binding.recycler.setAdapter(new NotificationAdapter(items, item ->
                    repo.markRead(item.getId(), () -> {
                    })));
            // Ouvrir la liste = marquer tout comme lu (badge cloche mis à jour au retour)
            repo.markAllRead(userId, null);
        });
    }
}
