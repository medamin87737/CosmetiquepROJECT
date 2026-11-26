package com.esb.cosmetiqueshop.data.repository;

import android.content.Context;
import android.text.TextUtils;

import com.esb.cosmetiqueshop.BuildConfig;
import com.esb.cosmetiqueshop.assistant.BeautyAiClient;
import com.esb.cosmetiqueshop.assistant.BeautyAiPromptBuilder;
import com.esb.cosmetiqueshop.data.dao.ProductDao;
import com.esb.cosmetiqueshop.data.db.DatabaseHelper;
import com.esb.cosmetiqueshop.model.Product;
import com.esb.cosmetiqueshop.utils.AppExecutors;

import java.util.List;

/**
 * Assistant beauté : catalogue app + API Groq uniquement (pas de réponse locale).
 * Clé : https://console.groq.com/keys
 */
public class BeautyAiRepository {

    public interface AdviceCallback {
        void onSuccess(String advice);

        void onFailure(String message);
    }

    private final ProductDao productDao;
    private final AppExecutors executors;

    public BeautyAiRepository(Context context) {
        productDao = new ProductDao(new DatabaseHelper(context));
        executors = AppExecutors.getInstance();
    }

    public void ask(String userQuestion, AdviceCallback callback) {
        executors.diskIO().execute(() -> {
            try {
                if (TextUtils.isEmpty(userQuestion) || userQuestion.trim().length() < 3) {
                    postFailure(callback, "Décrivez votre besoin en quelques mots.");
                    return;
                }
                String apiKey = BuildConfig.GROQ_API_KEY;
                if (TextUtils.isEmpty(apiKey)) {
                    postFailure(callback,
                            "Clé Groq manquante. Ajoutez GROQ_API_KEY dans local.properties "
                                    + "(https://console.groq.com/keys) puis rebuild.");
                    return;
                }
                List<Product> products = productDao.findAllActive();
                String catalog = BeautyAiPromptBuilder.catalogBlock(products);
                String system = BeautyAiPromptBuilder.systemPrompt();
                String user = BeautyAiPromptBuilder.userPrompt(catalog, userQuestion);
                String answer = BeautyAiClient.ask(apiKey, system, user);
                postSuccess(callback, answer);
            } catch (Exception e) {
                String msg = e.getMessage() != null ? e.getMessage() : "Conseil IA indisponible.";
                postFailure(callback, msg);
            }
        });
    }

    public boolean hasApiKey() {
        return !TextUtils.isEmpty(BuildConfig.GROQ_API_KEY);
    }

    private void postSuccess(AdviceCallback callback, String advice) {
        executors.mainThread().execute(() -> callback.onSuccess(advice));
    }

    private void postFailure(AdviceCallback callback, String message) {
        executors.mainThread().execute(() -> callback.onFailure(message));
    }
}
