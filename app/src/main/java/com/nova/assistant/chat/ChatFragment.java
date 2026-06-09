package com.nova.assistant.chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.nova.assistant.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatFragment extends Fragment {

    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend, btnMic;
    private TextView btnClear;
    private ChatAdapter adapter;
    private List<ChatMessage> messages;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<Intent> speechLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getData() != null) {
                List<String> results = result.getData()
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (results != null && !results.isEmpty()) {
                    etMessage.setText(results.get(0));
                }
            }
        });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        rvMessages = view.findViewById(R.id.rv_messages);
        etMessage  = view.findViewById(R.id.et_message);
        btnSend    = view.findViewById(R.id.btn_send);
        btnMic     = view.findViewById(R.id.btn_mic);
        btnClear   = view.findViewById(R.id.btn_clear_chat);

        messages = new ArrayList<>();
        adapter  = new ChatAdapter(messages);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(adapter);

        // Welcome message
        addNovaMessage("Hi " + com.nova.assistant.utils.PrefsManager.getCallName() +
                       "! I'm NOVA. Ask me anything or give me a command.");

        btnSend.setOnClickListener(v -> sendMessage());
        btnMic.setOnClickListener(v -> startVoiceInput());
        btnClear.setOnClickListener(v -> clearChat());

        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });

        return view;
    }

    private void sendMessage() {
        if (etMessage == null) return;
        String text = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        addUserMessage(text);
        etMessage.setText("");

        // Show thinking indicator
        addNovaMessage("…");
        int thinkingIndex = messages.size() - 1;

        AIClient.ask(text, new ArrayList<>(messages), new AIClient.AICallback() {
            @Override
            public void onSuccess(String response) {
                mainHandler.post(() -> {
                    replaceMessage(thinkingIndex, response);
                });
            }
            @Override
            public void onError(String error) {
                mainHandler.post(() -> {
                    replaceMessage(thinkingIndex, "Sorry, I ran into an issue: " + error);
                });
            }
        });
    }

    private void addUserMessage(String text) {
        messages.add(new ChatMessage(text, ChatMessage.ROLE_USER));
        adapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
    }

    private void addNovaMessage(String text) {
        messages.add(new ChatMessage(text, ChatMessage.ROLE_NOVA));
        adapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
    }

    private void replaceMessage(int index, String text) {
        if (index < messages.size()) {
            messages.set(index, new ChatMessage(text, ChatMessage.ROLE_NOVA));
            adapter.notifyItemChanged(index);
            scrollToBottom();
        }
    }

    private void scrollToBottom() {
        if (messages.size() > 0) {
            rvMessages.smoothScrollToPosition(messages.size() - 1);
        }
    }

    private void clearChat() {
        messages.clear();
        adapter.notifyDataSetChanged();
        addNovaMessage("Chat cleared. How can I help you?");
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your message…");
        try {
            speechLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Voice input unavailable", Toast.LENGTH_SHORT).show();
        }
    }
}
