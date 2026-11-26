package com.esb.cosmetiqueshop.ui.assistant;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.adapter.BeautyChatAdapter;
import com.esb.cosmetiqueshop.data.repository.BeautyAiRepository;
import com.esb.cosmetiqueshop.databinding.ActivityBeautyAssistantBinding;
import com.esb.cosmetiqueshop.model.BeautyChatMessage;
import com.esb.cosmetiqueshop.utils.DateUtils;
import com.esb.cosmetiqueshop.utils.ToolbarBackHelper;

/**
 * Chat assistant beauté : conseille uniquement les produits du catalogue.
 */
public class BeautyAssistantActivity extends AppCompatActivity {

    private ActivityBeautyAssistantBinding binding;
    private BeautyAiRepository repository;
    private BeautyChatAdapter adapter;
    private boolean busy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBeautyAssistantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ToolbarBackHelper.enableFinish(binding.toolbar, this);

        repository = new BeautyAiRepository(this);
        adapter = new BeautyChatAdapter();
        binding.recyclerChat.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerChat.setAdapter(adapter);

        if (!repository.hasApiKey()) {
            binding.txtAssistantHint.setText(R.string.beauty_assistant_hint_no_key);
        }

        // Message d’accueil
        adapter.add(new BeautyChatMessage(false,
                getString(R.string.beauty_assistant_welcome), DateUtils.now()));

        binding.btnSend.setOnClickListener(v -> sendQuestion());
    }

    private void sendQuestion() {
        if (busy) {
            return;
        }
        String q = binding.edtQuestion.getText() != null
                ? binding.edtQuestion.getText().toString().trim() : "";
        if (TextUtils.isEmpty(q)) {
            Toast.makeText(this, R.string.beauty_assistant_input_hint, Toast.LENGTH_SHORT).show();
            return;
        }
        binding.edtQuestion.setText("");
        adapter.add(new BeautyChatMessage(true, q, DateUtils.now()));
        scrollToEnd();
        setBusy(true);

        repository.ask(q, new BeautyAiRepository.AdviceCallback() {
            @Override
            public void onSuccess(String advice) {
                setBusy(false);
                adapter.add(new BeautyChatMessage(false, advice, DateUtils.now()));
                scrollToEnd();
            }

            @Override
            public void onFailure(String message) {
                setBusy(false);
                adapter.add(new BeautyChatMessage(false, message, DateUtils.now()));
                scrollToEnd();
            }
        });
    }

    private void setBusy(boolean value) {
        busy = value;
        binding.btnSend.setEnabled(!value);
        binding.edtQuestion.setEnabled(!value);
    }

    private void scrollToEnd() {
        binding.recyclerChat.post(() -> {
            int n = adapter.getItemCount();
            if (n > 0) {
                binding.recyclerChat.smoothScrollToPosition(n - 1);
            }
        });
    }
}
