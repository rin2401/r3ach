package com.rin2401.r3ach.firebases;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseContext {
    private static FirebaseContext instance;
    public FirebaseDatabase database;
    public DatabaseReference refTotalKeys;

    public static FirebaseContext getInstance(Context context) {
        if (instance == null) {
            FirebaseApp.initializeApp(context);
            instance = new FirebaseContext();
            instance.database = FirebaseDatabase.getInstance();
            FirebaseContext firebaseContext = instance;
            firebaseContext.refTotalKeys = firebaseContext.database.getReference("total_keys");
        }
        return instance;
    }
}
