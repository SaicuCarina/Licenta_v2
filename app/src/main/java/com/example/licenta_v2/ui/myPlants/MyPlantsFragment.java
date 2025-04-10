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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licenta_v2.R;
import com.example.licenta_v2.model.SavedPlant;
import com.example.licenta_v2.ui.findplants.FindPlantsFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyPlantsFragment extends Fragment {

    private RecyclerView recyclerView;
    private MyPlantsAdapter adapter;
    private ProgressBar loadingBar;
    private List<SavedPlant> plantList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_plants, container, false);

        recyclerView = view.findViewById(R.id.myPlantsRecyclerView);
        loadingBar = view.findViewById(R.id.loadingBarMyPlants);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MyPlantsAdapter(getContext(), plantList);
        recyclerView.setAdapter(adapter);

        View emptyLayout = view.findViewById(R.id.emptyStateLayout);
        View addButton = view.findViewById(R.id.addFirstPlantButton);

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

        loadingBar.setVisibility(View.VISIBLE);
        loadMyPlants();

        return view;
    }

    private void loadMyPlants() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            if (loadingBar != null) loadingBar.setVisibility(View.GONE);
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(currentUser.getUid())
                .collection("myPlants")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    plantList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        SavedPlant savedPlant = doc.toObject(SavedPlant.class);
                        plantList.add(savedPlant);
                    }

                    adapter.notifyDataSetChanged();

                    if (loadingBar != null) loadingBar.setVisibility(View.GONE);

                    View view = getView();
                    if (view != null) {
                        View emptyLayout = view.findViewById(R.id.emptyStateLayout);
                        if (emptyLayout != null) {
                            emptyLayout.setVisibility(plantList.isEmpty() ? View.VISIBLE : View.GONE);
                            recyclerView.setVisibility(plantList.isEmpty() ? View.GONE : View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load plants: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    if (loadingBar != null) loadingBar.setVisibility(View.GONE);
                });
    }

}
