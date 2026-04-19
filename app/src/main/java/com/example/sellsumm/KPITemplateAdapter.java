package com.example.sellsumm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class KPITemplateAdapter extends RecyclerView.Adapter<KPITemplateAdapter.TemplateViewHolder>
{

    public interface OnAddClickListener
    {
        void onAdd(KPITemplateModel template);
    }

    private final List<KPITemplateModel> templates;
    private final OnAddClickListener addListener;

    public KPITemplateAdapter(List<KPITemplateModel> templates, OnAddClickListener addListener)
    {
        this.templates   = templates;
        this.addListener = addListener;
    }

    @NonNull
    @Override
    public TemplateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.kpi_template_row, parent, false);
        return new TemplateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TemplateViewHolder holder, int position)
    {
        KPITemplateModel template = templates.get(position);
        holder.kpiName.setText(template.getName());
        holder.kpiDescription.setText(template.getDescription());
        holder.addButton.setOnClickListener(v -> addListener.onAdd(template));
    }

    @Override
    public int getItemCount()
    {
        return templates != null ? templates.size() : 0;
    }

    static class TemplateViewHolder extends RecyclerView.ViewHolder
    {
        TextView kpiName, kpiDescription;
        Button addButton;

        TemplateViewHolder(View itemView)
        {
            super(itemView);
            kpiName        = itemView.findViewById(R.id.kpiName);
            kpiDescription = itemView.findViewById(R.id.kpiDescription);
            addButton      = itemView.findViewById(R.id.Add_btn);
        }
    }
}