package com.example.sellsumm;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MakeSaleAdapter extends RecyclerView.Adapter<MakeSaleAdapter.ViewHolder>
{

    private Context context;
    private List<MakeSaleModel.DraftTransaction> transactions;
    private String storeId;
    private String staffId;
    private FirebaseFirestore db;

    public MakeSaleAdapter(Context context, List<MakeSaleModel.DraftTransaction> transactions, String storeId, String staffId) {
        this.context = context;
        this.transactions = transactions;
        this.storeId = storeId;
        this.staffId = staffId;
        this.db = FirebaseFirestore.getInstance();
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.make_sale_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        MakeSaleModel.DraftTransaction tx = transactions.get(position);

        holder.transactionNum.setText("Transaction " + (position + 1));

        holder.makeSaleBtn.setOnClickListener(v -> finalizeSale(tx, position));
        holder.itemView.setOnClickListener(v -> showItemsDialog(tx));

    }

    private void finalizeSale(MakeSaleModel.DraftTransaction tx, int position)
    {

        String transactionId = db.collection("stores").document(storeId).collection("transactions").document().getId();

        // Build transaction data
        Map<String, Object> saleData = new HashMap<>();
        saleData.put("transactionId", transactionId);
        saleData.put("staffId", staffId);
        saleData.put("totalUnits", tx.getTotalUnits());
        saleData.put("totalAmount", tx.getTotalAmount());
        saleData.put("totalPrice", tx.getTotalAmount());
        saleData.put("hasAddon", tx.isHasAddon());
        saleData.put("timestamp", FieldValue.serverTimestamp());

        // Save transaction to Firestore
        db.collection("stores").document(storeId).collection("transactions").document(transactionId)
                .set(saleData)
                .addOnSuccessListener(aVoid ->
                {

                    // Update staff performance summary
                    updateStaffPerformance(tx);

                    // Remove draft from list
                    transactions.remove(position);
                    notifyItemRemoved(position);

                });
    }

    private void updateStaffPerformance(MakeSaleModel.DraftTransaction tx)
    {

        DocumentReference perfRef = db.collection("stores").document(storeId).collection("staffPerformance").document(staffId);

        perfRef.get().addOnSuccessListener(doc ->
        {

            double oldTotalSales = doc.contains("totalSales") ? doc.getDouble("totalSales") : 0;
            int oldTotalUnits = doc.contains("totalUnits") ? doc.getLong("totalUnits").intValue() : 0;
            int oldTotalTransactions = doc.contains("totalTransactions") ? doc.getLong("totalTransactions").intValue() : 0;
            int oldTransactionsWithAddons = doc.contains("transactionsWithAddons") ? doc.getLong("transactionsWithAddons").intValue() : 0;

            double newTotalSales = oldTotalSales + tx.getTotalAmount();
            int newTotalUnits = oldTotalUnits + tx.getTotalUnits();
            int newTotalTransactions = oldTotalTransactions + 1;
            int newTransactionsWithAddons = oldTransactionsWithAddons + (tx.isHasAddon() ? 1 : 0);

            Map<String, Object> updated = new HashMap<>();
            updated.put("totalSales", newTotalSales);
            updated.put("totalUnits", newTotalUnits);
            updated.put("totalTransactions", newTotalTransactions);
            updated.put("transactionsWithAddons", newTransactionsWithAddons);

            perfRef.set(updated)
                    .addOnSuccessListener(a ->
                            updateKpiActualValues(
                                    newTotalSales,
                                    newTotalUnits,
                                    newTotalTransactions,
                                    newTransactionsWithAddons
                            )
                    );
        });
    }


    private void updateKpiActualValues(double totalSales, int totalUnits, int totalTransactions, int transactionsWithAddons)
    {

        double atv = totalSales / totalTransactions;
        double upt = (double) totalUnits / totalTransactions;
        double attachmentRate = ((double) transactionsWithAddons / totalTransactions) * 100;

        Map<String, Double> kpiValues = new HashMap<>();
        kpiValues.put("Average Transaction Value", atv);
        kpiValues.put("Units per Transaction", upt);
        kpiValues.put("Units Sold", (double) totalUnits);
        kpiValues.put("Sales Figure", totalSales);
        kpiValues.put("Attachment Rate", attachmentRate);
        kpiValues.put("Customers Engagement", (double) totalTransactions);

        // Load supervisor KPIs
        db.collection("stores").document(storeId).collection("kpis").get().addOnSuccessListener(query ->
                {

                    for (DocumentSnapshot doc : query.getDocuments())
                    {

                        String kpiName = doc.getString("name");
                        String kpiId = doc.getId();

                        if (kpiValues.containsKey(kpiName))
                        {

                            double actualValue = kpiValues.get(kpiName);
                            Map<String, Object> actualData = new HashMap<>();
                            actualData.put("actualValue", actualValue);

                            db.collection("stores").document(storeId).collection("staffPerformance").document(staffId).collection("kpis")
                                    .document(kpiId)
                                    .set(actualData, SetOptions.merge());
                        }
                    }
                });
    }



    @Override
    public int getItemCount()
    {
        return transactions.size();
    }

    private void showItemsDialog(MakeSaleModel.DraftTransaction tx)
    {

        if (tx.getItems().isEmpty())
        {
            new AlertDialog.Builder(context).setTitle("Items").setMessage("No items in this transaction yet.").setPositiveButton("OK", null)
                    .show();
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (String item : tx.getItems())
        {
            builder.append("• ").append(item).append("\n");
        }

        new AlertDialog.Builder(context).setTitle("Transaction Items").setMessage(builder.toString()).setPositiveButton("OK", null)
                .show();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView transactionNum;
        Button makeSaleBtn;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            transactionNum = itemView.findViewById(R.id.TransacationNum);
            makeSaleBtn = itemView.findViewById(R.id.make_sale_btn);
        }
    }
}
