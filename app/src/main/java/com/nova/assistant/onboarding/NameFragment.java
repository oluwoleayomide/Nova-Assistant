package com.nova.assistant.onboarding;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nova.assistant.R;
import com.nova.assistant.utils.PrefsManager;

public class NameFragment extends Fragment {

    private TextInputEditText etName, etCallName;
    private TextInputLayout tilName, tilCallName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding_name, container, false);

        etName     = view.findViewById(R.id.et_name);
        etCallName = view.findViewById(R.id.et_callname);
        tilName    = view.findViewById(R.id.til_name);
        tilCallName = view.findViewById(R.id.til_callname);

        // Pre-fill if exists
        etName.setText(PrefsManager.getUserName());
        etCallName.setText(PrefsManager.getCallName());

        return view;
    }

    public boolean validateAndSave() {
        String name     = etName.getText() != null ? etName.getText().toString().trim() : "";
        String callName = etCallName.getText() != null ? etCallName.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            tilName.setError("Please enter your name");
            return false;
        }
        tilName.setError(null);

        if (TextUtils.isEmpty(callName)) {
            callName = name;
        }

        PrefsManager.setUserName(name);
        PrefsManager.setCallName(callName);
        return true;
    }
}
