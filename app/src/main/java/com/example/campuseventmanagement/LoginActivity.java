package com.example.campuseventmanagement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences("EventNestAccount", MODE_PRIVATE);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(28, 35, 28, 25);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        layout.setBackgroundColor(Color.parseColor("#F0F7FF"));

        TextView title = new TextView(this);
        title.setText("Login to EventNest");
        title.setTextSize(26);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.parseColor("#172554"));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 24);
        layout.addView(title);

        emailInput = createStyledInput("Email Address", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        layout.addView(emailInput);

        passwordInput = createStyledInput("Password", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(passwordInput);

        Button loginButton = new Button(this);
        loginButton.setText("Login  →");
        loginButton.setTextSize(16);
        loginButton.setTypeface(null, Typeface.BOLD);
        loginButton.setTextColor(Color.WHITE);
        loginButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#3155D4")));

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(52));
        buttonParams.setMargins(0, 20, 0, 16);
        loginButton.setLayoutParams(buttonParams);

        layout.addView(loginButton);

        TextView signupLink = new TextView(this);
        signupLink.setText("Don't have an account? Sign Up");
        signupLink.setTextSize(15);
        signupLink.setTypeface(null, Typeface.BOLD);
        signupLink.setTextColor(Color.parseColor("#3155D4"));
        signupLink.setGravity(Gravity.CENTER);
        signupLink.setPadding(0, 15, 0, 0);
        layout.addView(signupLink);

        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Default Admin Credentials
            if ((email.equalsIgnoreCase("admin@eventnest.com") || email.toLowerCase().contains("admin"))
                    && (password.equals("admin123") || password.equalsIgnoreCase("admin"))) {

                preferences.edit()
                        .putBoolean("loggedIn", true)
                        .putString("email", email)
                        .putString("role", "Admin")
                        .apply();

                Toast.makeText(this, "Admin Login Successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, AdminDashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            }

            String savedEmail = preferences.getString("email", "");
            String savedPassword = preferences.getString("password", "");
            String savedRole = preferences.getString("role", "User");

            if (email.equalsIgnoreCase(savedEmail) && password.equals(savedPassword) && !savedEmail.isEmpty()) {
                preferences.edit()
                        .putBoolean("loggedIn", true)
                        .putString("role", savedRole)
                        .apply();

                Toast.makeText(this, "Welcome " + preferences.getString("name", "User") + "!", Toast.LENGTH_SHORT).show();

                Intent intent;
                if ("Admin".equalsIgnoreCase(savedRole) || email.toLowerCase().contains("admin")) {
                    intent = new Intent(this, AdminDashboardActivity.class);
                } else {
                    intent = new Intent(this, MainActivity.class);
                }

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid email or password. Sign up first.", Toast.LENGTH_LONG).show();
            }
        });

        signupLink.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
        });

        setContentView(layout);
    }

    private EditText createStyledInput(String hint, int inputType) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setSingleLine(true);
        input.setInputType(inputType);
        input.setTextSize(16);

        input.setTextColor(Color.parseColor("#172554"));
        input.setHintTextColor(Color.parseColor("#64748B"));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(12);
        bg.setStroke(2, Color.parseColor("#CBD5E1"));
        input.setBackground(bg);

        input.setPadding(22, 22, 22, 22);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 8, 0, 16);
        input.setLayoutParams(params);

        return input;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
