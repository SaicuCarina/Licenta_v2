package com.example.licenta_v2.ui.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.licenta_v2.R;
import com.example.licenta_v2.model.SavedPlant;

import java.util.List;

public class PlantDayAdapter extends RecyclerView.Adapter<PlantDayAdapter.ViewHolder> {

    private final List<SavedPlant> plants;

    public PlantDayAdapter(List<SavedPlant> plants) {
        this.plants = plants;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plant_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SavedPlant plant = plants.get(position);
        String name = (plant.getCustomName() != null && !plant.getCustomName().isEmpty())
                ? plant.getCustomName()
                : plant.getPlantData().getCommonName();
        holder.plantName.setText(name);

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
    }

    @Override
    public int getItemCount() {
        return plants.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView plantImage;
        TextView plantName;

        ViewHolder(View itemView) {
            super(itemView);
            plantImage = itemView.findViewById(R.id.plantImage);
            plantName = itemView.findViewById(R.id.plantName);
        }
    }
}

