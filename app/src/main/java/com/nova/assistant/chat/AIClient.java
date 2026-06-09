package com.nova.assistant.chat;

import com.nova.assistant.utils.PrefsManager;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AIClient {

    public interface AICallback {
        void onSuccess(String response);
        void onError(String error);
    }

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build();

    private static final String[] MOCK_RESPONSES = {
        "I'm NOVA, your AI assistant. I'm currently running in offline mode. Please add your API key in Settings to enable full AI chat.",
        "That's an interesting question! Once you configure your API key in Settings, I'll be able to give you a proper answer.",
        "I'd love to help with that! Set up your AI API key in Settings and I'll be fully operational.",
        "NOVA here — I'm ready to assist. Add your API key in Settings → AI Chat API to unlock my full capabilities.",
    };
    private static int mockIndex = 0;

    public static void ask(String userMessage, List<ChatMessage> history, AICallback callback) {
        String apiKey = PrefsManager.getApiKey();
        String endpoint = PrefsManager.getApiEndpoint();

        if (apiKey == null || apiKey.isEmpty()) {
            // Return mock response
            String mock = MOCK_RESPONSES[mockIndex % MOCK_RESPONSES.length];
            mockIndex++;
            callback.onSuccess(mock);
            return;
        }

        try {
            // Build messages array
            JSONArray messages = new JSONArray();

            // System prompt
            JSONObject system = new JSONObject();
            system.put("role", "system");
            system.put("content", buildSystemPrompt());
            messages.put(system);

            // History (last 10 messages for context)
            int start = Math.max(0, history.size() - 10);
            for (int i = start; i < history.size(); i++) {
                ChatMessage msg = history.get(i);
                JSONObject m = new JSONObject();
                m.put("role", msg.isUser() ? "user" : "assistant");
                m.put("content", msg.getText());
                messages.put(m);
            }

            // Current user message
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.put(userMsg);

            JSONObject body = new JSONObject();
            body.put("model", "llama3-8b-8192"); // Groq default model
            body.put("messages", messages);
            body.put("max_tokens", 500);
            body.put("temperature", 0.7);

            RequestBody requestBody = RequestBody.create(body.toString(), JSON);
            Request request = new Request.Builder()
                .url(endpoint)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    callback.onError("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        callback.onError("API error: " + response.code());
                        return;
                    }
                    try {
                        String responseStr = response.body().string();
                        JSONObject json = new JSONObject(responseStr);
                        String text = json.getJSONArray("choices")
                                         .getJSONObject(0)
                                         .getJSONObject("message")
                                         .getString("content");
                        callback.onSuccess(text.trim());
                    } catch (Exception e) {
                        callback.onError("Parse error: " + e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            callback.onError("Request error: " + e.getMessage());
        }
    }

    private static String buildSystemPrompt() {
        String name = PrefsManager.getCallName();
        return "You are NOVA, a smart, friendly, and concise AI voice assistant. " +
               "The user's name is " + (name.isEmpty() ? "there" : name) + ". " +
               "Keep responses short and clear. Be helpful, direct, and warm. " +
               "You can help with questions, advice, and general knowledge.";
    }
}
