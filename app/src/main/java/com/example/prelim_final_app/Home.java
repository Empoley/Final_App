package com.example.prelim_final_app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Home extends AppCompatActivity implements ItemAdapter.OnItemActionListener {

    private ListView listView;
    private ProgressBar homeProgressBar;
    private TextView emptyTextView;
    private Button addItemBtn;
    private ImageButton btnLogout;

    private final List<Item> itemList = new ArrayList<>();
    private ItemAdapter adapter;
    private DatabaseReference itemsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        listView = findViewById(R.id.listView);
        homeProgressBar = findViewById(R.id.homeProgressBar);
        emptyTextView = findViewById(R.id.emptyTextView);
        addItemBtn = findViewById(R.id.addItemBtn);
        btnLogout = findViewById(R.id.btnLogout);

        adapter = new ItemAdapter(this, itemList, this);
        listView.setAdapter(adapter);

        // Firebase database reference
        itemsRef = FirebaseHelper.getDatabase().getReference("items");

        // Seed initial items if database is empty
        seedInitialItemsIfEmpty();

        // Real-time synchronization listener
        listenForRealtimeItems();

        // + Item Button
        addItemBtn.setOnClickListener(v -> showAddItemDialog());

        // Logout Button
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(Home.this)
                    .setTitle("Sign Out")
                    .setMessage("Are you sure you want to sign out?")
                    .setPositiveButton("Sign Out", (dialog, which) -> {
                        FirebaseHelper.getAuth().signOut();
                        Toast.makeText(Home.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Home.this, SignIn.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void seedInitialItemsIfEmpty() {
        itemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    List<Item> defaultItems = new ArrayList<>();
                    defaultItems.add(new Item("", "Terrablade", "The Terra Blade is a Hardmode, post-Plantera broadsword. When swung, a green aura follows the swing, reaching farther than the sword itself and firing green slash projectiles.", ""));
                    defaultItems.add(new Item("", "Volcano", "The Volcano is a pre-Hardmode sword crafted from Hellstone Bars. It has a high base damage and produces fiery explosions on contact with enemies.", ""));
                    defaultItems.add(new Item("", "Night's Edge", "The Night's Edge is a pre-Hardmode sword crafted from Blade of Grass, Blood Butcherer or Light's Bane, Muramasa, and Volcano.", ""));
                    defaultItems.add(new Item("", "Zenith", "The Zenith is the ultimate melee weapon crafted from 10 different swords. When swung, it shoots orbiting sword trajectories across the entire screen.", ""));
                    defaultItems.add(new Item("", "Meowmere", "The Meowmere is a Hardmode sword dropped by the Moon Lord. It shoots bouncy cat projectiles that leave rainbow trails and meow on bounce.", ""));
                    defaultItems.add(new Item("", "Muramasa", "A classic cobalt-blue sword found in Dungeon locked gold chests. Has swift strikes and emits faint blue light.", ""));
                    defaultItems.add(new Item("", "Blade of Grass", "A massive leaf-blade sword crafted from Jungle Spores and Stingers. Has immense range and inflicts poison.", ""));
                    defaultItems.add(new Item("", "True Excalibur", "A holy blade infused with Chlorophyte Bars. When swung, it emits a shimmering glowing slash aura.", ""));
                    defaultItems.add(new Item("", "True Night's Edge", "A dark sword forged from Night's Edge and mechanical boss souls, sending spinning energy rings outward.", ""));
                    defaultItems.add(new Item("", "Flairon", "A Duke Fishron flail weapon that shoots tracking homing bubbles at high velocity toward nearby targets.", ""));

                    for (Item item : defaultItems) {
                        String id = itemsRef.push().getKey();
                        if (id != null) {
                            item.setId(id);
                            itemsRef.child(id).setValue(item);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Ignore or log
            }
        });
    }

    private void listenForRealtimeItems() {
        itemsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Item item = child.getValue(Item.class);
                    if (item != null) {
                        if (item.getId() == null || item.getId().isEmpty()) {
                            item.setId(child.getKey());
                        }
                        itemList.add(item);
                    }
                }
                adapter.notifyDataSetChanged();
                homeProgressBar.setVisibility(View.GONE);
                emptyTextView.setVisibility(itemList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                homeProgressBar.setVisibility(View.GONE);
                Toast.makeText(Home.this, "Failed to sync items: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddItemDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_item);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        EditText etItemName = dialog.findViewById(R.id.etItemName);
        EditText etItemDescription = dialog.findViewById(R.id.etItemDescription);
        EditText etItemImageUrl = dialog.findViewById(R.id.etItemImageUrl);
        Button btnCancel = dialog.findViewById(R.id.btnCancelAddItem);
        Button btnSubmit = dialog.findViewById(R.id.btnSubmitAddItem);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            String name = etItemName.getText().toString().trim();
            String description = etItemDescription.getText().toString().trim();
            String imageUrl = etItemImageUrl.getText().toString().trim();

            if (name.isEmpty()) {
                etItemName.setError("Weapon name is required");
                return;
            }

            String id = itemsRef.push().getKey();
            if (id != null) {
                Item newItem = new Item(id, name, description, imageUrl);
                itemsRef.child(id).setValue(newItem)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(Home.this, "Weapon '" + name + "' added to database!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(Home.this, "Failed to add weapon: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }
        });

        dialog.show();
    }

    @Override
    public void onItemClick(Item item) {
        if (item == null || item.getName() == null) return;

        String name = item.getName().trim().toLowerCase();

        if (name.equals("terrablade") || name.equals("terra blade")) {
            startActivity(new Intent(Home.this, Terrablade.class));
        } else if (name.equals("volcano")) {
            startActivity(new Intent(Home.this, Volcano.class));
        } else if (name.equals("night's edge") || name.equals("nights edge")) {
            startActivity(new Intent(Home.this, NightsEdge.class));
        } else if (name.equals("zenith")) {
            startActivity(new Intent(Home.this, Zenith.class));
        } else if (name.equals("meowmere")) {
            startActivity(new Intent(Home.this, Meowmere.class));
        } else {
            // Show custom item details modal
            showItemDetailsDialog(item);
        }
    }

    private void showItemDetailsDialog(Item item) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_item_details);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ImageView ivDetailImage = dialog.findViewById(R.id.ivDetailImage);
        TextView tvDetailTitle = dialog.findViewById(R.id.tvDetailTitle);
        TextView tvDetailDescription = dialog.findViewById(R.id.tvDetailDescription);
        Button btnClose = dialog.findViewById(R.id.btnCloseDetail);

        tvDetailTitle.setText(item.getName());
        String desc = (item.getDescription() != null && !item.getDescription().isEmpty())
                ? item.getDescription()
                : "No description available for this weapon.";
        tvDetailDescription.setText(desc);

        if (item.getImageUrl() != null && !item.getImageUrl().trim().isEmpty()) {
            Glide.with(this)
                    .load(item.getImageUrl().trim())
                    .placeholder(R.drawable.terrablade)
                    .error(R.drawable.terrablade)
                    .into(ivDetailImage);
        } else {
            ivDetailImage.setImageResource(R.drawable.terrablade);
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public void onItemDelete(Item item) {
        if (item == null || item.getId() == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Delete Weapon")
                .setMessage("Are you sure you want to remove '" + item.getName() + "' from the Firebase database?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    itemsRef.child(item.getId()).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(Home.this, "Weapon removed in real time!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(Home.this, "Failed to remove: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
