package com.example.sellsumm;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StaffProductAdapter extends RecyclerView.Adapter<StaffProductAdapter.ViewHolder>
{

    private Context context;
    private List<ProductModel> productList;

    public static List<MakeSaleModel.DraftTransaction> draftTransactions;

    public StaffProductAdapter(Context context, List<ProductModel> productList)
    {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.sales_row_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {

        ProductModel product = productList.get(position);
        holder.name.setText(product.getProductName());
        holder.price.setText("£" + product.getPrice());
        holder.category.setText(product.getProductCategory());
        holder.type.setText(product.getProductType());

        holder.addBtn.setOnClickListener(v ->
        {

            String qtyStr = holder.quantity.getText().toString().trim();

            if (TextUtils.isEmpty(qtyStr))
            {
                Toast.makeText(context, "Enter quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            int qty = Integer.parseInt(qtyStr);
            double totalAmount = qty * product.getPrice();
            boolean hasAddon = product.isAddon();

            showTransactionDialog(product, qty, totalAmount, hasAddon);
        });
    }

    @Override
    public int getItemCount()
    {
        return productList.size();
    }

    private void showTransactionDialog(ProductModel product, int qty, double totalAmount, boolean hasAddon)
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add to Transaction");

        builder.setItems(new CharSequence[]{"New Transaction", "Existing Transaction"}, (dialog, which) ->
        {
            if (which == 0)
            {
               MakeSaleModel.DraftTransaction newTx = new MakeSaleModel.DraftTransaction("tx_" + System.currentTimeMillis(), qty, totalAmount, hasAddon);

                // Add item to this new transaction
                newTx.getItems().add(product.getProductName() + " x" + qty + " (£" + product.getPrice() + ")");

                draftTransactions.add(newTx);

                // Notify UI
                if (StaffSalesFragment.adapterInstance != null)
                {
                    StaffSalesFragment.adapterInstance.notifyItemInserted(draftTransactions.size() - 1);
                }

                Toast.makeText(context, "Added to Transaction " + draftTransactions.size(), Toast.LENGTH_SHORT).show();
            }

            // for exisintg transactions
            else {

                if (draftTransactions.isEmpty())
                {
                    Toast.makeText(context, "No existing transactions", Toast.LENGTH_SHORT).show();
                    return;
                }

                String[] txNames = new String[draftTransactions.size()];
                for (int i = 0; i < draftTransactions.size(); i++)
                {
                    txNames[i] = "Transaction " + (i + 1);
                }

                AlertDialog.Builder chooseTx = new AlertDialog.Builder(context);
                chooseTx.setTitle("Choose Transaction");

                chooseTx.setItems(txNames, (dialog2, index) ->
                {

                    MakeSaleModel.DraftTransaction oldTx = draftTransactions.get(index);

                    // Update transaction total
                    int newUnits = oldTx.getTotalUnits() + qty;
                    double newAmount = oldTx.getTotalAmount() + totalAmount;
                    boolean newAddon = oldTx.isHasAddon() || hasAddon;

                    // Create updated transaction
                    MakeSaleModel.DraftTransaction updatedTx = new MakeSaleModel.DraftTransaction(oldTx.getId(), newUnits, newAmount, newAddon);

                    updatedTx.getItems().addAll(oldTx.getItems());

                    // Add new item
                    updatedTx.getItems().add(product.getProductName() + " x" + qty + " (£" + product.getPrice() + ")");

                    draftTransactions.set(index, updatedTx);

                    // Notify UI
                    if (StaffSalesFragment.adapterInstance != null)
                    {
                        StaffSalesFragment.adapterInstance.notifyItemChanged(index);
                    }

                    Toast.makeText(context, "Added to " + txNames[index], Toast.LENGTH_SHORT).show();
                });

                chooseTx.show();
            }
        });

        builder.show();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {

        TextView name, price, category, type;
        EditText quantity;
        Button addBtn;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            name = itemView.findViewById(R.id.sales_Name);
            price = itemView.findViewById(R.id.sales_price);
            category = itemView.findViewById(R.id.sales_cat);
            type = itemView.findViewById(R.id.sales_type);
            quantity = itemView.findViewById(R.id.sales_quant);
            addBtn = itemView.findViewById(R.id.button4);
        }
    }
}
