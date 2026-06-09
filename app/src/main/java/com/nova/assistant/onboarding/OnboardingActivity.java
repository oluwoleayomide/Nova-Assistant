package com.nova.assistant.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.nova.assistant.MainActivity;
import com.nova.assistant.R;
import com.nova.assistant.utils.PrefsManager;
import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 pager;
    private Button btnNext;
    private LinearLayout dotsContainer;
    private List<TextView> dots = new ArrayList<>();

    private static final int STEP_WELCOME     = 0;
    private static final int STEP_THEME       = 1;
    private static final int STEP_NAME        = 2;
    private static final int STEP_PERMISSIONS = 3;
    private static final int TOTAL_STEPS      = 4;

    private OnboardingAdapter adapter;
    private NameFragment nameFragment;
    private ThemeFragment themeFragment;
    private PermissionsFragment permissionsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (PrefsManager.isOnboarded()) {
            goToMain();
            return;
        }

        setContentView(R.layout.activity_onboarding);

        pager          = findViewById(R.id.onboarding_pager);
        btnNext        = findViewById(R.id.btn_next);
        dotsContainer  = findViewById(R.id.dots_container);

        themeFragment       = new ThemeFragment();
        nameFragment        = new NameFragment();
        permissionsFragment = new PermissionsFragment();

        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new WelcomeFragment());
        fragments.add(themeFragment);
        fragments.add(nameFragment);
        fragments.add(permissionsFragment);

        adapter = new OnboardingAdapter(this, fragments);
        pager.setAdapter(adapter);
        pager.setUserInputEnabled(false);

        buildDots();
        updateDots(0);

        btnNext.setOnClickListener(v -> handleNext());
    }

    private void handleNext() {
        int current = pager.getCurrentItem();

        if (current == STEP_NAME) {
            if (!nameFragment.validateAndSave()) return;
        }

        if (current == STEP_THEME) {
            themeFragment.saveTheme();
        }

        if (current < TOTAL_STEPS - 1) {
            pager.setCurrentItem(current + 1);
            updateDots(current + 1);
            if (current + 1 == TOTAL_STEPS - 1) {
                btnNext.setText(getString(R.string.btn_get_started));
            }
        } else {
            // Final step done
            PrefsManager.setOnboarded(true);
            goToMain();
        }
    }

    private void buildDots() {
        dotsContainer.removeAllViews();
        dots.clear();
        for (int i = 0; i < TOTAL_STEPS; i++) {
            TextView dot = new TextView(this);
            dot.setText("•");
            dot.setTextSize(24f);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(6, 0, 6, 0);
            dot.setLayoutParams(params);
            dots.add(dot);
            dotsContainer.addView(dot);
        }
    }

    private void updateDots(int active) {
        for (int i = 0; i < dots.size(); i++) {
            if (i == active) {
                dots.get(i).setTextColor(getResources().getColor(R.color.nova_primary));
                dots.get(i).setAlpha(1f);
            } else {
                dots.get(i).setTextColor(getResources().getColor(R.color.text_hint));
                dots.get(i).setAlpha(0.5f);
            }
        }
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
