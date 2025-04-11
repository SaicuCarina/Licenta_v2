package com.example.licenta_v2.ui.myPlants;

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

    private void saveCustomSite() {
        String name = siteNameInput.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a name", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean fullSun = false, partialSun = false, fullShade = false;

        int selected = lightRadioGroup.getCheckedRadioButtonId();
        if (selected == R.id.fullSunRadio) fullSun = true;
        else if (selected == R.id.partialSunRadio) partialSun = true;
        else if (selected == R.id.fullShadeRadio) fullShade = true;
        else {
            Toast.makeText(getContext(), "Please select a light level", Toast.LENGTH_SHORT).show();
            return;
        }

        int rainSelected = rainRadioGroup.getCheckedRadioButtonId();
        if (rainSelected == -1) {
            Toast.makeText(getContext(), "Please select whether it's exposed to rain", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean exposedToRain = rainSelected == R.id.rainYesRadio;

        Map<String, Object> customSite = new HashMap<>();
        customSite.put("name", name);
        customSite.put("full_sun", fullSun);
        customSite.put("partial_sun", partialSun);
        customSite.put("full_shade", fullShade);
        customSite.put("exposed_to_rain", exposedToRain);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(user.getUid())
                .collection("customSites")
                .add(customSite)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(getContext(), "Room added!", Toast.LENGTH_SHORT).show();

                    if (savedPlant != null) {
                        savedPlant.setAddedSite(name);
                        String currentDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
                        savedPlant.setAddedDate(currentDate);
                        savedPlant.setExposedToRain(exposedToRain);

                        db.collection("users")
                                .document(user.getUid())
                                .collection("myPlants")
                                .document(savedPlant.getPlantData().getCommonName())
                                .set(savedPlant)
                                .addOnSuccessListener(aVoid -> {
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
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Eroare la salvarea plantei: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Eroare la salvarea camerei: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
