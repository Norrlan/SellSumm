package com.example.sellsumm;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class StaffAnalysisFragment extends Fragment
{

    RecyclerView recyclerView;
    StaffKPIAdapter adapter;
    List<StaffKPIModel> kpiList = new ArrayList<>();

    FirebaseFirestore db;
    String staffId;

    Button filterWeekly, filterMonthly, filterAll;
    List<StaffKPIModel> fullList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_staff_analysis, container, false);

        db = FirebaseFirestore.getInstance();
        staffId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        recyclerView = view.findViewById(R.id.staffKpiRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new StaffKPIAdapter(getContext(), kpiList);
        recyclerView.setAdapter(adapter);

        loadSupervisorKpis();
        adapter.notifyDataSetChanged();
        calculateKpiScore();

        filterWeekly = view.findViewById(R.id.filterWeekly);
        filterMonthly = view.findViewById(R.id.filterMonthly);
        filterAll = view.findViewById(R.id.filterAll);

        filterWeekly.setOnClickListener(v -> applyFilter("Weekly"));
        filterMonthly.setOnClickListener(v -> applyFilter("Monthly"));
        filterAll.setOnClickListener(v -> applyFilter("All"));



        return view;
    }

    private void loadSupervisorKpis()
    {
        db.collection("kpis").get().addOnSuccessListener(query ->
        {
                    fullList.clear();
                    kpiList.clear();

                  for (DocumentSnapshot doc : query.getDocuments())
                    {
                        KPITemplateModel template = doc.toObject(KPITemplateModel.class);
                        loadStaffActualValue(template);
                    }
                });
    }

    private void loadStaffActualValue(KPITemplateModel template) {
        db.collection("staffPerformance").document(staffId).collection("kpis").document(template.getId()).get().addOnSuccessListener(doc ->
        {
                double actual = doc.exists() && doc.contains("actualValue") ? doc.getDouble("actualValue") : 0;

                    StaffKPIModel model = new StaffKPIModel(
                            template.getName(),
                            template.getDescription(),
                            template.getTargetValue(),
                            actual,
                            calculateStatus(template.getTargetValue(), actual),
                            template.getFrequency()
                    );

                    fullList.add(model);
                    kpiList.add(model);

                    adapter.notifyDataSetChanged();
                    calculateKpiScore();
                });
    }

    private String calculateStatus(double target, double actual)
    {
        if (actual >= target) return "On Track";
        if (actual >= target * 0.6) return "Keep Pushing";
        return "Off Track";
    }

    private void calculateKpiScore()
    {
        int totalKpis = kpiList.size();
        int achieved = 0;

        for (StaffKPIModel kpi : kpiList)
        {
            if (kpi.getActualValue() >= kpi.getTargetValue())
            {
                achieved++;
            }
        }

        double ratio = (double) achieved / totalKpis;

        String scoreStatus;
        if (ratio >= 1.0)
        {
            scoreStatus = "On Track";
        }
        else if (ratio >= 0.6)
        {
            scoreStatus = "Keep Pushing";
        }
        else
        {
            scoreStatus = "Off Track";
        }

        // Display kpi score at the top of the Analytics screen
        Log.d("KPI_SCORE", "Score: " + achieved + "/" + totalKpis + " → " + scoreStatus);
    }

    private void applyFilter(String type)
    {
        kpiList.clear();

        if (type.equals("All"))
        {
            kpiList.addAll(fullList);
        } else
        {
            for (StaffKPIModel kpi : fullList)
            {
                if (kpi.getFrequency().equalsIgnoreCase(type))
                {
                    kpiList.add(kpi);
                }
            }
        }

        adapter.notifyDataSetChanged();
        calculateKpiScore();
    }


}
