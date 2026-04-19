package com.example.sellsumm;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class KpiTemplateFragment extends Fragment {

    public interface OnKpiCreatedListener {
        void onKpiCreated(KPITemplateModel kpi);
    }

    private OnKpiCreatedListener kpiCreatedListener;

    public void setOnKpiCreatedListener(OnKpiCreatedListener listener) {
        this.kpiCreatedListener = listener;
    }

    private final List<KPITemplateModel> templates = Arrays.asList(
            new KPITemplateModel("", "Average Transaction Value",
                    "Average spend per customer served", 0, "Higher", "Daily"),
            new KPITemplateModel("", "Units per Transaction",
                    "Higher is better - Shift", 0, "Higher", "Daily"),
            new KPITemplateModel("", "Personal Sales Figure",
                    "Total sales value achieved this shift", 0, "Higher", "Daily"),
            new KPITemplateModel("", "Attachment Rate",
                    "Percentage of sales with an add-on product ", 0, "Higher", "Daily"),
            new KPITemplateModel("", "Sales Figure",
                    "Higher is better - Shift", 0, "Higher", "Daily"),
            new KPITemplateModel("", "Customers Served",
                    "Number of purchasing customers", 0, "Higher", "Daily"),
            new KPITemplateModel("", "Products Demos",
                    "Number of product demos given to customers", 0, "Lower", "Weekly")
    );

    public KpiTemplateFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_kpi_template, container, false);

        ImageView btnBack = view.findViewById(R.id.btn_back);
        RecyclerView recyclerView =
                view.findViewById(R.id.templates_recycler);

        btnBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .popBackStack());

        KPITemplateAdapter adapter = new KPITemplateAdapter(
                templates,
                template -> openConfigDialog(template));

        recyclerView.setLayoutManager(
                new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void openConfigDialog(KPITemplateModel template) {
        KpiConfigDialog dialog =
                KpiConfigDialog.newInstanceForEdit(template);

        dialog.setSaveListener(savedKpi -> {
            if (kpiCreatedListener != null) {
                kpiCreatedListener.onKpiCreated(savedKpi);
            }
            requireActivity().getSupportFragmentManager()
                    .popBackStack();
        });

        dialog.show(getChildFragmentManager(), "KpiConfigDialog");
    }
}