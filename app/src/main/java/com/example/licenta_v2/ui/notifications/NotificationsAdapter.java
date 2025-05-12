
package com.example.licenta_v2.ui.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.licenta_v2.R;
import com.example.licenta_v2.model.SavedPlant;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    public interface OnPlantWateredListener {
        void onPlantWatered(SavedPlant plant);
    }

    private final List<SavedPlant> plants;
    private final OnPlantWateredListener listener;

    public NotificationsAdapter(List<SavedPlant> plants, OnPlantWateredListener listener) {
        this.plants = plants;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plant_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SavedPlant plant = plants.get(position);

        String displayName = plant.getCustomName() != null && !plant.getCustomName().isEmpty()
                ? plant.getCustomName()
                : plant.getPlantData().getCommonName();
        holder.plantName.setText(displayName);

        holder.plantLocation.setText("Location: " + plant.getAddedSite());

        String imageUrl = plant.getPlantData().getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (!imageUrl.startsWith("data:image")) {
                imageUrl = "data:image/png;base64," + imageUrl;
            }
            Glide.with(holder.plantImage.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.plantImage);
        } else {
            holder.plantImage.setImageResource(R.drawable.ic_launcher_background);
        }

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(false);
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                listener.onPlantWatered(plant);
            }
        });
    }


    @Override
    public int getItemCount() {
        return plants.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView plantName;
        CheckBox checkBox;
        TextView plantLocation;
        ImageView plantImage;

        ViewHolder(View itemView) {
            super(itemView);
            plantName = itemView.findViewById(R.id.notificationPlantName);
            plantLocation = itemView.findViewById(R.id.notificationPlantLocation);
            checkBox = itemView.findViewById(R.id.notificationCheckBox);
            plantImage = itemView.findViewById(R.id.notificationPlantImage);
        }
    }
}
