package com.example.licenta_v2.ui.favorites;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.licenta_v2.R;
import com.example.licenta_v2.model.PlantDetailsResponse;

import java.util.List;

public class FavoritePlantAdapter extends RecyclerView.Adapter<FavoritePlantAdapter.FavoriteViewHolder> {

    private List<PlantDetailsResponse> plantList;

    public FavoritePlantAdapter(List<PlantDetailsResponse> plantList) {
        this.plantList = plantList;
    }

    public static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameView;

        public FavoriteViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.plantImage);
            nameView = itemView.findViewById(R.id.plantName);
        }
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite_plant, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        PlantDetailsResponse plant = plantList.get(position);
        holder.nameView.setText(plant.getCommonName());

        Glide.with(holder.itemView.getContext())
                .load(plant.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return plantList.size();
    }

    public void updateList(List<PlantDetailsResponse> newList) {
        plantList = newList;
        notifyDataSetChanged();
    }
}

