package com.example.sellsumm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

    private EditText emailField, fullNameField, passwordField;
    private Spinner roleSpinner;
    private Button signUpBtn;
    private TextView loginLink;

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

        // Bind XML fields
        emailField = findViewById(R.id.emailField);
        fullNameField = findViewById(R.id.fullnameField);
        passwordField = findViewById(R.id.PwdField);
        roleSpinner = findViewById(R.id.spinner);
        signUpBtn = findViewById(R.id.sign_Up);
        loginLink = findViewById(R.id.loginlink);

        Spinner spinner = findViewById(R.id.spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_text,
                new String[]{"staff", "supervisor"}
        );

        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spinner.setAdapter(adapter);


        // Register button click
        signUpBtn.setOnClickListener(v -> attemptRegistration());

        // Login hyperlink
        loginLink.setOnClickListener(v ->
                startActivity(new Intent(RegisterScreen.this, LoginScreen.class))
        );
    }

    private void attemptRegistration() {
        String email = emailField.getText().toString().trim();
        String fullName = fullNameField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String role = roleSpinner.getSelectedItem().toString();

        if (email.isEmpty() || fullName.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            passwordField.setError("Password must be at least 6 characters");
            return;
        }

        registerUser(email, password, fullName, role);
    }

    private void registerUser(String email, String password, String fullName, String role) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) {
                        Toast.makeText(this, "Unexpected error", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String uid = user.getUid();

                    Map<String, Object> profile = new HashMap<>();
                    profile.put("email", email);
                    profile.put("fullName", fullName);
                    profile.put("role", role);
                    profile.put("uid", uid);

                    db.collection("users").document(uid)
                            .set(profile)
                            .addOnSuccessListener(aVoid -> {
                                user.sendEmailVerification();
                                Toast.makeText(this, "Account created. Verify your email.", Toast.LENGTH_LONG).show();

                                mAuth.signOut();
                                startActivity(new Intent(this, LoginScreen.class));
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error saving profile: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
