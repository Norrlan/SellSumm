package com.example.sellsumm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProductInventoryAdapter extends
        RecyclerView.Adapter<ProductInventoryAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onClick(ProductModel product);
    }

    public interface OnProductDeleteListener {
        void onDelete(ProductModel product);
    }

    private List<ProductModel> productList;
    private List<ProductModel> filteredList;
    private final OnProductClickListener  clickListener;
    private final OnProductDeleteListener deleteListener;

    public ProductInventoryAdapter(List<ProductModel> productList, OnProductClickListener clickListener, OnProductDeleteListener deleteListener)
    {
        this.productList    = productList;
        this.filteredList   = new ArrayList<>(productList);
        this.clickListener  = clickListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.created_product_row, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ProductViewHolder holder, int position)
    {
        ProductModel product = filteredList.get(position);

        holder.productName.setText(product.getProductName());

        // Tap card → edit dialog
        holder.itemView.setOnClickListener(
                v -> clickListener.onClick(product));

        // Tap delete
        holder.deleteBtn.setOnClickListener(
                v -> deleteListener.onDelete(product));
    }

    @Override
    public int getItemCount() {
        return filteredList != null ? filteredList.size() : 0;
    }

    // Called from InventoryFragment search bar
    public void filter(String query)
    {
        filteredList.clear();
        if (query.isEmpty())
        {
            filteredList.addAll(productList);
        }
        else
        {
            String lower = query.toLowerCase().trim();
            for (ProductModel p : productList)
            {
                if (p.getProductName().toLowerCase().contains(lower))
                {
                    filteredList.add(p);
                }
            }
        }
        notifyDataSetChanged();
    }

    // Called after Firestore adds or updates a product
    public void updateList(List<ProductModel> newList)
    {
        productList = newList;
        filteredList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder
    {
        TextView  productName;
        ImageView deleteBtn;

        ProductViewHolder(View itemView)
        {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            deleteBtn   = itemView.findViewById(R.id.productDeleteButton);
        }
    }
}