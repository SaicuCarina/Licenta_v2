package com.example.licenta_v2.ui.settings;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.licenta_v2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AccountSettingsFragment extends Fragment {

    private EditText nameEditText, locationEditText;
    private Button saveButton, resetPasswordButton;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private EditText currentPasswordEditText, newPasswordEditText;
    private Button changePasswordButton;


    public AccountSettingsFragment() {
        super(R.layout.fragment_account_settings);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View bottomNav = requireActivity().findViewById(R.id.bottomNavigationView);
        if (bottomNav != null) bottomNav.setVisibility(View.GONE);

        nameEditText = view.findViewById(R.id.editTextName);
        locationEditText = view.findViewById(R.id.editTextLocation);
        saveButton = view.findViewById(R.id.buttonSave);
        currentPasswordEditText = view.findViewById(R.id.editTextCurrentPassword);
        newPasswordEditText = view.findViewById(R.id.editTextNewPassword);
        changePasswordButton = view.findViewById(R.id.buttonChangePassword);
        changePasswordButton.setOnClickListener(v -> changePassword());


        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadUserData();

        saveButton.setOnClickListener(v -> updateUserData());

        ImageView backButton = view.findViewById(R.id.back_account_settings);
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

    }

    private void loadUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        nameEditText.setText(doc.getString("name"));
                        locationEditText.setText(doc.getString("location"));
                    }
                });
    }

    private void updateUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String name = nameEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Name required");
            return;
        }

        db.collection("users").document(user.getUid())
                .update("name", name, "location", location)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show());
    }

    private void changePassword() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String currentPassword = currentPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword)) {
            Toast.makeText(getContext(), "Complete both password fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.matches("^(?=.*[0-9])(?=.*[!@#$%^&*()_+=\\-]).{6,}$")) {
            newPasswordEditText.setError("Password must be at least 6 characters, contain a digit and a special character.");
            return;
        }

        String email = user.getEmail();
        auth.signInWithEmailAndPassword(email, currentPassword)
                .addOnSuccessListener(authResult -> {
                    user.updatePassword(newPassword)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(getContext(), "Password updated", Toast.LENGTH_SHORT).show();
                                currentPasswordEditText.setText("");
                                newPasswordEditText.setText("");
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Failed to update password: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Reauthentication failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();

        View bottomNav = requireActivity().findViewById(R.id.bottomNavigationView);
        if (bottomNav != null) bottomNav.setVisibility(View.VISIBLE);
    }

}