package com.example.campuseventmanagement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class MyRegistrationsActivity extends AppCompatActivity {

    private TextView backButton;
    private TextView activeCount;
    private TextView emptyMessage;
    private Button exploreMoreButton;
    private LinearLayout registrationContainer;
    private EditText searchInputBar;

    private JSONArray allLoadedRegistrations = new JSONArray();

    private static final String TAG = "MyRegistrations";

    private static final String API_URL =
            "http://192.168.0.104/eventnest_api/get_my_registrations.php";

    private static final String PREFS_NAME = "EventNestPrefs";
    private static final String KEY_UNIVERSITY_ID = "university_id";
    private static final String KEY_LOCAL_REGISTRATIONS = "local_registrations_json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_registrations);

        backButton = findViewById(R.id.backButton);
        activeCount = findViewById(R.id.activeCount);
        emptyMessage = findViewById(R.id.emptyMessage);
        exploreMoreButton = findViewById(R.id.exploreMoreButton);
        registrationContainer = findViewById(R.id.registrationContainer);

        backButton.setOnClickListener(v -> finish());

        exploreMoreButton.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MyRegistrationsActivity.this,
                    EventListActivity.class
            );
            startActivity(intent);
        });

        // Search Bar for filtering registrations by student name or event name
        setupSearchInputBar();

        loadRegistrations();
    }

    private void setupSearchInputBar() {
        if (registrationContainer == null) return;

        LinearLayout parentLayout = (LinearLayout) registrationContainer.getParent();
        if (parentLayout != null && searchInputBar == null) {
            searchInputBar = new EditText(this);
            searchInputBar.setHint("🔍 Search registrations by student name or event...");
            searchInputBar.setSingleLine(true);
            searchInputBar.setTextSize(14);
            searchInputBar.setTextColor(Color.parseColor("#172554"));
            searchInputBar.setHintTextColor(Color.parseColor("#64748B"));

            GradientDrawable bg = new GradientDrawable();
            bg.setColor(Color.WHITE);
            bg.setCornerRadius(dp(12));
            bg.setStroke(2, Color.parseColor("#CBD5E1"));
            searchInputBar.setBackground(bg);
            searchInputBar.setPadding(dp(16), dp(12), dp(16), dp(12));

            LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            searchParams.setMargins(0, 0, 0, dp(16));
            searchInputBar.setLayoutParams(searchParams);

            int insertIndex = parentLayout.indexOfChild(registrationContainer);
            parentLayout.addView(searchInputBar, Math.max(0, insertIndex));

            searchInputBar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterRegistrationsByQuery(s.toString().trim());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (registrationContainer != null) {
            loadRegistrations();
        }
    }

    private void loadRegistrations() {
        SharedPreferences preferences =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        SharedPreferences accountPrefs =
                getSharedPreferences("EventNestAccount", MODE_PRIVATE);

        String userName = accountPrefs.getString("name", "Student");
        String universityId = preferences.getString(KEY_UNIVERSITY_ID, "");
        String localJsonStr = preferences.getString(KEY_LOCAL_REGISTRATIONS, "[]");

        JSONArray localArray = new JSONArray();

        try {
            localArray = new JSONArray(localJsonStr);
            // Ensure student_name is set for local items
            for (int i = 0; i < localArray.length(); i++) {
                JSONObject obj = localArray.getJSONObject(i);
                if (!obj.has("student_name")) {
                    obj.put("student_name", userName);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing local JSON", e);
        }

        allLoadedRegistrations = localArray;
        displayRegistrations(allLoadedRegistrations);

        if ((universityId == null || universityId.trim().isEmpty())
                && localArray.length() == 0) {

            activeCount.setText("0");
            registrationContainer.removeAllViews();

            emptyMessage.setText(
                    "No registrations found. Please register for an event first."
            );
            emptyMessage.setVisibility(View.VISIBLE);
            return;
        }

        final JSONArray finalLocalArray = localArray;
        final String finalUnivId = universityId;

        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                String encodedId = URLEncoder.encode(finalUnivId, "UTF-8");
                URL url = new URL(API_URL + "?university_id=" + encodedId);

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(6000);
                connection.setReadTimeout(6000);
                connection.setRequestProperty("Accept", "application/json");

                int responseCode = connection.getResponseCode();
                InputStream inputStream = (responseCode >= 200 && responseCode < 300)
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                StringBuilder response = new StringBuilder();
                if (inputStream != null) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                    }
                }

                if (responseCode == 200 && response.length() > 0) {
                    JSONObject json = new JSONObject(response.toString());
                    boolean success = json.optBoolean("success", false);

                    if (success) {
                        JSONArray remoteArray = json.optJSONArray("registrations");
                        if (remoteArray != null && remoteArray.length() > 0) {
                            JSONArray merged = mergeRegistrations(finalLocalArray, remoteArray);
                            allLoadedRegistrations = merged;
                            runOnUiThread(() -> displayRegistrations(merged));
                            return;
                        }
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Server connection failed, displaying local data", e);
            } finally {
                if (connection != null) connection.disconnect();
            }

            runOnUiThread(() -> {
                if (finalLocalArray.length() > 0) {
                    allLoadedRegistrations = finalLocalArray;
                    displayRegistrations(finalLocalArray);
                }
            });

        }).start();
    }

    private void filterRegistrationsByQuery(String query) {
        if (query.isEmpty()) {
            displayRegistrations(allLoadedRegistrations);
            return;
        }

        String lowerQuery = query.toLowerCase();
        JSONArray filtered = new JSONArray();

        for (int i = 0; i < allLoadedRegistrations.length(); i++) {
            JSONObject obj = allLoadedRegistrations.optJSONObject(i);
            if (obj != null) {
                String studentName = obj.optString("student_name", obj.optString("name", "")).toLowerCase();
                String eventName = obj.optString("event_name", "").toLowerCase();
                String venue = obj.optString("venue", "").toLowerCase();
                String regId = obj.optString("registration_id", "").toLowerCase();

                if (studentName.contains(lowerQuery) || eventName.contains(lowerQuery)
                        || venue.contains(lowerQuery) || regId.contains(lowerQuery)) {
                    filtered.put(obj);
                }
            }
        }

        displayRegistrations(filtered);
    }

    private JSONArray mergeRegistrations(JSONArray local, JSONArray remote) {
        JSONArray result = new JSONArray();

        try {
            for (int i = 0; i < remote.length(); i++) {
                result.put(remote.getJSONObject(i));
            }

            for (int i = 0; i < local.length(); i++) {
                JSONObject locObj = local.getJSONObject(i);
                String locEventId = locObj.optString("event_id", "");
                boolean existsInRemote = false;

                for (int j = 0; j < remote.length(); j++) {
                    JSONObject remObj = remote.getJSONObject(j);
                    if (locEventId.equals(remObj.optString("event_id", ""))) {
                        existsInRemote = true;
                        break;
                    }
                }

                if (!existsInRemote) {
                    result.put(locObj);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Merge error", e);
            return local;
        }

        return result;
    }

    private void displayRegistrations(JSONArray registrations) {
        registrationContainer.removeAllViews();

        int count = registrations.length();
        activeCount.setText(String.valueOf(count));

        if (count == 0) {
            emptyMessage.setText("No matching registrations found.");
            emptyMessage.setVisibility(View.VISIBLE);
            return;
        }

        emptyMessage.setVisibility(View.GONE);

        for (int i = 0; i < count; i++) {
            JSONObject event = registrations.optJSONObject(i);
            if (event == null) continue;

            String eventName = event.optString("event_name", "Campus Event");
            String eventDate = event.optString("event_date", "15 October 2026");
            String venue = event.optString("venue", "University Auditorium");
            String registrationId = event.optString("registration_id", String.valueOf(i + 1));
            String eventId = event.optString("event_id", "");
            String organizer = event.optString("organizer", "CSE Department");
            String studentName = event.optString("student_name", "Student");

            addRegistrationCard(
                    eventName,
                    eventDate,
                    venue,
                    registrationId,
                    eventId,
                    organizer,
                    studentName
            );
        }
    }

    private void addRegistrationCard(
            String eventName,
            String eventDate,
            String venue,
            String registrationId,
            String eventId,
            String organizer,
            String studentName
    ) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.WHITE);
        background.setCornerRadius(dp(16));

        card.setBackground(background);
        card.setElevation(dp(3));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(16));
        card.setLayoutParams(cardParams);

        TextView icon = new TextView(this);
        icon.setText("✦  STUDENT: " + studentName.toUpperCase());
        icon.setTextSize(12);
        icon.setTypeface(null, Typeface.BOLD);
        icon.setTextColor(Color.rgb(49, 85, 212));
        card.addView(icon);

        TextView title = new TextView(this);
        title.setText(eventName);
        title.setTextSize(20);
        title.setTextColor(Color.rgb(23, 37, 84));
        title.setTypeface(null, Typeface.BOLD);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.topMargin = dp(6);
        title.setLayoutParams(titleParams);
        card.addView(title);

        TextView details = new TextView(this);
        details.setText(formatDate(eventDate) + "  •  " + venue);
        details.setTextSize(13);
        details.setTextColor(Color.rgb(105, 112, 130));

        LinearLayout.LayoutParams detailsParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        detailsParams.topMargin = dp(8);
        details.setLayoutParams(detailsParams);
        card.addView(details);

        TextView organizerText = new TextView(this);
        organizerText.setText("Organizer: " + organizer);
        organizerText.setTextSize(13);
        organizerText.setTextColor(Color.rgb(105, 112, 130));

        LinearLayout.LayoutParams organizerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        organizerParams.topMargin = dp(6);
        organizerText.setLayoutParams(organizerParams);
        card.addView(organizerText);

        LinearLayout bottomRow = new LinearLayout(this);
        bottomRow.setOrientation(LinearLayout.HORIZONTAL);
        bottomRow.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.topMargin = dp(14);
        bottomRow.setLayoutParams(rowParams);

        TextView registeredText = new TextView(this);
        registeredText.setText("✓  REGISTERED");
        registeredText.setTextColor(Color.rgb(22, 163, 74));
        registeredText.setTextSize(11);
        registeredText.setTypeface(null, Typeface.BOLD);

        LinearLayout.LayoutParams registeredParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        registeredText.setLayoutParams(registeredParams);
        bottomRow.addView(registeredText);

        Button viewPassButton = new Button(this);
        viewPassButton.setText("View Pass");
        viewPassButton.setAllCaps(false);
        viewPassButton.setTextSize(12);
        viewPassButton.setTextColor(Color.rgb(49, 85, 212));
        viewPassButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(Color.rgb(238, 242, 255))
        );

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(46)
        );
        viewPassButton.setLayoutParams(buttonParams);

        viewPassButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyRegistrationsActivity.this, EventPassActivity.class);
            intent.putExtra("eventName", eventName);
            intent.putExtra("eventDate", formatDate(eventDate));
            intent.putExtra("eventVenue", venue);
            intent.putExtra("passId", "EN-" + registrationId);
            intent.putExtra("event_id", eventId);
            intent.putExtra("organizer", organizer);
            startActivity(intent);
        });

        bottomRow.addView(viewPassButton);
        card.addView(bottomRow);

        registrationContainer.addView(card);
    }

    private String formatDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return "Date not available";
        }

        try {
            String[] parts = date.split("-");
            if (parts.length == 3) {
                String year = parts[0];
                int month = Integer.parseInt(parts[1]);
                String day = parts[2];

                String[] months = {
                        "", "January", "February", "March", "April", "May", "June",
                        "July", "August", "September", "October", "November", "December"
                };

                if (month >= 1 && month <= 12) {
                    return day + " " + months[month] + " " + year;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Date formatting error", e);
        }

        return date;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
