package com.example.licenta_v2.ui.notifications;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licenta_v2.R;
import com.example.licenta_v2.model.SavedPlant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar loadingBar;
    private TextView emptyMessage;
    private NotificationsAdapter adapter;
    private final List<SavedPlant> plantsToWater = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        recyclerView = view.findViewById(R.id.notificationsRecyclerView);
        loadingBar = view.findViewById(R.id.loadingBar);
        emptyMessage = view.findViewById(R.id.emptyMessage);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationsAdapter(plantsToWater, this::onPlantWatered);
        recyclerView.setAdapter(adapter);

        loadPlantsToWater();

        return view;
    }

    private void loadPlantsToWater() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        loadingBar.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("myPlants")
                .get()
                .addOnSuccessListener(snapshot -> {
                    plantsToWater.clear();
                    List<Pair<Integer, Integer>> intervals = new ArrayList<>();
                    List<SavedPlant> allPlants = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        SavedPlant plant = doc.toObject(SavedPlant.class);
                        plant.setId(doc.getId());

                        if (plant.getPlantData() == null || plant.getPlantData().getWateringInterval() == null) continue;

                        try {
                            String intervalStr = plant.getPlantData().getWateringInterval().replaceAll("[^0-9\\-]", "-");
                            String[] parts = intervalStr.split("-");
                            int minDays = Integer.parseInt(parts[0].trim());
                            int maxDays = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : minDays;

                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            Date lastWateredDate = format.parse(plant.getLastWateredDate());
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(lastWateredDate);
                            cal.add(Calendar.DATE, minDays);
                            int start = (int) ((cal.getTimeInMillis() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24));
                            cal.setTime(lastWateredDate);
                            cal.add(Calendar.DATE, maxDays);
                            int end = (int) ((cal.getTimeInMillis() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24));

                            int today = 0;
                            intervals.add(new Pair<>(start + today, end + today));
                            allPlants.add(plant);
                        } catch (Exception e) {
                            Log.e("OptimizareDebug", "Eroare parsare interval: " + e.getMessage());
                        }
                    }

                    // Greedy pe intervale
                    List<Integer> wateringDays = new ArrayList<>();
                    List<SavedPlant> selectedPlants = new ArrayList<>();

                    List<Integer> indexes = new ArrayList<>();
                    for (int i = 0; i < intervals.size(); i++) indexes.add(i);

                    indexes.sort(Comparator.comparingInt(i -> intervals.get(i).second));

                    int lastDay = Integer.MIN_VALUE;
                    for (int i : indexes) {
                        int start = intervals.get(i).first;
                        int end = intervals.get(i).second;
                        if (start > lastDay) {
                            lastDay = end;
                            wateringDays.add(end);
                        }
                    }

                    for (int i = 0; i < intervals.size(); i++) {
                        int start = intervals.get(i).first;
                        int end = intervals.get(i).second;
                        for (int day : wateringDays) {
                            if (start <= day && day <= end) {
                                selectedPlants.add(allPlants.get(i));
                                break;
                            }
                        }
                    }

                    plantsToWater.clear();
                    plantsToWater.addAll(selectedPlants);
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load plants", Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> loadingBar.setVisibility(View.GONE));
    }


    private void onPlantWatered(SavedPlant plant) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        plant.setLastWateredDate(today);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && plant.getId() != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .collection("myPlants")
                    .document(plant.getId())
                    .set(plant)
                    .addOnSuccessListener(aVoid -> {
                        plantsToWater.remove(plant);
                        adapter.notifyDataSetChanged();
                        updateEmptyState();
                    });
        }
    }

    private void updateEmptyState() {
        boolean isEmpty = plantsToWater.isEmpty();
        emptyMessage.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

}
