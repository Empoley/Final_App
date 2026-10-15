package com.example.prelim_final_app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class SignIn extends AppCompatActivity {

    EditText usernameEdit, passwordEdit;
    TextView signUp;
    Button signInBtn;
    FloatingActionButton googleBtn, facebookBtn, discordBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);

        usernameEdit = findViewById(R.id.usernameEdit);
        passwordEdit = findViewById(R.id.passwordEdit);
        signUp = findViewById(R.id.signUp);
        signInBtn = findViewById(R.id.SigninBtn);
        googleBtn = findViewById(R.id.googleBtn);
        facebookBtn = findViewById(R.id.facebookBtn);
        discordBtn = findViewById(R.id.discordBtn);

        // SIGN IN BUTTON
        signInBtn.setOnClickListener(v -> handleSignIn());

        // SIGN UP
        signUp.setOnClickListener(v -> {
            Intent intent = new Intent(SignIn.this, SignUp.class);
            startActivity(intent);
        });

        // GOOGLE
        googleBtn.setOnClickListener(v -> {
            String url = "https://accounts.google.com/v3/signin/accountchooser";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });

        // FACEBOOK
        facebookBtn.setOnClickListener(v -> {
            String url = "https://www.facebook.com/login.php/";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });

        // DISCORD
        discordBtn.setOnClickListener(v -> {
            String url = "https://discord.com/login";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });
    }

    private void handleSignIn() {
        String username = usernameEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString();

        if (username.isEmpty()) {
            usernameEdit.setError("Username is required");
            return;
        }
        if (password.isEmpty()) {
            passwordEdit.setError("Password is required");
            return;
        }

        signInBtn.setEnabled(false);
        signInBtn.setText("Signing In...");

        // If user entered email directly, authenticate right away
        if (username.contains("@")) {
            authenticateWithFirebase(username, password);
            return;
        }

        // Look up registered email by username in Firebase Realtime Database
        DatabaseReference db = FirebaseHelper.getDatabase().getReference();
        db.child("usernames").child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String email = snapshot.child("email").getValue(String.class);
                    if (email != null && !email.isEmpty()) {
                        authenticateWithFirebase(email, password);
                    } else {
                        // Fallback check
                        lookupInUsersNode(username, password);
                    }
                } else {
                    // Fallback search in users node
                    lookupInUsersNode(username, password);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                signInBtn.setEnabled(true);
                signInBtn.setText("Sign In");
                Toast.makeText(SignIn.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void lookupInUsersNode(String username, String password) {
        DatabaseReference db = FirebaseHelper.getDatabase().getReference("users");
        db.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnap : snapshot.getChildren()) {
                        String email = userSnap.child("email").getValue(String.class);
                        if (email != null && !email.isEmpty()) {
                            authenticateWithFirebase(email, password);
                            return;
                        }
                    }
                }

                signInBtn.setEnabled(true);
                signInBtn.setText("Sign In");
                usernameEdit.setError("Username not found");
                Toast.makeText(SignIn.this, "No account found with this username. Please Sign Up.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                signInBtn.setEnabled(true);
                signInBtn.setText("Sign In");
                Toast.makeText(SignIn.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void authenticateWithFirebase(String email, String password) {
        FirebaseHelper.getAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    signInBtn.setEnabled(true);
                    signInBtn.setText("Sign In");

                    if (task.isSuccessful()) {
                        Toast.makeText(
                                SignIn.this,
                                "Sign In Successful",
                                Toast.LENGTH_SHORT
                        ).show();

                        Intent intent = new Intent(SignIn.this, Home.class);
                        startActivity(intent);
                        finish();
                    } else {
                        passwordEdit.setError("Incorrect password or sign in failed");
                        String msg = task.getException() != null ? task.getException().getMessage() : "Authentication failed";
                        Toast.makeText(SignIn.this, "Error: " + msg, Toast.LENGTH_LONG).show();
                    }
                });
    }
}