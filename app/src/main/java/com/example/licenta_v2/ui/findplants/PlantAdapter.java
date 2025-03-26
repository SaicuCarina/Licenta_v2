package com.example.licenta_v2.ui.findplants;

import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Base64;
import android.graphics.Bitmap;
import com.bumptech.glide.Glide;


import androidx.recyclerview.widget.RecyclerView;

import com.example.licenta_v2.R;
import com.example.licenta_v2.model.PlantDetailsResponse;

import java.util.List;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {
    private List<PlantDetailsResponse> plantList;

    public PlantAdapter(List<PlantDetailsResponse> plantList) {
        this.plantList = plantList;
    }

    public static class PlantViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameView;

        public PlantViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.plantImage);
            nameView = itemView.findViewById(R.id.plantName);
        }
    }

    @Override
    public PlantViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plant, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlantViewHolder holder, int position) {
        PlantDetailsResponse plant = plantList.get(position);
        holder.nameView.setText(plant.getCommonName());

        String imageUrl = plant.getImageUrl();

        Log.d("PlantAdapter", "Plant: " + plant.getCommonName() + " - imageUrl=" + imageUrl);

        // Dacă planta are o imagine validă base64, o afișăm
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_launcher_background);
        }
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

