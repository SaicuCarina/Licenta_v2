package com.example.licenta_v2.ui.myPlants;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.licenta_v2.R;
import com.example.licenta_v2.model.SavedPlant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddCustomSiteFragment extends Fragment {

    private EditText siteNameInput;
    private RadioGroup lightRadioGroup;
    private RadioGroup rainRadioGroup;
    private SavedPlant savedPlant;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_custom_site, container, false);

        siteNameInput = view.findViewById(R.id.siteNameInput);

        siteNameInput.setOnEditorActionListener((v, actionId, event) -> {
            siteNameInput.clearFocus();
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(siteNameInput.getWindowToken(), 0);
            }
            return true;
        });

        lightRadioGroup = view.findViewById(R.id.lightRadioGroup);
        rainRadioGroup = view.findViewById(R.id.rainRadioGroup);
        Button saveButton = view.findViewById(R.id.saveSiteButton);
        ImageView backButton = view.findViewById(R.id.backButtonAddSite);

        saveButton.setOnClickListener(v -> saveCustomSite());
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());


        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            savedPlant = (SavedPlant) getArguments().getSerializable("savedPlant");
        }
    }

    public static AddCustomSiteFragment newInstance(SavedPlant savedPlant) {
        AddCustomSiteFragment fragment = new AddCustomSiteFragment();
        Bundle args = new Bundle();
        args.putSerializable("savedPlant", savedPlant);
        fragment.setArguments(args);
        return fragment;
    }

    private void saveSiteAndPlant(String name, boolean fullSun, boolean partialSun, boolean fullShade, boolean exposedToRain, FirebaseFirestore db, FirebaseUser user) {
        Map<String, Object> customSite = new HashMap<>();
        customSite.put("name", name);
        customSite.put("full_sun", fullSun);
        customSite.put("partial_sun", partialSun);
        customSite.put("full_shade", fullShade);
        customSite.put("exposed_to_rain", exposedToRain);

        db.collection("users")
                .document(user.getUid())
                .collection("customSites")
                .add(customSite)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(getContext(), "Room added!", Toast.LENGTH_SHORT).show();

                    if (savedPlant != null) {
                        savedPlant.setAddedSite(name);
                        String currentDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
                        savedPlant.setLastWateredDate(currentDate);
                        savedPlant.setAddedDate(currentDate);
                        savedPlant.setExposedToRain(exposedToRain);

                        db.collection("users")
                                .document(user.getUid())
                                .collection("myPlants")
                                .add(savedPlant)
                                .addOnSuccessListener(docRef -> {
                                    savedPlant.setId(docRef.getId());
                                    docRef.set(savedPlant);

                                    Toast.makeText(getContext(), "Plant saved!", Toast.LENGTH_SHORT).show();

                                    View bottomNavView = requireActivity().findViewById(R.id.bottomNavigationView);
                                    if (bottomNavView instanceof com.google.android.material.bottomnavigation.BottomNavigationView) {
                                        bottomNavView.setVisibility(View.VISIBLE);
                                        ((com.google.android.material.bottomnavigation.BottomNavigationView) bottomNavView)
                                                .setSelectedItemId(R.id.myPlants);
                                    }

                                    requireActivity().getSupportFragmentManager()
                                            .beginTransaction()
                                            .replace(R.id.frame_layout, new MyPlantsFragment())
                                            .addToBackStack(null)
                                            .commit();

                                    View bottomNav = requireActivity().findViewById(R.id.bottomNavigationView);
                                    if (bottomNav != null) bottomNav.setVisibility(View.VISIBLE);

                                    View settingsIcon = requireActivity().findViewById(R.id.settings);
                                    if (settingsIcon != null) settingsIcon.setVisibility(View.VISIBLE);
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    private void saveCustomSite() {
        String name = siteNameInput.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a name", Toast.LENGTH_SHORT).show();
            return;
        }

        final boolean fullSun, partialSun, fullShade;

        int selected = lightRadioGroup.getCheckedRadioButtonId();
        if (selected == R.id.fullSunRadio) {
            fullSun = true;
            partialSun = false;
            fullShade = false;
        } else if (selected == R.id.partialSunRadio) {
            fullSun = false;
            partialSun = true;
            fullShade = false;
        } else if (selected == R.id.fullShadeRadio) {
            fullSun = false;
            partialSun = false;
            fullShade = true;
        } else {
            Toast.makeText(getContext(), "Please select a light level", Toast.LENGTH_SHORT).show();
            return;
        }

        final boolean exposedToRain = rainRadioGroup.getCheckedRadioButtonId() == R.id.rainYesRadio;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (user == null) return;

        db.collection("users").document(user.getUid()).get().addOnSuccessListener(snapshot -> {
            if (exposedToRain && !snapshot.contains("location")) {
                View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_enter_location, null);
                EditText input = dialogView.findViewById(R.id.locationInput); // asigură-te că id-ul este corect

                new AlertDialog.Builder(requireContext())
                        .setTitle("Set location")
                        .setMessage("You marked this site as exposed to rain.\nPlease enter your city to track rainfall.")
                        .setView(dialogView)
                        .setPositiveButton("Save", (dialog, which) -> {
                            String city = input.getText().toString().trim();
                            if (!city.isEmpty()) {
                                db.collection("users").document(user.getUid())
                                        .update("location", city)
                                        .addOnSuccessListener(aVoid ->
                                                saveSiteAndPlant(name, fullSun, partialSun, fullShade, exposedToRain, db, user));
                            } else {
                                Toast.makeText(getContext(), "City cannot be empty", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();

            } else {
                saveSiteAndPlant(name, fullSun, partialSun, fullShade, exposedToRain, db, user);
            }
        });
    }
}
