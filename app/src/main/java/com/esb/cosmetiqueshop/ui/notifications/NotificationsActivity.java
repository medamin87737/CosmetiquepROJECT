package com.esb.cosmetiqueshop.ui.notifications;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.esb.cosmetiqueshop.adapter.NotificationAdapter;
import com.esb.cosmetiqueshop.data.repository.NotificationRepository;
import com.esb.cosmetiqueshop.databinding.ActivitySimpleListBinding;
import com.esb.cosmetiqueshop.utils.SessionManager;

public class NotificationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySimpleListBinding binding = ActivitySimpleListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.toolbar.setTitle(com.esb.cosmetiqueshop.R.string.notifications_title);
        com.esb.cosmetiqueshop.utils.ToolbarBackHelper.enableFinish(binding.toolbar, this);

        long userId = new SessionManager(this).getUserId();
        NotificationRepository repo = new NotificationRepository(this);
        repo.loadForUser(userId, items -> binding.recycler.setAdapter(
                new NotificationAdapter(items, item -> repo.markRead(item.getId(), () -> {
                }))));
    }
}