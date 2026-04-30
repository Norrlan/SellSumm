package com.example.sellsumm;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class StaffProfileFragment extends Fragment {

    private static final String TAG = "StaffProfileFragment";

    private String storeId;
    private String staffId;

    private FirebaseFirestore db;

    private TextView fullNameText, emailText, storeNameText, storeCodeText, roleText;

    public StaffProfileFragment() {}

    // ⭐ Required for StaffMainActivity
    public static StaffProfileFragment newInstance(String storeId) {
        StaffProfileFragment fragment = new StaffProfileFragment();
        Bundle args = new Bundle();
        args.putString("storeId", storeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            storeId = getArguments().getString("storeId");
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        staffId = currentUser != null ? currentUser.getUid() : null;
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_staff_profile, container, false);

        fullNameText = view.findViewById(R.id.profileFullName);
        emailText = view.findViewById(R.id.profileEmail);
        storeNameText = view.findViewById(R.id.profileStoreName);
        storeCodeText = view.findViewById(R.id.profileStoreCode);
        roleText = view.findViewById(R.id.profileRole);

        loadStaffInfo();
        loadStoreInfo();

        return view;
        /*fullNameText = view.findViewById(R.id.profileFullName);
                                             ^
  symbol:   variable profileFullName
  location: class id*/
    }

    // ⭐ Load staff personal info
    private void loadStaffInfo() {
        if (staffId == null) {
            Log.e(TAG, "No logged in staff user found");
            return;
        }

        db.collection("users")
                .document(staffId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String fullName = doc.getString("fullName");
                        String email = doc.getString("email");

                        fullNameText.setText(fullName != null ? fullName : "N/A");
                        emailText.setText(email != null ? email : "N/A");
                        roleText.setText("Staff");
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to load staff info: " + e.getMessage()));
    }

    // ⭐ Load store info
    private void loadStoreInfo() {
        if (storeId == null || storeId.isEmpty()) {
            Log.e(TAG, "storeId missing - cannot load store info");
            storeNameText.setText("N/A");
            storeCodeText.setText("N/A");
            return;
        }

        db.collection("stores")
                .document(storeId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String storeName = doc.getString("storeName");
                        String storeCode = doc.getId(); // document ID is the store code

                        storeNameText.setText(storeName != null ? storeName : "N/A");
                        storeCodeText.setText(storeCode != null ? storeCode : "N/A");
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to load store info: " + e.getMessage()));
    }
}
