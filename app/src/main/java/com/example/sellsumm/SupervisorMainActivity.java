package com.example.sellsumm;

import android.os.Bundle;

import androidx.activity.EdgeToEdge; import androidx.appcompat.app.AppCompatActivity; import androidx.core.graphics.Insets; import androidx.core.view.ViewCompat; import androidx.core.view.WindowInsetsCompat; import androidx.fragment.app.Fragment;

import com.example.sellsumm.AnalyticsFragment;
import com.example.sellsumm.DashboardFragment;
import com.example.sellsumm.InventoryFragment;
import com.example.sellsumm.KPIFragment;
import com.example.sellsumm.ProfileFragment;
import com.example.sellsumm.R;
import com.github.mikephil.charting.charts.BarChart;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SupervisorMainActivity extends AppCompatActivity
{

    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_supervisor_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Logic for switching between different tabs on the barchart
        TabLayout tabLayout = findViewById(R.id.salesTabLayout);
        BarChart barChart = findViewById(R.id.salesBarChart);

        tabLayout.addTab(tabLayout.newTab().setText("Daily"));
        tabLayout.addTab(tabLayout.newTab().setText("Weekly"));
        tabLayout.addTab(tabLayout.newTab().setText("Monthly"));

        // Bottom navigation setup
        bottomNav = findViewById(R.id.bottom_navigation);
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

    private boolean loadFragment(Fragment fragment) {
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