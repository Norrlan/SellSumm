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

public class KpiTemplateFragment extends Fragment
{

    public interface OnKpiCreatedListener
    {
        void onKpiCreated(KPITemplateModel kpi);
    }

    private OnKpiCreatedListener kpiCreatedListener;

    public void setOnKpiCreatedListener(OnKpiCreatedListener listener)
    {
        this.kpiCreatedListener = listener;
    }
        // Hardcoded KPI that the supervisor can select from.
    private final List<KPITemplateModel> templates = Arrays.asList
            (
            new KPITemplateModel("", "Average Transaction Value", "Average spend per transactiont", 0, "£", "Higher", "Wekkly"),
            new KPITemplateModel("", "Units per Transaction", "Average products sold per transaction", 0, "", "Higher", "Weekly"),
            new KPITemplateModel("", "Units Sold", "Total number of products sold", 0, "", "Higher", "Weekly"),
            new KPITemplateModel("", "Attachment Rate", "Percentage of sales with an add-on product", 0, "%", "Higher", "Weekly"),
            new KPITemplateModel("", "Sales Figure", "Sales made weekly or monthly", 0, "£", "Higher", "Weekly"),
            new KPITemplateModel("", "Customers Engagement", "Number of customers attended to", 0, "", "Higher", "Weekly"),
            new KPITemplateModel("", "Add-on item count", "Number of add-on items sold", 0, "", "Higher", "Monthly"),
            new KPITemplateModel("", "Default item count", "Number of default items sold", 0, "", "Higher", "Weekly"),
            new KPITemplateModel("", "KPI Score", "Number of KPI Targets achieved", 0, "", "Higher", "Weekly")

    );

    public KpiTemplateFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_kpi_template, container, false);

        ImageView btnBack = view.findViewById(R.id.btn_back);
        RecyclerView recyclerView = view.findViewById(R.id.templates_recycler);

        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        KPITemplateAdapter adapter = new KPITemplateAdapter(templates, template -> openConfigDialog(template));

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void openConfigDialog(KPITemplateModel template)
    {
        KpiConfigDialog dialog = KpiConfigDialog.newInstanceForEdit(template);

        dialog.setSaveListener(savedKpi ->
        {
            if (kpiCreatedListener != null)
            {
                kpiCreatedListener.onKpiCreated(savedKpi);
            }
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        dialog.show(getChildFragmentManager(), "KpiConfigDialog");
    }
}