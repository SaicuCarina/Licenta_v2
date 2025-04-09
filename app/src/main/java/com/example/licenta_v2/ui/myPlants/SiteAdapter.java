package com.example.licenta_v2.ui.myPlants;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.licenta_v2.R;
import com.example.licenta_v2.model.Site;

import java.util.List;

public class SiteAdapter extends RecyclerView.Adapter<SiteAdapter.SiteViewHolder> {
    public interface OnSiteClickListener {
        void onSiteClick(Site site);
    }

    private List<Site> siteList;
    private Context context;
    private OnSiteClickListener listener;

    public SiteAdapter(Context context, List<Site> siteList, OnSiteClickListener listener) {
        this.context = context;
        this.siteList = siteList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SiteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_site, parent, false);
        return new SiteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SiteViewHolder holder, int position) {
        Site site = siteList.get(position);
        String imageBase64 = site.getPhoto();

        if (imageBase64 != null && !imageBase64.isEmpty()) {
            if (!imageBase64.startsWith("data:image")) {
                imageBase64 = "data:image/png;base64," + imageBase64;
            }

            Glide.with(context)
                    .load(imageBase64)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.siteImage);
        } else {
            holder.siteImage.setImageResource(R.drawable.ic_launcher_background);
        }

        holder.siteName.setText(site.getName());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onSiteClick(site);
        });
    }


    @Override
    public int getItemCount() {
        return siteList.size();
    }

    public static class SiteViewHolder extends RecyclerView.ViewHolder {
        ImageView siteImage;
        TextView siteName;

        public SiteViewHolder(@NonNull View itemView) {
            super(itemView);
            siteImage = itemView.findViewById(R.id.siteImage);
            siteName = itemView.findViewById(R.id.siteName);
        }
    }
}
