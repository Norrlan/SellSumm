package com.example.sellsumm;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SupcommissionFragment extends Fragment
{

    private static final String TAG = "SupcommissionFragment";

    private FirebaseFirestore db;

    private TextView currentRateDisplay;
    private EditText inputRate;
    private Button   btnSave;

    public SupcommissionFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_supcommission, container, false);

        db = FirebaseFirestore.getInstance();

        currentRateDisplay = view.findViewById(R.id.textView5);
        inputRate          = view.findViewById(R.id.editTextNumberDecimal);
        btnSave            = view.findViewById(R.id.button2);

        loadCurrentRate();

        btnSave.setOnClickListener(v -> saveRate());

        return view;
    }

    //Read current rate from Firestore and display it
    private void loadCurrentRate() {
        db.collection("commissionSettings").document("default").get().addOnSuccessListener(doc ->
                {
                    if (doc.exists() && doc.getDouble("rate") != null)
                    {
                        double rate = doc.getDouble("rate");
                        currentRateDisplay.setText(
                                String.format("%.1f%%", rate));
                    }
                    else
                    {
                        currentRateDisplay.setText("0%");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load rate: " + e.getMessage()));
    }


    private void saveRate()
    {
        String input = inputRate.getText() != null ? inputRate.getText().toString().trim() : "";

        if (input.isEmpty())
        {
            inputRate.setError("Please enter a commission rate");
            return;
        }

        double rate;
        try
        {
            rate = Double.parseDouble(input);
        } catch (NumberFormatException e)
        {
            inputRate.setError("Invalid number");
            return;
        }


        if (rate < 0 || rate > 100)
        {
            inputRate.setError("Rate must be between 0 and 100");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("rate", rate);

        db.collection("commissionSettings").document("default").set(data).addOnSuccessListener(aVoid ->
             {
                    currentRateDisplay.setText(String.format("%.1f%%", rate));

                    inputRate.setText("");

                    Toast.makeText(requireContext(), "Commission rate saved", Toast.LENGTH_SHORT).show();

                    Log.d(TAG, "Commission rate saved: " + rate);
                })
                .addOnFailureListener(e ->
                {
                    Toast.makeText(requireContext(), "Failed to save rate", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Save failed: " + e.getMessage());
                });
    }
}