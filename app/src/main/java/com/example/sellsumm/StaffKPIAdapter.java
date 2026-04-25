package com.example.sellsumm;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StaffKPIAdapter extends RecyclerView.Adapter<StaffKPIAdapter.KPIViewHolder>
{

    private Context context;
    private List<StaffKPIModel> kpiList;

    public StaffKPIAdapter(Context context, List<StaffKPIModel> kpiList)
    {
        this.context = context;
        this.kpiList = kpiList;
    }

    @NonNull
    @Override
    public KPIViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.staff_kpi_row, parent, false);
        return new KPIViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KPIViewHolder holder, int position)
    {
        StaffKPIModel kpi = kpiList.get(position);

        holder.kpiName.setText(kpi.getName());
        holder.kpiDescription.setText(kpi.getDescription());
        holder.kpiTarget.setText("Target: " + kpi.getTargetValue());
        holder.kpiActual.setText("Actual: " + kpi.getActualValue());
        holder.kpiStatus.setText(kpi.getStatus());

        switch (kpi.getStatus())
        {
            case "On Track":
                holder.kpiStatus.setTextColor(Color.parseColor("#4CAF50")); // green
                break;

            case "Keep Pushing":
                holder.kpiStatus.setTextColor(Color.parseColor("#FFA500")); // orange
                break;

            case "Off Track":
                holder.kpiStatus.setTextColor(Color.parseColor("#FF0000")); // red
                break;
        }
    }


    @Override
    public int getItemCount()
    {
        return kpiList.size();
    }

    public static class KPIViewHolder extends RecyclerView.ViewHolder
    {

        TextView kpiName, kpiDescription, kpiTarget, kpiActual, kpiStatus;

        public KPIViewHolder(@NonNull View itemView)
        {
            super(itemView);

            kpiName = itemView.findViewById(R.id.kpiName);
            kpiDescription = itemView.findViewById(R.id.kpiDescription);
            kpiTarget = itemView.findViewById(R.id.kpiTarget);
            kpiActual = itemView.findViewById(R.id.kpiActual);
            kpiStatus = itemView.findViewById(R.id.kpiStatus);
        }
    }
}
