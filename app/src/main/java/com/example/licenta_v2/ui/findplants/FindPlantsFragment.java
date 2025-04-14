package com.example.licenta_v2.ui.findplants;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licenta_v2.R;
import com.example.licenta_v2.model.PlantDetailsResponse;
import com.example.licenta_v2.ui.details.PlantDetailsFragment;
import com.example.licenta_v2.ui.favorites.FavoritesFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class FindPlantsFragment extends Fragment {

    private Button identifyButton;
    private ProgressBar loadingBar;

    private FindPlantsViewModel viewModel;
    private ActivityResultLauncher<Intent> cameraLauncher;

    private static final int CAMERA_PERMISSION_CODE = 100;

    private RecyclerView recyclerView;
    private PlantAdapter adapter;
    private List<PlantDetailsResponse> plantList = new ArrayList<>();
    private Bitmap lastCapturedImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(FindPlantsViewModel.class);

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Bitmap imageBitmap = (Bitmap) result.getData().getExtras().get("data");
                        if (imageBitmap != null) {
                            lastCapturedImage = imageBitmap;

                            String base64Image = convertBitmapToBase64(imageBitmap);
                            viewModel.identifyPlant(base64Image);
                        }
                    }
                }
        );
    }

    private void loadPlantsFromFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        loadingBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        db.collection("plants").get().addOnSuccessListener(queryDocumentSnapshots -> {
            plantList.clear();
            List<PlantDetailsResponse> allPlants = new ArrayList<>();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                PlantDetailsResponse plant = doc.toObject(PlantDetailsResponse.class);
                plant.setFavorite(false); // default
                allPlants.add(plant);
            }

            if (user != null) {
                db.collection("users")
                        .document(user.getUid())
                        .collection("favorites")
                        .get()
                        .addOnSuccessListener(favSnapshots -> {
                            List<String> favoriteNames = new ArrayList<>();
                            for (QueryDocumentSnapshot favDoc : favSnapshots) {
                                String favName = favDoc.getId();  // AICI E MODIFICAREA
                                if (favName != null) {
                                    favoriteNames.add(favName);
                                }
                            }

                            for (PlantDetailsResponse plant : allPlants) {
                                String plantName = plant.getCommonName();
                                if (plantName != null &&
                                        favoriteNames.stream().anyMatch(fav -> fav.trim().equalsIgnoreCase(plantName.trim()))) {
                                    plant.setFavorite(true);
                                }
                            }

                            plantList.clear();
                            plantList.addAll(allPlants);
                            adapter.updateList(plantList);
                            loadingBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firebase", "Eroare la favorite: " + e.getMessage());
                            plantList.clear();
                            plantList.addAll(allPlants);
                            adapter.updateList(plantList);
                            loadingBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        });
            } else {
                plantList.clear();
                plantList.addAll(allPlants);
                adapter.updateList(plantList);
                loadingBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(e -> {
            Log.e("Firebase", "Eroare la plante: " + e.getMessage());
            loadingBar.setVisibility(View.GONE);
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_plants, container, false);

        View bottomNav = requireActivity().findViewById(R.id.bottomNavigationView);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.VISIBLE);
        }


        identifyButton = view.findViewById(R.id.identify);
        recyclerView = view.findViewById(R.id.plantsRecyclerView);
        loadingBar = view.findViewById(R.id.loadingBar);

        recyclerView.setVisibility(View.GONE);
        loadingBar.setVisibility(View.GONE);

        ImageView favoriteIcon = view.findViewById(R.id.favorite);
        favoriteIcon.setOnClickListener(v -> {
            FavoritesFragment fragment = FavoritesFragment.newInstance("findPlants");

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        identifyButton.setOnClickListener(v -> openCamera());

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true));
        adapter = new PlantAdapter(plantList, plant -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, PlantDetailsFragment.newInstance(plant))
                    .addToBackStack(null)
                    .commit();
        });

        recyclerView.setAdapter(adapter);

        loadPlantsFromFirebase();
        observeViewModel();

        return view;
    }


    private void openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            launchCamera();
        }
    }

    private void launchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(takePictureIntent);
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                loadingBar.setVisibility(View.VISIBLE);
                identifyButton.setEnabled(false);
            } else {
                loadingBar.setVisibility(View.GONE);
                identifyButton.setEnabled(true);
            }
        });

        viewModel.getPlantDetails().observe(getViewLifecycleOwner(), plant -> {
            if (plant != null) {
                showPlantDetails(plant);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showPlantDetails(PlantDetailsResponse details) {
        new Handler(Looper.getMainLooper()).post(() -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, PlantDetailsFragment.newInstance(details))
                    .addToBackStack(null)
                    .commit();

            viewModel.setPlantDetails(null);
        });
    }


    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] byteArray = baos.toByteArray();
        return "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }
}
