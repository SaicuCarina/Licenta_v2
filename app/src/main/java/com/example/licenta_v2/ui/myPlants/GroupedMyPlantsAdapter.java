package com.example.licenta_v2.ui.myPlants;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GroupedMyPlantsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_PLANT = 1;

    private final Context context;
    private final List<Object> items = new ArrayList<>();

    public GroupedMyPlantsAdapter(Context context) {
        this.context = context;
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

            ((PlantViewHolder) holder).siteName.setText(plant.getCommonName());

            String site = savedPlant.getAddedSite() != null ? savedPlant.getAddedSite() : "Unknown site";
            String date = savedPlant.getAddedDate() != null ? savedPlant.getAddedDate() : "Unknown date";
            ((PlantViewHolder) holder).siteInfo.setText("Site: " + site + "\nAdded: " + date);

            String imageUrl = plant.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                if (!imageUrl.startsWith("data:image")) {
                    imageUrl = "data:image/png;base64," + imageUrl;
                }
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(((PlantViewHolder) holder).siteImage);
            } else {
                ((PlantViewHolder) holder).siteImage.setImageResource(R.drawable.ic_launcher_background);
            }

            ((PlantViewHolder) holder).deleteButton.setOnClickListener(v -> {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser == null) return;

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users")
                        .document(currentUser.getUid())
                        .collection("myPlants")
                        .document(plant.getCommonName())
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
                                Toast.makeText(context, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
            });
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerTitle;
        HeaderViewHolder(View view) {
            super(view);
            headerTitle = view.findViewById(R.id.plantGroupTitle);
        }
    }

    static class PlantViewHolder extends RecyclerView.ViewHolder {
        ImageView siteImage;
        TextView siteName, siteInfo;
        ImageView deleteButton;

        PlantViewHolder(View view) {
            super(view);
            siteImage = view.findViewById(R.id.siteImage);
            siteName = view.findViewById(R.id.siteName);
            siteInfo = view.findViewById(R.id.siteInfo);
            deleteButton = view.findViewById(R.id.deleteButton);
        }
    }
}
