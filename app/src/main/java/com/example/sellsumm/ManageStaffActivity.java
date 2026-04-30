package com.example.sellsumm;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
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
    private StaffAnalysisAdapter adapter;

    private String storeId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_staff);


        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });

        db = FirebaseFirestore.getInstance();

        //display the storeId from DashboardFragment
        storeId = getIntent().getStringExtra("storeId");

        if (storeId == null)
        {
            Toast.makeText(this, "Store ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Logic for the back button to take the user back to the dashboard
        ImageView backBtn = findViewById(R.id.to_dashboard);
        backBtn.setOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.manage_staff_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        TextView storeCodeValue = findViewById(R.id.store_code_value);
        storeCodeValue.setText(storeId);


        adapter = new StaffAnalysisAdapter(staffList, (uid, position) -> deleteStaff(uid, position));
        recyclerView.setAdapter(adapter);

        loadStaff();
    }

    // Method to load staff data for each specific store
    private void loadStaff() {
        db.collection("users").whereEqualTo("role", "staff").whereEqualTo("storeId", storeId).get().addOnSuccessListener(querySnapshot ->
                {
                    staffList.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot)
                    {

                        String uid = doc.getId();
                        String name = doc.getString("fullName");
                        String email = doc.getString("email");

                        StaffModel staff = new StaffModel(uid, name, email, "staff");
                        staffList.add(staff);
                    }

                    adapter.notifyDataSetChanged();

                    if (staffList.isEmpty())
                    {
                        Toast.makeText(this, "No staff found for this store", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                {
                    Log.e(TAG, "Failed to load staff: " + e.getMessage());
                    Toast.makeText(this, "Failed to load staff", Toast.LENGTH_SHORT).show();
                });
    }

    // Method to delete staff data from all collections
    private void deleteStaff(String uid, int position)
    {

        if (uid == null || uid.isEmpty())
        {
            Toast.makeText(this, "Invalid staff ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // delete from the users collection
        db.collection("users").document(uid).delete();

        // delete from the stores collection and staff sub-collection
        db.collection("stores").document(storeId).collection("staff").document(uid).delete();

        // Delete staff performance
        db.collection("stores").document(storeId).collection("staffPerformance").document(uid).delete();

        staffList.remove(position);
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, staffList.size());
        // display in the UI
        Toast.makeText(this, "Staff removed", Toast.LENGTH_SHORT).show();
    }
}
