package com.example.prelim_final_app;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseHelper {
    public static final String DATABASE_URL = "https://prelimfinalapp-bf2b3-default-rtdb.asia-southeast1.firebasedatabase.app";

    public static FirebaseDatabase getDatabase() {
        return FirebaseDatabase.getInstance(DATABASE_URL);
    }

    public static FirebaseAuth getAuth() {
        return FirebaseAuth.getInstance();
    }
}
