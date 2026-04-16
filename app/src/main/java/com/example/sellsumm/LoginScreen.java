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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_screen);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Bind XML fields
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.PwdField);
        registerLink = findViewById(R.id.Registerlink);
        forgottenPwd = findViewById(R.id.forgotten_Pwd);
        loginButton = findViewById(R.id.button3);

        // Create Account link
        registerLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginScreen.this, RegisterScreen.class));
        });

        // Forgotten password link
        //forgottenPwd.setOnClickListener(v -> {
           // startActivity(new Intent(LoginScreen.this, ForgottenPassword.class));
       // });

        // Login button
        loginButton.setOnClickListener(v -> clickedloginbtn());

        // Auto-login if already authenticated + verified
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            fetchUserRoleAndNavigate(currentUser.getUid());
        }
    }


    private boolean validateInfo(EditText emailField, EditText passwordField) {

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


    public void login(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(authTask -> {

                    if (!authTask.isSuccessful()) {
                        Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseUser user = mAuth.getCurrentUser();

                    if (user == null) {
                        Toast.makeText(this, "Unexpected error", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!user.isEmailVerified()) {
                        mAuth.signOut();
                        Toast.makeText(this, "Verify your email first", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Email verified → fetch role
                    fetchUserRoleAndNavigate(user.getUid());
                });
    }


    private void fetchUserRoleAndNavigate(String uid) {

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        Toast.makeText(this, "User profile missing", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String role = doc.getString("role");

                    if (role == null) {
                        Toast.makeText(this, "Role not assigned", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (role.equals("supervisor")) {
                        startActivity(new Intent(this, SupervisorMainActivity.class));
                    } else {
                        startActivity(new Intent(this, StaffMainActivity.class));
                    }

                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error fetching role", Toast.LENGTH_SHORT).show()
                );
    }


    public void clickedloginbtn() {

        if (!validateInfo(emailField, passwordField)) {
            return;
        }

        login(
                emailField.getText().toString().trim(),
                passwordField.getText().toString().trim()
        );
    }
}
