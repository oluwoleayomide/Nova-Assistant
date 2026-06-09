package com.nova.assistant.onboarding;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.nova.assistant.R;
import com.nova.assistant.utils.PermissionHelper;

public class PermissionsFragment extends Fragment {

    private LinearLayout permissionsList;

    private static final String[][] PERMISSIONS = {
        {Manifest.permission.RECORD_AUDIO,       "🎤", "Microphone",  "Listen for your voice commands"},
        {Manifest.permission.READ_CONTACTS,      "👥", "Contacts",    "Call or message your contacts"},
        {Manifest.permission.CALL_PHONE,         "📞", "Phone/Call",  "Make calls on your command"},
        {Manifest.permission.SEND_SMS,           "💬", "SMS",         "Send text messages for you"},
        {Manifest.permission.CAMERA,             "📷", "Camera",      "Control flashlight"},
        {Manifest.permission.READ_EXTERNAL_STORAGE, "💾", "Storage",  "Access files when needed"},
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_permissions, container, false);
        permissionsList = view.findViewById(R.id.permissions_list);
        buildPermissionRows(inflater);
        return view;
    }

    private void buildPermissionRows(LayoutInflater inflater) {
        permissionsList.removeAllViews();
        for (String[] perm : PERMISSIONS) {
            View row = inflater.inflate(R.layout.item_permission_row, permissionsList, false);

            TextView icon    = row.findViewById(R.id.perm_icon);
            TextView title   = row.findViewById(R.id.perm_title);
            TextView desc    = row.findViewById(R.id.perm_desc);
            Button   btnGrant = row.findViewById(R.id.btn_grant);

            String permission = perm[0];
            icon.setText(perm[1]);
            title.setText(perm[2]);
            desc.setText(perm[3]);

            boolean granted = PermissionHelper.hasPermission(requireContext(), permission);
            updateGrantButton(btnGrant, granted);

            btnGrant.setOnClickListener(v -> {
                requestPermissions(new String[]{permission}, 100);
            });

            permissionsList.addView(row);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Rebuild rows to update button states
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        buildPermissionRows(inflater);
    }

    private void updateGrantButton(Button btn, boolean granted) {
        if (granted) {
            btn.setText(getString(R.string.btn_granted));
            btn.setEnabled(false);
            btn.setAlpha(0.6f);
        } else {
            btn.setText(getString(R.string.btn_grant));
            btn.setEnabled(true);
            btn.setAlpha(1f);
        }
    }
}
