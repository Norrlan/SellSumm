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

public class StaffSalesFragment extends Fragment
{

    private RecyclerView recyclerView;
    private MakeSaleAdapter adapter;

    // TEMP: hardcoded IDs until you wire real auth/store selection
    private String storeId = "STORE_ID_HERE";
    private String staffId = "STAFF_ID_HERE";

    public static MakeSaleAdapter adapterInstance;


    public StaffSalesFragment()
    {
        // Required empty public constructor
    }

    public static StaffSalesFragment newInstance()
    {
        return new StaffSalesFragment();
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
            Fragment next = new staffProductsFragment();
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
