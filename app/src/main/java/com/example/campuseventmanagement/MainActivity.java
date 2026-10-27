package com.example.campuseventmanagement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button exploreEventsButton;
    private Button myRegistrationsButton;
    private Button accountButton;
    private Button dashboardButton;
    private Button calendarButton;
    private Button reminderButton;
    private Button notificationButton;
    private Button logoutButton;
    private ImageView campusImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        campusImage = findViewById(R.id.campusImage);
        exploreEventsButton = findViewById(R.id.eventButton);

        if (campusImage != null) {
            campusImage.setImageResource(R.drawable.eventnest_logo_blue);
        }

        if (exploreEventsButton != null) {
            exploreEventsButton.setText("Explore Campus Events  →");
            exploreEventsButton.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, EventListActivity.class));
            });
        }

        LinearLayout mainLayout = findMainLayout();

        if (mainLayout != null) {
            SharedPreferences accountPrefs = getSharedPreferences("EventNestAccount", MODE_PRIVATE);
            boolean isLoggedIn = accountPrefs.getBoolean("loggedIn", false);
            String userName = accountPrefs.getString("name", "Student");
            String userRole = accountPrefs.getString("role", "User");

            // User Welcome Header
            TextView welcomeHeader = new TextView(this);
            if (isLoggedIn) {
                welcomeHeader.setText("Welcome, " + userName + " 👋 (" + userRole + ")");
            } else {
                welcomeHeader.setText("Welcome to EventNest 👋");
            }
            welcomeHeader.setTextColor(Color.parseColor("#172554"));
            welcomeHeader.setTextSize(18);
            welcomeHeader.setTypeface(null, Typeface.BOLD);
            welcomeHeader.setPadding(24, 12, 24, 6);
            mainLayout.addView(welcomeHeader, 0);

            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            buttonParams.setMargins(24, 8, 24, 8);

            // 1. Account (Login / Sign Up)
            if (!isLoggedIn) {
                accountButton = createMenuButton("🔐 Sign In / Sign Up", "#3155D4");
                mainLayout.addView(accountButton, buttonParams);
                accountButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, LoginActivity.class)));
            }

            // 2. My Registrations
            myRegistrationsButton = createMenuButton("🎟️ My Event Registrations", "#3155D4");
            mainLayout.addView(myRegistrationsButton, buttonParams);
            myRegistrationsButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MyRegistrationsActivity.class)));

            // 3. Admin Dashboard
            dashboardButton = createMenuButton("📊 Admin Dashboard", "#172554");
            mainLayout.addView(dashboardButton, buttonParams);
            dashboardButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AdminDashboardActivity.class)));

            // 4. Calendar View
            calendarButton = createMenuButton("📅 Event Calendar", "#0F766E");
            mainLayout.addView(calendarButton, buttonParams);
            calendarButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CalendarActivity.class)));

            // 5. Event Reminders
            reminderButton = createMenuButton("⏰ Event Reminders", "#7C3AED");
            mainLayout.addView(reminderButton, buttonParams);
            reminderButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, EventReminderActivity.class)));

            // 6. Notifications
            notificationButton = createMenuButton("🔔 Notifications", "#2563EB");
            mainLayout.addView(notificationButton, buttonParams);
            notificationButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, NotificationActivity.class)));

            // 7. Logout Option
            if (isLoggedIn) {
                logoutButton = createMenuButton("🚪 Sign Out (" + userName + ")", "#DC2626");
                mainLayout.addView(logoutButton, buttonParams);
                logoutButton.setOnClickListener(v -> {
                    accountPrefs.edit().putBoolean("loggedIn", false).apply();
                    Toast.makeText(MainActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            }
        }
    }

    private Button createMenuButton(String text, String colorHex) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextSize(15);
        button.setTypeface(null, Typeface.BOLD);
        button.setTextColor(Color.WHITE);
        button.setPadding(20, 24, 20, 24);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(colorHex)));
        return button;
    }

    private LinearLayout findMainLayout() {
        ViewGroup root = findViewById(android.R.id.content);
        return findVerticalLayout(root);
    }

    private LinearLayout findVerticalLayout(ViewGroup parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            android.view.View child = parent.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout layout = (LinearLayout) child;
                if (layout.getOrientation() == LinearLayout.VERTICAL) {
                    if (layout.findViewById(R.id.eventButton) != null) {
                        return layout;
                    }
                    LinearLayout nested = findVerticalLayout(layout);
                    if (nested != null) return nested;
                }
            }
            if (child instanceof ViewGroup) {
                LinearLayout nested = findVerticalLayout((ViewGroup) child);
                if (nested != null) return nested;
            }
        }
        return null;
    }
}
