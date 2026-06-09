package com.nova.assistant.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.nova.assistant.R;

public class WelcomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding_welcome, container, false);

        ImageView logo   = view.findViewById(R.id.iv_logo);
        TextView appName = view.findViewById(R.id.tv_app_name);
        TextView tagline = view.findViewById(R.id.tv_tagline);

        // Fade in animations staggered
        logo.setAlpha(0f);
        appName.setAlpha(0f);
        tagline.setAlpha(0f);

        logo.animate().alpha(1f).setDuration(600).setStartDelay(100).start();
        appName.animate().alpha(1f).translationYBy(-20f).setDuration(700).setStartDelay(300).start();
        tagline.animate().alpha(1f).translationYBy(-15f).setDuration(700).setStartDelay(500).start();

        return view;
    }
}
