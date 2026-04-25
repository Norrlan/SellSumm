package com.example.sellsumm;

import android.os.Bundle;
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

public class staffProductsFragment extends Fragment
{

    private RecyclerView recyclerView;
    private StaffProductAdapter adapter;
    private List<ProductModel> productList;
    private FirebaseFirestore db;

    public staffProductsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        View view = inflater.inflate(R.layout.fragment_staff_products, container, false);

        recyclerView = view.findViewById(R.id.sales_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();

        productList = new ArrayList<>();
        StaffProductAdapter.draftTransactions = new ArrayList<>();

        adapter = new StaffProductAdapter(getContext(), productList);
        recyclerView.setAdapter(adapter);

        loadProducts();

        return view;
    }

    private void loadProducts()
    {
        db.collection("products").get().addOnSuccessListener(query ->
        {
                    productList.clear();
                    for (DocumentSnapshot doc : query.getDocuments())
                    {

                        String id = doc.getString("productId");
                        double price = doc.getDouble("price") != null ? doc.getDouble("price") : 0;
                        String name = doc.getString("productName");
                        String category = doc.getString("productCategory");
                        String type = doc.getString("productType");

                        boolean isAddon = type != null && type.equalsIgnoreCase("Add-on");

                        ProductModel product = new ProductModel(id, price, name, category, type, isAddon);

                        productList.add(product);
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}
