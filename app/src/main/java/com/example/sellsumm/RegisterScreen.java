package com.example.sellsumm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText emailField, fullNameField, passwordField, storeNameField;
    private Spinner roleSpinner;
    private Button signUpBtn;
    private TextView loginLink, storeNameLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_screen);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emailField     = findViewById(R.id.emailField);
        fullNameField  = findViewById(R.id.fullnameField);
        passwordField  = findViewById(R.id.PwdField);
        storeNameField = findViewById(R.id.storeNameField);
        storeNameLabel = findViewById(R.id.storeNameLabel);

        roleSpinner = findViewById(R.id.spinner);
        signUpBtn   = findViewById(R.id.sign_Up);
        loginLink   = findViewById(R.id.loginlink);

        // Role spinner: supervisor / staff
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_text,
                new String[]{"supervisor", "staff"}
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        roleSpinner.setAdapter(adapter);

        // Change label + hint based on role
        roleSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                String role = roleSpinner.getSelectedItem().toString();
                if (role.equals("supervisor")) {
                    storeNameLabel.setText("STORE NAME");
                    storeNameField.setHint("Enter the name of the store");
                } else {
                    storeNameLabel.setText("STORE CODE");
                    storeNameField.setHint("Enter the store code");
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        signUpBtn.setOnClickListener(v -> attemptRegistration());

        loginLink.setOnClickListener(v ->
                startActivity(new Intent(RegisterScreen.this, LoginScreen.class))
        );
    }

    private void attemptRegistration() {
        String email      = emailField.getText().toString().trim();
        String fullName   = fullNameField.getText().toString().trim();
        String password   = passwordField.getText().toString().trim();
        String storeInput = storeNameField.getText().toString().trim();
        String role       = roleSpinner.getSelectedItem().toString();

        if (email.isEmpty() || fullName.isEmpty() || password.isEmpty() || storeInput.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            passwordField.setError("Password must be at least 6 characters");
            return;
        }

        if (role.equals("supervisor")) {
            registerSupervisor(email, password, fullName, storeInput);
        } else {
            registerStaff(email, password, fullName, storeInput);
        }
    }

    // SUPERVISOR REGISTRATION: creates a new store + storeId (store code)
    private void registerSupervisor(String email, String password, String fullName, String storeName) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) {
                        Toast.makeText(this, "Unexpected error", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String uid = user.getUid();

                    // Generate storeId (store code)
                    String storeId = db.collection("stores").document().getId();

                    // Create store document
                    Map<String, Object> storeData = new HashMap<>();
                    storeData.put("storeName", storeName);
                    storeData.put("supervisorId", uid);

                    db.collection("stores").document(storeId).set(storeData)
                            .addOnSuccessListener(aVoid -> {
                                // Create user profile
                                Map<String, Object> profile = new HashMap<>();
                                profile.put("email", email);
                                profile.put("fullName", fullName);
                                profile.put("role", "supervisor");
                                profile.put("storeId", storeId);
                                profile.put("storeName", storeName);

                                db.collection("users").document(uid).set(profile)
                                        .addOnSuccessListener(v -> {
                                            Toast.makeText(this, "Store created! Logging in...", Toast.LENGTH_SHORT).show();
                                            loginUser(email, password);
                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(this, "Error saving profile: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                        );
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error creating store: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // STAFF REGISTRATION: joins existing store using store code
    private void registerStaff(String email, String password, String fullName, String storeCode) {
        // Validate store code
        db.collection("stores").document(storeCode).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Invalid store code", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener(authResult -> {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user == null) {
                                    Toast.makeText(this, "Unexpected error", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                String uid = user.getUid();

                                // Create staff profile
                                Map<String, Object> profile = new HashMap<>();
                                profile.put("email", email);
                                profile.put("fullName", fullName);
                                profile.put("role", "staff");
                                profile.put("storeId", storeCode);

                                db.collection("users").document(uid).set(profile)
                                        .addOnSuccessListener(v -> {
                                            // Add staff under store/staff
                                            db.collection("stores")
                                                    .document(storeCode)
                                                    .collection("staff")
                                                    .document(uid)
                                                    .set(new HashMap<>());

                                            Toast.makeText(this, "Account created! Logging in...", Toast.LENGTH_SHORT).show();
                                            loginUser(email, password);
                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(this, "Error saving profile: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                        );
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error validating store code: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) {
                        Toast.makeText(this, "Unexpected error", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String uid = user.getUid();

                    db.collection("users").document(uid).get()
                            .addOnSuccessListener(doc -> {
                                String role = doc.getString("role");

                                if (role == null) {
                                    Toast.makeText(this, "Role missing", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (role.equals("supervisor"))
                                {

                                    String storeId = doc.getString("storeId");

                                    Intent intent = new Intent(this, SupervisorMainActivity.class);
                                    intent.putExtra("storeId", storeId);
                                    startActivity(intent);

                                }
                                else
                                {

                                    String storeId = doc.getString("storeId");

                                    Intent intent = new Intent(this, StaffMainActivity.class);
                                    intent.putExtra("storeId", storeId);
                                    startActivity(intent);
                                }



                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error fetching profile: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Auto-login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
