package com.example.licenta_v2.ui.details;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.licenta_v2.R;
import com.example.licenta_v2.model.PlantDetailsResponse;
import com.example.licenta_v2.ui.myPlants.SiteRecommendationFragment;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

import java.util.List;

public class PlantDetailsFragment extends Fragment {

    private static final String ARG_PLANT = "plant_arg";
    private PlantDetailsResponse plant;
    private boolean isProgrammaticScroll = false;
    private boolean isProgrammaticTabChange = false;



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
        if (bottomNav != null) bottomNav.setVisibility(View.GONE);

        View settingsIcon = requireActivity().findViewById(R.id.settings);
        if (settingsIcon != null) settingsIcon.setVisibility(View.GONE);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        CollapsingToolbarLayout collapsingToolbar = view.findViewById(R.id.collapsing_toolbar);
        AppBarLayout appBarLayout = view.findViewById(R.id.appbar_layout);

        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        collapsingToolbar.setTitle(" ");
        collapsingToolbar.setCollapsedTitleTextColor(Color.BLACK);
        collapsingToolbar.setExpandedTitleColor(Color.TRANSPARENT);
        collapsingToolbar.setContentScrimColor(Color.WHITE);

        toolbar.setNavigationIcon(R.drawable.arrow_back);
        toolbar.setNavigationOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
            if (Math.abs(verticalOffset) >= appBarLayout1.getTotalScrollRange()) {
                collapsingToolbar.setTitle(plant.getCommonName());
            } else {
                collapsingToolbar.setTitle(" ");
            }
        });

        ImageView image = view.findViewById(R.id.plantImageDetails);
        TextView name = view.findViewById(R.id.plantCommonName);
        TextView synonyms = view.findViewById(R.id.plantSynonyms);
        TextView family = view.findViewById(R.id.plantFamily);
        TextView genus = view.findViewById(R.id.plantGenus);
        TextView careLevel = view.findViewById(R.id.plantCareLevel);
        TextView wateringInterval = view.findViewById(R.id.plantWateringInterval);
        TextView description = view.findViewById(R.id.plantDescription);
        TextView lightSummary = view.findViewById(R.id.lightSummary);
        TextView toxicitySummaryText = view.findViewById(R.id.toxicity);
        TextView bestWatering = view.findViewById(R.id.bestWatering);
        TextView bestLight = view.findViewById(R.id.bestLight);
        TextView bestSoil = view.findViewById(R.id.bestSoil);
        TextView edibleParts = view.findViewById(R.id.edibleParts);
        TextView propagationMethods = view.findViewById(R.id.propagationMethods);
        TextView toxicityTextExtra = view.findViewById(R.id.toxicityTextExtra);
        TextView ediblePartsTitle = view.findViewById(R.id.ediblePartsTitle);
        TextView propagationTitle = view.findViewById(R.id.propagationTitle);
        TextView toxicityExtraTitle = view.findViewById(R.id.toxicityExtraTitle);
        FlexboxLayout gptTagsLayout = view.findViewById(R.id.gptTagsLayout);

        TabLayout sectionTabs = view.findViewById(R.id.stickySectionTabs);
        NestedScrollView scrollView = view.findViewById(R.id.scroll);

        TextView wateringTitle = view.findViewById(R.id.wateringTitle);
        TextView lightTitle = view.findViewById(R.id.lightTitle);
        TextView soilTitle = view.findViewById(R.id.soilTitle);

        sectionTabs.addTab(sectionTabs.newTab().setText("Watering"));
        sectionTabs.addTab(sectionTabs.newTab().setText("Light"));
        sectionTabs.addTab(sectionTabs.newTab().setText("Soil"));

        if (plant.getEdibleParts() != null && !plant.getEdibleParts().isEmpty())
            sectionTabs.addTab(sectionTabs.newTab().setText("Edible"));
        if (plant.getPropagationMethods() != null && !plant.getPropagationMethods().isEmpty())
            sectionTabs.addTab(sectionTabs.newTab().setText("Propagation"));
        if (plant.getToxicity() != null && !plant.getToxicity().isEmpty())
            sectionTabs.addTab(sectionTabs.newTab().setText("Toxicity"));

        scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (isProgrammaticScroll) return;

            View[] sections = {
                    wateringTitle, lightTitle, soilTitle,
                    ediblePartsTitle.getVisibility() == View.VISIBLE ? ediblePartsTitle : null,
                    propagationTitle.getVisibility() == View.VISIBLE ? propagationTitle : null,
                    toxicityExtraTitle.getVisibility() == View.VISIBLE ? toxicityExtraTitle : null
            };

            String[] tabNames = {
                    "Watering", "Light", "Soil", "Edible", "Propagation", "Toxicity"
            };

            int smallestDistance = Integer.MAX_VALUE;
            int currentSectionIndex = -1;

            int scrollThreshold = 200;

            for (int i = 0; i < sections.length; i++) {
                if (sections[i] == null) continue;
                int viewTop = getRelativeTop(sections[i], scrollView);
                int distance = Math.abs(viewTop);
                if (viewTop >= 0 && viewTop <= scrollThreshold && distance < smallestDistance) {
                    smallestDistance = distance;
                    currentSectionIndex = i;
                }
            }

            if (currentSectionIndex != -1) {
                TabLayout.Tab tab = findTabByText(sectionTabs, tabNames[currentSectionIndex]);
                if (tab != null && !tab.isSelected()) {
                    isProgrammaticTabChange = true;
                    tab.select();
                }
            }
        });


        sectionTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (isProgrammaticTabChange) {
                    isProgrammaticTabChange = false;
                    return;
                }

                String tabText = tab.getText() != null ? tab.getText().toString() : "";
                View targetView;
                switch (tabText) {
                    case "Watering": targetView = wateringTitle; break;
                    case "Light": targetView = lightTitle; break;
                    case "Soil": targetView = soilTitle; break;
                    case "Edible": targetView = ediblePartsTitle; break;
                    case "Propagation": targetView = propagationTitle; break;
                    case "Toxicity": targetView = toxicityExtraTitle; break;
                    default: targetView = null;
                }

                if (targetView != null) {
                    scrollView.post(() -> {
                        isProgrammaticScroll = true;

                        AppBarLayout appBar = requireActivity().findViewById(R.id.appbar_layout);
                        appBar.setExpanded(false, true);

                        scrollView.postDelayed(() -> {
                            scrollView.smoothScrollTo(0, targetView.getTop());

                            scrollView.postDelayed(() -> isProgrammaticScroll = false, 400);
                        }, 200);
                    });

                }
            }


            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        if (plant.getImageUrl() != null && !plant.getImageUrl().isEmpty()) {
            Glide.with(requireContext()).load(plant.getImageUrl()).into(image);
            image.setOnClickListener(v -> ImageDialogFragment.newInstance(plant.getImageUrl())
                    .show(requireActivity().getSupportFragmentManager(), "image_dialog"));
        }

        name.setText(plant.getCommonName());

        List<String> gptTags = plant.getGpt();
        if (gptTags != null && !gptTags.isEmpty()) {
            for (String tag : gptTags) {
                TextView tagView = new TextView(requireContext());
                tagView.setText("#" + tag);
                tagView.setTextColor(Color.parseColor("#006B3C"));
                tagView.setTextSize(14);
                tagView.setPadding(16, 8, 16, 8);
                tagView.setBackgroundResource(R.drawable.hashtag_background);

                FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(8, 4, 8, 4);
                tagView.setLayoutParams(params);

                gptTagsLayout.addView(tagView);
            }
        } else {
            gptTagsLayout.setVisibility(View.GONE);
        }

        List<String> syns = plant.getSynonyms();
        synonyms.setText(syns != null && !syns.isEmpty() ?
                "Synonyms: " + String.join(", ", syns.subList(0, Math.min(syns.size(), 3))) :
                "Synonyms: -");

        if (plant.getTaxonomy() != null) {
            family.setText("Family name: " + plant.getTaxonomy().getFamily());
            genus.setText("Genus name: " + plant.getTaxonomy().getGenus());
        } else {
            family.setText("Family name: -");
            genus.setText("Genus name: -");
        }

        careLevel.setText("Soil Moisture Level: " + (plant.getCareLevel() != null ? plant.getCareLevel() : "-"));
        wateringInterval.setText("Watering: " + (plant.getWateringInterval() != null ? plant.getWateringInterval() + " days" : "-"));

        description.setText(plant.getDescription() != null && plant.getDescription().getValue() != null ?
                plant.getDescription().getValue() : "No description available.");
        bestWatering.setText(plant.getBestWatering() != null ? plant.getBestWatering() : "Not specified.");
        bestLight.setText(plant.getBestLightCondition() != null ? plant.getBestLightCondition() : "Not specified.");
        bestSoil.setText(plant.getBestSoilType() != null ? plant.getBestSoilType() : "Not specified.");
        lightSummary.setText(plant.getLightSummary() != null ? plant.getLightSummary() : "Not specified.");


        if (plant.getToxic() == 1) {
            toxicitySummaryText.setText("Toxic");
            toxicitySummaryText.setTextColor(Color.parseColor("#8B0000"));
            toxicitySummaryText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.toxic_red, 0, 0, 0);
        } else {
            toxicitySummaryText.setText("Non-toxic");
            toxicitySummaryText.setTextColor(Color.parseColor("#006B3C"));
            toxicitySummaryText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.toxic_green, 0, 0, 0);
        }

        if (plant.getEdibleParts() != null && !plant.getEdibleParts().isEmpty()) {
            edibleParts.setText(String.join("\n", plant.getEdibleParts()));
            edibleParts.setVisibility(View.VISIBLE);
            ediblePartsTitle.setVisibility(View.VISIBLE);
        }

        if (plant.getPropagationMethods() != null && !plant.getPropagationMethods().isEmpty()) {
            propagationMethods.setText(String.join("\n", plant.getPropagationMethods()));
            propagationMethods.setVisibility(View.VISIBLE);
            propagationTitle.setVisibility(View.VISIBLE);
        }

        if (plant.getToxicity() != null && !plant.getToxicity().isEmpty()) {
            toxicityTextExtra.setText(plant.getToxicity());
            toxicityTextExtra.setVisibility(View.VISIBLE);
            toxicityExtraTitle.setVisibility(View.VISIBLE);
        }

        ImageView favoriteButtonDetails = view.findViewById(R.id.favoriteButtonDetails);
        Button addPlantButton = view.findViewById(R.id.addPlantButton);

        updateFavoriteIcon(favoriteButtonDetails, plant.isFavorite());

        favoriteButtonDetails.setOnClickListener(v -> {
            boolean newState = !plant.isFavorite();
            plant.setFavorite(newState);
            updateFavoriteIcon(favoriteButtonDetails, newState);
            updateFavoriteInFirestore(plant);
        });

        addPlantButton.setOnClickListener(v -> {
            Fragment fragment = SiteRecommendationFragment.newInstance(plant);

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



        return view;
    }

    private void updateFavoriteIcon(ImageView icon, boolean isFavorite) {
        icon.setImageResource(isFavorite ? R.drawable.favorite_red : R.drawable.favorite_border);
    }

    private void updateFavoriteInFirestore(PlantDetailsResponse plant) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference favRef = db.collection("users")
                .document(currentUser.getUid())
                .collection("favorites")
                .document(plant.getCommonName());

        if (plant.isFavorite()) {
            favRef.set(plant);
        } else {
            favRef.delete();
        }
    }


    private int getRelativeTop(View view, View container) {
        int top = 0;
        View current = view;
        while (current != null && current != container) {
            top += current.getTop();
            current = (View) current.getParent();
        }
        return top - container.getScrollY();
    }

    private TabLayout.Tab findTabByText(TabLayout tabLayout, String text) {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null && text.equalsIgnoreCase(tab.getText() != null ? tab.getText().toString() : "")) {
                return tab;
            }
        }
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Fragment currentFragment = requireActivity()
                .getSupportFragmentManager()
                .findFragmentById(R.id.frame_layout);

        if (!(currentFragment instanceof SiteRecommendationFragment)) {
            View bottomNav = requireActivity().findViewById(R.id.bottomNavigationView);
            if (bottomNav != null) bottomNav.setVisibility(View.VISIBLE);

            View settingsIcon = requireActivity().findViewById(R.id.settings);
            if (settingsIcon != null) settingsIcon.setVisibility(View.VISIBLE);
        }
    }

}