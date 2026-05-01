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

public class InventoryFragment extends Fragment {

    private static final String TAG = "InventoryFragment";

    private FirebaseFirestore db;
    private List<ProductModel> productList = new ArrayList<>();
    private ProductInventoryAdapter adapter;

    private TextView emptyStateText;
    private RecyclerView recyclerView;

    private String storeId;

    public InventoryFragment() {}

    public static InventoryFragment newInstance(String storeId)
    {
        InventoryFragment fragment = new InventoryFragment();
        Bundle args = new Bundle();
        args.putString("storeId", storeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            storeId = getArguments().getString("storeId");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_inventory, container, false);

        db = FirebaseFirestore.getInstance();

        emptyStateText = view.findViewById(R.id.textView7);
        recyclerView = view.findViewById(R.id.products_results_recycler);
        ImageView btnAdd = view.findViewById(R.id.imageView);
        TextInputEditText searchBar = view.findViewById(R.id.Search_bar);

        adapter = new ProductInventoryAdapter(
                productList, product -> openProductDialog(product), product -> deleteProduct(product)
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                adapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnAdd.setOnClickListener(v -> openProductDialog(null));

        loadProducts();

        return view;
    }

    // Load the products from the store collection in Firestore
    private void loadProducts() {
        db.collection("stores").document(storeId).collection("products").get()
                .addOnSuccessListener(querySnapshot -> {
                    productList.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot)
                    {

                        String id = doc.getString("productId");
                        double price = doc.getDouble("price") != null ? doc.getDouble("price") : 0;
                        String name = doc.getString("productName");
                        String category = doc.getString("productCategory");
                        String type = doc.getString("productType");

                        boolean isAddon = type != null && type.equalsIgnoreCase("Add-on");

                        ProductModel product = new ProductModel(
                                id,
                                price,
                                name,
                                category,
                                type,
                                isAddon
                        );

                        productList.add(product);
                    }

                    adapter.updateList(productList);
                    updateEmptyState();
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to load products: " + e.getMessage()));
    }

    //Method to open product dialog when creating the product
    private void openProductDialog(ProductModel existingProduct)
    {
        ProductConfigDialog dialog = existingProduct == null ? ProductConfigDialog.newInstanceForAdd(storeId) :
                ProductConfigDialog.newInstanceForEdit(existingProduct, storeId);

        dialog.setProductSaveListener(savedProduct ->
        {

            boolean isEdit = false;
            for (int i = 0; i < productList.size(); i++) {
                if (productList.get(i).getProductId().equals(savedProduct.getProductId())) {
                    productList.set(i, savedProduct);
                    isEdit = true;
                    break;
                }
            }

            if (!isEdit) {
                productList.add(savedProduct);
            }

            adapter.updateList(productList);
            updateEmptyState();
        });

        dialog.show(getChildFragmentManager(), "ProductConfigDialog");
    }

    //method to delete product
    private void deleteProduct(ProductModel product)
    {
        db.collection("stores").document(storeId).collection("products").document(product.getProductId()).delete()
                .addOnSuccessListener(aVoid -> {
                    productList.remove(product);
                    adapter.updateList(productList);
                    updateEmptyState();
                    Log.d(TAG, "Product deleted: " + product.getProductName());
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Delete failed: " + e.getMessage()));
    }

    private void updateEmptyState()
    {
        if (productList.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
