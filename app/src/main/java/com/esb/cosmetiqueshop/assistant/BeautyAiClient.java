package com.esb.cosmetiqueshop.assistant;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Client HTTP Groq (API gratuite).
 * Clé : https://console.groq.com/keys
 */
public final class BeautyAiClient {

    private static final String ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";
    /** Modèle gratuit Groq (rapide). */
    private static final String MODEL = "llama-3.1-8b-instant";

    private BeautyAiClient() {
    }

    public static String ask(String apiKey, String systemPrompt, String userPrompt) throws Exception {
        if (TextUtils.isEmpty(apiKey)) {
            throw new IllegalStateException(
                    "Clé API Groq manquante (local.properties → GROQ_API_KEY). "
                            + "Créez-la sur https://console.groq.com/keys");
        }

        JSONObject root = new JSONObject();
        root.put("model", MODEL);
        root.put("temperature", 0.4);
        root.put("max_tokens", 512);

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
                .put("role", "system")
                .put("content", systemPrompt));
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", userPrompt));
        root.put("messages", messages);

        byte[] body = root.toString().getBytes(StandardCharsets.UTF_8);
        HttpURLConnection conn = (HttpURLConnection) new URL(ENDPOINT).openConnection();
        conn.setConnectTimeout(20000);
        conn.setReadTimeout(40000);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body);
        }

        int code = conn.getResponseCode();
        InputStream stream = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
        String response = readAll(stream);
        if (code < 200 || code >= 300) {
            throw new IllegalStateException("Erreur Groq (" + code + ") : " + shorten(response));
        }
        return extractText(response);
    }

    private static String extractText(String json) throws Exception {
        JSONObject root = new JSONObject(json);
        JSONArray choices = root.optJSONArray("choices");
        if (choices == null || choices.length() == 0) {
            throw new IllegalStateException("Réponse Groq vide.");
        }
        JSONObject message = choices.getJSONObject(0).optJSONObject("message");
        if (message == null) {
            throw new IllegalStateException("Réponse Groq invalide.");
        }
        String content = message.optString("content", "").trim();
        if (content.isEmpty()) {
            throw new IllegalStateException("Réponse Groq vide.");
        }
        return content;
    }

    private static String readAll(InputStream stream) throws Exception {
        if (stream == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private static String shorten(String s) {
        if (s == null) {
            return "";
        }
        return s.length() > 180 ? s.substring(0, 180) + "…" : s;
    }
}
