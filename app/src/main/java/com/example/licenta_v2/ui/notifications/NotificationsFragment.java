package com.example.licenta_v2.ui.notifications;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.ImageButton;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar loadingBar;
    private TextView emptyMessage;
    private NotificationsAdapter adapter;
    private final List<SavedPlant> plantsToWater = new ArrayList<>();
    private final Map<CalendarDay, List<SavedPlant>> plantsByDay = new HashMap<>();



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

        MaterialCalendarView calendarView = view.findViewById(R.id.calendarView);
        ImageButton toggleCalendarButton = view.findViewById(R.id.toggleCalendarButton);
        calendarView.setVisibility(View.GONE); // ascuns inițial

        toggleCalendarButton.setOnClickListener(v -> {
            if (calendarView.getVisibility() == View.VISIBLE) {
                calendarView.setVisibility(View.GONE);
            } else {
                calendarView.setVisibility(View.VISIBLE);
            }
        });


        loadPlantsToWater();

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            List<SavedPlant> plantsForThatDay = plantsByDay.get(date);

            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_plants, null);

            TextView title = dialogView.findViewById(R.id.bottomSheetTitle);
            RecyclerView recyclerView = dialogView.findViewById(R.id.plantRecyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault());
            Calendar cal = Calendar.getInstance();
            cal.set(date.getYear(), date.getMonth() - 1, date.getDay());
            String formattedDate = sdf.format(cal.getTime());

            if (plantsForThatDay != null && !plantsForThatDay.isEmpty()) {
                title.setText("Plants to water on " + formattedDate);
                recyclerView.setAdapter(new PlantDayAdapter(plantsForThatDay));
            } else {
                title.setText("No plants to water on " + formattedDate);
            }


            new AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .setPositiveButton("OK", null)
                    .show();

        });


        return view;
    }

    static class Triplet<A, B, C> {
        public final A first;
        public final B second;
        public final C third;

        public Triplet(A first, B second, C third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }
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
                    plantsByDay.clear();

                    List<SavedPlant> allPlants = new ArrayList<>();
                    List<Triplet<SavedPlant, Integer, Integer>> plantIntervals = new ArrayList<>();
                    List<CalendarDay> markedDays = new ArrayList<>();
                    List<CalendarDay> allFutureWateringDates = new ArrayList<>();

                    Calendar today = Calendar.getInstance();
                    Date now = new Date();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                    for (QueryDocumentSnapshot doc : snapshot) {
                        SavedPlant plant = doc.toObject(SavedPlant.class);
                        plant.setId(doc.getId());

                        if (plant.getPlantData() == null || plant.getPlantData().getWateringInterval() == null)
                            continue;

                        try {
                            String intervalStr = plant.getPlantData().getWateringInterval().replaceAll("[^0-9\\-]", "-");
                            String[] parts = intervalStr.split("-");
                            int minDays = Integer.parseInt(parts[0].trim());
                            int maxDays = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : minDays;

                            Date lastWateredDate = format.parse(plant.getLastWateredDate());
                            long daysSince = TimeUnit.DAYS.convert(now.getTime() - lastWateredDate.getTime(), TimeUnit.MILLISECONDS);

                            // Calculează corect intervalul față de azi
                            int start = (int) (minDays - daysSince);
                            int end = (int) (maxDays - daysSince);

                            if (end < 0 || start > 30) continue;

                            start = Math.max(0, start);
                            end = Math.min(30, end);

                            plantIntervals.add(new Triplet<>(plant, start, end));

                            // doar pentru marcarea estimativă (nu afectează optimizarea)
                            Calendar future = Calendar.getInstance();
                            future.setTime(lastWateredDate);
                            future.add(Calendar.DATE, minDays);
                            allFutureWateringDates.add(CalendarDay.from(future));

                        } catch (Exception e) {
                            Log.e("OptimizareDebug", "Eroare parsare interval: " + e.getMessage());
                        }
                    }

                    // Sortează după capătul din dreapta (end)
                    plantIntervals.sort(Comparator.comparingInt(p -> p.third));

                    int i = 0;
                    while (i < plantIntervals.size()) {
                        Triplet<SavedPlant, Integer, Integer> current = plantIntervals.get(i);
                        int groupStart = current.second;
                        int groupEnd = current.third;

                        List<Triplet<SavedPlant, Integer, Integer>> cluster = new ArrayList<>();
                        cluster.add(current);
                        i++;

                        // Caută alte intervale care se intersectează
                        while (i < plantIntervals.size()) {
                            Triplet<SavedPlant, Integer, Integer> next = plantIntervals.get(i);
                            if (next.second <= groupEnd) {
                                cluster.add(next);
                                groupEnd = Math.min(groupEnd, next.third); // opțional
                                i++;
                            } else {
                                break;
                            }
                        }

                        int chosenDay;
                        if (cluster.size() == 1) {
                            Triplet<SavedPlant, Integer, Integer> only = cluster.get(0);
                            chosenDay = (only.second + only.third) / 2;
                        } else {
                            chosenDay = current.third;
                        }

                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.DATE, chosenDay);
                        CalendarDay notificationDay = CalendarDay.from(cal);

                        List<SavedPlant> validPlants = new ArrayList<>();
                        for (Triplet<SavedPlant, Integer, Integer> t : cluster) {
                            if (t.first != null && t.first.getPlantData() != null) {
                                validPlants.add(t.first);
                            }
                        }

                        if (!validPlants.isEmpty()) {
                            plantsByDay.put(notificationDay, validPlants);
                            markedDays.add(notificationDay);
                        }
                    }

                    // doar plantele pentru azi, în pagina principală
                    CalendarDay todayDay = CalendarDay.from(Calendar.getInstance());
                    if (plantsByDay.containsKey(todayDay)) {
                        plantsToWater.addAll(plantsByDay.get(todayDay));
                    }

                    if (getView() != null) {
                        MaterialCalendarView calendarView = getView().findViewById(R.id.calendarView);
                        if (calendarView != null) {
                            calendarView.removeDecorators();
                            calendarView.addDecorator(new DotDecorator(markedDays, Color.parseColor("#4CAF50")));
//                            calendarView.addDecorator(new DotDecorator(allFutureWateringDates, Color.parseColor("#A5D6A7")));
                        }
                    }

                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load plants", Toast.LENGTH_SHORT).show())
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
                    .update("lastWateredDate", today)
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
