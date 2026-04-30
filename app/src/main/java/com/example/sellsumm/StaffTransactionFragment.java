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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StaffTransactionFragment extends Fragment {

    private static final String TAG = "StaffTransactionFragment";

    private String storeId;
    private String staffId;

    private FirebaseFirestore db;

    private RecyclerView recyclerView;
    private StaffTransactionAdapter adapter;
    private List<TransactionModel> transactionList;

    public StaffTransactionFragment() {}

    public static StaffTransactionFragment newInstance(String storeId)
    {
        StaffTransactionFragment fragment = new StaffTransactionFragment();
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

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        staffId = currentUser != null ? currentUser.getUid() : null;
        db = FirebaseFirestore.getInstance();

        transactionList = new ArrayList<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_staff_transaction, container, false);

        recyclerView = view.findViewById(R.id.analytics_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new StaffTransactionAdapter(transactionList);
        recyclerView.setAdapter(adapter);

        if (storeId == null || storeId.isEmpty() || staffId == null) {
            Log.e(TAG, "storeId is NULL — cannot load transactions");
        } else {
            loadTransactions();
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (storeId != null && !storeId.isEmpty() && staffId != null) {
            loadTransactions();
        }
    }

    // ⭐ Load ONLY this staff member’s transactions
    private void loadTransactions() {
        db.collection("stores")
                .document(storeId)
                .collection("transactions")
                .whereEqualTo("staffId", staffId)
                .get()
                .addOnSuccessListener(query -> {
                    transactionList.clear();

                    List<DocumentSnapshot> documents = new ArrayList<>(query.getDocuments());
                    Collections.sort(documents, (first, second) -> {
                        if (first.getTimestamp("timestamp") == null && second.getTimestamp("timestamp") == null) {
                            return 0;
                        }
                        if (first.getTimestamp("timestamp") == null) {
                            return 1;
                        }
                        if (second.getTimestamp("timestamp") == null) {
                            return -1;
                        }
                        return second.getTimestamp("timestamp").compareTo(first.getTimestamp("timestamp"));
                    });

                    int transactionNumber = 1;

                    for (DocumentSnapshot doc : documents) {

                        Long unitsLong = doc.getLong("totalUnits");
                        Double priceDouble = doc.getDouble("totalAmount");

                        if (unitsLong == null || priceDouble == null) {
                            Log.e(TAG, "Skipping invalid transaction document: " + doc.getId());
                            continue;
                        }

                        int units = unitsLong.intValue();
                        double price = priceDouble;

                        transactionList.add(new TransactionModel(transactionNumber, units, price));
                        transactionNumber++;
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to load transactions: " + e.getMessage()));
    }
}
