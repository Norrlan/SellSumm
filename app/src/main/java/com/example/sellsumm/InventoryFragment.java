package com.example.sellsumm;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class InventoryFragment extends Fragment
{

    private static final String TAG = "InventoryFragment";

    private FirebaseFirestore db;
    private List<ProductModel> productList = new ArrayList<>();
    private ProductInventoryAdapter adapter;

    private TextView  emptyStateText;
    private RecyclerView recyclerView;

    public InventoryFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_inventory, container, false);

        db = FirebaseFirestore.getInstance();


        emptyStateText  = view.findViewById(R.id.textView7);
        recyclerView    = view.findViewById(R.id.products_results_recycler);
        ImageView btnAdd = view.findViewById(R.id.imageView);
        TextInputEditText searchBar = view.findViewById(R.id.Search_bar);

        // Adapter setup for Tap product card → open edit dialog and the delete button
        adapter = new ProductInventoryAdapter(productList, product -> openProductDialog(product), product -> deleteProduct(product)
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Search bar
        searchBar.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        //Add button
        btnAdd.setOnClickListener(v -> openProductDialog(null));

        loadProducts();

        return view;
    }

    // Load all products from Firestore
    private void loadProducts() {db.collection("products").get().addOnSuccessListener(querySnapshot -> {productList.clear();
        for (QueryDocumentSnapshot doc : querySnapshot)
        {
            ProductModel product = new ProductModel(doc.getString("productId"), doc.getString("sku"),doc.getDouble("price") != null ? doc.getDouble("price") : 0, doc.getString("productName"), doc.getString("productType"));
            productList.add(product);
        } adapter.updateList(productList); updateEmptyState();
                }).addOnFailureListener(e -> Log.e(TAG, "Failed to load products: " + e.getMessage()));
    }

    // Open dialog for add or edit
    private void openProductDialog(ProductModel existingProduct)
    {
        ProductConfigDialog dialog = existingProduct == null ? ProductConfigDialog.newInstanceForAdd() : ProductConfigDialog.newInstanceForEdit(existingProduct);

        dialog.setProductSaveListener(savedProduct ->
        {
            //  Logic to check if the product is an edit or a new addition
            boolean isEdit = false;
            for (int i = 0; i < productList.size(); i++)
            {
                if (productList.get(i).getProductId().equals(savedProduct.getProductId()))
                {
                    productList.set(i, savedProduct);
                    isEdit = true;
                    break;
                }
            }

            if (!isEdit)
            {
                productList.add(savedProduct);
            }
            adapter.updateList(productList);
            updateEmptyState();
        });

        dialog.show(getChildFragmentManager(), "ProductConfigDialog");
    }

    // Method to Delete product
    private void deleteProduct(ProductModel product)
    {
        // Remove from Firestore
        db.collection("products").document(product.getProductId()).delete().addOnSuccessListener(aVoid -> {
                    // Remove from local list
                    productList.remove(product);
                    adapter.updateList(productList);
                    updateEmptyState();
                    Log.d(TAG, "Product deleted: " + product.getProductName());
                })
                .addOnFailureListener(e -> Log.e(TAG, "Delete failed: " + e.getMessage()));
    }

    //Show or hide empty state
    private void updateEmptyState()
    {
        boolean isEmpty = productList.isEmpty();
        emptyStateText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}