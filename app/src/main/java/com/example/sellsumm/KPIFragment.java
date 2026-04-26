package com.example.sellsumm;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class KPIFragment extends Fragment {

    private final List<KPITemplateModel> createdKpis = new ArrayList<>();
    private CreatedKPIAdapter adapter;
    private TextView emptyStateText;
    private RecyclerView recyclerView;

    private FirebaseFirestore db;
    private String storeId; // ⭐ store-aware

    public KPIFragment() {}

    // ⭐ Correct newInstance() for KPIFragment
    public static KPIFragment newInstance(String storeId) {
        KPIFragment fragment = new KPIFragment();
        Bundle args = new Bundle();
        args.putString("storeId", storeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ⭐ Read storeId from arguments
        if (getArguments() != null) {
            storeId = getArguments().getString("storeId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_k_p_i, container, false);

        db = FirebaseFirestore.getInstance();

        emptyStateText = view.findViewById(R.id.empty_state_text);
        recyclerView   = view.findViewById(R.id.kpi_recycler);
        ImageView btnAdd = view.findViewById(R.id.btn_add_kpi);

        adapter = new CreatedKPIAdapter(
                createdKpis,
                kpi -> openEditDialog(kpi),
                kpi -> deleteKpiFromFirestore(kpi)
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> openTemplateScreen());

        loadKpisFromFirestore();

        return view;
    }

    private void openTemplateScreen() {
        KpiTemplateFragment templateFragment = new KpiTemplateFragment();

        templateFragment.setOnKpiCreatedListener(newKpi -> {
            saveKpiToFirestore(newKpi);
        });

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, templateFragment)
                .addToBackStack(null)
                .commit();
    }

    // ⭐ Save KPI under the correct store
    private void saveKpiToFirestore(KPITemplateModel kpi) {
        db.collection("stores")
                .document(storeId)
                .collection("kpis")
                .document(kpi.getId())
                .set(kpi)
                .addOnSuccessListener(unused -> {
                    createdKpis.add(kpi);
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                });
    }

    // ⭐ Load KPIs from the correct store
    private void loadKpisFromFirestore() {
        db.collection("stores")
                .document(storeId)
                .collection("kpis")
                .get()
                .addOnSuccessListener(query -> {
                    createdKpis.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        KPITemplateModel kpi = doc.toObject(KPITemplateModel.class);
                        createdKpis.add(kpi);
                    }
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                });
    }

    // ⭐ Delete KPI from the correct store
    private void deleteKpiFromFirestore(KPITemplateModel kpi) {
        db.collection("stores")
                .document(storeId)
                .collection("kpis")
                .document(kpi.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    createdKpis.remove(kpi);
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                });
    }

    private void openEditDialog(KPITemplateModel kpi) {
        KpiConfigDialog dialog = KpiConfigDialog.newInstanceForEdit(kpi);

        dialog.setSaveListener(updatedKpi -> {
            db.collection("stores")
                    .document(storeId)
                    .collection("kpis")
                    .document(updatedKpi.getId())
                    .set(updatedKpi)
                    .addOnSuccessListener(unused -> {
                        int index = createdKpis.indexOf(kpi);
                        if (index >= 0) {
                            createdKpis.set(index, updatedKpi);
                            adapter.notifyDataSetChanged();
                        }
                    });
        });

        dialog.show(getChildFragmentManager(), "EditKpiDialog");
    }

    private void updateEmptyState() {
        boolean isEmpty = createdKpis.isEmpty();
        emptyStateText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}
