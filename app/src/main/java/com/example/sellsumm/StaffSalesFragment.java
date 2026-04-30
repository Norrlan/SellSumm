package com.example.sellsumm;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

public class StaffSalesFragment extends Fragment
{

    private RecyclerView recyclerView;
    private MakeSaleAdapter adapter;

    private String storeId;
    private String staffId;

    public static MakeSaleAdapter adapterInstance;


    public StaffSalesFragment()
    {
        // Required empty public constructor
    }

    public static StaffSalesFragment newInstance(String storeId)
    {
        StaffSalesFragment fragment = new StaffSalesFragment();
        Bundle args = new Bundle();
        args.putString("storeId", storeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            storeId = getArguments().getString("storeId");
        }

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            staffId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        View view = inflater.inflate(R.layout.fragment_staff_sales, container, false);

        recyclerView = view.findViewById(R.id.sales_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Make sure the shared list is not null
        if (StaffProductAdapter.draftTransactions == null)
        {
            StaffProductAdapter.draftTransactions = new java.util.ArrayList<>();
        }

        // Match MakeSaleAdapter constructor: (Context, List, storeId, staffId)
        adapter = new MakeSaleAdapter(
                getContext(),
                StaffProductAdapter.draftTransactions,
                storeId,
                staffId
        );
        adapterInstance = adapter;
        recyclerView.setAdapter(adapter);

        ImageView addBtn = view.findViewById(R.id.salesaddbtn);
        addBtn.setOnClickListener(v ->
        {
            Fragment next = staffProductsFragment.newInstance(storeId);
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.staff_fragment_container, next).addToBackStack(null)
                    .commit();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
