package com.example.campuseventmanagement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class ManageEventsActivity extends AppCompatActivity {

    private LinearLayout manageEventsContainer;
    private TextView backButton, noEventsText;
    private EditText searchBar;

    private static final String[] API_URLS = {
            "http://10.0.2.2/eventnest_api/api.php",
            "http://192.168.0.104/eventnest_api/api.php"
    };

    private final JSONArray loadedEventsList = new JSONArray();

    private static final String[][] DEFAULT_EVENTS = {
            {"Tech Fest 2026", "15 October 2026", "University Auditorium", "CSE Department"},
            {"Programming Contest", "20 October 2026", "CSE Lab", "CSE Department"},
            {"Cultural Night", "25 October 2026", "Central Auditorium", "Cultural Club"},
            {"Inter-University Debate", "30 October 2026", "Seminar Hall", "Debate Club"},
            {"Robotics Workshop", "05 November 2026", "Innovation Lab", "Robotics Club"},
            {"Career Seminar", "10 November 2026", "Conference Hall", "Career Club"},
            {"Sports Carnival", "15 November 2026", "University Playground", "Sports Club"},
            {"Art & Photography Exhibition", "20 November 2026", "Art Gallery", "Art Club"},
            {"Science & Innovation Fair", "25 November 2026", "Science Complex", "Science Club"},
            {"Campus Music Fest", "01 December 2026", "Open Air Stage", "Music Club"},
            {"Business Idea Competition", "05 December 2026", "Business Auditorium", "Entrepreneurship Club"},
            {"Blood Donation & Health Camp", "10 December 2026", "Student Centre", "Red Crescent Youth"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_events);

        backButton = findViewById(R.id.backButton);
        noEventsText = findViewById(R.id.noEventsText);
        manageEventsContainer = findViewById(R.id.manageEventsContainer);

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        setupControlBar();
        loadEventsToManage();
    }

    private void setupControlBar() {
        if (manageEventsContainer == null) return;

        LinearLayout parentLayout = (LinearLayout) manageEventsContainer.getParent();
        if (parentLayout != null) {
            // 1. Add New Event Action Button
            Button addNewEventBtn = new Button(this);
            addNewEventBtn.setText("➕  Create & Add New Event");
            addNewEventBtn.setAllCaps(false);
            addNewEventBtn.setTextSize(16);
            addNewEventBtn.setTypeface(null, Typeface.BOLD);
            addNewEventBtn.setTextColor(Color.WHITE);
            addNewEventBtn.setGravity(Gravity.CENTER);
            addNewEventBtn.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#3155D4"))
            );

            LinearLayout.LayoutParams addParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(52)
            );
            addParams.setMargins(0, 0, 0, dp(12));
            addNewEventBtn.setLayoutParams(addParams);
            addNewEventBtn.setOnClickListener(v -> {
                startActivity(new Intent(ManageEventsActivity.this, AddEventActivity.class));
            });

            // 2. Search Bar
            searchBar = new EditText(this);
            searchBar.setHint("🔍 Search events by title, venue, category...");
            searchBar.setSingleLine(true);
            searchBar.setTextSize(14);
            searchBar.setTextColor(Color.parseColor("#172554"));
            searchBar.setHintTextColor(Color.parseColor("#64748B"));

            GradientDrawable searchBg = new GradientDrawable();
            searchBg.setColor(Color.WHITE);
            searchBg.setCornerRadius(dp(12));
            searchBg.setStroke(2, Color.parseColor("#CBD5E1"));
            searchBar.setBackground(searchBg);
            searchBar.setPadding(dp(16), dp(12), dp(16), dp(12));

            LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(48)
            );
            searchParams.setMargins(0, 0, 0, dp(16));
            searchBar.setLayoutParams(searchParams);

            searchBar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterEvents(s.toString().trim().toLowerCase());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            int insertIndex = parentLayout.indexOfChild(manageEventsContainer);
            parentLayout.addView(addNewEventBtn, Math.max(0, insertIndex));
            parentLayout.addView(searchBar, Math.max(0, insertIndex + 1));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEventsToManage();
    }

    private void loadEventsToManage() {
        if (manageEventsContainer == null) return;

        manageEventsContainer.removeAllViews();

        SharedPreferences prefs = getSharedPreferences("CampusEvents", MODE_PRIVATE);
        Set<String> deletedEvents = prefs.getStringSet("deleted_events_set", new HashSet<>());

        new Thread(() -> {
            JSONArray dbEvents = null;

            for (String apiUrl : API_URLS) {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(apiUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(4000);
                    connection.setReadTimeout(4000);

                    InputStream stream = connection.getInputStream();
                    StringBuilder response = new StringBuilder();
                    if (stream != null) {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }
                        }
                    }

                    String respStr = response.toString().trim();
                    if (respStr.startsWith("[")) {
                        dbEvents = new JSONArray(respStr);
                    } else if (respStr.startsWith("{")) {
                        JSONObject json = new JSONObject(respStr);
                        dbEvents = json.optJSONArray("events");
                    }

                    if (dbEvents != null && dbEvents.length() > 0) break;
                } catch (Exception ignored) {
                } finally {
                    if (connection != null) connection.disconnect();
                }
            }

            final JSONArray finalDbEvents = dbEvents;

            runOnUiThread(() -> {
                int totalCount = 0;

                // 1. Add Database Events
                if (finalDbEvents != null) {
                    for (int i = 0; i < finalDbEvents.length(); i++) {
                        JSONObject obj = finalDbEvents.optJSONObject(i);
                        if (obj != null) {
                            int id = obj.optInt("id", obj.optInt("event_id", -1));
                            String name = obj.optString("event_name", obj.optString("name", "Event"));
                            String date = obj.optString("event_date", obj.optString("date", "Date"));
                            String venue = obj.optString("venue", "Venue");
                            String organizer = obj.optString("organizer", "Organizer");

                            if (!deletedEvents.contains(name) && !deletedEvents.contains("db_id_" + id)) {
                                addManageCard(id, name, date, venue, organizer, "DATABASE EVENT");
                                totalCount++;
                            }
                        }
                    }
                }

                // 2. Add Built-in Default Events
                for (String[] event : DEFAULT_EVENTS) {
                    String title = event[0];
                    String date = event[1];
                    String venue = event[2];
                    String organizer = event[3];

                    if (deletedEvents.contains(title)) continue;

                    addManageCard(-1, title, date, venue, organizer, "DEFAULT EVENT");
                    totalCount++;
                }

                // 3. Add Custom Local Events
                String customJson = prefs.getString("custom_events", "");
                if (!customJson.isEmpty()) {
                    try {
                        JSONArray array = new JSONArray(customJson);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            String name = obj.optString("name", "Custom Event");
                            String date = obj.optString("date", "Upcoming");
                            String venue = obj.optString("venue", "Campus");
                            String organizer = obj.optString("organizer", "Campus Committee");

                            if (deletedEvents.contains(name)) continue;

                            addManageCard(-1, name, date, venue, organizer, "CUSTOM EVENT");
                            totalCount++;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (noEventsText != null) {
                    noEventsText.setVisibility(totalCount == 0 ? View.VISIBLE : View.GONE);
                }
            });
        }).start();
    }

    private void filterEvents(String query) {
        if (manageEventsContainer == null) return;

        for (int i = 0; i < manageEventsContainer.getChildCount(); i++) {
            View child = manageEventsContainer.getChildAt(i);
            if (!(child instanceof LinearLayout)) continue;

            LinearLayout card = (LinearLayout) child;
            StringBuilder text = new StringBuilder();

            for (int j = 0; j < card.getChildCount(); j++) {
                View subView = card.getChildAt(j);
                if (subView instanceof TextView) {
                    text.append(((TextView) subView).getText().toString()).append(" ");
                }
            }

            boolean match = text.toString().toLowerCase().contains(query);
            card.setVisibility(match ? View.VISIBLE : View.GONE);
        }
    }

    private void addManageCard(int eventId, String title, String date, String venue, String organizer, String category) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(20), dp(18), dp(20), dp(18));

        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.WHITE);
        cardBg.setCornerRadius(dp(16));
        cardBg.setStroke(2, Color.parseColor("#E2E8F0"));
        card.setBackground(cardBg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(16));
        card.setLayoutParams(params);

        TextView categoryText = new TextView(this);
        categoryText.setText("✦  " + category);
        categoryText.setTextColor(Color.parseColor("#3155D4"));
        categoryText.setTextSize(11);
        categoryText.setTypeface(null, Typeface.BOLD);
        card.addView(categoryText);

        TextView titleText = new TextView(this);
        titleText.setText(title);
        titleText.setTextColor(Color.parseColor("#172554"));
        titleText.setTextSize(20);
        titleText.setTypeface(null, Typeface.BOLD);
        titleText.setPadding(0, dp(4), 0, dp(4));
        card.addView(titleText);

        TextView infoText = new TextView(this);
        infoText.setText("📅  " + date + "  •  📍  " + venue + "\n👤  Organizer: " + organizer);
        infoText.setTextColor(Color.parseColor("#697082"));
        infoText.setTextSize(13);
        infoText.setPadding(0, dp(2), 0, dp(12));
        card.addView(infoText);

        Button deleteBtn = new Button(this);
        deleteBtn.setText("🗑️ Delete Event");
        deleteBtn.setAllCaps(false);
        deleteBtn.setTextColor(Color.WHITE);
        deleteBtn.setTextSize(14);
        deleteBtn.setTypeface(null, Typeface.BOLD);
        deleteBtn.setGravity(Gravity.CENTER);
        deleteBtn.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(Color.parseColor("#DC2626"))
        );

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(48)
        );
        deleteBtn.setLayoutParams(btnParams);

        deleteBtn.setOnClickListener(v -> confirmDelete(eventId, title, card));
        card.addView(deleteBtn);

        manageEventsContainer.addView(card);
    }

    private void confirmDelete(int eventId, String eventTitle, View cardView) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete \"" + eventTitle + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (eventId > 0) {
                        deleteDatabaseEvent(eventId, eventTitle, cardView);
                    } else {
                        deleteEventLocally(eventTitle, cardView);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteDatabaseEvent(int eventId, String eventTitle, View cardView) {
        if (cardView != null) cardView.setVisibility(View.GONE);

        new Thread(() -> {
            boolean success = false;
            String message = "";

            for (String apiUrl : API_URLS) {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(apiUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

                    String postData = "action=delete&id=" + eventId + "&event_id=" + eventId;
                    try (OutputStream output = connection.getOutputStream()) {
                        output.write(postData.getBytes(StandardCharsets.UTF_8));
                        output.flush();
                    }

                    int responseCode = connection.getResponseCode();
                    InputStream stream = (responseCode >= 200 && responseCode < 400)
                            ? connection.getInputStream()
                            : connection.getErrorStream();

                    StringBuilder response = new StringBuilder();
                    if (stream != null) {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }
                        }
                    }

                    JSONObject json = new JSONObject(response.toString());
                    success = json.optBoolean("success", false);
                    message = json.optString("message", success ? "Event deleted from MySQL!" : "Delete failed.");

                    if (success) break;

                } catch (Exception e) {
                    success = false;
                    message = "Delete failed: " + e.getMessage();
                } finally {
                    if (connection != null) connection.disconnect();
                }
            }

            final String finalMsg = message;

            runOnUiThread(() -> {
                Toast.makeText(ManageEventsActivity.this, finalMsg, Toast.LENGTH_SHORT).show();
                deleteEventLocally(eventTitle, cardView);
            });
        }).start();
    }

    private void deleteEventLocally(String eventTitle, View cardView) {
        if (cardView != null) cardView.setVisibility(View.GONE);

        SharedPreferences prefs = getSharedPreferences("CampusEvents", MODE_PRIVATE);
        Set<String> deletedEvents = new HashSet<>(prefs.getStringSet("deleted_events_set", new HashSet<>()));
        deletedEvents.add(eventTitle);

        String customJson = prefs.getString("custom_events", "");
        if (!customJson.isEmpty()) {
            try {
                JSONArray array = new JSONArray(customJson);
                JSONArray updatedArray = new JSONArray();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    if (!eventTitle.equalsIgnoreCase(obj.optString("name", ""))) {
                        updatedArray.put(obj);
                    }
                }
                prefs.edit().putString("custom_events", updatedArray.toString()).apply();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        prefs.edit().putStringSet("deleted_events_set", deletedEvents).apply();
        Toast.makeText(this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
