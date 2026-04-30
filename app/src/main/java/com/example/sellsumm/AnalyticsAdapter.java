package com.example.sellsumm;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AnalyticsAdapter extends RecyclerView.Adapter<AnalyticsAdapter.ViewHolder> {

    private Context context;
    private List<AnalyticKPIModel> kpiList;

    public AnalyticsAdapter(Context context, List<AnalyticKPIModel> kpiList)
    {
        this.context = context;
        this.kpiList = kpiList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.analytic_row_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {

        AnalyticKPIModel kpi = kpiList.get(position);

        holder.title.setText(kpi.title);
        holder.value.setText(kpi.value);

        // Set progress
        holder.progress.setProgress(kpi.progress);

        // Apply the status colour tint
        holder.progress.setProgressTintList(ColorStateList.valueOf(kpi.color));
    }

    // returns the number of KPI items in the  kpi list
    @Override
    public int getItemCount()
    {
        return kpiList.size();
    }

    // viewholder for for displaying KPI cards in the Analytics screen

    public static class ViewHolder extends RecyclerView.ViewHolder
    {

        TextView title, value;
        ProgressBar progress;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);

            title = itemView.findViewById(R.id.kpi_title);
            value = itemView.findViewById(R.id.kpi_value);
            progress = itemView.findViewById(R.id.kpi_progress);
        }
    }
}
