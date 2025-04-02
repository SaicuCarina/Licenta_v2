package com.example.licenta_v2.ui.details;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.licenta_v2.R;
import com.example.licenta_v2.model.PlantDetailsResponse;

import java.util.List;

public class PlantDetailsFragment extends Fragment {

    private static final String ARG_PLANT = "plant_arg";
    private PlantDetailsResponse plant;

    public static PlantDetailsFragment newInstance(PlantDetailsResponse plant) {
        PlantDetailsFragment fragment = new PlantDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PLANT, plant);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            plant = (PlantDetailsResponse) getArguments().getSerializable(ARG_PLANT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plant_details, container, false);

        View bottomNav = requireActivity().findViewById(R.id.bottomNavigationView);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }

        View settingsIcon = requireActivity().findViewById(R.id.settings);
        if (settingsIcon != null) {
            settingsIcon.setVisibility(View.GONE);
        }


        ImageView image = view.findViewById(R.id.plantImageDetails);
        TextView name = view.findViewById(R.id.plantCommonName);
        TextView synonyms = view.findViewById(R.id.plantSynonyms);
        TextView family = view.findViewById(R.id.plantFamily);
        TextView genus = view.findViewById(R.id.plantGenus);
        ImageView backButton = view.findViewById(R.id.back_button);
        TextView careLevel = view.findViewById(R.id.plantCareLevel);
        TextView wateringInterval = view.findViewById(R.id.plantWateringInterval);
        TextView description = view.findViewById(R.id.plantDescription);


        if (plant.getImageUrl() != null && !plant.getImageUrl().isEmpty()) {
            Glide.with(requireContext())
                    .load(plant.getImageUrl())
                    .into(image);
        }

        image.setOnClickListener(v -> {
            if (plant.getImageUrl() != null && !plant.getImageUrl().isEmpty()) {
                ImageDialogFragment dialog = ImageDialogFragment.newInstance(plant.getImageUrl());
                dialog.show(requireActivity().getSupportFragmentManager(), "image_dialog");
            }
        });

        name.setText(plant.getCommonName());

        List<String> syns = plant.getSynonyms();
        if (syns != null && !syns.isEmpty()) {
            StringBuilder sb = new StringBuilder("Synonyms: ");
            for (int i = 0; i < Math.min(syns.size(), 3); i++) {
                if (i > 0) sb.append(", ");
                sb.append(syns.get(i));
            }
            synonyms.setText(sb.toString());
        } else {
            synonyms.setText("Synonyms: -");
        }

        if (plant.getTaxonomy() != null) {
            family.setText("Family name: " + plant.getTaxonomy().getFamily());
            genus.setText("Genus name: " + plant.getTaxonomy().getGenus());
        } else {
            family.setText("Family name: -");
            genus.setText("Genus name: -");
        }
        if (plant.getCareLevel() != null && !plant.getCareLevel().isEmpty()) {
            careLevel.setText("Soil Moisture Level: " + plant.getCareLevel());
        } else {
            careLevel.setText("Soil Moisture Level: -");
        }

        if (plant.getWateringInterval() != null && !plant.getWateringInterval().isEmpty()) {
            wateringInterval.setText("Watering: " + plant.getWateringInterval() + "days");
        } else {
            wateringInterval.setText("Watering: -");
        }
        if (plant.getDescription() != null && plant.getDescription().getValue() != null) {
            description.setText(plant.getDescription().getValue());
        } else {
            description.setText("No description available.");
        }

        backButton.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        return view;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        View bottomNav = requireActivity().findViewById(R.id.bottomNavigationView);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.VISIBLE);
        }

        View settingsIcon = requireActivity().findViewById(R.id.settings);
        if (settingsIcon != null) {
            settingsIcon.setVisibility(View.VISIBLE);
        }
    }


}
