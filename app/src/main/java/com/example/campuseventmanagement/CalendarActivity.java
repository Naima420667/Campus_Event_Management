package com.example.campuseventmanagement;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class CalendarActivity extends Activity {

    private CalendarView calendarView;
    private LinearLayout eventContainer;
    private final ArrayList<JSONObject> events = new ArrayList<>();
    private String selectedDate;

    private static final String[] API_URLS = {
            "http://10.0.2.2/eventnest_api/api.php",
            "http://192.168.0.104/eventnest_api/api.php"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Calendar today = Calendar.getInstance();
        selectedDate = String.format(
                Locale.US, "%04d-%02d-%02d",
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH) + 1,
                today.get(Calendar.DAY_OF_MONTH)
        );

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(20, 20, 20, 20);
        main.setBackgroundColor(Color.parseColor("#F0F7FF"));

        TextView backButton = new TextView(this);
        backButton.setText("←  Back to Home");
        backButton.setTextColor(Color.parseColor("#3155D4"));
        backButton.setTextSize(15);
        backButton.setTypeface(null, Typeface.BOLD);
        backButton.setPadding(4, 4, 4, 16);
        backButton.setOnClickListener(v -> finish());
        main.addView(backButton);

        TextView title = new TextView(this);
        title.setText("📅 Event Calendar");
        title.setTextSize(26);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.parseColor("#172554"));
        title.setGravity(Gravity.CENTER);
        main.addView(title);

        LinearLayout calendarCard = new LinearLayout(this);
        calendarCard.setOrientation(LinearLayout.VERTICAL);
        calendarCard.setPadding(10, 10, 10, 10);

        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.WHITE);
        cardBg.setCornerRadius(16);
        cardBg.setStroke(2, Color.parseColor("#CBD5E1"));
        calendarCard.setBackground(cardBg);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 16, 0, 16);
        calendarCard.setLayoutParams(cardParams);

        calendarView = new CalendarView(this);
        calendarCard.addView(calendarView);
        main.addView(calendarCard);

        TextView heading = new TextView(this);
        heading.setText("Events on Selected Date (" + selectedDate + "):");
        heading.setTextSize(16);
        heading.setTypeface(null, Typeface.BOLD);
        heading.setTextColor(Color.parseColor("#172554"));
        heading.setPadding(4, 10, 4, 10);
        main.addView(heading);

        ScrollView scrollView = new ScrollView(this);
        eventContainer = new LinearLayout(this);
        eventContainer.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(eventContainer);
        main.addView(scrollView);

        calendarView.setOnDateChangeListener((view, year, month, day) -> {
            selectedDate = String.format(
                    Locale.US, "%04d-%02d-%02d",
                    year, month + 1, day
            );
            heading.setText("Events on Selected Date (" + selectedDate + "):");
            showSelectedEvents();
        });

        setContentView(main);
        loadEvents();
    }

    private void loadEvents() {
        new Thread(() -> {
            ArrayList<JSONObject> loaded = new ArrayList<>();

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
                    JSONArray array = null;
                    if (trimResp.startsWith("[")) {
                        array = new JSONArray(trimResp);
                    } else if (trimResp.startsWith("{")) {
                        JSONObject json = new JSONObject(trimResp);
                        array = json.optJSONArray("events");
                    }

                    if (array != null) {
                        for (int i = 0; i < array.length(); i++) {
                            loaded.add(array.getJSONObject(i));
                        }
                        if (!loaded.isEmpty()) break;
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
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        JSONObject eventObj = new JSONObject();
                        eventObj.put("event_name", obj.optString("name", "Custom Event"));
                        eventObj.put("event_date", obj.optString("date", ""));
                        eventObj.put("venue", obj.optString("venue", "Campus"));
                        eventObj.put("organizer", obj.optString("organizer", "Campus Committee"));
                        loaded.add(eventObj);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            runOnUiThread(() -> {
                events.clear();
                events.addAll(loaded);
                showSelectedEvents();
            });
        }).start();
    }

    private void showSelectedEvents() {
        eventContainer.removeAllViews();
        int count = 0;

        for (JSONObject event : events) {
            String eventDate = event.optString("event_date", "");
            if (selectedDate.equals(eventDate) || eventDate.contains(selectedDate)) {
                TextView item = new TextView(this);
                item.setText("🎉 " + event.optString("event_name", "Event") +
                        "\n📍 Venue: " + event.optString("venue", "Campus") +
                        "\n👤 Organizer: " + event.optString("organizer", "Committee"));

                item.setTextSize(15);
                item.setTypeface(null, Typeface.BOLD);
                item.setTextColor(Color.parseColor("#172554"));
                item.setPadding(18, 18, 18, 18);

                GradientDrawable bg = new GradientDrawable();
                bg.setColor(Color.WHITE);
                bg.setCornerRadius(12);
                bg.setStroke(2, Color.parseColor("#3155D4"));
                item.setBackground(bg);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 0, 14);
                eventContainer.addView(item, params);
                count++;
            }
        }

        if (count == 0) {
            TextView empty = new TextView(this);
            empty.setText("No campus events scheduled on " + selectedDate);
            empty.setTextSize(15);
            empty.setTextColor(Color.parseColor("#64748B"));
            empty.setPadding(10, 16, 10, 16);
            eventContainer.addView(empty);
        }
    }
}