package com.example.sellsumm;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class StaffMainActivity extends AppCompatActivity {

    private static final String TAG = "StaffMainActivity";

    private BottomNavigationView bottomNavigation;
    private String storeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_staff_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top,
                            systemBars.right, systemBars.bottom);
                    return insets;
                });

        bottomNavigation = findViewById(R.id.staff_bottom_nav);

        // Load storeId first
        loadStoreIdThenSetupNav();
    }

    private void loadStoreIdThenSetupNav() {
        String staffId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(staffId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists()) {
                        storeId = doc.getString("storeId");
                    }

                    if (storeId == null || storeId.isEmpty()) {
                        Log.e(TAG, "storeId missing — cannot continue");
                        return; // STOP — do not load fragments
                    }

                    // Now that storeId is valid, set up nav
                    setupBottomNav();

                    // Load default fragment ONCE
                    bottomNavigation.setSelectedItemId(R.id.nav_dashboard);

                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to load storeId: " + e.getMessage()));
    }

    private void setupBottomNav() {
        bottomNavigation.setOnItemSelectedListener(item -> {

            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_dashboard) {
                selectedFragment = StaffDashboardFragment.newInstance(storeId);
            } else if (itemId == R.id.nav_analytics) {
                selectedFragment = StaffAnalysisFragment.newInstance(storeId);
            } else if (itemId == R.id.nav_transactions) {
                selectedFragment = StaffTransactionFragment.newInstance(storeId);
            } else if (itemId == R.id.nav_sales) {
                selectedFragment = StaffSalesFragment.newInstance(storeId);
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = StaffProfileFragment.newInstance(storeId);
            }

            return loadFragment(selectedFragment);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment == null) return false;

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.staff_fragment_container, fragment)
                .commit();

        return true;
    }

    public void openFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.staff_fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    public String getStoreId() {
        return storeId;
    }
}
