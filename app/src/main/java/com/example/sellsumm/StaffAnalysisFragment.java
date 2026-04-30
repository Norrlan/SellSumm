package com.example.sellsumm;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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

public class StaffAnalysisFragment extends Fragment {

    private static final String TAG = "StaffAnalysisFragment";

    private String storeId;
    private String staffId;

    private RecyclerView recyclerView;
    private StaffKPIAdapter adapter;
    private List<StaffKPIModel> kpiList = new ArrayList<>();
    private List<StaffKPIModel> fullList = new ArrayList<>();

    private FirebaseFirestore db;

    private Button filterWeekly, filterMonthly, filterAll;
    private TextView kpiScoreValue;

    public StaffAnalysisFragment() {}

    public static StaffAnalysisFragment newInstance(String storeId)
    {
        StaffAnalysisFragment fragment = new StaffAnalysisFragment();
        Bundle args = new Bundle();
        args.putString("storeId", storeId);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
        {
            storeId = getArguments().getString("storeId");
        }

        staffId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {

        View view = inflater.inflate(R.layout.fragment_staff_analysis, container, false);

        recyclerView = view.findViewById(R.id.staffKpiRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new StaffKPIAdapter(getContext(), kpiList);
        recyclerView.setAdapter(adapter);

        filterWeekly = view.findViewById(R.id.filterWeekly);
        filterMonthly = view.findViewById(R.id.filterMonthly);
        filterAll = view.findViewById(R.id.filterAll);
        kpiScoreValue = view.findViewById(R.id.kpiScoreValue);

        filterWeekly.setOnClickListener(v -> applyFilter("Weekly"));
        filterMonthly.setOnClickListener(v -> applyFilter("Monthly"));
        filterAll.setOnClickListener(v -> applyFilter("All"));

        loadSupervisorKpis();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSupervisorKpis();
    }

    // ⭐ Load KPIs created by THIS store's supervisor
    private void loadSupervisorKpis() {
        db.collection("stores")
                .document(storeId)
                .collection("kpis")
                .get()
                .addOnSuccessListener(query -> {
                    fullList.clear();
                    kpiList.clear();
                    adapter.notifyDataSetChanged();
                    calculateKpiScore();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        KPITemplateModel template = doc.toObject(KPITemplateModel.class);
                        if (template != null) {
                            template.setId(doc.getId());
                            loadStaffActualValue(template);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to load KPIs: " + e.getMessage()));
    }

    // ⭐ Load staff's actual KPI performance
    private void loadStaffActualValue(KPITemplateModel template) {

        db.collection("stores")
                .document(storeId)
                .collection("staffPerformance")
                .document(staffId)
                .collection("kpis")
                .document(template.getId())
                .get()
                .addOnSuccessListener(doc -> {

                    double actual = doc.exists() && doc.contains("actualValue")
                            ? doc.getDouble("actualValue")
                            : 0;

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
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to load staff KPI: " + e.getMessage()));
    }

    private String calculateStatus(double target, double actual)
    {
        if (actual >= target) return "On Track";
        if (actual >= target * 0.6) return "Keep Pushing";
        return "Off Track";
    }

    private void calculateKpiScore() {
        int totalKpis = 0;
        int achieved = 0;
        StaffKPIModel scoreKpi = null;

        for (StaffKPIModel kpi : kpiList)
        {
            if ("KPI Score".equalsIgnoreCase(kpi.getName())) {
                scoreKpi = kpi;
                continue;
            }

            totalKpis++;

            if (kpi.getActualValue() >= kpi.getTargetValue())
            {
                achieved++;
            }
        }

        double ratio = totalKpis > 0 ? (double) achieved / totalKpis : 0;
        kpiScoreValue.setText("Score: " + achieved + "/" + totalKpis);

        if (scoreKpi != null) {
            scoreKpi.setActualValue(achieved);
            scoreKpi.setStatus(calculateStatus(scoreKpi.getTargetValue(), achieved));
        }

        adapter.notifyDataSetChanged();
        Log.d("KPI_SCORE", "Score: " + achieved + "/" + totalKpis);
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
