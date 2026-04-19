package com.example.sellsumm;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.UUID;

public class KpiConfigDialog extends DialogFragment
{

    private KPITemplateModel existingKpi;
    private KpiSaveListener listener;

    public interface KpiSaveListener {
        void onKpiSaved(KPITemplateModel kpi);
    }

    public static KpiConfigDialog newInstanceForAdd()
    {
        return new KpiConfigDialog();
    }

    public static KpiConfigDialog newInstanceForEdit(KPITemplateModel kpi)
    {
        KpiConfigDialog dialog = new KpiConfigDialog();
        Bundle args = new Bundle();
        args.putString("kpi_id",          kpi.getId());
        args.putString("kpi_name",        kpi.getName());
        args.putString("kpi_description", kpi.getDescription());
        args.putDouble("kpi_target",      kpi.getTargetValue());
        args.putString("kpi_direction",   kpi.getDirection());
        args.putString("kpi_frequency",   kpi.getFrequency());
        dialog.setArguments(args);
        return dialog;
    }

    public void setSaveListener(KpiSaveListener listener)
    {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog);

        if (getArguments() != null)
        {
            existingKpi = new KPITemplateModel(
                    getArguments().getString("kpi_id"),
                    getArguments().getString("kpi_name"),
                    getArguments().getString("kpi_description"),
                    getArguments().getDouble("kpi_target"),
                    getArguments().getString("kpi_direction"),
                    getArguments().getString("kpi_frequency")
            );
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(
                R.layout.kpi_dialog_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        TextView   tvTitle        = view.findViewById(R.id.kpiSettingsTitle);
        ImageView  btnClose       = view.findViewById(R.id.closeDialogBtn);
        Button     btnSave        = view.findViewById(R.id.saveKpiButton);
        EditText   inputName      = view.findViewById(R.id.kpiNameInput);
        EditText   inputDesc      = view.findViewById(R.id.kpiDescriptionInput);
        EditText   inputTarget    = view.findViewById(R.id.kpiTargetInput);
        EditText   inputEvent     = view.findViewById(R.id.kpiEventInput);
        RadioGroup radioPeriod    = view.findViewById(R.id.kpiPeriodGroup);
        RadioGroup radioDirection = view.findViewById(R.id.kpiDirectionGroup);

        // Pre-fill if editing
        if (existingKpi != null)
        {
            tvTitle.setText("Edit KPI");
            inputName.setText(existingKpi.getName());
            inputDesc.setText(existingKpi.getDescription());

            if (existingKpi.getTargetValue() > 0)
            {
                inputTarget.setText(String.valueOf(existingKpi.getTargetValue()));
            }

            // Logic to switch between the rime periods.Set daily as default
            switch (existingKpi.getFrequency() != null ? existingKpi.getFrequency() : "")
            {
                case "Weekly":    radioPeriod.check(R.id.periodWeekly);    break;
                case "Monthly":   radioPeriod.check(R.id.periodMonthly);   break;
                default:          radioPeriod.check(R.id.periodDaily);     break;
            }

            // Pre-select direction
            if ("Higher".equals(existingKpi.getDirection()))
            {
                radioDirection.check(R.id.directionHigher);
            }
            else
            {
                radioDirection.check(R.id.directionLower);
            }

        }
        else
        {
            tvTitle.setText("KPI Settings");
            radioPeriod.check(R.id.periodDaily);
            radioDirection.check(R.id.directionHigher);
        }

        btnClose.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v ->
        {
            String name = inputName.getText() != null ? inputName.getText().toString().trim() : "";
            String desc = inputDesc.getText() != null ? inputDesc.getText().toString().trim() : "";
            String targetStr = inputTarget.getText() != null ? inputTarget.getText().toString().trim() : "0";

           //Validation
            if (name.isEmpty())
            {
                inputName.setError("KPI name is required");
                return;
            }

            // Get selected period
            int periodId = radioPeriod.getCheckedRadioButtonId();
            String frequency = "Daily";
            if (periodId == R.id.periodWeekly)         frequency = "Weekly";
            else if (periodId == R.id.periodMonthly)   frequency = "Monthly";
            else if (periodId == R.id.periodQuarterly) frequency = "Quarterly";

            // Get selected direction
            int dirId = radioDirection.getCheckedRadioButtonId();
            String direction = dirId == R.id.directionLower ?
                    "Lower" : "Higher";

            double targetValue = 0;
            try
            {
                targetValue = Double.parseDouble(targetStr);
            }

            catch (NumberFormatException e)
            {
                targetValue = 0;
            }

            String id = (existingKpi != null && existingKpi.getId() != null && !existingKpi.getId().isEmpty()) ? existingKpi.getId() : UUID.randomUUID().toString();

            KPITemplateModel saved = new KPITemplateModel(id, name, desc, targetValue, direction, frequency);

            if (listener != null) listener.onKpiSaved(saved);
            dismiss();
        });
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null)
        {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }
}