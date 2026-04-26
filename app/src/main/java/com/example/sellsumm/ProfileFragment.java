package com.example.sellsumm;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


public class ProfileFragment extends Fragment
{

    private String storeId;
    private FirebaseFirestore db;

    private TextView storeNameText, storeCodeText, emailField;

    public ProfileFragment() {}

    public static ProfileFragment newInstance(String storeId)
    {
    ProfileFragment fragment = new ProfileFragment();
    Bundle args = new Bundle();
    args.putString("storeId", storeId);
    fragment.setArguments(args);
    return fragment;
}

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
        {
            storeId = getArguments().getString("storeId");
        }

        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        storeNameText = view.findViewById(R.id.profile_store_name);
        storeCodeText = view.findViewById(R.id.profile_store_code);
        emailField    = view.findViewById(R.id.EmailAddressField);

        // Set store code immediately
        storeCodeText.setText(storeId);

        // Load store name from Firestore
        loadStoreName();

        // Load email
        loadEmail();

        return view;
    }

    private void loadStoreName() {
        db.collection("stores")
                .document(storeId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String storeName = doc.getString("storeName");
                        storeNameText.setText(storeName != null ? storeName : "Unknown Store");
                    }
                });
    }

    private void loadEmail()
    {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String email = doc.getString("email");
                        emailField.setText(email);
                    }
                });
    }

}

