package com.nova.assistant.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.nova.assistant.R;
import com.nova.assistant.utils.PrefsManager;
import com.nova.assistant.utils.ThemeManager;

public class ThemeFragment extends Fragment {

    private LinearLayout optionDark, optionLight;
    private TextView darkCheck, lightCheck;
    private boolean isDarkSelected = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding_theme, container, false);

        optionDark  = view.findViewById(R.id.option_dark);
        optionLight = view.findViewById(R.id.option_light);
        darkCheck   = view.findViewById(R.id.dark_check);
        lightCheck  = view.findViewById(R.id.light_check);

        optionDark.setOnClickListener(v -> selectDark());
        optionLight.setOnClickListener(v -> selectLight());

        selectDark();
        return view;
    }

    private void selectDark() {
        isDarkSelected = true;
        optionDark.setSelected(true);
        optionLight.setSelected(false);
        darkCheck.setVisibility(View.VISIBLE);
        lightCheck.setVisibility(View.INVISIBLE);
    }

    private void selectLight() {
        isDarkSelected = false;
        optionDark.setSelected(false);
        optionLight.setSelected(true);
        darkCheck.setVisibility(View.INVISIBLE);
        lightCheck.setVisibility(View.VISIBLE);
    }

    public void saveTheme() {
        String theme = isDarkSelected ? PrefsManager.THEME_DARK : PrefsManager.THEME_LIGHT;
        PrefsManager.setTheme(theme);
        ThemeManager.applyTheme(theme);
    }
}
