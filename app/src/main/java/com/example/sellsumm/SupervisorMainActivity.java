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

public class SupervisorMainActivity extends AppCompatActivity {

    private static final String TAG = "SupervisorMainActivity";

    BottomNavigationView bottomNav;
    public static String storeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_supervisor_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.supervisor_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // get the storeId from LoginScreen
        storeId = getIntent().getStringExtra("storeId");

        bottomNav = findViewById(R.id.supervisor_bottom_nav);

        loadStoreIdThenSetupNav();
    }

    private void loadStoreIdThenSetupNav() {
        if (storeId != null && !storeId.isEmpty() && !"UNKNOWN".equals(storeId))
        {
            setupBottomNav();
            loadFragment(DashboardFragment.newInstance(storeId));
            return;
        }

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            storeId = "UNKNOWN";
            Log.e(TAG, "No logged in supervisor found");
            setupBottomNav();
            loadFragment(DashboardFragment.newInstance(storeId));
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("users").document(uid).get().addOnSuccessListener(doc ->
                {
                    String resolvedStoreId = doc.getString("storeId");
                    if (resolvedStoreId != null && !resolvedStoreId.isEmpty())
                    {
                        storeId = resolvedStoreId;
                    } else {
                        storeId = "UNKNOWN";
                        Log.e(TAG, "Supervisor storeId missing from user profile");
                    }

                    setupBottomNav();
                    loadFragment(DashboardFragment.newInstance(storeId));
                })
                .addOnFailureListener(e ->
                {
                    storeId = "UNKNOWN";
                    Log.e(TAG, "Failed to load supervisor storeId: " + e.getMessage());
                    setupBottomNav();
                    loadFragment(DashboardFragment.newInstance(storeId));
                });
    }

        // Bottom navigation method
    private void setupBottomNav()
    {
        bottomNav.setOnItemSelectedListener(item ->
        {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_dashboard) {
                selectedFragment = DashboardFragment.newInstance(storeId);
            }
            else if (item.getItemId() == R.id.nav_analytics) {
                selectedFragment = AnalyticsFragment.newInstance(storeId);
            }
            else if (item.getItemId() == R.id.nav_kpi) {
                selectedFragment = KPIFragment.newInstance(storeId);
            }
            else if (item.getItemId() == R.id.nav_inventory) {
                selectedFragment = InventoryFragment.newInstance(storeId);
            }
            else if (item.getItemId() == R.id.nav_profile) {
                selectedFragment = ProfileFragment.newInstance(storeId);
            }

            return loadFragment(selectedFragment);
        });
    }

        // method to enable switches between the views in the bottom nav
    private boolean loadFragment(Fragment fragment)
    {
        if (fragment == null) return false;

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();

        return true;
    }
    // method to enable back button to return to  previous fragment
    public void openFragment(Fragment fragment)
    {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
    }
}
