package com.example.licenta_v2.ui.myPlants;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.licenta_v2.R;
import com.example.licenta_v2.model.Site;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SiteRecommendationFragment extends Fragment {

    private FirebaseFirestore db;
    private String lightSummary;
    private List<Site> recommendedSites = new ArrayList<>();
    private List<Site> notRecommendedSites = new ArrayList<>();

    private GridLayout recommendedContainer;
    private GridLayout notRecommendedContainer;
    private ProgressBar loadingSpinner;
    private ScrollView scrollableContent;

    public SiteRecommendationFragment() {}

    public static SiteRecommendationFragment newInstance(String lightSummary) {
        SiteRecommendationFragment fragment = new SiteRecommendationFragment();
        Bundle args = new Bundle();
        args.putString("light_summary", lightSummary);
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
            lightSummary = getArguments().getString("light_summary");
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ascundem nav bar-ul jos
        View bottomNav = requireActivity().findViewById(R.id.bottomNavigationView);
        if (bottomNav != null) bottomNav.setVisibility(View.GONE);

        // Începem încărcarea datelor
        db = FirebaseFirestore.getInstance();
        loadSites();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Reafisăm nav bar-ul când ieșim
        View bottomNav = requireActivity().findViewById(R.id.bottomNavigationView);
        if (bottomNav != null) bottomNav.setVisibility(View.VISIBLE);
    }

    private void loadSites() {
        loadingSpinner.setVisibility(View.VISIBLE);
        scrollableContent.setVisibility(View.GONE);

        db.collection("sites")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    recommendedSites.clear();
                    notRecommendedSites.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Site site = document.toObject(Site.class);

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

        nameView.setText(site.getName());

        String imageBase64 = site.getPhoto();
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            if (!imageBase64.startsWith("data:image")) {
                imageBase64 = "data:image/png;base64," + imageBase64;
            }

            Glide.with(getContext())
                    .load(imageBase64)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_launcher_background);
        }

        view.setOnClickListener(v -> onSiteClicked(site));
    }

    private void onSiteClicked(Site site) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference plantRef = db.collection("users")
                .document(currentUser.getUid())
                .collection("myPlants")
                .document(site.getName());

        Toast.makeText(getContext(), "Plant added to " + site.getName(), Toast.LENGTH_SHORT).show();
    }
}
