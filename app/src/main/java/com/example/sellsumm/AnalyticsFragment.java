package com.example.sellsumm;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsFragment extends Fragment {

    private RecyclerView recycler;
    private AnalyticsAdapter adapter;
    private List<AnalyticKPIModel> kpiList;

    private FirebaseFirestore db;
    private String storeId = "STORE_ID_HERE"; // Replace later

    public AnalyticsFragment() {}

    public static AnalyticsFragment newInstance() {
        return new AnalyticsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_analytics, container, false);

        recycler = view.findViewById(R.id.analytics_recycler);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        kpiList = new ArrayList<>();
        adapter = new AnalyticsAdapter(getContext(), kpiList);
        recycler.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadAnalytics();

        return view;
    }

    private void loadAnalytics() {

        db.collection("stores")
                .document(storeId)
                .collection("staffPerformance")
                .get()
                .addOnSuccessListener(query -> {

                    double totalSales = 0;
                    int totalUnits = 0;
                    int totalTransactions = 0;
                    int addonCount = 0;
                    int defaultCount = 0;

                    for (DocumentSnapshot doc : query.getDocuments()) {

                        totalSales += doc.getDouble("totalSales");
                        totalUnits += doc.getLong("totalUnits");
                        totalTransactions += doc.getLong("totalTransactions");
                        addonCount += doc.getLong("transactionsWithAddons");

                        // Default item = transaction without addon
                        if (doc.getLong("transactionsWithAddons") == 0) {
                            defaultCount++;
                        }
                    }

                    double atv = totalTransactions == 0 ? 0 : totalSales / totalTransactions;
                    double upt = totalTransactions == 0 ? 0 : (double) totalUnits / totalTransactions;
                    double attachmentRate = totalTransactions == 0 ? 0 :
                            ((double) addonCount / totalTransactions) * 100;

                    // Add KPIs
                    addKPI("ATV", "£" + format(atv), atv, 50);
                    addKPI("Units per Transaction", format(upt), upt, 10);
                    addKPI("Attachment Rate", format(attachmentRate) + "%", attachmentRate, 30);
                    addKPI("Sales Figure", "£" + format(totalSales), totalSales, 10000);
                    addKPI("Customer Engagement", totalTransactions + "", totalTransactions, 200);
                    addKPI("Add-on Count", addonCount + "", addonCount, 100);
                    addKPI("Default item count", defaultCount + "", defaultCount, 100);

                    adapter.notifyDataSetChanged();
                });
    }

    private void addKPI(String title, String value, double actual, double target) {

        int progress = (int) ((actual / target) * 100);
        if (progress > 100) progress = 100;

        int color;
        if (progress >= 80) color = Color.GREEN;
        else if (progress >= 50) color = Color.YELLOW;
        else color = Color.RED;

        kpiList.add(new AnalyticKPIModel(title, value, progress, color));
    }

    private String format(double num) {
        return String.format("%.2f", num);
    }
}
