package com.esb.cosmetiqueshop.ui.messages;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.adapter.MessageAdapter;
import com.esb.cosmetiqueshop.data.repository.MessageRepository;
import com.esb.cosmetiqueshop.databinding.ActivityConversationBinding;
import com.esb.cosmetiqueshop.model.Message;
import com.esb.cosmetiqueshop.utils.SessionManager;
import com.esb.cosmetiqueshop.utils.ToolbarBackHelper;

import java.util.List;

public class ConversationActivity extends AppCompatActivity {

    private static final String EXTRA_CONV_ID = "conv_id";

    private ActivityConversationBinding binding;
    private MessageRepository repo;
    private long convId;
    private long userId;

    public static Intent intent(Context context, long conversationId) {
        Intent i = new Intent(context, ConversationActivity.class);
        i.putExtra(EXTRA_CONV_ID, conversationId);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConversationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ToolbarBackHelper.enableFinish(binding.toolbar, this);
        binding.toolbar.setTitle(R.string.messages_title);

        convId = getIntent().getLongExtra(EXTRA_CONV_ID, -1);
        repo = new MessageRepository(this);
        userId = new SessionManager(this).getUserId();

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        binding.recyclerMessages.setLayoutManager(lm);

        loadMessages();

        binding.btnSend.setOnClickListener(v -> sendMessage());
    }

    private void loadMessages() {
        repo.getMessages(convId, this::showMessages);
    }

    private void showMessages(List<Message> messages) {
        binding.recyclerMessages.setAdapter(new MessageAdapter(messages, userId));
        if (messages != null && !messages.isEmpty()) {
            binding.recyclerMessages.scrollToPosition(messages.size() - 1);
        }
    }

    private void sendMessage() {
        String body = binding.edtMessage.getText() != null
                ? binding.edtMessage.getText().toString().trim() : "";
        if (TextUtils.isEmpty(body)) {
            return;
        }
        binding.btnSend.setEnabled(false);
        repo.send(convId, userId, body, id -> {
            binding.btnSend.setEnabled(true);
            if (id <= 0) {
                Toast.makeText(this, R.string.error_invalid_login, Toast.LENGTH_SHORT).show();
                return;
            }
            binding.edtMessage.setText("");
            loadMessages();
        });
    }
}
