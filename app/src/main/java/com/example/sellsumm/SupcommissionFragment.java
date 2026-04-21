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

public class SupcommissionFragment extends Fragment {

    private static final String TAG = "SupcommissionFragment";

    private FirebaseFirestore db;

    // Views — using your exact XML IDs
    private TextView currentRateDisplay;
    private EditText inputRate;
    private Button   btnSave;

    public SupcommissionFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_supcommission, container, false);

        db = FirebaseFirestore.getInstance();

        // ── Bind views using your exact IDs ─────────────────────
        currentRateDisplay = view.findViewById(R.id.textView5);
        inputRate          = view.findViewById(R.id.editTextNumberDecimal);
        btnSave            = view.findViewById(R.id.button2);

        // ── Load existing rate from Firestore ────────────────────
        loadCurrentRate();

        // ── Save button ──────────────────────────────────────────
        btnSave.setOnClickListener(v -> saveRate());

        return view;
    }

    // ── Read current rate from Firestore and display it ──────────
    private void loadCurrentRate() {
        db.collection("commissionSettings")
                .document("default")
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.getDouble("rate") != null) {
                        double rate = doc.getDouble("rate");
                        // Show current rate in the display TextView
                        currentRateDisplay.setText(
                                String.format("%.1f%%", rate));
                    } else {
                        // No rate set yet — show default placeholder
                        currentRateDisplay.setText("0%");
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to load rate: "
                                + e.getMessage()));
    }

    // ── Validate and save new rate to Firestore ──────────────────
    private void saveRate() {
        String input = inputRate.getText() != null ?
                inputRate.getText().toString().trim() : "";

        // Validate — must not be empty
        if (input.isEmpty()) {
            inputRate.setError("Please enter a commission rate");
            return;
        }

        double rate;
        try {
            rate = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            inputRate.setError("Invalid number");
            return;
        }

        // Validate — must be between 0 and 100
        if (rate < 0 || rate > 100) {
            inputRate.setError("Rate must be between 0 and 100");
            return;
        }

        // Build Firestore document
        Map<String, Object> data = new HashMap<>();
        data.put("rate", rate);

        db.collection("commissionSettings")
                .document("default")
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    // Update the display immediately
                    currentRateDisplay.setText(
                            String.format("%.1f%%", rate));

                    // Clear the input field
                    inputRate.setText("");

                    Toast.makeText(requireContext(),
                            "Commission rate saved",
                            Toast.LENGTH_SHORT).show();

                    Log.d(TAG, "Commission rate saved: " + rate);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "Failed to save rate",
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Save failed: " + e.getMessage());
                });
    }
}