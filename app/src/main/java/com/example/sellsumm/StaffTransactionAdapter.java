package com.example.sellsumm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StaffTransactionAdapter extends RecyclerView.Adapter<StaffTransactionAdapter.ViewHolder>
{

    private List<TransactionModel> list;

    public StaffTransactionAdapter(List<TransactionModel> list)
    {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.a_transaction_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        TransactionModel model = list.get(position);

        holder.transNumb.setText("Transaction #" + model.getTransactionNumber());
        holder.unitSold.setText(model.getTotalUnits() + " units");
        holder.priceDescription.setText("£" + String.format("%.2f", model.getTotalPrice()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView transNumb, unitSold, priceDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            transNumb = itemView.findViewById(R.id.transNumb);
            unitSold = itemView.findViewById(R.id.unitSold);
            priceDescription = itemView.findViewById(R.id.priceDescription);
        }
    }
}
