package com.example.sellsumm;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class staffProductsFragment extends Fragment {

    private static final String TAG = "staffProductsFragment";

    private String storeId;

    private RecyclerView recyclerView;
    private StaffProductAdapter adapter;
    private List<ProductModel> productList;
    private FirebaseFirestore db;

    public staffProductsFragment() {}

    public static staffProductsFragment newInstance(String storeId)
    {
        staffProductsFragment fragment = new staffProductsFragment();
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

        db = FirebaseFirestore.getInstance();
        productList = new ArrayList<>();

        if (StaffProductAdapter.draftTransactions == null) {
            StaffProductAdapter.draftTransactions = new ArrayList<>();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_staff_products, container, false);

        recyclerView = view.findViewById(R.id.sales_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new StaffProductAdapter(getContext(), productList);
        recyclerView.setAdapter(adapter);

        if (storeId == null || storeId.isEmpty()) {
            Log.e(TAG, "storeId is NULL — cannot load products");
        } else {
            loadProducts();
        }

        return view;
    }

    private void loadProducts() {
        db.collection("stores")
                .document(storeId)
                .collection("products")
                .get()
                .addOnSuccessListener(query -> {
                    productList.clear();

                    for (DocumentSnapshot doc : query.getDocuments()) {

                        String id = doc.getId();
                        double price = doc.getDouble("price") != null ? doc.getDouble("price") : 0;
                        String name = doc.getString("productName");
                        String category = doc.getString("productCategory");
                        String type = doc.getString("productType");

                        boolean isAddon = type != null && type.equalsIgnoreCase("Add-on");

                        ProductModel product = new ProductModel(
                                id, price, name, category, type, isAddon
                        );

                        productList.add(product);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to load products: " + e.getMessage()));
    }
}
