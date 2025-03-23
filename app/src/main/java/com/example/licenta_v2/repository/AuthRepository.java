// repository/AuthRepository.java

package com.example.licenta_v2.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthRepository {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public interface LoginCallback {
        void onResult(boolean success);
    }

    public void login(String email, String password, LoginCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> callback.onResult(true))
                .addOnFailureListener(e -> callback.onResult(false));
    }
    public void register(String name, String email, String password, LoginCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = auth.getCurrentUser().getUid();

                        // Setează numele în Firebase Auth
                        auth.getCurrentUser().updateProfile(
                                new UserProfileChangeRequest.Builder().setDisplayName(name).build());

                        // Salvează în Firestore
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("name", name);
                        userMap.put("email", email);

                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(uid)
                                .set(userMap)
                                .addOnSuccessListener(aVoid -> callback.onResult(true))
                                .addOnFailureListener(e -> callback.onResult(false));
                    } else {
                        callback.onResult(false);
                    }
                });
    }

}
