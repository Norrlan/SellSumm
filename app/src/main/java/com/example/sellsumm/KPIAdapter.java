package com.example.sellsumm;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class KPIAdapter extends RecyclerView.Adapter<KPIAdapter.KPIViewHolder>
{

    private List<KPIModel> kpiList;

    public KPIAdapter(List<KPIModel> kpiList) {
        this.kpiList = kpiList;
    }


    @NonNull
    @Override
    public KPIViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull KPIViewHolder holder, int position) {
        KPIModel kpi = kpiList.get(position);

        holder.kpiName.setText(kpi.name);
        holder.kpiActual.setText("Actual: " + kpi.actual);
        holder.kpiTarget.setText("Target: " + kpi.target);

        int progress = (int) ((kpi.actual / kpi.target) * 100);
        holder.kpiProgress.setProgress(progress);

        if (kpi.actual >= kpi.target) {
            holder.kpiStatus.setText("On Track");
            holder.kpiStatus.setTextColor(Color.GREEN);
        } else {
            holder.kpiStatus.setText("Keep Pushing");
            holder.kpiStatus.setTextColor(Color.parseColor("#FFA500"));
        }
    }

    @Override
    public int getItemCount() {
        return kpiList.size();
    }

    static class KPIViewHolder extends RecyclerView.ViewHolder {
        TextView kpiName, kpiActual, kpiTarget, kpiStatus;
        ProgressBar kpiProgress;

        public KPIViewHolder(@NonNull View itemView) {
            super(itemView);
            kpiName = itemView.findViewById(R.id.kpiName);
            kpiActual = itemView.findViewById(R.id.kpiActual);
            kpiTarget = itemView.findViewById(R.id.kpiTarget);
            kpiStatus = itemView.findViewById(R.id.kpiStatus);
            kpiProgress = itemView.findViewById(R.id.kpiProgress);
        }
    }
}

