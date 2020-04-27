package com.junjunguo.pocketmaps.db;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FBauth {

    private static final String TAG = "FBauth";
    private static FirebaseAuth mAuth;

    public static void userSignOut() {
        FirebaseAuth.getInstance().signOut();
    }
}
