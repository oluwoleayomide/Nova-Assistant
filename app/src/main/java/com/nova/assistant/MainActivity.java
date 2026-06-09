package com.nova.assistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nova.assistant.chat.ChatFragment;
import com.nova.assistant.home.HomeFragment;
import com.nova.assistant.security.SecurityFragment;
import com.nova.assistant.settings.SettingsFragment;
import com.nova.assistant.voice.VoiceService;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private HomeFragment homeFragment;
    private ChatFragment chatFragment;
    private SecurityFragment securityFragment;
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_nav);

        initFragments();
        setupBottomNav();
        showFragment(getHomeFragment());
        startVoiceService();
    }

    private void initFragments() {
        homeFragment     = new HomeFragment();
        chatFragment     = new ChatFragment();
        securityFragment = new SecurityFragment();
        settingsFragment = new SettingsFragment();
    }

    private void setupBottomNav() {
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                showFragment(homeFragment);
            } else if (id == R.id.nav_chat) {
                showFragment(chatFragment);
            } else if (id == R.id.nav_security) {
                showFragment(securityFragment);
            } else if (id == R.id.nav_settings) {
                showFragment(settingsFragment);
            }
            return true;
        });
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();
    }

    private HomeFragment getHomeFragment() {
        return homeFragment;
    }

    private void startVoiceService() {
        Intent serviceIntent = new Intent(this, VoiceService.class);
        startService(serviceIntent);
    }

    public void navigateToHome() {
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    public void navigateToChat() {
        bottomNav.setSelectedItemId(R.id.nav_chat);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
