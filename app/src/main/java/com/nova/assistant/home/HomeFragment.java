package com.nova.assistant.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.nova.assistant.R;
import com.nova.assistant.utils.PrefsManager;
import com.nova.assistant.voice.VoiceService;
import java.util.Calendar;

public class HomeFragment extends Fragment {

    public static final String ACTION_ORB_STATE  = "com.nova.assistant.ORB_STATE";
    public static final String ACTION_TRANSCRIPT = "com.nova.assistant.TRANSCRIPT";
    public static final String EXTRA_STATE       = "state";
    public static final String EXTRA_TEXT        = "text";

    private OrbView orbView;
    private TextView tvStatus;
    private TextView tvTranscript;
    private TextView tvGreeting;
    private LinearLayout quickActions;

    private final BroadcastReceiver orbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_ORB_STATE.equals(intent.getAction())) {
                String stateName = intent.getStringExtra(EXTRA_STATE);
                updateOrbState(stateName);
            } else if (ACTION_TRANSCRIPT.equals(intent.getAction())) {
                String text = intent.getStringExtra(EXTRA_TEXT);
                updateTranscript(text);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        orbView       = view.findViewById(R.id.orb_view);
        tvStatus      = view.findViewById(R.id.tv_status);
        tvTranscript  = view.findViewById(R.id.tv_transcript);
        tvGreeting    = view.findViewById(R.id.tv_greeting);
        quickActions  = view.findViewById(R.id.quick_actions);

        setGreeting();
        buildQuickActions();

        orbView.setOnClickListener(v -> onOrbTapped());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_ORB_STATE);
        filter.addAction(ACTION_TRANSCRIPT);
        requireContext().registerReceiver(orbReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        try { requireContext().unregisterReceiver(orbReceiver); } catch (Exception ignored) {}
    }

    private void onOrbTapped() {
        if (orbView.getCurrentState() == OrbView.OrbState.IDLE) {
            Intent intent = new Intent(requireContext(), VoiceService.class);
            intent.setAction(VoiceService.ACTION_WAKE);
            requireContext().startService(intent);
        }
    }

    private void updateOrbState(String stateName) {
        if (stateName == null) return;
        switch (stateName) {
            case "LISTENING":
                orbView.setState(OrbView.OrbState.LISTENING);
                tvStatus.setText(getString(R.string.orb_listening));
                tvStatus.setTextColor(requireContext().getColor(R.color.state_listening));
                break;
            case "PROCESSING":
                orbView.setState(OrbView.OrbState.PROCESSING);
                tvStatus.setText(getString(R.string.orb_processing));
                tvStatus.setTextColor(requireContext().getColor(R.color.state_processing));
                break;
            case "SPEAKING":
                orbView.setState(OrbView.OrbState.SPEAKING);
                tvStatus.setText(getString(R.string.orb_speaking));
                tvStatus.setTextColor(requireContext().getColor(R.color.state_speaking));
                break;
            default:
                orbView.setState(OrbView.OrbState.IDLE);
                tvStatus.setText(getString(R.string.orb_idle));
                tvStatus.setTextColor(requireContext().getColor(R.color.text_secondary));
                break;
        }
    }

    private void updateTranscript(String text) {
        if (tvTranscript != null && text != null) {
            tvTranscript.setText(text);
        }
    }

    private void setGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String callName = PrefsManager.getCallName();
        String greeting;
        if (hour < 12) greeting = "Good morning";
        else if (hour < 17) greeting = "Good afternoon";
        else greeting = "Good evening";
        if (!callName.isEmpty()) greeting += ", " + callName;
        tvGreeting.setText(greeting);
    }

    private void buildQuickActions() {
        String[] actions = {"📞 Call", "⏰ Alarm", "🔦 Flash", "📱 Open App", "💬 SMS"};
        for (String action : actions) {
            TextView chip = new TextView(requireContext());
            chip.setText(action);
            chip.setTextColor(requireContext().getColor(R.color.nova_primary));
            chip.setTextSize(13f);
            chip.setPadding(28, 14, 28, 14);
            chip.setBackground(requireContext().getDrawable(R.drawable.bg_chip));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(0, 0, 12, 0);
            chip.setLayoutParams(lp);
            chip.setOnClickListener(v -> handleQuickAction(action));
            quickActions.addView(chip);
        }
    }

    private void handleQuickAction(String action) {
        String command = "";
        if (action.contains("Call"))    command = "Call someone";
        else if (action.contains("Alarm"))  command = "Set an alarm";
        else if (action.contains("Flash"))  command = "Turn on flashlight";
        else if (action.contains("Open"))   command = "Open an app";
        else if (action.contains("SMS"))    command = "Send a message";

        Intent intent = new Intent(requireContext(), VoiceService.class);
        intent.setAction(VoiceService.ACTION_COMMAND);
        intent.putExtra(VoiceService.EXTRA_COMMAND_TEXT, command);
        requireContext().startService(intent);
    }
}
