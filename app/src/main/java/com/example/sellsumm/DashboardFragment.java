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

    private TextView salesFigureValue;
    private TextView atvValue;
    private TextView uptValue;
    private TextView salesPerEmployeeValue;
    private TextView bestEmployeeName, bestEmployeeAtv, bestEmployeeUpt, bestEmployeeAttachment;
    private FirebaseFirestore db;
    private String storeId; // no default

    public DashboardFragment() {
        // Required empty public constructor
    }

    // Correct newInstance method
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

        // Read storeId passed from SupervisorMainActivity
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

        // Navigate to Manage Staff screen and pass the store id into it
        LinearLayout manageStaffTab = view.findViewById(R.id.staff_member_tab);
        manageStaffTab.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ManageStaffActivity.class);
            intent.putExtra("storeId", storeId);
        });

        salesFigureValue = view.findViewById(R.id.text_sales_figure_value);
        atvValue = view.findViewById(R.id.text_atv_value);
        uptValue = view.findViewById(R.id.text_upt_value);
        salesPerEmployeeValue = view.findViewById(R.id.text_spe_value);
        bestEmployeeName = view.findViewById(R.id.best_employee_name);
        bestEmployeeAtv = view.findViewById(R.id.best_employee_atv);
        bestEmployeeUpt = view.findViewById(R.id.best_employee_upt);
        bestEmployeeAttachment = view.findViewById(R.id.best_employee_attachment);

        db = FirebaseFirestore.getInstance();

        loadStoreMetrics();
        loadBestEmployee();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }

        if (storeId != null && !storeId.isEmpty()) {
            loadStoreMetrics();
            loadBestEmployee();
        }
    }

    private void loadStoreMetrics() {
        db.collection("stores")
                .document(storeId)
                .collection("staffPerformance")
                .get()
                .addOnSuccessListener(query -> {
                    double totalSales = 0;
                    int totalUnits = 0;
                    int totalTransactions = 0;

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Double staffSales = doc.getDouble("totalSales");
                        Long staffUnits = doc.getLong("totalUnits");
                        Long staffTransactions = doc.getLong("totalTransactions");

                        totalSales += staffSales != null ? staffSales : 0;
                        totalUnits += staffUnits != null ? staffUnits.intValue() : 0;
                        totalTransactions += staffTransactions != null ? staffTransactions.intValue() : 0;
                    }

                    double atv = totalTransactions > 0 ? totalSales / totalTransactions : 0;
                    double upt = totalTransactions > 0 ? (double) totalUnits / totalTransactions : 0;

                    salesFigureValue.setText("£" + String.format("%.2f", totalSales));
                    atvValue.setText("£" + String.format("%.2f", atv));
                    uptValue.setText(String.format("%.1f", upt));

                    loadSalesPerEmployee(totalSales);
                });
    }

    private void loadSalesPerEmployee(double totalSales) {
        db.collection("users")
                .whereEqualTo("role", "staff")
                .whereEqualTo("storeId", storeId)
                .get()
                .addOnSuccessListener(query -> {
                    int employeeCount = query.size();
                    double salesPerEmployee = employeeCount > 0 ? totalSales / employeeCount : 0;
                    salesPerEmployeeValue.setText("£" + String.format("%.2f", salesPerEmployee));
                });
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
                        Double totalSalesValue = doc.getDouble("totalSales");
                        Long totalTransactionsValue = doc.getLong("totalTransactions");

                        double totalSales = totalSalesValue != null ? totalSalesValue : 0;
                        int totalTransactions = totalTransactionsValue != null ? totalTransactionsValue.intValue() : 0;

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

                    Double totalSalesValue = doc.getDouble("totalSales");
                    Long totalUnitsValue = doc.getLong("totalUnits");
                    Long totalTransactionsValue = doc.getLong("totalTransactions");
                    Long transactionsWithAddonsValue = doc.getLong("transactionsWithAddons");

                    double totalSales = totalSalesValue != null ? totalSalesValue : 0;
                    int totalUnits = totalUnitsValue != null ? totalUnitsValue.intValue() : 0;
                    int totalTransactions = totalTransactionsValue != null ? totalTransactionsValue.intValue() : 0;
                    int transactionsWithAddons = transactionsWithAddonsValue != null ? transactionsWithAddonsValue.intValue() : 0;

                    if (totalTransactions == 0) {
                        bestEmployeeAtv.setText("ATV: £0.00");
                        bestEmployeeUpt.setText("UPT: 0.0");
                        bestEmployeeAttachment.setText("Attachment Rate: 0.0%");
                        loadStaffName(staffId, 0, 0, 0);
                        return;
                    }

                    double atv = totalSales / totalTransactions;
                    double upt = (double) totalUnits / totalTransactions;
                    double attachmentRate = ((double) transactionsWithAddons / totalTransactions) * 100;

                    loadStaffName(staffId, atv, upt, attachmentRate);
                });
    }

    private void loadStaffName(String staffId, double atv, double upt, double attachmentRate) {
        db.collection("users")
                .document(staffId)
                .get()
                .addOnSuccessListener(doc -> {

                    String name = doc.contains("fullName") ? doc.getString("fullName") : "Unknown";

                    bestEmployeeName.setText("Employee: " + name);
                    bestEmployeeAtv.setText("ATV: £" + String.format("%.2f", atv));
                    bestEmployeeUpt.setText("UPT: " + String.format("%.1f", upt));
                    bestEmployeeAttachment.setText("Attachment Rate: " + String.format("%.1f", attachmentRate) + "%");
                });
    }
}
