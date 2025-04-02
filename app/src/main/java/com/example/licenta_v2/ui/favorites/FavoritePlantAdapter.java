package com.example.licenta_v2.ui.favorites;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.licenta_v2.R;
import com.example.licenta_v2.model.PlantDetailsResponse;
import com.example.licenta_v2.ui.details.PlantDetailsFragment;

import java.util.List;

public class FavoritePlantAdapter extends RecyclerView.Adapter<FavoritePlantAdapter.FavoriteViewHolder> {

    private List<PlantDetailsResponse> plantList;

    public FavoritePlantAdapter(List<PlantDetailsResponse> plantList) {
        this.plantList = plantList;
    }

    public static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameView;
        TextView lightSummaryView;
        TextView careLevelView;

        public FavoriteViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.plantImage);
            nameView = itemView.findViewById(R.id.plantName);
            lightSummaryView = itemView.findViewById(R.id.plantLightSummary);
            careLevelView = itemView.findViewById(R.id.plantCareLevel);
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
        holder.lightSummaryView.setText(plant.getLightSummary());
        holder.careLevelView.setText(plant.getCareLevel());

        Glide.with(holder.itemView.getContext())
                .load(plant.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.imageView);

        holder.itemView.setOnClickListener(v -> {
            FragmentActivity activity = (FragmentActivity) v.getContext();
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, PlantDetailsFragment.newInstance(plant))
                    .addToBackStack(null)
                    .commit();
        });
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
