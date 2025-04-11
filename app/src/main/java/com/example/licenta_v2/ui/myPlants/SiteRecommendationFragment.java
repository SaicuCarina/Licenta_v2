package com.example.licenta_v2.ui.myPlants;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.licenta_v2.R;
import com.example.licenta_v2.model.PlantDetailsResponse;
import com.example.licenta_v2.model.SavedPlant;
import com.example.licenta_v2.model.Site;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SiteRecommendationFragment extends Fragment {

    private FirebaseFirestore db;
    private String lightSummary;
    private List<Site> recommendedSites = new ArrayList<>();
    private List<Site> notRecommendedSites = new ArrayList<>();
    private GridLayout recommendedContainer;
    private GridLayout notRecommendedContainer;
    private ProgressBar loadingSpinner;
    private ScrollView scrollableContent;
    private PlantDetailsResponse plant;
    private SavedPlant savedPlant;


    public SiteRecommendationFragment() {}

    public static SiteRecommendationFragment newInstance(PlantDetailsResponse plant) {
        SiteRecommendationFragment fragment = new SiteRecommendationFragment();
        Bundle args = new Bundle();
        args.putSerializable("plant", plant);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_site_recommendation, container, false);

        ImageView backButton = view.findViewById(R.id.back_site_recommendation);
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        recommendedContainer = view.findViewById(R.id.recommendedContainer);
        notRecommendedContainer = view.findViewById(R.id.notRecommendedContainer);
        loadingSpinner = view.findViewById(R.id.loadingSpinner);
        scrollableContent = view.findViewById(R.id.scrollableContent);

        if (getArguments() != null) {
            plant = (PlantDetailsResponse) getArguments().getSerializable("plant");
            lightSummary = plant.getLightSummary();
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View bottomNav = requireActivity().findViewById(R.id.bottomNavigationView);
        if (bottomNav != null) bottomNav.setVisibility(View.GONE);

        View settingsIcon = requireActivity().findViewById(R.id.settings);
        if (settingsIcon != null) settingsIcon.setVisibility(View.GONE);

        db = FirebaseFirestore.getInstance();

        loadSites();
    }

    private void loadSites() {
        loadingSpinner.setVisibility(View.VISIBLE);
        scrollableContent.setVisibility(View.GONE);

        recommendedSites.clear();
        notRecommendedSites.clear();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        db.collection("sites").get()
                .addOnSuccessListener(globalSnapshots -> {

                    List<Site> allSites = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : globalSnapshots) {
                        allSites.add(doc.toObject(Site.class));
                    }

                    db.collection("users")
                            .document(currentUser.getUid())
                            .collection("customSites")
                            .get()
                            .addOnSuccessListener(customSnapshots -> {
                                for (QueryDocumentSnapshot doc : customSnapshots) {
                                    Site site = doc.toObject(Site.class);
                                    allSites.add(site);
                                }

                                for (Site site : allSites) {
                                    if (isRecommended(site, lightSummary)) {
                                        recommendedSites.add(site);
                                    } else {
                                        notRecommendedSites.add(site);
                                    }
                                }

                                displaySites();
                                loadingSpinner.setVisibility(View.GONE);
                                scrollableContent.setVisibility(View.VISIBLE);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Error loading custom sites: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                loadingSpinner.setVisibility(View.GONE);
                                scrollableContent.setVisibility(View.VISIBLE);
                            });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading sites: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    loadingSpinner.setVisibility(View.GONE);
                    scrollableContent.setVisibility(View.VISIBLE);
                });
    }


    private boolean isRecommended(Site site, String lightSummary) {
        if (lightSummary == null) return false;

        switch (lightSummary.toLowerCase()) {
            case "full sun": return site.isFull_sun();
            case "partial sun": return site.isPartial_sun();
            case "full shade": return site.isFull_shade();
            default: return false;
        }
    }

    private void displaySites() {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (Site site : recommendedSites) {
            View siteView = inflater.inflate(R.layout.item_site, recommendedContainer, false);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            siteView.setLayoutParams(params);

            bindSiteToView(site, siteView);
            recommendedContainer.addView(siteView);
        }

        View addCustomCard = inflater.inflate(R.layout.item_site, recommendedContainer, false);

        TextView nameView = addCustomCard.findViewById(R.id.siteName);
        ImageView imageView = addCustomCard.findViewById(R.id.siteImage);

        nameView.setText("Add Your Room");
        imageView.setImageResource(R.drawable.add);

        RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(300, 300);
        imageParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        imageView.setLayoutParams(imageParams);

        GridLayout.LayoutParams addParams = new GridLayout.LayoutParams();
        addParams.width = 0;
        addParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        addCustomCard.setLayoutParams(addParams);

        addCustomCard.setOnClickListener(v -> {
            SavedPlant sp = new SavedPlant();
            sp.setPlantData(plant);
            AddCustomSiteFragment fragment = AddCustomSiteFragment.newInstance(sp);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_left,
                            R.anim.slide_out_right
                    )
                    .replace(R.id.frame_layout, fragment)
                    .addToBackStack(null)
                    .commit();
        });


        recommendedContainer.addView(addCustomCard);

        for (Site site : notRecommendedSites) {
            View siteView = inflater.inflate(R.layout.item_site, notRecommendedContainer, false);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            siteView.setLayoutParams(params);

            bindSiteToView(site, siteView);
            notRecommendedContainer.addView(siteView);
        }
    }


    private void bindSiteToView(Site site, View view) {
        TextView nameView = view.findViewById(R.id.siteName);
        ImageView imageView = view.findViewById(R.id.siteImage);
        RelativeLayout cardContainer = view.findViewById(R.id.siteCardContainer);

        nameView.setText(site.getName());

        String imageBase64 = site.getPhoto();
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            if (!imageBase64.startsWith("data:image")) {
                imageBase64 = "data:image/png;base64," + imageBase64;
            }

            Glide.with(getContext())
                    .load(imageBase64)
                    .placeholder(R.drawable.location)
                    .into(imageView);

            cardContainer.setBackground(null);

        } else {
            imageView.setImageResource(R.drawable.location);

            cardContainer.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.background_icon_box));

            ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
            layoutParams.width = 300;
            layoutParams.height = 300;
            imageView.setLayoutParams(layoutParams);

            if (layoutParams instanceof RelativeLayout.LayoutParams) {
                ((RelativeLayout.LayoutParams) layoutParams).addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            } else if (layoutParams instanceof FrameLayout.LayoutParams) {
                ((FrameLayout.LayoutParams) layoutParams).gravity = Gravity.CENTER;
            }
        }

        view.setOnClickListener(v -> onSiteClicked(site));
    }



    private void onSiteClicked(Site site) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || plant == null) {
            Toast.makeText(getContext(), "Something went wrong.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference plantRef = db.collection("users")
                .document(currentUser.getUid())
                .collection("myPlants")
                .document(plant.getCommonName());

        SavedPlant savedPlant = new SavedPlant();
        savedPlant.setPlantData(plant);
        savedPlant.setAddedSite(site.getName());

        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        savedPlant.setAddedDate(currentDate);

        plantRef.set(savedPlant)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Plant added to My Plants!", Toast.LENGTH_SHORT).show();

                    View bottomNavView = requireActivity().findViewById(R.id.bottomNavigationView);
                    if (bottomNavView instanceof com.google.android.material.bottomnavigation.BottomNavigationView) {
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
                        Toast.makeText(getContext(), "Failed to add plant: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }



}
