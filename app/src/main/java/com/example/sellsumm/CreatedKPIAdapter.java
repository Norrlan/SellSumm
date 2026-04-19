package com.example.sellsumm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CreatedKPIAdapter extends RecyclerView.Adapter<CreatedKPIAdapter.ViewHolder>
{

    public interface OnKPIClickListener
    {
        void onClick(KPITemplateModel kpi);
    }

    public interface OnKPIDeleteListener
    {
        void onDelete(KPITemplateModel kpi);
    }

    private final List<KPITemplateModel> kpis;
    private final OnKPIClickListener  clickListener;
    private final OnKPIDeleteListener deleteListener;

    public CreatedKPIAdapter(List<KPITemplateModel> kpis, OnKPIClickListener clickListener, OnKPIDeleteListener deleteListener)
    {
        this.kpis           = kpis;
        this.clickListener  = clickListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.created_kpi_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        KPITemplateModel kpi = kpis.get(position);

        holder.kpiName.setText(kpi.getName());
        holder.kpiDescription.setText(kpi.getDirection() + " is better · " + kpi.getFrequency());

        holder.itemView.setOnClickListener(v ->
        {
            if (clickListener != null) clickListener.onClick(kpi);
        });

        holder.deleteButton.setOnClickListener(v ->
        {
            if (deleteListener != null) deleteListener.onDelete(kpi);
        });
    }

    @Override
    public int getItemCount()
    {
        return kpis != null ? kpis.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView  kpiName, kpiDescription;
        ImageView deleteButton;

        ViewHolder(View itemView)
        {
            super(itemView);
            kpiName        = itemView.findViewById(R.id.kpiName);
            kpiDescription = itemView.findViewById(R.id.kpiDescription);
            deleteButton   = itemView.findViewById(R.id.kpiDeleteButton);
        }
    }
}