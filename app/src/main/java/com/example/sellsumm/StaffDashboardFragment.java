package com.example.sellsumm;

import android.graphics.Color;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StaffDashboardFragment extends Fragment {

    private static final String TAG = "StaffDashboardFragment";

    // ── Firebase ─────────────────────────────────────────────────
    private FirebaseFirestore db;
    private String staffId;
    private String storeId;

    // ── Data ─────────────────────────────────────────────────────
    private double totalSales        = 0;
    private double commissionRate    = 0;
    private int    totalTransactions = 0;
    private int    transWithAddOn    = 0;
    private int    totalUnitsSold    = 0;
    private int    totalKpiTarget    = 0;
    private int    kpisHit           = 0;

    // ── Views ─────────────────────────────────────────────────────
    private TextView welcomeText;
    private TextView targetSalesValue;
    private TextView salesFigureValue;
    private TextView atvValue;
    private TextView transactionValue;
    private TextView unitsSoldValue;
    private TextView unitsPerTransValue;
    private TextView attachRateValue;
    private TextView salesOverviewStatus;
    private TextView performanceStatus;
    private TextView targetScoreText;
    private TextView actualScoreText;
    private TextView viewRateValue;
    private TextView commissionValue;
    private ProgressBar kpiProgressBar;

    public StaffDashboardFragment() {}

    public static StaffDashboardFragment newInstance(String storeId)
    {
        StaffDashboardFragment fragment = new StaffDashboardFragment();
        Bundle args = new Bundle();
        args.putString("storeId", storeId);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_staff_dashboard, container, false);

        db      = FirebaseFirestore.getInstance();
        staffId = FirebaseAuth.getInstance()
                .getCurrentUser().getUid();

        // ── Bind all views ────────────────────────────────────────
        welcomeText        = view.findViewById(R.id.welcomeText);
        targetSalesValue   = view.findViewById(R.id.targetSalesValue);
        salesFigureValue   = view.findViewById(R.id.salesFigureValue);
        atvValue           = view.findViewById(R.id.atvValue);
        transactionValue   = view.findViewById(R.id.transactionValue);
        unitsSoldValue     = view.findViewById(R.id.unitsSoldValue);
        unitsPerTransValue = view.findViewById(R.id.unitsPerTransValue);
        attachRateValue    = view.findViewById(R.id.attachRateValue);
        salesOverviewStatus = view.findViewById(R.id.salesOverviewStatus);
        performanceStatus  = view.findViewById(R.id.performanceStatus);
        targetScoreText    = view.findViewById(R.id.targetScoreText);
        actualScoreText    = view.findViewById(R.id.actualScoreText);
        viewRateValue      = view.findViewById(R.id.viewRateValue);
        commissionValue    = view.findViewById(R.id.commissionValue);
        kpiProgressBar     = view.findViewById(R.id.kpiProgressBar);

        // ── Step 1: Load storeId from user document first ─────────
        // Everything else depends on storeId so it must load first
        loadStoreIdThenData();

        return view;
    }

    // ── Step 1 — Get storeId from users collection ───────────────
    private void loadStoreIdThenData() {
        db.collection("users")
                .document(staffId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String fullName = doc.getString("fullName");
                        storeId = doc.getString("storeId");

                        // Set welcome message
                        if (fullName != null) {
                            welcomeText.setText(
                                    "Welcome back " + fullName + "!");
                        }

                        if (storeId == null || storeId.isEmpty()) {
                            Log.e(TAG, "storeId missing from user doc");
                            return;
                        }

                        // ── Now load everything else ──────────────
                        loadTargetSales();
                        loadTransactions();
                        loadCommissionRate();
                        loadKpiPerformance();
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to load user: "
                                + e.getMessage()));
    }

    // ── Step 2 — Load target sales from KPIs collection ──────────
    // The supervisor sets this via KPIFragment
    private void loadTargetSales() {
        db.collection("stores")
                .document(storeId)
                .collection("kpis")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    double target = 0;
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String kpiName = doc.getString("name");
                        if ("Sales Figure".equalsIgnoreCase(kpiName)
                                || "Personal Sales Figure".equalsIgnoreCase(kpiName)) {
                            Double t = doc.getDouble("targetValue");
                            if (t != null) {
                                target = t;
                                break;
                            }
                        }
                    }
                    targetSalesValue.setText(
                            String.format("£%.2f", target));

                    // Store target for status calculation
                    final double finalTarget = target;
                    // Update status once transactions are loaded
                    // Status is updated inside updateSalesDisplay()
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to load target: "
                                + e.getMessage()));
    }

    // ── Step 3 — Load transactions for this staff member ─────────
    private void loadTransactions() {
        db.collection("stores")
                .document(storeId)
                .collection("transactions")
                .whereEqualTo("staffId", staffId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    totalSales        = 0;
                    totalTransactions = 0;
                    transWithAddOn    = 0;
                    totalUnitsSold    = 0;

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        totalTransactions++;

                        // Add to sales total
                        Double txValue = doc.getDouble("totalAmount");
                        if (txValue != null) totalSales += txValue;

                        // Count add-on transactions
                        Boolean hasAddOn = doc.getBoolean("hasAddon");
                        if (hasAddOn != null && hasAddOn) {
                            transWithAddOn++;
                        }

                        // Count units sold from stored total units
                        Long units = doc.getLong("totalUnits");
                        if (units != null) {
                            totalUnitsSold += units.intValue();
                        }
                    }

                    updateSalesDisplay();
                    // Recalculate commission now that sales are loaded
                    updateCommissionDisplay();
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to load transactions: "
                                + e.getMessage()));
    }

    // ── Step 4 — Load commission rate ────────────────────────────
    private void loadCommissionRate() {
        db.collection("commissionSettings")
                .document(storeId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Double rate = doc.getDouble("rate");
                        commissionRate = rate != null ? rate : 0;
                        viewRateValue.setText(
                                "Rate: " + commissionRate + "%");
                    } else {
                        viewRateValue.setText("Rate: 0%");
                    }
                    updateCommissionDisplay();
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to load commission: "
                                + e.getMessage()));
    }

    // ── Step 5 — Load KPI performance ────────────────────────────
    private void loadKpiPerformance() {
        db.collection("stores")
                .document(storeId)
                .collection("kpis")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<DocumentSnapshot> trackedKpis = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String name = doc.getString("name");
                        if (name != null && !"KPI Score".equalsIgnoreCase(name)) {
                            trackedKpis.add(doc);
                        }
                    }

                    totalKpiTarget = trackedKpis.size();
                    loadKpisHit(trackedKpis);
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to load KPIs: "
                                + e.getMessage()));
    }

    private void loadKpisHit(List<DocumentSnapshot> trackedKpis) {
        db.collection("stores")
                .document(storeId)
                .collection("staffPerformance")
                .document(staffId)
                .collection("kpis")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    kpisHit = 0;

                    for (DocumentSnapshot templateDoc : trackedKpis) {
                        String kpiId = templateDoc.getId();
                        Double targetValue = templateDoc.getDouble("targetValue");
                        if (targetValue == null) {
                            continue;
                        }

                        Double actualValue = null;
                        for (com.google.firebase.firestore.DocumentSnapshot actualDoc : querySnapshot.getDocuments()) {
                            if (kpiId.equals(actualDoc.getId())) {
                                actualValue = actualDoc.getDouble("actualValue");
                                break;
                            }
                        }

                        if (actualValue != null && actualValue >= targetValue) {
                            kpisHit++;
                        }
                    }

                    updateKpiPerformanceDisplay();
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to load KPI performance: "
                                + e.getMessage()));
    }

    // ── Display updaters ─────────────────────────────────────────

    private void updateSalesDisplay() {
        // Sales figure
        salesFigureValue.setText(
                String.format("£%.2f", totalSales));

        // Number of transactions
        transactionValue.setText(
                String.valueOf(totalTransactions));

        // ATV — total sales / transactions
        double atv = totalTransactions > 0 ?
                totalSales / totalTransactions : 0;
        atvValue.setText(String.format("£%.2f", atv));

        // Units sold
        unitsSoldValue.setText(
                String.valueOf(totalUnitsSold));

        // Avg products per transaction
        double upt = totalTransactions > 0 ?
                (double) totalUnitsSold / totalTransactions : 0;
        unitsPerTransValue.setText(
                String.format("%.1f", upt));

        // Attach rate
        double attachRate = totalTransactions > 0 ?
                ((double) transWithAddOn / totalTransactions) * 100
                : 0;
        attachRateValue.setText(
                String.format("%.1f%%", attachRate));

        // Sales overview status based on sales vs target
        // Re-fetch target to compare
        db.collection("stores")
                .document(storeId)
                .collection("kpis")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    double target = 0;
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String kpiName = doc.getString("name");
                        if ("Sales Figure".equalsIgnoreCase(kpiName)
                                || "Personal Sales Figure".equalsIgnoreCase(kpiName)) {
                            Double t = doc.getDouble("targetValue");
                            if (t != null) {
                                target = t;
                                break;
                            }
                        }
                    }

                    targetSalesValue.setText(
                            String.format("£%.2f", target));

                    if (target > 0) {
                        double pct = (totalSales / target) * 100;
                        applySalesStatus(pct);
                    }
                });
    }

    private void applySalesStatus(double pct) {
        if (pct >= 80) {
            salesOverviewStatus.setText("On Track");
            salesOverviewStatus.setTextColor(
                    Color.parseColor("#4CAF50"));
        } else if (pct >= 40) {
            salesOverviewStatus.setText("Keep Pushing");
            salesOverviewStatus.setTextColor(
                    Color.parseColor("#FFA500"));
        } else {
            salesOverviewStatus.setText("Falling Behind");
            salesOverviewStatus.setTextColor(
                    Color.parseColor("#F44336"));
        }
    }

    private void updateCommissionDisplay() {
        // Commission = total sales × rate / 100
        double commission = totalSales * (commissionRate / 100);
        commissionValue.setText(
                String.format("Amount (£): %.2f", commission));
    }

    private void updateKpiPerformanceDisplay() {
        targetScoreText.setText(
                "Target KPI Score: " + totalKpiTarget + " KPIs");
        actualScoreText.setText(
                "Actual Score: " + kpisHit + " KPIs");

        int progress = totalKpiTarget > 0 ?
                (kpisHit * 100) / totalKpiTarget : 0;
        kpiProgressBar.setProgress(progress);

        // Three tier status
        if (totalKpiTarget > 0) {
            double pct = ((double) kpisHit / totalKpiTarget) * 100;

            if (pct >= 80) {
                performanceStatus.setText("On Track");
                performanceStatus.setTextColor(
                        Color.parseColor("#4CAF50"));
            } else if (pct >= 40) {
                performanceStatus.setText("Keep Pushing");
                performanceStatus.setTextColor(
                        Color.parseColor("#FFA500"));
            } else {
                performanceStatus.setText("Falling Behind");
                performanceStatus.setTextColor(
                        Color.parseColor("#F44336"));
            }
        }
    }
}
