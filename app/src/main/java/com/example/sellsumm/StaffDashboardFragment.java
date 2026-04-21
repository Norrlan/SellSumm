package com.example.sellsumm;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StaffDashboardFragment extends Fragment {

    private static final String TAG = "StaffDashboardFragment";

    // Firestore + Auth
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUid;

    // Views
    private TextView welcomeText;
    private TextView targetSalesValue;
    private TextView salesFigureValue;
    private TextView atvValue;
    private TextView commissionValue;
    private TextView unitsSoldValue;
    private TextView unitsPerTransValue;
    private TextView attachRateValue;
    private TextView salesOverviewStatus;
    private TextView performanceStatus;
    private TextView targetScoreText;
    private TextView actualScoreText;
    private ProgressBar kpiProgressBar;

    // Data
    private double totalSales       = 0;
    private double commissionRate   = 0;
    private int    totalTransactions = 0;
    private int    transWithAddOn   = 0;
    private int    totalUnitsSold   = 0;
    private int    totalKpiTarget   = 0;
    private int    kpisHit          = 0;

    public StaffDashboardFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_staff_dashboard, container, false);

        db      = FirebaseFirestore.getInstance();
        mAuth   = FirebaseAuth.getInstance();
        currentUid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        // Bind views
        welcomeText        = view.findViewById(R.id.welcomeText);
        targetSalesValue   = view.findViewById(R.id.targetSalesValue);
        salesFigureValue   = view.findViewById(R.id.salesFigureValue);
        atvValue           = view.findViewById(R.id.atvValue);
        commissionValue    = view.findViewById(R.id.commissionValue);
        unitsSoldValue     = view.findViewById(R.id.unitsSoldValue);
        unitsPerTransValue = view.findViewById(R.id.unitsPerTransValue);
        attachRateValue    = view.findViewById(R.id.attachRateValue);
        salesOverviewStatus = view.findViewById(R.id.salesOverviewStatus);
        performanceStatus  = view.findViewById(R.id.performanceStatus);
        targetScoreText    = view.findViewById(R.id.targetScoreText);
        actualScoreText    = view.findViewById(R.id.actualScoreText);
        kpiProgressBar     = view.findViewById(R.id.kpiProgressBar);

        if (currentUid == null)
        {
            Log.e(TAG, "No logged in user found");
            return view;
        }

        // Load data in sequence
        loadUserProfile();
        loadCommissionRate();
        loadTransactions();
        loadKpiPerformance();

        return view;
    }

    // Load staff name
    private void loadUserProfile()
    {
        db.collection("users").document(currentUid).get().addOnSuccessListener(doc ->
                {
                    String fullName = doc.getString("fullName");
                    if (fullName != null)
                    {
                        welcomeText.setText("Welcome back " + fullName + "!");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load profile: " + e.getMessage()));
    }

    //Load commission rate from store settings
    private void loadCommissionRate()
    {
        db.collection("commissionSettings").document("default").get().addOnSuccessListener(doc ->
                {
                    if (doc.exists() && doc.getDouble("rate") != null)
                    {
                        commissionRate = doc.getDouble("rate");
                    }
                    // Recalculate commission once rate is loaded
                    updateCommissionDisplay();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load commission rate: " + e.getMessage()));
    }

    // Load transactions for this staff member
    private void loadTransactions()
    {
        db.collection("transactions").whereEqualTo("staffId", currentUid).get().addOnSuccessListener(querySnapshot ->
                {
                    totalSales        = 0;
                    totalTransactions = 0;
                    transWithAddOn    = 0;
                    totalUnitsSold    = 0;

                    for (QueryDocumentSnapshot doc : querySnapshot)
                    {
                        totalTransactions++;

                        // Add transaction total to sales figure
                        Double txValue = doc.getDouble("totalValue");
                        if (txValue != null) totalSales += txValue;

                        // Count add-on transactions
                        Boolean hasAddOn = doc.getBoolean("hasAddOn");
                        if (hasAddOn != null && hasAddOn)
                        {
                            transWithAddOn++;
                        }

                        // Count total units sold from products array
                        List<Object> products = (List<Object>) doc.get("products");
                        if (products != null)
                        {
                            totalUnitsSold += products.size();
                        }
                    }

                    updateSalesDisplay();
                    updateCommissionDisplay();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load transactions: " + e.getMessage()));
    }

    // Load KPI performance
    // KPIs were created by the supervisor and stored in Firestore
    // We count total KPIs as the target and check how many the
    // staff member has logged an entry against as "hit"
    private void loadKpiPerformance()
    {
        db.collection("kpis").get().addOnSuccessListener(querySnapshot ->
                {
                    totalKpiTarget = querySnapshot.size();
                    checkKpisHit();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load KPIs: " + e.getMessage()));
    }

    // Check how many KPIs this staff member has logged against
    private void checkKpisHit()
    {
        db.collection("kpiLogs").whereEqualTo("staffId", currentUid).get().addOnSuccessListener(querySnapshot -> {
                    // Count distinct KPI IDs logged by this staff
                    List<String> distinctKpiIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot)
                    {
                        String kpiId = doc.getString("kpiId");
                        if (kpiId != null && !distinctKpiIds.contains(kpiId))
                        {
                            distinctKpiIds.add(kpiId);
                        }
                    }
                    kpisHit = distinctKpiIds.size();
                    updateKpiPerformanceDisplay();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load KPI logs: " + e.getMessage()));
    }

    // Display updaters

    private void updateSalesDisplay()
    {
        // Sales figure
        salesFigureValue.setText(String.format("£%.2f", totalSales));

        // ATV — total sales divided by number of transactions
        double atv = totalTransactions > 0 ? totalSales / totalTransactions : 0;
        atvValue.setText(String.format("£%.2f", atv));

        // Units sold
        unitsSoldValue.setText(String.valueOf(totalUnitsSold));

        // Units per transaction
        double upt = totalTransactions > 0 ? (double) totalUnitsSold / totalTransactions : 0;
        unitsPerTransValue.setText(String.format("%.1f", upt));

        // Attach rate — transactions with add-on / total × 100
        double attachRate = totalTransactions > 0 ? ((double) transWithAddOn / totalTransactions) * 100 : 0;
        attachRateValue.setText(String.format("%.1f%%", attachRate));

        // Sales overview status — based on ATV vs target
        // Target sales is fetched separately below
        updateSalesOverviewStatus();
    }

    private void updateCommissionDisplay()
    {
        // Commission = total sales × fixed rate %
        double commission = totalSales * (commissionRate / 100);
        commissionValue.setText(String.format("£%.2f", commission));
    }

    private void updateSalesOverviewStatus()
    {
        // Load target sales set by supervisor for this staff
        db.collection("kpis").whereEqualTo("name", "Personal Sales Figure").get().addOnSuccessListener(querySnapshot -> {
                    double targetSales = 0;
                    for (QueryDocumentSnapshot doc : querySnapshot)
                    {
                        Double t = doc.getDouble("targetValue");
                        if (t != null) targetSales = t;
                    }

                    targetSalesValue.setText(String.format("£%.2f", targetSales));

                    // Determine status based on % of target reached
                    if (targetSales > 0)
                    {
                        double pct = (totalSales / targetSales) * 100;
                        if (pct >= 80)
                        {
                            salesOverviewStatus.setText("On Track");
                            salesOverviewStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
                        }
                        else if (pct >= 40)
                        {
                            salesOverviewStatus.setText("Keep Pushing");
                            salesOverviewStatus.setTextColor(android.graphics.Color.parseColor("#FFA500"));
                        }
                        else
                        {
                            salesOverviewStatus.setText("Falling Behind");
                            salesOverviewStatus.setTextColor(android.graphics.Color.parseColor("#F44336"));
                        }
                    }
                });
    }

    private void updateKpiPerformanceDisplay()
    {
        // Target and actual score text
        targetScoreText.setText("Target Score: " + totalKpiTarget + " KPIs");
        actualScoreText.setText("Actual Score: " + kpisHit + " KPIs");

        // Progress bar
        int progress = totalKpiTarget > 0 ? (kpisHit * 100) / totalKpiTarget : 0;
        kpiProgressBar.setProgress(progress);

        // Three-tier status logic green: hit 80%+ of KPIs, yellow: hit 40–79% , Red: hit less than 40% (1 or 2 out of 7)
        if (totalKpiTarget > 0)
        {
            double pct = ((double) kpisHit / totalKpiTarget) * 100;

            if (pct >= 80)
            {
                performanceStatus.setText("On Track");
                performanceStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
            }
            else if (pct >= 40)
            {
                performanceStatus.setText("Keep Pushing");
                performanceStatus.setTextColor(android.graphics.Color.parseColor("#FFA500"));
            }
            else
            {
                performanceStatus.setText("Falling Behind");
                performanceStatus.setTextColor(android.graphics.Color.parseColor("#F44336"));
            }
        }
    }
}