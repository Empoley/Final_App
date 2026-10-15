package com.example.prelim_final_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.List;

public class ItemAdapter extends ArrayAdapter<Item> {

    public interface OnItemActionListener {
        void onItemClick(Item item);
        void onItemDelete(Item item);
    }

    private final Context context;
    private final List<Item> items;
    private final OnItemActionListener listener;

    public ItemAdapter(@NonNull Context context, @NonNull List<Item> items, OnItemActionListener listener) {
        super(context, R.layout.listviewdesign, items);
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.listviewdesign, parent, false);
        }

        Item item = items.get(position);

        ImageView ivItemIcon = convertView.findViewById(R.id.ivItemIcon);
        TextView tvItemName = convertView.findViewById(R.id.listView1);
        ImageButton btnDeleteItem = convertView.findViewById(R.id.btnDeleteItem);
        View itemCard = convertView.findViewById(R.id.itemCard);

        if (item != null) {
            tvItemName.setText(item.getName());

            // Image loading
            int localDrawableRes = getLocalDrawableForWeapon(item.getName());
            if (localDrawableRes != 0) {
                ivItemIcon.setImageResource(localDrawableRes);
            } else if (item.getImageUrl() != null && !item.getImageUrl().trim().isEmpty()) {
                Glide.with(context)
                        .load(item.getImageUrl().trim())
                        .placeholder(R.drawable.terrablade)
                        .error(R.drawable.terrablade)
                        .into(ivItemIcon);
            } else {
                ivItemIcon.setImageResource(R.drawable.terrablade);
            }

            // Click listener for details / action
            itemCard.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });

            // Delete button listener
            btnDeleteItem.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemDelete(item);
                }
            });
        }

        return convertView;
    }

    private int getLocalDrawableForWeapon(String name) {
        if (name == null) return 0;
        String clean = name.trim().toLowerCase();
        if (clean.equals("terrablade") || clean.equals("terra blade")) {
            return R.drawable.terrablade;
        } else if (clean.equals("volcano")) {
            return R.drawable.volcano;
        } else if (clean.equals("night's edge") || clean.equals("nights edge")) {
            return R.drawable.nightsedge;
        } else if (clean.equals("zenith")) {
            return R.drawable.zenith;
        } else if (clean.equals("meowmere")) {
            return R.drawable.meowmere;
        }
        return 0;
    }
}
