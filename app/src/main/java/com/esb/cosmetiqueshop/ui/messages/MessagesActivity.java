package com.esb.cosmetiqueshop.ui.messages;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.adapter.ConversationAdapter;
import com.esb.cosmetiqueshop.data.repository.MessageRepository;
import com.esb.cosmetiqueshop.databinding.ActivityMessagesBinding;
import com.esb.cosmetiqueshop.databinding.DialogNewMessageBinding;
import com.esb.cosmetiqueshop.utils.SessionManager;
import com.esb.cosmetiqueshop.utils.ToolbarBackHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MessagesActivity extends AppCompatActivity {

    private ActivityMessagesBinding binding;
    private MessageRepository repo;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessagesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ToolbarBackHelper.enableFinish(binding.toolbar, this);

        repo = new MessageRepository(this);
        userId = new SessionManager(this).getUserId();

        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.btnNewMessage.setOnClickListener(v -> openNewConversationDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadConversations();
    }

    private void loadConversations() {
        repo.listForClient(userId, conversations -> {
            binding.recycler.setAdapter(new ConversationAdapter(conversations, c ->
                    startActivity(ConversationActivity.intent(this, c.getId()))));
            boolean empty = conversations == null || conversations.isEmpty();
            binding.stateEmpty.getRoot().setVisibility(empty ? View.VISIBLE : View.GONE);
            if (empty) {
                binding.stateEmpty.txtEmpty.setText(R.string.messages_empty);
                binding.stateEmpty.txtEmptyHint.setText(R.string.new_message_hint);
            }
            binding.recycler.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
    }

    private void openNewConversationDialog() {
        DialogNewMessageBinding dialogBinding = DialogNewMessageBinding.inflate(getLayoutInflater());
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogBinding.getRoot())
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogBinding.btnDialogCancel.setOnClickListener(v -> dialog.dismiss());
        dialogBinding.btnDialogSend.setOnClickListener(v -> {
            String subject = dialogBinding.edtSubject.getText() != null
                    ? dialogBinding.edtSubject.getText().toString().trim() : "";
            if (TextUtils.isEmpty(subject)) {
                dialogBinding.edtSubject.setError(getString(R.string.subject_hint));
                return;
            }
            dialogBinding.btnDialogSend.setEnabled(false);
            repo.startConversation(userId, null, subject, id -> {
                dialogBinding.btnDialogSend.setEnabled(true);
                if (id <= 0) {
                    Toast.makeText(this, R.string.error_invalid_login, Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                startActivity(ConversationActivity.intent(this, id));
            });
        });
        dialog.show();
    }
}
