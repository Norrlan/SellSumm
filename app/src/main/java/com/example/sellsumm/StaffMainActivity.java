package com.example.sellsumm;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class StaffMainActivity extends AppCompatActivity
{
    BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_staff_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNavigation = findViewById(R.id.staff_bottom_nav);

        // Default fragment on start
        loadFragment(new StaffDashboardFragment());

        // Handle bottom navigation item selection
        bottomNavigation.setOnItemSelectedListener(item ->
        {

            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_dashboard)
            {
                selectedFragment = new StaffDashboardFragment();
            }

            else if (item.getItemId() == R.id.nav_analytics)
            {
                selectedFragment = new StaffAnalysisFragment();
            }

            else if (item.getItemId() == R.id.nav_kpi)
            {
                selectedFragment = new StaffKPIFragment();
            }
            else if (item.getItemId() == R.id.nav_sales)
            {
                selectedFragment = new StaffSalesFragment();
            }
            else if (item.getItemId() == R.id.nav_profile)
            {
                selectedFragment = new StaffProfileFragment();
            }

            return loadFragment(selectedFragment);
        });

        // Load default fragment again (safe)
        loadFragment(new StaffDashboardFragment());
    }

    private boolean loadFragment(Fragment fragment)
    {
        if (fragment == null) return false;

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.staff_fragment_container, fragment)
                .commit();

        return true;
    }

    public void openFragment(Fragment fragment)
    {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.staff_fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }





}