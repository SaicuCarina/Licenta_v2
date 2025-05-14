package com.example.licenta_v2.ui.findplants;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.licenta_v2.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FilterFragment extends Fragment {

    private final String[] LIGHT_OPTIONS = {"Full sun", "Partial sun", "Full shade"};
    private final String[] WATER_OPTIONS = {"Dry", "Dry to Medium", "Medium", "Medium to Wet", "Wet"};
    private final String[] TOXICITY_OPTIONS = {"Pet-friendly", "Toxic"};

    private final List<String> selectedLight = new ArrayList<>();
    private final List<String> selectedWater = new ArrayList<>();
    private final List<String> selectedToxicity = new ArrayList<>();
    private final List<String> selectedFeatures = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            selectedLight.clear();
            selectedWater.clear();
            selectedToxicity.clear();
            selectedFeatures.clear();

            selectedLight.addAll(args.getStringArrayList("light") != null ? args.getStringArrayList("light") : new ArrayList<>());
            selectedWater.addAll(args.getStringArrayList("water") != null ? args.getStringArrayList("water") : new ArrayList<>());
            selectedToxicity.addAll(args.getStringArrayList("toxicity") != null ? args.getStringArrayList("toxicity") : new ArrayList<>());
            selectedFeatures.addAll(args.getStringArrayList("features") != null ? args.getStringArrayList("features") : new ArrayList<>());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter, container, false);

        View bottomNav = requireActivity().findViewById(R.id.bottomNavigationView);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }

        ImageView backBtn = view.findViewById(R.id.back_filter);
        backBtn.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        populateStaticFilters(view);
        fetchAndPopulateTopGptTags(view);

        view.findViewById(R.id.resetFilters).setOnClickListener(v -> resetAll());
        view.findViewById(R.id.applyFilters).setOnClickListener(v -> applyFilters());
    }

    private void populateStaticFilters(View view) {
        GridLayout lightGroup = view.findViewById(R.id.light_filter_group);
        GridLayout waterGroup = view.findViewById(R.id.water_filter_group);
        GridLayout toxGroup = view.findViewById(R.id.toxicity_filter_group);

        for (String light : LIGHT_OPTIONS) {
            lightGroup.addView(createSelectableTag(light, selectedLight));
        }

        for (String water : WATER_OPTIONS) {
            waterGroup.addView(createSelectableTag(water, selectedWater));
        }

        for (String tox : TOXICITY_OPTIONS) {
            toxGroup.addView(createSelectableTag(tox, selectedToxicity));
        }
    }

    private void fetchAndPopulateTopGptTags(View view) {
        GridLayout featuresGroup = view.findViewById(R.id.features_filter_group);

        FirebaseFirestore.getInstance().collection("plants").get()
                .addOnSuccessListener(snapshot -> {
                    Map<String, Integer> tagFrequency = new HashMap<>();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        List<String> tags = (List<String>) doc.get("gpt");
                        if (tags != null) {
                            for (String tag : tags) {
                                tagFrequency.put(tag, tagFrequency.getOrDefault(tag, 0) + 1);
                            }
                        }
                    }

                    List<String> topTags = tagFrequency.entrySet().stream()
                            .sorted((a, b) -> b.getValue() - a.getValue())
                            .limit(8)
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toList());

                    for (String tag : topTags) {
                        featuresGroup.addView(createSelectableTag(tag, selectedFeatures));
                    }
                });
    }

    private TextView createSelectableTag(String text, List<String> selectionList) {
        TextView tag = new TextView(requireContext());
        tag.setText(text);
        tag.setPadding(24, 16, 24, 16);
        tag.setBackgroundResource(R.drawable.tag_background);
        tag.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        tag.setClickable(true);
        tag.setFocusable(true);
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        int marginInDp = 2;
        int marginInPx = (int) (marginInDp * getResources().getDisplayMetrics().density);
        params.setMargins(marginInPx, marginInPx, marginInPx, marginInPx);

        tag.setLayoutParams(params);

        if (selectionList.contains(text)) {
            tag.setBackgroundResource(R.drawable.tag_selected_background);
        }

        tag.setOnClickListener(v -> {
            if (selectionList.contains(text)) {
                selectionList.remove(text);
                tag.setBackgroundResource(R.drawable.tag_background);
            } else {
                selectionList.add(text);
                tag.setBackgroundResource(R.drawable.tag_selected_background);
            }
        });

        return tag;
    }

    private void resetAll() {
        selectedLight.clear();
        selectedWater.clear();
        selectedToxicity.clear();
        selectedFeatures.clear();

        View view = getView();
        if (view != null) {
            ((GridLayout) view.findViewById(R.id.light_filter_group)).removeAllViews();
            ((GridLayout) view.findViewById(R.id.water_filter_group)).removeAllViews();
            ((GridLayout) view.findViewById(R.id.toxicity_filter_group)).removeAllViews();
            ((GridLayout) view.findViewById(R.id.features_filter_group)).removeAllViews();
            populateStaticFilters(view);
            fetchAndPopulateTopGptTags(view);
        }
    }

    private void applyFilters() {
        Bundle result = new Bundle();
        result.putStringArrayList("light", new ArrayList<>(selectedLight));
        result.putStringArrayList("water", new ArrayList<>(selectedWater));
        result.putStringArrayList("toxicity", new ArrayList<>(selectedToxicity));
        result.putStringArrayList("features", new ArrayList<>(selectedFeatures));

        getParentFragmentManager().setFragmentResult("filters_applied", result);

        requireActivity().getSupportFragmentManager().popBackStack();
    }


}
