package com.example.campuseventmanagement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private EditText nameInput, emailInput, studentIdInput, departmentInput, phoneInput;
    private TextView badgeNameText, badgeInfoText;
    private Button goToHomeBtn, saveButton, logoutButton;

    private SharedPreferences preferences, accountPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        preferences = getSharedPreferences("EventNestProfile", MODE_PRIVATE);
        accountPreferences = getSharedPreferences("EventNestAccount", MODE_PRIVATE);

        goToHomeBtn = findViewById(R.id.goToHomeBtn);
        badgeNameText = findViewById(R.id.badgeNameText);
        badgeInfoText = findViewById(R.id.badgeInfoText);

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        studentIdInput = findViewById(R.id.studentIdInput);
        departmentInput = findViewById(R.id.departmentInput);
        phoneInput = findViewById(R.id.phoneInput);

        saveButton = findViewById(R.id.saveButton);
        logoutButton = findViewById(R.id.logoutButton);

        if (goToHomeBtn != null) {
            goToHomeBtn.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
            });
        }

        // Pre-fill fields
        String accountName = accountPreferences.getString("name", "");
        String accountEmail = accountPreferences.getString("email", "");

        if (nameInput != null) nameInput.setText(preferences.getString("name", accountName));
        if (emailInput != null) emailInput.setText(preferences.getString("email", accountEmail));
        if (studentIdInput != null) studentIdInput.setText(preferences.getString("studentId", ""));
        if (departmentInput != null) departmentInput.setText(preferences.getString("department", ""));
        if (phoneInput != null) phoneInput.setText(preferences.getString("phone", ""));

        updateBadgeCard();

        if (saveButton != null) {
            saveButton.setOnClickListener(v -> {
                String name = nameInput != null ? nameInput.getText().toString().trim() : "";
                String email = emailInput != null ? emailInput.getText().toString().trim() : "";
                String studentId = studentIdInput != null ? studentIdInput.getText().toString().trim() : "";
                String department = departmentInput != null ? departmentInput.getText().toString().trim() : "";
                String phone = phoneInput != null ? phoneInput.getText().toString().trim() : "";

                preferences.edit()
                        .putString("name", name)
                        .putString("email", email)
                        .putString("studentId", studentId)
                        .putString("department", department)
                        .putString("phone", phone)
                        .apply();

                Toast.makeText(ProfileActivity.this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
                updateBadgeCard();
            });
        }

        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> {
                accountPreferences.edit().putBoolean("loggedIn", false).apply();
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void updateBadgeCard() {
        String name = preferences.getString("name", accountPreferences.getString("name", "Student Name"));
        String email = preferences.getString("email", accountPreferences.getString("email", "student@university.edu"));
        String studentId = preferences.getString("studentId", "");
        String department = preferences.getString("department", "");

        if (badgeNameText != null) {
            badgeNameText.setText(name.isEmpty() ? "Student Name" : name);
        }

        if (badgeInfoText != null) {
            StringBuilder info = new StringBuilder();
            info.append("📧 ").append(email.isEmpty() ? "student@university.edu" : email);
            if (!studentId.isEmpty()) {
                info.append("  •  🎓 ID: ").append(studentId);
            }
            if (!department.isEmpty()) {
                info.append("\n🏫 Dept: ").append(department);
            }
            badgeInfoText.setText(info.toString());
        }
    }
}