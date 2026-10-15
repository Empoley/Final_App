package com.example.prelim_final_app;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SignUp extends AppCompatActivity {

    EditText emailEdit, userEdit, passEdit, confirmPassEdit, birthEdit, phnEdit;
    CheckBox termsChk;
    TextView tvTermsLink;
    Button signupBtn;
    private boolean hasAgreedTerms = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        emailEdit = findViewById(R.id.emailEdit);
        userEdit = findViewById(R.id.userEdit);
        passEdit = findViewById(R.id.passEdit);
        confirmPassEdit = findViewById(R.id.confirmPassEdit);
        birthEdit = findViewById(R.id.birthEdit);
        phnEdit = findViewById(R.id.phnEdit);
        termsChk = findViewById(R.id.termsChk);
        tvTermsLink = findViewById(R.id.tvTermsLink);
        signupBtn = findViewById(R.id.signupBtn);

        // Click on "Terms & Conditions" text opens modal dialog
        tvTermsLink.setOnClickListener(v -> showTermsModal());

        // Checkbox toggle: block manual checking until user has scrolled & agreed in modal
        termsChk.setOnClickListener(v -> {
            if (!hasAgreedTerms) {
                // Prevent checking and force opening the modal
                termsChk.setChecked(false);
                Toast.makeText(SignUp.this, "Please review and agree to the Terms & Conditions first", Toast.LENGTH_SHORT).show();
                showTermsModal();
            } else {
                // If already agreed, allow user to uncheck to revoke agreement
                if (!termsChk.isChecked()) {
                    hasAgreedTerms = false;
                    signupBtn.setEnabled(false);
                }
            }
        });

        // SIGN UP BUTTON
        signupBtn.setOnClickListener(v -> handleSignUp());
    }

    private void showTermsModal() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_terms);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ScrollView scrollTerms = dialog.findViewById(R.id.scrollTerms);
        TextView tvTermsContent = dialog.findViewById(R.id.tvTermsContent);
        TextView tvScrollHint = dialog.findViewById(R.id.tvScrollHint);
        Button btnDecline = dialog.findViewById(R.id.btnDeclineTerms);
        Button btnAccept = dialog.findViewById(R.id.btnAcceptTerms);

        String termsText = "Last Updated: October 2026\n\n"
                + "Welcome to Prelim Final App!\n\n"
                + "1. Acceptance of Terms\n"
                + "By creating an account or using this application, you agree to be bound by these Terms and Conditions. If you do not agree to all terms, please do not use the app.\n\n"
                + "2. User Accounts & Security\n"
                + "You are responsible for keeping your credentials confidential and for all activities under your account. You agree to provide true, accurate, and current information during registration.\n\n"
                + "3. Weapons Database & Community Content\n"
                + "This application allows registered users to view, add, and remove weapon entries in a shared community catalog. You agree not to upload offensive, inappropriate, abusive, or copyrighted material without authorization.\n\n"
                + "4. Real-time Database Synchronization\n"
                + "All additions and removals are synchronized in real-time across all active users via Firebase Realtime Database. Please exercise care when removing items from the catalog.\n\n"
                + "5. Privacy & Data Handling\n"
                + "Your account information (username, email, birthdate, and phone number) is stored securely in Firebase for authentication and identity verification. We do not sell or distribute personal data to third parties.\n\n"
                + "6. Modifications to Terms\n"
                + "We reserve the right to modify these terms at any time without prior notice. Continued use of the application indicates your acceptance of any revisions.";

        tvTermsContent.setText(termsText);

        // Initially disable "I Agree" button until user scrolls down completely
        if (hasAgreedTerms) {
            btnAccept.setEnabled(true);
            btnAccept.setAlpha(1.0f);
            btnAccept.setText("I Agree");
            if (tvScrollHint != null) {
                tvScrollHint.setVisibility(View.GONE);
            }
        } else {
            btnAccept.setEnabled(false);
            btnAccept.setAlpha(0.4f);
            btnAccept.setText("Scroll down to Agree");
            if (tvScrollHint != null) {
                tvScrollHint.setVisibility(View.VISIBLE);
            }
        }

        // Listen for scrolling to the bottom
        if (scrollTerms != null) {
            scrollTerms.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                View child = scrollTerms.getChildAt(scrollTerms.getChildCount() - 1);
                if (child != null) {
                    int diff = child.getBottom() - (scrollTerms.getHeight() + scrollY);
                    if (diff <= 35) { // Reached bottom (within 35px tolerance)
                        btnAccept.setEnabled(true);
                        btnAccept.setAlpha(1.0f);
                        btnAccept.setText("I Agree");
                        if (tvScrollHint != null) {
                            tvScrollHint.setVisibility(View.GONE);
                        }
                    }
                }
            });

            // Fallback: If content fits without scrolling on large screens
            scrollTerms.post(() -> {
                View child = scrollTerms.getChildAt(0);
                if (child != null && child.getBottom() <= scrollTerms.getHeight()) {
                    btnAccept.setEnabled(true);
                    btnAccept.setAlpha(1.0f);
                    btnAccept.setText("I Agree");
                    if (tvScrollHint != null) {
                        tvScrollHint.setVisibility(View.GONE);
                    }
                }
            });
        }

        btnAccept.setOnClickListener(v -> {
            hasAgreedTerms = true;
            termsChk.setChecked(true);
            signupBtn.setEnabled(true);
            dialog.dismiss();
            Toast.makeText(SignUp.this, "Terms and Conditions Accepted", Toast.LENGTH_SHORT).show();
        });

        btnDecline.setOnClickListener(v -> {
            hasAgreedTerms = false;
            termsChk.setChecked(false);
            signupBtn.setEnabled(false);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void handleSignUp() {
        String email = emailEdit.getText().toString().trim();
        String username = userEdit.getText().toString().trim();
        String password = passEdit.getText().toString();
        String confirmPassword = confirmPassEdit.getText().toString();
        String birthdate = birthEdit.getText().toString().trim();
        String phone = phnEdit.getText().toString().trim();

        boolean valid = true;

        // EMAIL
        if (email.isEmpty()) {
            emailEdit.setError("Please enter your Email");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEdit.setError("Please enter a valid email address");
            valid = false;
        }

        // USERNAME
        if (username.isEmpty()) {
            userEdit.setError("Please enter your Username");
            valid = false;
        } else if (username.length() < 5) {
            userEdit.setError("Username must be at least 5 characters");
            valid = false;
        }

        // PASSWORD
        if (password.isEmpty()) {
            passEdit.setError("Please enter your Password");
            valid = false;
        } else if (password.length() < 7) {
            passEdit.setError("Password must be at least 7 characters");
            valid = false;
        }

        // CONFIRM PASSWORD
        if (confirmPassword.isEmpty()) {
            confirmPassEdit.setError("Please confirm your password");
            valid = false;
        } else if (!password.equals(confirmPassword)) {
            confirmPassEdit.setError("Passwords do not match");
            valid = false;
        }

        // BIRTHDATE
        if (birthdate.isEmpty()) {
            birthEdit.setError("Please enter your Birthdate");
            valid = false;
        } else if (!isValidDate(birthdate)) {
            birthEdit.setError("Use MM/dd/yyyy format");
            valid = false;
        }

        // PHONE NUMBER
        if (phone.isEmpty()) {
            phnEdit.setError("Please enter your Phone Number");
            valid = false;
        } else if (!phone.matches("\\+63\\d{10}")) {
            phnEdit.setError("Phone must be in +63XXXXXXXXXX format");
            valid = false;
        }

        // TERMS AND CONDITIONS
        if (!termsChk.isChecked()) {
            Toast.makeText(
                    SignUp.this,
                    "Please agree to the Terms and Conditions",
                    Toast.LENGTH_SHORT
            ).show();
            valid = false;
        }

        if (!valid) {
            return;
        }

        // Start Firebase registration
        signupBtn.setEnabled(false);
        signupBtn.setText("Signing Up...");

        DatabaseReference db = FirebaseHelper.getDatabase().getReference();

        // 1. Check if username is already taken
        db.child("usernames").child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    signupBtn.setEnabled(true);
                    signupBtn.setText("Sign Up");
                    userEdit.setError("Username is already taken");
                    Toast.makeText(SignUp.this, "Username is already taken. Please choose another.", Toast.LENGTH_SHORT).show();
                } else {
                    // 2. Create user with Firebase Auth
                    FirebaseHelper.getAuth().createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful() && task.getResult() != null) {
                                    FirebaseUser fbUser = task.getResult().getUser();
                                    String uid = fbUser != null ? fbUser.getUid() : "";

                                    User user = new User(uid, username, email, birthdate, phone);

                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put("users/" + uid, user);

                                    Map<String, String> usernameRecord = new HashMap<>();
                                    usernameRecord.put("uid", uid);
                                    usernameRecord.put("email", email);
                                    updates.put("usernames/" + username, usernameRecord);

                                    db.updateChildren(updates).addOnCompleteListener(dbTask -> {
                                        signupBtn.setEnabled(true);
                                        signupBtn.setText("Sign Up");

                                        // Sign out so user can sign in with their credentials on the Sign In page
                                        FirebaseHelper.getAuth().signOut();

                                        Toast.makeText(
                                                SignUp.this,
                                                "Sign Up Successful! Please sign in.",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        Intent intent = new Intent(SignUp.this, SignIn.class);
                                        startActivity(intent);
                                        finish();
                                    });
                                } else {
                                    signupBtn.setEnabled(true);
                                    signupBtn.setText("Sign Up");
                                    String msg = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                                    Toast.makeText(SignUp.this, "Error: " + msg, Toast.LENGTH_LONG).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                signupBtn.setEnabled(true);
                signupBtn.setText("Sign Up");
                Toast.makeText(SignUp.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // CHECK DATE FORMAT
    private boolean isValidDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat(
                "MM/dd/yyyy",
                Locale.getDefault()
        );
        sdf.setLenient(false);
        try {
            Date parsedDate = sdf.parse(date);
            return parsedDate != null;
        } catch (ParseException e) {
            return false;
        }
    }
}