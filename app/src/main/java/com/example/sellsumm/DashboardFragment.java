package com.example.sellsumm;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardFragment extends Fragment {

    private TextView bestEmployeeName, bestEmployeeAtv, bestEmployeeUpt, bestEmployeeAttachment;
    private FirebaseFirestore db;
    private String storeId; // no default

    public DashboardFragment() {
        // Required empty public constructor
    }

    // ⭐ Correct newInstance method
    public static DashboardFragment newInstance(String storeId) {
        DashboardFragment fragment = new DashboardFragment();
        Bundle args = new Bundle();
        args.putString("storeId", storeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ⭐ Read storeId passed from SupervisorMainActivity
        if (getArguments() != null) {
            storeId = getArguments().getString("storeId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Navigate to Commissions screen
        LinearLayout salesTab = view.findViewById(R.id.sales_tab);
        salesTab.setOnClickListener(v -> {
            Fragment sup = new SupcommissionFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, sup)
                    .addToBackStack(null)
                    .commit();
        });

        // Navigate to Manage Staff screen
        LinearLayout manageStaffTab = view.findViewById(R.id.staff_member_tab);
        manageStaffTab.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ManageStaffActivity.class);
            intent.putExtra("storeId", storeId); // ⭐ pass storeId
            startActivity(intent);
        });

        bestEmployeeName = view.findViewById(R.id.best_employee_name);
        bestEmployeeAtv = view.findViewById(R.id.best_employee_atv);
        bestEmployeeUpt = view.findViewById(R.id.best_employee_upt);
        bestEmployeeAttachment = view.findViewById(R.id.best_employee_attachment);

        db = FirebaseFirestore.getInstance();

        loadBestEmployee();

        return view;
    }

    private void loadBestEmployee() {
        db.collection("stores")
                .document(storeId)
                .collection("staffPerformance")
                .get()
                .addOnSuccessListener(query -> {

                    String topStaffId = null;
                    double topAtv = -1;

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        double totalSales = doc.contains("totalSales") ? doc.getDouble("totalSales") : 0;
                        int totalTransactions = doc.contains("totalTransactions") ? doc.getLong("totalTransactions").intValue() : 0;

                        if (totalTransactions == 0) continue;

                        double atv = totalSales / totalTransactions;

                        if (atv > topAtv) {
                            topAtv = atv;
                            topStaffId = doc.getId();
                        }
                    }

                    if (topStaffId != null) {
                        loadBestEmployeeKPIs(topStaffId);
                    }
                });
    }

    private void loadBestEmployeeKPIs(String staffId) {
        db.collection("stores")
                .document(storeId)
                .collection("staffPerformance")
                .document(staffId)
                .get()
                .addOnSuccessListener(doc -> {

                    double totalSales = doc.getDouble("totalSales");
                    int totalUnits = doc.getLong("totalUnits").intValue();
                    int totalTransactions = doc.getLong("totalTransactions").intValue();
                    int transactionsWithAddons = doc.getLong("transactionsWithAddons").intValue();

                    double atv = totalSales / totalTransactions;
                    int upt = totalUnits / totalTransactions;
                    double attachmentRate = ((double) transactionsWithAddons / totalTransactions) * 100;

                    loadStaffName(staffId, atv, upt, attachmentRate);
                });
    }

    private void loadStaffName(String staffId, double atv, int upt, double attachmentRate) {
        db.collection("stores")
                .document(storeId)
                .collection("staff")
                .document(staffId)
                .get()
                .addOnSuccessListener(doc -> {

                    String name = doc.contains("name") ? doc.getString("name") : "Unknown";

                    bestEmployeeName.setText("Employee: " + name);
                    bestEmployeeAtv.setText("ATV: £" + String.format("%.2f", atv));
                    bestEmployeeUpt.setText("UPT: " + upt);
                    bestEmployeeAttachment.setText("Attachment Rate: " + String.format("%.1f", attachmentRate) + "%");
                });
    }
}
