package com.nova.assistant.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.textfield.TextInputEditText;
import com.nova.assistant.R;
import com.nova.assistant.utils.PrefsManager;
import com.nova.assistant.utils.ThemeManager;
import com.nova.assistant.voice.TTSHandler;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private RadioGroup  rgGender, rgTheme;
    private RadioButton rbFemale, rbMale, rbDark, rbLight, rbAuto;
    private SeekBar     sbSpeed, sbPitch;
    private TextView    tvSpeedVal, tvPitchVal;
    private TextInputEditText etWakeWord, etApiKey, etApiEndpoint;
    private Button      btnTestWake, btnSaveApi;
    private SwitchCompat swBattery, swTemp, swStorage;

    private TTSHandler testTts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        bindViews(view);
        loadSettings();
        setupListeners();

        return view;
    }

    private void bindViews(View v) {
        rgGender       = v.findViewById(R.id.rg_voice_gender);
        rbFemale       = v.findViewById(R.id.rb_female);
        rbMale         = v.findViewById(R.id.rb_male);
        sbSpeed        = v.findViewById(R.id.sb_speed);
        sbPitch        = v.findViewById(R.id.sb_pitch);
        tvSpeedVal     = v.findViewById(R.id.tv_speed_val);
        tvPitchVal     = v.findViewById(R.id.tv_pitch_val);
        etWakeWord     = v.findViewById(R.id.et_wake_word);
        btnTestWake    = v.findViewById(R.id.btn_test_wake);
        rgTheme        = v.findViewById(R.id.rg_theme);
        rbDark         = v.findViewById(R.id.rb_dark);
        rbLight        = v.findViewById(R.id.rb_light);
        rbAuto         = v.findViewById(R.id.rb_auto);
        swBattery      = v.findViewById(R.id.sw_battery_alert);
        swTemp         = v.findViewById(R.id.sw_temp_alert);
        swStorage      = v.findViewById(R.id.sw_storage_alert);
        etApiKey       = v.findViewById(R.id.et_api_key);
        etApiEndpoint  = v.findViewById(R.id.et_api_endpoint);
        btnSaveApi     = v.findViewById(R.id.btn_save_api);
    }

    private void loadSettings() {
        // Voice gender
        if (PrefsManager.GENDER_MALE.equals(PrefsManager.getVoiceGender())) {
            rbMale.setChecked(true);
        } else {
            rbFemale.setChecked(true);
        }

        // Speed (stored 0.5 - 2.0, seekbar 0-20 → 0.5 + progress * 0.075)
        float speed = PrefsManager.getVoiceSpeed();
        int speedProgress = Math.round((speed - 0.5f) / 0.075f);
        sbSpeed.setProgress(Math.max(0, Math.min(20, speedProgress)));
        tvSpeedVal.setText(String.format(Locale.US, "%.1fx", speed));

        // Pitch
        float pitch = PrefsManager.getVoicePitch();
        int pitchProgress = Math.round((pitch - 0.5f) / 0.075f);
        sbPitch.setProgress(Math.max(0, Math.min(20, pitchProgress)));
        tvPitchVal.setText(String.format(Locale.US, "%.1fx", pitch));

        // Wake word
        etWakeWord.setText(PrefsManager.getWakeWord());

        // Theme
        switch (PrefsManager.getTheme()) {
            case PrefsManager.THEME_LIGHT: rbLight.setChecked(true); break;
            case PrefsManager.THEME_AUTO:  rbAuto.setChecked(true);  break;
            default:                        rbDark.setChecked(true);  break;
        }

        // Alerts
        swBattery.setChecked(PrefsManager.isBatteryAlertEnabled());
        swTemp.setChecked(PrefsManager.isTempAlertEnabled());
        swStorage.setChecked(PrefsManager.isStorageAlertEnabled());

        // API
        etApiKey.setText(PrefsManager.getApiKey());
        etApiEndpoint.setText(PrefsManager.getApiEndpoint());
    }

    private void setupListeners() {
        // Gender
        rgGender.setOnCheckedChangeListener((group, checkedId) -> {
            String gender = checkedId == R.id.rb_male ?
                PrefsManager.GENDER_MALE : PrefsManager.GENDER_FEMALE;
            PrefsManager.setVoiceGender(gender);
        });

        // Speed
        sbSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean user) {
                float val = 0.5f + progress * 0.075f;
                tvSpeedVal.setText(String.format(Locale.US, "%.1fx", val));
                PrefsManager.setVoiceSpeed(val);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        // Pitch
        sbPitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean user) {
                float val = 0.5f + progress * 0.075f;
                tvPitchVal.setText(String.format(Locale.US, "%.1fx", val));
                PrefsManager.setVoicePitch(val);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        // Wake word save on focus lost
        etWakeWord.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) saveWakeWord();
        });

        // Test wake word TTS
        btnTestWake.setOnClickListener(v -> testWakeWord());

        // Theme
        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            String theme;
            if (checkedId == R.id.rb_light)       theme = PrefsManager.THEME_LIGHT;
            else if (checkedId == R.id.rb_auto)   theme = PrefsManager.THEME_AUTO;
            else                                  theme = PrefsManager.THEME_DARK;
            PrefsManager.setTheme(theme);
            ThemeManager.applyTheme(theme);
        });

        // Alert switches
        swBattery.setOnCheckedChangeListener((btn, checked) -> PrefsManager.setBatteryAlert(checked));
        swTemp.setOnCheckedChangeListener((btn, checked) -> PrefsManager.setTempAlert(checked));
        swStorage.setOnCheckedChangeListener((btn, checked) -> PrefsManager.setStorageAlert(checked));

        // Save API
        btnSaveApi.setOnClickListener(v -> saveApiSettings());
    }

    private void saveWakeWord() {
        String word = etWakeWord.getText() != null ? etWakeWord.getText().toString().trim() : "";
        if (!word.isEmpty()) PrefsManager.setWakeWord(word);
    }

    private void testWakeWord() {
        saveWakeWord();
        String wakeWord = PrefsManager.getWakeWord();
        if (testTts == null) {
            testTts = new TTSHandler(requireContext(), new TTSHandler.TTSListener() {
                @Override public void onStart() {}
                @Override public void onDone()  {}
                @Override public void onError() {}
            });
        }
        testTts.speak("Wake word set to: " + wakeWord + ". Say " + wakeWord + " to activate me.");
    }

    private void saveApiSettings() {
        String key      = etApiKey.getText() != null ? etApiKey.getText().toString().trim() : "";
        String endpoint = etApiEndpoint.getText() != null ? etApiEndpoint.getText().toString().trim() : "";
        PrefsManager.setApiKey(key);
        if (!endpoint.isEmpty()) PrefsManager.setApiEndpoint(endpoint);
        Toast.makeText(requireContext(), "API settings saved", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (testTts != null) testTts.shutdown();
    }
}
