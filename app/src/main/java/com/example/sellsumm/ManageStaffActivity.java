package com.example.sellsumm;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageStaffActivity extends AppCompatActivity
{

    private static final String TAG = "ManageStaffActivity";

    private FirebaseFirestore db;
    private List<StaffModel> staffList = new ArrayList<>();
    private StaffAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_staff);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) ->
                {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });

        db = FirebaseFirestore.getInstance();

        // RecyclerView setup
        RecyclerView recyclerView = findViewById(R.id.manage_staff_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new StaffAdapter(staffList, (uid, position) -> deleteStaff(uid, position));

        recyclerView.setAdapter(adapter);

        // Load staff from Firestore
        loadStaff();
    }

    // Fetch all users where role == "staff"
    private void loadStaff() {
        db.collection("users").whereEqualTo("role", "staff").get().addOnSuccessListener(querySnapshot ->
                {
                    staffList.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot)
                    {
                        StaffModel staff = new StaffModel(doc.getString("uid"), doc.getString("fullName"), doc.getString("email"), doc.getString("role"));
                        staffList.add(staff);
                    }

                    adapter.notifyDataSetChanged();

                    if (staffList.isEmpty())
                    {
                        Toast.makeText(this, "No staff accounts found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                {
                    Log.e(TAG, "Failed to load staff: " + e.getMessage());
                    Toast.makeText(this, "Failed to load staff", Toast.LENGTH_SHORT).show();
                });
    }

    // Delete staff from Firestore
    private void deleteStaff(String uid, int position)
    {
        if (uid == null || uid.isEmpty())
        {
            Toast.makeText(this, "Cannot delete: invalid user", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(uid).delete().addOnSuccessListener(aVoid ->
                {
                    staffList.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, staffList.size());
                    Toast.makeText(this, "Staff account removed", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                {
                    Log.e(TAG, "Delete failed: " + e.getMessage());
                    Toast.makeText(this, "Failed to delete staff", Toast.LENGTH_SHORT).show();
                });
    }
}