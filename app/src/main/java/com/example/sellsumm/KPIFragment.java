package com.example.sellsumm;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class KPIFragment extends Fragment
{

    private final List<KPITemplateModel> createdKpis = new ArrayList<>();
    private CreatedKPIAdapter adapter;
    private TextView emptyStateText;
    private RecyclerView recyclerView;

    public KPIFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_k_p_i, container, false);

        emptyStateText = view.findViewById(R.id.empty_state_text);
        recyclerView   = view.findViewById(R.id.kpi_recycler);
        ImageView btnAdd = view.findViewById(R.id.btn_add_kpi);

        // Set up adapter for created KPI cards
        adapter = new CreatedKPIAdapter(createdKpis,
                // Tap KPI card → open edit dialog
                kpi -> openEditDialog(kpi),
                // Tap delete button
                kpi ->
                {
                    createdKpis.remove(kpi);
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                    // TODO: delete from Firestore
                }
        );

        recyclerView.setLayoutManager(
                new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // + button → navigate to KPI Templates screen
        btnAdd.setOnClickListener(v ->
        {
            KpiTemplateFragment templateFragment = new KpiTemplateFragment();

            // Receive saved KPI back from template screen
            templateFragment.setOnKpiCreatedListener(newKpi ->
            {
                createdKpis.add(newKpi);
                adapter.notifyDataSetChanged();
                updateEmptyState();
                // TODO: save to Firestore
            });

            // + button → navigate to KPI Templates screen
            btnAdd.setOnClickListener(view1 ->
            {
                KpiTemplateFragment templateFragment1 = new KpiTemplateFragment();

                templateFragment.setOnKpiCreatedListener(newKpi -> {
                    createdKpis.add(newKpi);
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                });

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, templateFragment)
                        .addToBackStack(null)
                        .commit();
            });
        });

        updateEmptyState();
        return view;
    }

    private void openEditDialog(KPITemplateModel kpi)
    {
        KpiConfigDialog dialog = KpiConfigDialog.newInstanceForEdit(kpi);

        dialog.setSaveListener(updatedKpi ->
        {
            int index = createdKpis.indexOf(kpi);
            if (index >= 0) {
                createdKpis.set(index, updatedKpi);
                adapter.notifyDataSetChanged();
                // TODO: update in Firestore
            }
        });

        dialog.show(getChildFragmentManager(), "EditKpiDialog");
    }

    private void updateEmptyState()
    {
        boolean isEmpty = createdKpis.isEmpty();
        emptyStateText.setVisibility(
                isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(
                isEmpty ? View.GONE : View.VISIBLE);
    }
}