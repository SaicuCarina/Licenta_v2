package com.example.licenta_v2.ui.myPlants;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licenta_v2.R;
import com.example.licenta_v2.model.GroupedPlantItem;
import com.example.licenta_v2.model.SavedPlant;
import com.example.licenta_v2.ui.details.PlantDetailsFragment;
import com.example.licenta_v2.ui.favorites.FavoritesFragment;
import com.example.licenta_v2.ui.findplants.FindPlantsFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyPlantsFragment extends Fragment {

    private RecyclerView recyclerView;
    private GroupedMyPlantsAdapter adapter;
    private ProgressBar loadingBar;
    private View emptyLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_plants, container, false);

        recyclerView = view.findViewById(R.id.myPlantsRecyclerView);
        loadingBar = view.findViewById(R.id.loadingBarMyPlants);
        emptyLayout = view.findViewById(R.id.emptyStateLayout);
        View addButton = view.findViewById(R.id.addFirstPlantButton);

        View favoriteButton = view.findViewById(R.id.favorite);
        favoriteButton.setOnClickListener(v -> {
            FavoritesFragment fragment = FavoritesFragment.newInstance("myPlants");

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, fragment)
                    .addToBackStack(null)
                    .commit();

        });


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GroupedMyPlantsAdapter(
                getContext(),
                plant -> {
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame_layout, PlantDetailsFragment.newInstance(plant.getPlantData()))
                            .addToBackStack(null)
                            .commit();
                },
                this::loadMyPlants
        );

        recyclerView.setAdapter(adapter);

        addButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, new FindPlantsFragment())
                    .addToBackStack(null)
                    .commit();

            View bottomNavView = requireActivity().findViewById(R.id.bottomNavigationView);
            if (bottomNavView instanceof com.google.android.material.bottomnavigation.BottomNavigationView) {
                ((com.google.android.material.bottomnavigation.BottomNavigationView) bottomNavView)
                        .setSelectedItemId(R.id.findPlants);
            }
        });

        loadMyPlants();
        return view;
    }

    private void loadMyPlants() {
        loadingBar.setVisibility(View.VISIBLE);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            showError("User not logged in");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(currentUser.getUid())
                .collection("myPlants")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, List<SavedPlant>> grouped = new HashMap<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        SavedPlant plant = doc.toObject(SavedPlant.class);
                        String room = plant.getAddedSite();
                        if (!grouped.containsKey(room)) {
                            grouped.put(room, new ArrayList<>());
                        }
                        grouped.get(room).add(plant);
                    }

                    List<GroupedPlantItem> groupedList = new ArrayList<>();
                    for (Map.Entry<String, List<SavedPlant>> entry : grouped.entrySet()) {
                        groupedList.add(new GroupedPlantItem(entry.getKey(), entry.getValue()));
                    }

                    adapter.setGroupedItems(groupedList);

                    boolean isEmpty = groupedList.isEmpty();
                    emptyLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                    loadingBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> showError("Error loading plants: " + e.getMessage()));
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        loadingBar.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();

        View bottomNavView = requireActivity().findViewById(R.id.bottomNavigationView);
        if (bottomNavView != null) {
            bottomNavView.setVisibility(View.VISIBLE);
        }
    }

}
