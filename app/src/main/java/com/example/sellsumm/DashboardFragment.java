package com.example.sellsumm;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.BarChart;
import com.google.android.material.tabs.TabLayout;


public class DashboardFragment extends Fragment
{

    public DashboardFragment() {
        // Required empty public constructor
    }
    public static DashboardFragment newInstance(String param1, String param2)
    {

        DashboardFragment fragment = new DashboardFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // 1. Inflate the layout
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        //navigate to the Commissions screen when the container is clicked

        LinearLayout salesTab = view.findViewById(R.id.sales_tab);

        salesTab.setOnClickListener(v ->
        {
            Fragment sup = new SupcommissionFragment();

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, sup)
                    .addToBackStack(null)
                    .commit();
        });

        // 4. Return the inflated view
        return view;
    }



    // Inflate the layout for this fragment
    //return inflater.inflate(R.layout.fragment_dashboard, container, false);

        /*
        *   // Logic for switching between different tabs on the barchart
        TabLayout tabLayout = findViewById(R.id.salesTabLayout);
        BarChart barChart = findViewById(R.id.salesBarChart);

        tabLayout.addTab(tabLayout.newTab().setText("Daily"));
        tabLayout.addTab(tabLayout.newTab().setText("Weekly"));
        tabLayout.addTab(tabLayout.newTab().setText("Monthly"));
        * */
}