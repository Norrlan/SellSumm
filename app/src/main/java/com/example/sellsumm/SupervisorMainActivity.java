package com.example.sellsumm;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SupervisorMainActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;
    public static String storeId; // accessible to all fragments

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

        // ⭐ Get storeId from LoginScreen
        storeId = getIntent().getStringExtra("storeId");

        if (storeId == null) {
            // fallback safety
            storeId = "UNKNOWN";
        }

        bottomNav = findViewById(R.id.supervisor_bottom_nav);

        // Load default fragment WITH storeId
        loadFragment(DashboardFragment.newInstance(storeId));

        bottomNav.setOnItemSelectedListener(item -> {

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

    private boolean loadFragment(Fragment fragment) {
        if (fragment == null) return false;

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();

        return true;
    }

    public void openFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
