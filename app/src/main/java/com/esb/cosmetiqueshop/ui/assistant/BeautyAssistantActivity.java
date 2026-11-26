package com.esb.cosmetiqueshop.ui.assistant;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
 * Chat assistant beauté : réponses Groq affichées lettre par lettre.
 */
public class BeautyAssistantActivity extends AppCompatActivity {

    private static final long TYPE_DELAY_MS = 18L;

    private ActivityBeautyAssistantBinding binding;
    private BeautyAiRepository repository;
    private BeautyChatAdapter adapter;
    private final Handler typeHandler = new Handler(Looper.getMainLooper());
    private Runnable typeRunnable;
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

        // Accueil avec effet typewriter
        revealLetterByLetter(getString(R.string.beauty_assistant_welcome), false);

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
        binding.txtTypingStatus.setVisibility(View.VISIBLE);

        repository.ask(q, new BeautyAiRepository.AdviceCallback() {
            @Override
            public void onSuccess(String advice) {
                binding.txtTypingStatus.setVisibility(View.GONE);
                revealLetterByLetter(advice, true);
            }

            @Override
            public void onFailure(String message) {
                binding.txtTypingStatus.setVisibility(View.GONE);
                revealLetterByLetter(message, true);
            }
        });
    }

    /** Affiche la réponse IA caractère par caractère. */
    private void revealLetterByLetter(String fullText, boolean unlockWhenDone) {
        cancelTyping();
        if (fullText == null) {
            fullText = "";
        }
        final String text = fullText;
        adapter.add(new BeautyChatMessage(false, "", DateUtils.now()));
        scrollToEnd();
        setBusy(true);

        typeRunnable = new Runnable() {
            private int index = 0;

            @Override
            public void run() {
                if (isFinishing() || binding == null) {
                    return;
                }
                if (index <= text.length()) {
                    adapter.updateLastText(text.substring(0, index));
                    scrollToEnd();
                    index++;
                    if (index <= text.length()) {
                        typeHandler.postDelayed(this, TYPE_DELAY_MS);
                    } else if (unlockWhenDone) {
                        setBusy(false);
                    } else {
                        setBusy(false);
                    }
                }
            }
        };
        typeHandler.post(typeRunnable);
    }

    private void cancelTyping() {
        if (typeRunnable != null) {
            typeHandler.removeCallbacks(typeRunnable);
            typeRunnable = null;
        }
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

    @Override
    protected void onDestroy() {
        cancelTyping();
        super.onDestroy();
    }
}
