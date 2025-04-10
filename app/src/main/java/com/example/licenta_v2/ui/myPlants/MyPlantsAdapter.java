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

import java.util.List;

public class MyPlantsAdapter extends RecyclerView.Adapter<MyPlantsAdapter.PlantViewHolder> {

    private final Context context;
    private final List<SavedPlant> plantList;

    public MyPlantsAdapter(Context context, List<SavedPlant> plantList) {
        this.context = context;
        this.plantList = plantList;
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_plant, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        SavedPlant savedPlant = plantList.get(position);
        PlantDetailsResponse plant = savedPlant.getPlantData();

        holder.siteName.setText(plant.getCommonName());

        String site = savedPlant.getAddedSite() != null ? savedPlant.getAddedSite() : "Unknown site";
        String date = savedPlant.getAddedDate() != null ? savedPlant.getAddedDate() : "Unknown date";
        holder.siteInfo.setText("Site: " + site + "\nAdded: " + date);

        String imageUrl = plant.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (!imageUrl.startsWith("data:image")) {
                imageUrl = "data:image/png;base64," + imageUrl;
            }
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.siteImage);
        } else {
            holder.siteImage.setImageResource(R.drawable.ic_launcher_background);
        }

        // 🔥 Ștergere plantă
        holder.deleteButton.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) return;

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                    .document(currentUser.getUid())
                    .collection("myPlants")
                    .document(plant.getCommonName()) // cheia e numele plantei
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Plant removed", Toast.LENGTH_SHORT).show();
                        plantList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, plantList.size());
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });
    }

    @Override
    public int getItemCount() {
        return plantList.size();
    }

    public static class PlantViewHolder extends RecyclerView.ViewHolder {
        ImageView siteImage;
        TextView siteName;
        TextView siteInfo;
        ImageView deleteButton;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            siteImage = itemView.findViewById(R.id.siteImage);
            siteName = itemView.findViewById(R.id.siteName);
            siteInfo = itemView.findViewById(R.id.siteInfo);
            deleteButton = itemView.findViewById(R.id.deleteButton); // trebuie să existe în layout!
        }
    }
}
