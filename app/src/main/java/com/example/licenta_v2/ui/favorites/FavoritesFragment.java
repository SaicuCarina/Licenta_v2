package com.example.licenta_v2.ui.favorites;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licenta_v2.R;
import com.example.licenta_v2.model.PlantDetailsResponse;
import com.example.licenta_v2.ui.favorites.FavoritePlantAdapter;
import com.example.licenta_v2.ui.findplants.GridSpacingItemDecoration;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private RecyclerView recyclerView;
    private FavoritePlantAdapter adapter;
    private List<PlantDetailsResponse> favoritePlants = new ArrayList<>();
    private ProgressBar loadingBar;
    public static final String ARG_PREVIOUS_FRAGMENT = "previous_fragment";

    public static FavoritesFragment newInstance(String from) {
        FavoritesFragment fragment = new FavoritesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PREVIOUS_FRAGMENT, from);
        fragment.setArguments(args);
        return fragment;
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        loadingBar = view.findViewById(R.id.loadingBarFavorites);

        View bottomNav = requireActivity().findViewById(R.id.bottomNavigationView);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }

        recyclerView = view.findViewById(R.id.favoritesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new FavoritePlantAdapter(favoritePlants);
        recyclerView.setAdapter(adapter);

        ImageView backButton = view.findViewById(R.id.back_favorite);
        backButton.setOnClickListener(v -> {
            String previous = getArguments() != null ? getArguments().getString(ARG_PREVIOUS_FRAGMENT) : null;
            Fragment destinationFragment;

            if ("myPlants".equals(previous)) {
                destinationFragment = new com.example.licenta_v2.ui.myPlants.MyPlantsFragment();
            } else {
                destinationFragment = new com.example.licenta_v2.ui.findplants.FindPlantsFragment();
            }

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, destinationFragment)
                    .addToBackStack(null)
                    .commit();
        });


        loadFavoritesFromFirestore();

        return view;
    }
    private void loadFavoritesFromFirestore() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        loadingBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("favorites")
                .get()
                .addOnSuccessListener(snapshot -> {
                    favoritePlants.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        PlantDetailsResponse plant = doc.toObject(PlantDetailsResponse.class);
                        plant.setFavorite(true);
                        favoritePlants.add(plant);
                    }
                    adapter.updateList(favoritePlants);
                    loadingBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    Log.e("Favorites", "Error loading favorites", e);
                    loadingBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                });
    }

}
