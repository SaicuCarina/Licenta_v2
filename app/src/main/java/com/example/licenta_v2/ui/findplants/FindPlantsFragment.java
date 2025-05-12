package com.example.licenta_v2.ui.findplants;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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
import androidx.recyclerview.widget.RecyclerView;

import com.example.licenta_v2.R;
import com.example.licenta_v2.model.PlantDetailsResponse;
import com.example.licenta_v2.ui.details.PlantDetailsFragment;
import com.example.licenta_v2.ui.favorites.FavoritesFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
    private List<PlantDetailsResponse> allPlants = new ArrayList<>();
    private Bitmap lastCapturedImage;
    private EditText searchEditText;

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

    private void applyFilterAfterLoading() {
        String query = searchEditText.getText().toString().trim();
        filterPlantsByPartialMatch(query);
    }

    private void loadPlantsFromFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        loadingBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        db.collection("plants").get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<PlantDetailsResponse> fetchedPlants = new ArrayList<>();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                PlantDetailsResponse plant = doc.toObject(PlantDetailsResponse.class);
                plant.setFavorite(false);
                fetchedPlants.add(plant);
            }

            if (user != null) {
                db.collection("users")
                        .document(user.getUid())
                        .collection("favorites")
                        .get()
                        .addOnSuccessListener(favSnapshots -> {
                            List<String> favoriteNames = new ArrayList<>();
                            for (QueryDocumentSnapshot favDoc : favSnapshots) {
                                String favName = favDoc.getId();
                                if (favName != null) favoriteNames.add(favName);
                            }
                            for (PlantDetailsResponse plant : fetchedPlants) {
                                if (plant.getCommonName() != null &&
                                        favoriteNames.stream().anyMatch(fav -> fav.trim().equalsIgnoreCase(plant.getCommonName().trim()))) {
                                    plant.setFavorite(true);
                                }
                            }
                            allPlants.clear();
                            allPlants.addAll(fetchedPlants);
                            applyFilterAfterLoading();
                            loadingBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        })
                        .addOnFailureListener(e -> {
                            allPlants.clear();
                            allPlants.addAll(fetchedPlants);
                            applyFilterAfterLoading();
                            loadingBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        });
            } else {
                allPlants.clear();
                allPlants.addAll(fetchedPlants);
                applyFilterAfterLoading();
                loadingBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(e -> {
            Log.e("Firebase", "Eroare la plante: " + e.getMessage());
            loadingBar.setVisibility(View.GONE);
        });
    }

    private void filterPlantsByPartialMatch(String query) {
        if (query.isEmpty()) {
            adapter.updateList(allPlants);
            return;
        }
        String lowerQuery = query.toLowerCase();
        List<PlantDetailsResponse> filteredList = new ArrayList<>();
        for (PlantDetailsResponse plant : allPlants) {
            boolean matchesCommonName = plant.getCommonName() != null &&
                    plant.getCommonName().toLowerCase().contains(lowerQuery);
            boolean matchesSynonyms = plant.getSynonyms() != null &&
                    plant.getSynonyms().stream().anyMatch(s -> s.toLowerCase().contains(lowerQuery));
            boolean matchesGpt = plant.getGpt() != null &&
                    plant.getGpt().stream().anyMatch(tag -> tag.toLowerCase().contains(lowerQuery));
            if (matchesCommonName || matchesSynonyms || matchesGpt) {
                filteredList.add(plant);
            }
        }
        adapter.updateList(filteredList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_plants, container, false);

        view.setOnTouchListener((v, event) -> {
            hideKeyboard();
            searchEditText.clearFocus();
            return false;
        });

        View bottomNav = requireActivity().findViewById(R.id.bottomNavigationView);
        if (bottomNav != null) bottomNav.setVisibility(View.VISIBLE);

        identifyButton = view.findViewById(R.id.identify);
        searchEditText = view.findViewById(R.id.search);
        recyclerView = view.findViewById(R.id.plantsRecyclerView);
        loadingBar = view.findViewById(R.id.loadingBar);

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                hideKeyboard();
                searchEditText.clearFocus();
                return true;
            }
            return false;
        });

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true));

        adapter = new PlantAdapter(allPlants, plant -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, PlantDetailsFragment.newInstance(plant))
                    .addToBackStack(null)
                    .commit();
        });
        recyclerView.setAdapter(adapter);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                filterPlantsByPartialMatch(s.toString().trim());
            }
        });

        loadPlantsFromFirebase();
        observeViewModel();
        identifyButton.setOnClickListener(v -> openCamera());

        ImageView favoriteIcon = view.findViewById(R.id.favorite);
        favoriteIcon.setOnClickListener(v -> {
            FavoritesFragment fragment = FavoritesFragment.newInstance("findPlants");
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, fragment)
                    .addToBackStack(null)
                    .commit();
        });

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
            loadingBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            identifyButton.setEnabled(!isLoading);
        });

        viewModel.getPlantDetails().observe(getViewLifecycleOwner(), plant -> {
            if (plant != null) showPlantDetails(plant);
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

    private void hideKeyboard() {
        View view = requireActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (searchEditText != null) {
            String currentQuery = searchEditText.getText().toString().trim();
            filterPlantsByPartialMatch(currentQuery);
        }
    }
}