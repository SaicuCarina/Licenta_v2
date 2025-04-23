package com.example.licenta_v2.ui.myPlants;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.licenta_v2.R;
import com.example.licenta_v2.model.PlantDetailsResponse;
import com.example.licenta_v2.model.SavedPlant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class GroupedMyPlantsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_PLANT = 1;

    private final Context context;
    private final List<Object> items = new ArrayList<>();
    private final OnPlantClickListener clickListener;
    private final Runnable onPlantEdited;

    public GroupedMyPlantsAdapter(Context context, OnPlantClickListener clickListener, Runnable onPlantEdited) {
        this.context = context;
        this.clickListener = clickListener;
        this.onPlantEdited = onPlantEdited;
    }

    public void setGroupedItems(List<com.example.licenta_v2.model.GroupedPlantItem> groupedList) {
        items.clear();
        for (com.example.licenta_v2.model.GroupedPlantItem group : groupedList) {
            items.add(group.getRoomName());
            items.addAll(group.getPlants());
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return (items.get(position) instanceof String) ? TYPE_HEADER : TYPE_PLANT;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_plant_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_my_plant, parent, false);
            return new PlantViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).headerTitle.setText((String) items.get(position));
        } else if (holder instanceof PlantViewHolder) {
            SavedPlant savedPlant = (SavedPlant) items.get(position);
            PlantDetailsResponse plant = savedPlant.getPlantData();
            PlantViewHolder vh = (PlantViewHolder) holder;

            String displayName = savedPlant.getCustomName() != null && !savedPlant.getCustomName().isEmpty()
                    ? savedPlant.getCustomName()
                    : plant.getCommonName();
            vh.siteName.setText(displayName);

            String date = savedPlant.getAddedDate() != null ? savedPlant.getAddedDate() : "Unknown date";
            String wateringInterval = plant.getWateringInterval() != null ? plant.getWateringInterval() : "Unknown";
            vh.siteInfo.setText("Watering every " + wateringInterval + " days\nAdded: " + date);

            String imageUrl = plant.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                if (!imageUrl.startsWith("data:image")) {
                    imageUrl = "data:image/png;base64," + imageUrl;
                }
                Glide.with(context).load(imageUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(vh.siteImage);
            } else {
                vh.siteImage.setImageResource(R.drawable.ic_launcher_background);
            }

            // Load dropdown sites
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                List<String> siteOptions = new ArrayList<>();

                db.collection("sites").get()
                        .addOnSuccessListener(siteSnapshots -> {
                            for (var doc : siteSnapshots) {
                                String name = doc.getString("name");
                                if (name != null) siteOptions.add(name);
                            }

                            db.collection("users")
                                    .document(currentUser.getUid())
                                    .collection("customSites")
                                    .get()
                                    .addOnSuccessListener(customSnapshots -> {
                                        for (var doc : customSnapshots) {
                                            String name = doc.getString("name");
                                            if (name != null && !siteOptions.contains(name)) {
                                                siteOptions.add(name);
                                            }
                                        }

                                        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                                                android.R.layout.simple_dropdown_item_1line, siteOptions);
                                        if (vh.editSiteInput instanceof AutoCompleteTextView) {
                                            AutoCompleteTextView actv = (AutoCompleteTextView) vh.editSiteInput;
                                            actv.setAdapter(adapter);
                                            actv.setText(savedPlant.getAddedSite(), false);
                                            actv.setOnClickListener(v -> actv.showDropDown());
                                        }
                                    });
                        });
            }

            // Delete
            vh.deleteButton.setOnClickListener(v -> {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users")
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .collection("myPlants")
                        .document(savedPlant.getId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "Plant removed", Toast.LENGTH_SHORT).show();
                            items.remove(position);
                            notifyItemRemoved(position);
                            if (position > 0 && items.get(position - 1) instanceof String &&
                                    (position == items.size() || items.get(position) instanceof String)) {
                                items.remove(position - 1);
                                notifyItemRemoved(position - 1);
                            }
                            notifyItemRangeChanged(position, getItemCount() - position);
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(context, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            });

            // Toggle edit
            vh.editButton.setOnClickListener(v -> {
                vh.editPopup.setVisibility(
                        vh.editPopup.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
            });

            // Save edit
            vh.saveEditButton.setOnClickListener(v -> {
                String newName = vh.editPlantNameInput.getText().toString().trim();
                String newSite = vh.editSiteInput.getText().toString().trim();

                if (!newName.isEmpty()) savedPlant.setCustomName(newName);
                if (!newSite.isEmpty()) savedPlant.setAddedSite(newSite);

                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .collection("myPlants")
                        .document(savedPlant.getId())
                        .set(savedPlant)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "Updated!", Toast.LENGTH_SHORT).show();
                            onPlantEdited.run();
                            vh.editPopup.setVisibility(View.GONE);
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(context, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            });

            // Open detail
            vh.itemView.setOnClickListener(v -> clickListener.onPlantClicked(savedPlant));
        }
    }

    public interface OnPlantClickListener {
        void onPlantClicked(SavedPlant plant);
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerTitle;
        HeaderViewHolder(View view) {
            super(view);
            headerTitle = view.findViewById(R.id.plantGroupTitle);
        }
    }

    static class PlantViewHolder extends RecyclerView.ViewHolder {
        ImageView siteImage, deleteButton, editButton;
        TextView siteName, siteInfo;
        LinearLayout editPopup;
        EditText editPlantNameInput;
        AutoCompleteTextView editSiteInput; // changed from EditText
        Button saveEditButton;

        PlantViewHolder(View view) {
            super(view);
            siteImage = view.findViewById(R.id.siteImage);
            siteName = view.findViewById(R.id.siteName);
            siteInfo = view.findViewById(R.id.siteInfo);
            deleteButton = view.findViewById(R.id.deleteButton);
            editButton = view.findViewById(R.id.editButton);
            editPopup = view.findViewById(R.id.editPopup);
            editPlantNameInput = view.findViewById(R.id.editPlantNameInput);
            editSiteInput = view.findViewById(R.id.editSiteInput); // casted as AutoCompleteTextView
            saveEditButton = view.findViewById(R.id.saveEditButton);
        }
    }
}
