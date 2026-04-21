package com.example.sellsumm;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SupervisorMainActivity extends AppCompatActivity
{

    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_supervisor_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.supervisor_main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Bottom navigation setup
        bottomNav = findViewById(R.id.supervisor_bottom_nav);
        // default fragment on start
        loadFragment(new DashboardFragment());

        // Handle bottom navigation item selection.
        bottomNav.setOnItemSelectedListener(item ->
        {

            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_dashboard)
            {
                selectedFragment = new DashboardFragment();
            }
            else if (item.getItemId() == R.id.nav_analytics)
            {
                selectedFragment = new AnalyticsFragment();
            }
            else if (item.getItemId() == R.id.nav_kpi)
            {
                selectedFragment = new KPIFragment();
            }
            else if (item.getItemId() == R.id.nav_inventory)
            {
                selectedFragment = new InventoryFragment();
            }
            else if (item.getItemId() == R.id.nav_profile)
            {
                selectedFragment = new ProfileFragment();
            }
            // Replace the current fragment with the selected one.

            return loadFragment(selectedFragment);
        });


        // Load default fragment
        loadFragment(new DashboardFragment());
    }

    private boolean loadFragment(Fragment fragment)
    {
        if (fragment == null) return false;

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
                .commit();

        return true;
    }

    public void openFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null)
                .commit();
    }

}