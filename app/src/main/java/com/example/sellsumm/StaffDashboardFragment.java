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

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUid;

    // UI
    private TextView welcomeText;
    private TextView targetSalesValue;
    private TextView salesFigureValue;
    private TextView atvValue;
    private TextView unitsSoldValue;
    private TextView unitsPerTransValue;
    private TextView attachRateValue;
    private TextView salesOverviewStatus;
    private TextView performanceStatus;
    private TextView targetScoreText;
    private TextView actualScoreText;
    private ProgressBar kpiProgressBar;

    private TextView rateValue;
    private TextView commissionAmountValue;

    // Data
    private double totalSales = 0;
    private double commissionRate = 0;
    private int totalTransactions = 0;
    private int transWithAddOn = 0;
    private int totalUnitsSold = 0;
    private int totalKpiTarget = 0;
    private int kpisHit = 0;

    public StaffDashboardFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_staff_dashboard, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        rateValue = view.findViewById(R.id.viewRateValue);
        commissionAmountValue = view.findViewById(R.id.commissionValue);

        welcomeText        = view.findViewById(R.id.welcomeText);
        targetSalesValue   = view.findViewById(R.id.targetSalesValue);
        salesFigureValue   = view.findViewById(R.id.salesFigureValue);
        atvValue           = view.findViewById(R.id.atvValue);
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

        loadUserProfile();
        loadCommissionRate();
        loadTransactions();
        loadKpiPerformance();

        return view;
    }

    // Load staff name
    private void loadUserProfile() {
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

    // Load commission rate from global settings
    private void loadCommissionRate() {
        db.collection("commissionSettings").document("default").get().addOnSuccessListener(doc ->
                {
                    if (doc.exists() && doc.getDouble("rate") != null)
                    {
                        commissionRate = doc.getDouble("rate");
                    }
                    updateCommissionDisplay();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load commission rate: " + e.getMessage()));
    }

    // Load transactions for this staff member
    private void loadTransactions()
    {

        db.collection("stores").document("STORE_ID_HERE").collection("transactions").whereEqualTo("staffId", currentUid).get()
                .addOnSuccessListener(querySnapshot ->
                {
                    totalSales = 0;
                    totalTransactions = 0;
                    transWithAddOn = 0;
                    totalUnitsSold = 0;

                    for (QueryDocumentSnapshot doc : querySnapshot)
                    {
                        totalTransactions++;

                        Double txValue = doc.getDouble("totalAmount");
                        if (txValue != null) totalSales += txValue;

                        Long units = doc.getLong("totalUnits");
                        if (units != null) totalUnitsSold += units;

                        Boolean hasAddOn = doc.getBoolean("hasAddon");
                        if (hasAddOn != null && hasAddOn) transWithAddOn++;
                    }

                    updateSalesDisplay();
                    updateCommissionDisplay();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load transactions: " + e.getMessage()));
    }



    // Load KPI performance
    private void loadKpiPerformance() {
        db.collection("kpis").get()
                .addOnSuccessListener(querySnapshot -> {
                    totalKpiTarget = querySnapshot.size();
                    checkKpisHit();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load KPIs: " + e.getMessage()));
    }

    private void checkKpisHit()
    {
        db.collection("kpiLogs").whereEqualTo("staffId", currentUid).get().addOnSuccessListener(querySnapshot -> {

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



    private void updateSalesDisplay()
    {

        salesFigureValue.setText(String.format("£%.2f", totalSales));

        double atv = totalTransactions > 0 ? totalSales / totalTransactions : 0;
        atvValue.setText(String.format("£%.2f", atv));

        unitsSoldValue.setText(String.valueOf(totalUnitsSold));

        double upt = totalTransactions > 0 ? (double) totalUnitsSold / totalTransactions : 0;
        unitsPerTransValue.setText(String.format("%.1f", upt));

        double attachRate = totalTransactions > 0 ? ((double) transWithAddOn / totalTransactions) * 100 : 0;
        attachRateValue.setText(String.format("%.1f%%", attachRate));

        updateSalesOverviewStatus();
    }

    private void updateCommissionDisplay()
    {

        if (rateValue != null) {
            rateValue.setText(String.format("Rate: %.1f%%", commissionRate));
        }

        if (commissionAmountValue != null)
        {
            double commission = totalSales * (commissionRate / 100);
            commissionAmountValue.setText(String.format("Amount (£): %.2f", commission));
        }
    }


    private void updateSalesOverviewStatus()
    {

        db.collection("kpis")
                .whereEqualTo("name", "Personal Sales Figure")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    double targetSales = 0;

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Double t = doc.getDouble("targetValue");
                        if (t != null) targetSales = t;
                    }

                    targetSalesValue.setText(String.format("£%.2f", targetSales));

                    if (targetSales > 0) {

                        double pct = (totalSales / targetSales) * 100;

                        if (pct >= 80) {
                            salesOverviewStatus.setText("On Track");
                            salesOverviewStatus.setTextColor(0xFF4CAF50);
                        }
                        else if (pct >= 40) {
                            salesOverviewStatus.setText("Keep Pushing");
                            salesOverviewStatus.setTextColor(0xFFFFA500);
                        }
                        else {
                            salesOverviewStatus.setText("Falling Behind");
                            salesOverviewStatus.setTextColor(0xFFF44336);
                        }
                    }
                });
    }

    private void updateKpiPerformanceDisplay() {

        targetScoreText.setText("Target Score: " + totalKpiTarget + " KPIs");
        actualScoreText.setText("Actual Score: " + kpisHit + " KPIs");

        int progress = totalKpiTarget > 0 ? (kpisHit * 100) / totalKpiTarget : 0;
        kpiProgressBar.setProgress(progress);

        if (totalKpiTarget > 0) {

            double pct = ((double) kpisHit / totalKpiTarget) * 100;

            if (pct >= 80) {
                performanceStatus.setText("On Track");
                performanceStatus.setTextColor(0xFF4CAF50);
            }
            else if (pct >= 40) {
                performanceStatus.setText("Keep Pushing");
                performanceStatus.setTextColor(0xFFFFA500);
            }
            else {
                performanceStatus.setText("Falling Behind");
                performanceStatus.setTextColor(0xFFF44336);
            }
        }
    }
}
