package com.example.campuseventmanagement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView totalEventsText, upcomingEventsText, pastEventsText, backButton;
    private Button manageEventsBtn, addNewEventBtn, calendarBtn, remindersBtn, myRegistrationsBtn;

    private static final String[] API_URLS = {
            "http://10.0.2.2/eventnest_api/api.php",
            "http://192.168.0.104/eventnest_api/api.php"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        backButton = findViewById(R.id.backButton);
        totalEventsText = findViewById(R.id.totalEventsText);
        upcomingEventsText = findViewById(R.id.upcomingEventsText);
        pastEventsText = findViewById(R.id.pastEventsText);

        manageEventsBtn = findViewById(R.id.manageEventsBtn);
        addNewEventBtn = findViewById(R.id.addNewEventBtn);
        calendarBtn = findViewById(R.id.calendarBtn);
        remindersBtn = findViewById(R.id.remindersBtn);
        myRegistrationsBtn = findViewById(R.id.myRegistrationsBtn);

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        if (manageEventsBtn != null) {
            manageEventsBtn.setOnClickListener(v -> startActivity(new Intent(this, ManageEventsActivity.class)));
        }

        if (addNewEventBtn != null) {
            addNewEventBtn.setOnClickListener(v -> startActivity(new Intent(this, AddEventActivity.class)));
        }

        if (calendarBtn != null) {
            calendarBtn.setOnClickListener(v -> startActivity(new Intent(this, CalendarActivity.class)));
        }

        if (remindersBtn != null) {
            remindersBtn.setOnClickListener(v -> startActivity(new Intent(this, EventReminderActivity.class)));
        }

        if (myRegistrationsBtn != null) {
            myRegistrationsBtn.setOnClickListener(v -> startActivity(new Intent(this, MyRegistrationsActivity.class)));
        }

        loadDashboard();
    }

    private void loadDashboard() {
        new Thread(() -> {
            int total = 0;
            int upcoming = 0;
            int past = 0;

            String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(new java.util.Date());

            for (String apiUrl : API_URLS) {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(apiUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(4000);
                    connection.setReadTimeout(4000);

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    String trimResp = response.toString().trim();
                    JSONArray events = null;
                    if (trimResp.startsWith("[")) {
                        events = new JSONArray(trimResp);
                    } else if (trimResp.startsWith("{")) {
                        JSONObject json = new JSONObject(trimResp);
                        events = json.optJSONArray("events");
                    }

                    if (events != null) {
                        total += events.length();
                        for (int i = 0; i < events.length(); i++) {
                            JSONObject event = events.optJSONObject(i);
                            if (event != null) {
                                String date = event.optString("event_date", "");
                                if (date.compareTo(today) >= 0) {
                                    upcoming++;
                                } else {
                                    past++;
                                }
                            }
                        }
                        break;
                    }
                } catch (Exception ignored) {
                } finally {
                    if (connection != null) connection.disconnect();
                }
            }

            SharedPreferences prefs = getSharedPreferences("CampusEvents", MODE_PRIVATE);
            String customJson = prefs.getString("custom_events", "");
            if (!customJson.isEmpty()) {
                try {
                    JSONArray array = new JSONArray(customJson);
                    total += array.length();
                    upcoming += array.length();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (total == 0) {
                total = 12;
                upcoming = 12;
            }

            final int finalTotal = total;
            final int finalUpcoming = upcoming;
            final int finalPast = past;

            runOnUiThread(() -> {
                if (totalEventsText != null) totalEventsText.setText(String.valueOf(finalTotal));
                if (upcomingEventsText != null) upcomingEventsText.setText(String.valueOf(finalUpcoming));
                if (pastEventsText != null) pastEventsText.setText(String.valueOf(finalPast));
            });
        }).start();
    }
}