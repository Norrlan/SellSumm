package com.example.sellsumm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Spinner;
import android.widget.ArrayAdapter;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText emailField, passwordField;
    private TextView registerLink, forgottenPwd;
    private View loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_screen);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // IMPORTANT: force logout so login screen is always fresh
        mAuth.signOut();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.PwdField);
        registerLink = findViewById(R.id.Registerlink);
        forgottenPwd = findViewById(R.id.forgotten_Pwd);
        loginButton = findViewById(R.id.button3);

        registerLink.setOnClickListener(v ->
                startActivity(new Intent(LoginScreen.this, RegisterScreen.class))
        );

        forgottenPwd.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email first", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(a -> Toast.makeText(this, "Reset link sent", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        loginButton.setOnClickListener(v -> clickedloginbtn());
    }

    private boolean validateInfo(EditText emailField, EditText passwordField)
    {

        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (email.isEmpty()) {
            emailField.setError("Email is required");
            return false;
        }

        if (email.contains(" ")) {
            emailField.setError("Email cannot contain spaces");
            return false;
        }

        if (!email.equals(email.toLowerCase())) {
            emailField.setError("Email must be lowercase");
            return false;
        }

        if (!email.contains("@")) {
            emailField.setError("Email must contain '@'");
            return false;
        }

        if (password.isEmpty()) {
            passwordField.setError("Password is required");
            return false;
        }

        if (password.contains(" ")) {
            passwordField.setError("Password cannot contain spaces");
            return false;
        }

        if (password.length() < 8 || password.length() > 16) {
            passwordField.setError("Password must be 8–16 characters");
            return false;
        }

        if (!password.matches(".*[A-Z].*")) {
            passwordField.setError("Password must contain an uppercase letter");
            return false;
        }

        if (!password.matches(".*[!@#$%^&*()_+=<>?/{}~|].*")) {
            passwordField.setError("Password must contain a special character");
            return false;
        }

        return true;
    }


    public void login(String email, String password)
    {

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(authTask ->
        {

                    if (!authTask.isSuccessful())
                    {
                        Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseUser user = mAuth.getCurrentUser();

                    if (user == null)
                    {
                        Toast.makeText(this, "Unexpected error", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //  fetch role
                    fetchUserRoleAndNavigate(user.getUid());
                });
    }


    private void fetchUserRoleAndNavigate(String uid)
    {

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc ->
                {

                    if (!doc.exists()) {
                        Toast.makeText(this, "User profile missing", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String role = doc.getString("role");
                    String storeId = doc.getString("storeId");

                    if (role == null) {
                        Toast.makeText(this, "Role not assigned", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (role.equals("supervisor")) {
                        Intent intent = new Intent(this, SupervisorMainActivity.class);
                        intent.putExtra("storeId", storeId);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(this, StaffMainActivity.class);
                        intent.putExtra("storeId", storeId);
                        startActivity(intent);
                    }

                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error fetching role", Toast.LENGTH_SHORT).show()
                );
    }


    public void clickedloginbtn()
    {

        if (!validateInfo(emailField, passwordField)) {

            return;
        }

        login(
                emailField.getText().toString().trim(),
                passwordField.getText().toString().trim()
        );
    }
}
