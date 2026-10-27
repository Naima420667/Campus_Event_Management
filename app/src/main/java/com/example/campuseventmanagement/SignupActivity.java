package com.example.campuseventmanagement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    private EditText nameInput, emailInput, passwordInput, confirmPasswordInput;
    private Spinner roleSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(28, 35, 28, 25);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        layout.setBackgroundColor(Color.parseColor("#F0F7FF"));

        TextView title = new TextView(this);
        title.setText("Create EventNest Account");
        title.setTextSize(25);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.parseColor("#172554"));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 18);
        layout.addView(title);

        nameInput = addField(layout, "Full Name", false);
        emailInput = addField(layout, "Email Address", false);
        passwordInput = addField(layout, "Password", true);
        confirmPasswordInput = addField(layout, "Confirm Password", true);

        TextView roleLabel = new TextView(this);
        roleLabel.setText("Account Type / Role:");
        roleLabel.setTextColor(Color.parseColor("#172554"));
        roleLabel.setTextSize(14);
        roleLabel.setTypeface(null, Typeface.BOLD);
        roleLabel.setPadding(0, 8, 0, 4);
        layout.addView(roleLabel);

        roleSpinner = new Spinner(this);
        String[] roles = {"User / Student", "Admin"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        GradientDrawable spinnerBg = new GradientDrawable();
        spinnerBg.setColor(Color.WHITE);
        spinnerBg.setCornerRadius(12);
        spinnerBg.setStroke(2, Color.parseColor("#CBD5E1"));
        roleSpinner.setBackground(spinnerBg);
        roleSpinner.setPadding(16, 16, 16, 16);

        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(48));
        spinnerParams.setMargins(0, 0, 0, 16);
        roleSpinner.setLayoutParams(spinnerParams);
        layout.addView(roleSpinner);

        Button signupButton = new Button(this);
        signupButton.setText("Sign Up  →");
        signupButton.setTextSize(16);
        signupButton.setTypeface(null, Typeface.BOLD);
        signupButton.setTextColor(Color.WHITE);
        signupButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#3155D4")));

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(52));
        buttonParams.setMargins(0, 12, 0, 14);
        signupButton.setLayoutParams(buttonParams);

        layout.addView(signupButton);

        TextView loginLink = new TextView(this);
        loginLink.setText("Already have an account? Login");
        loginLink.setTextSize(15);
        loginLink.setTypeface(null, Typeface.BOLD);
        loginLink.setTextColor(Color.parseColor("#3155D4"));
        loginLink.setGravity(Gravity.CENTER);
        loginLink.setPadding(0, 15, 0, 0);
        layout.addView(loginLink);

        signupButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString();
            String confirm = confirmPasswordInput.getText().toString();
            String selectedRole = (roleSpinner.getSelectedItem() != null && roleSpinner.getSelectedItem().toString().contains("Admin")) ? "Admin" : "User";

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.setError("Enter a valid email");
                return;
            }

            if (password.length() < 6) {
                passwordInput.setError("Password must be at least 6 characters");
                return;
            }

            if (!password.equals(confirm)) {
                confirmPasswordInput.setError("Passwords do not match");
                return;
            }

            SharedPreferences preferences = getSharedPreferences("EventNestAccount", MODE_PRIVATE);
            String existingEmail = preferences.getString("email", "");

            if (email.equalsIgnoreCase(existingEmail)) {
                Toast.makeText(this, "Account already exists. Please login.", Toast.LENGTH_LONG).show();
                return;
            }

            preferences.edit()
                    .putString("name", name)
                    .putString("email", email)
                    .putString("password", password)
                    .putString("role", selectedRole)
                    .putBoolean("loggedIn", false)
                    .apply();

            Toast.makeText(this, "Sign Up successful as " + selectedRole + "! Please login.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        loginLink.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });

        setContentView(layout);
    }

    private EditText addField(LinearLayout layout, String hint, boolean password) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setSingleLine(true);
        input.setTextSize(16);

        input.setTextColor(Color.parseColor("#172554"));
        input.setHintTextColor(Color.parseColor("#64748B"));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(12);
        bg.setStroke(2, Color.parseColor("#CBD5E1"));
        input.setBackground(bg);

        input.setPadding(22, 22, 22, 22);

        if (password) {
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        } else if (hint.equals("Email Address")) {
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 4, 0, 12);
        layout.addView(input, params);

        return input;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
