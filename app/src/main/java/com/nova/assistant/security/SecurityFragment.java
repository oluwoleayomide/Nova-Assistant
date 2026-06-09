package com.nova.assistant.security;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.nova.assistant.R;

public class SecurityFragment extends Fragment {

    private ProgressBar pbScan, pbStorage;
    private TextView tvScanStatus, tvStorageUsed, tvStorageTotal;
    private Button btnScanNow, btnRingDevice, btnShowLocation, btnLogin, btnSignup;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_security, container, false);

        pbScan         = view.findViewById(R.id.pb_scan);
        pbStorage      = view.findViewById(R.id.pb_storage);
        tvScanStatus   = view.findViewById(R.id.tv_scan_status);
        tvStorageUsed  = view.findViewById(R.id.tv_storage_used);
        tvStorageTotal = view.findViewById(R.id.tv_storage_total);
        btnScanNow     = view.findViewById(R.id.btn_scan_now);
        btnRingDevice  = view.findViewById(R.id.btn_ring_device);
        btnShowLocation = view.findViewById(R.id.btn_show_location);
        btnLogin       = view.findViewById(R.id.btn_login);
        btnSignup      = view.findViewById(R.id.btn_signup);

        loadStorageInfo();

        btnScanNow.setOnClickListener(v -> runMalwareScan());
        btnRingDevice.setOnClickListener(v ->
            Toast.makeText(requireContext(), "Ring Device — Coming in Phase 2", Toast.LENGTH_SHORT).show());
        btnShowLocation.setOnClickListener(v -> openMaps());
        btnLogin.setOnClickListener(v ->
            Toast.makeText(requireContext(), "Login backend coming in Phase 2", Toast.LENGTH_SHORT).show());
        btnSignup.setOnClickListener(v ->
            Toast.makeText(requireContext(), "Sign up backend coming in Phase 2", Toast.LENGTH_SHORT).show());

        return view;
    }

    private void loadStorageInfo() {
        try {
            StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
            long total     = stat.getBlockCountLong() * stat.getBlockSizeLong();
            long available = stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
            long used      = total - available;

            float usedGB  = used / (1024f * 1024f * 1024f);
            float totalGB = total / (1024f * 1024f * 1024f);
            int   percent = (int)(100f * used / total);

            tvStorageUsed.setText(String.format("Used: %.1f GB", usedGB));
            tvStorageTotal.setText(String.format("/ %.1f GB", totalGB));
            pbStorage.setProgress(percent);
        } catch (Exception e) {
            tvStorageUsed.setText("Used: — GB");
            tvStorageTotal.setText("/ — GB");
        }
    }

    private void runMalwareScan() {
        btnScanNow.setEnabled(false);
        btnScanNow.setText("Scanning…");
        pbScan.setVisibility(View.VISIBLE);
        pbScan.setProgress(0);
        tvScanStatus.setText("Scanning…");

        // Simulate scan progress
        final int[] progress = {0};
        Runnable tick = new Runnable() {
            @Override
            public void run() {
                progress[0] += 4;
                pbScan.setProgress(progress[0]);
                if (progress[0] < 100) {
                    handler.postDelayed(this, 80);
                } else {
                    onScanComplete();
                }
            }
        };
        handler.postDelayed(tick, 80);
    }

    private void onScanComplete() {
        pbScan.setProgress(100);
        tvScanStatus.setText("✓ No threats found — Last scan: just now");
        btnScanNow.setEnabled(true);
        btnScanNow.setText(getString(R.string.scan_now));
        Toast.makeText(requireContext(), "Scan complete. Device is clean!", Toast.LENGTH_SHORT).show();
    }

    private void openMaps() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("geo:0,0?q=my+location"));
            intent.setPackage("com.google.android.apps.maps");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Google Maps not found", Toast.LENGTH_SHORT).show();
        }
    }
}
