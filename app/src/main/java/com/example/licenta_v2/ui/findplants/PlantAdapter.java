package com.example.licenta_v2.ui.findplants;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.licenta_v2.R;
import com.example.licenta_v2.model.PlantDetailsResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {
    private List<PlantDetailsResponse> plantList;
    private OnItemClickListener listener;

    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    public interface OnItemClickListener {
        void onItemClick(PlantDetailsResponse plant);
    }

    public PlantAdapter(List<PlantDetailsResponse> plantList, OnItemClickListener listener) {
        this.plantList = plantList;
        this.listener = listener;
    }

    public static class PlantViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameView;
        ImageView favoriteButton;
        TextView lightSummaryView;
        TextView careLevelView;

        public PlantViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.plantImage);
            nameView = itemView.findViewById(R.id.plantName);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
            lightSummaryView = itemView.findViewById(R.id.plantLightSummary);
            careLevelView = itemView.findViewById(R.id.plantCareLevel);
        }

    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plant, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        PlantDetailsResponse plant = plantList.get(position);
        holder.nameView.setText(plant.getCommonName());
        holder.nameView.setText(plant.getCommonName());
        holder.lightSummaryView.setText(plant.getLightSummary());
        holder.careLevelView.setText(plant.getCareLevel());


        String imageUrl = plant.getImageUrl();
        Log.d("PlantAdapter", "Plant: " + plant.getCommonName() + " - imageUrl=" + imageUrl);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_launcher_background);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(plant);
            }
        });


        updateFavoriteIcon(holder.favoriteButton, plant.isFavorite());

        holder.favoriteButton.setOnClickListener(v -> {
            boolean isNowFavorite = !plant.isFavorite();
            plant.setFavorite(isNowFavorite);
            updateFavoriteIcon(holder.favoriteButton, isNowFavorite);
            updateFavoriteInFirestore(plant);
        });
    }

    private void updateFavoriteIcon(ImageView icon, boolean isFavorite) {
        icon.setImageResource(isFavorite ? R.drawable.favorite_red : R.drawable.favorite_border);
    }

    private void updateFavoriteInFirestore(PlantDetailsResponse plant) {
        if (currentUser == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference favRef = db.collection("users")
                .document(currentUser.getUid())
                .collection("favorites")
                .document(plant.getCommonName());

        if (plant.isFavorite()) {
            favRef.set(plant);
        } else {
            favRef.delete();
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
