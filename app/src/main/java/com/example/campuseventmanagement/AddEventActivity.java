package com.example.campuseventmanagement;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

public class AddEventActivity extends AppCompatActivity {

    private EditText eventNameInput, eventDateInput;
    private EditText eventVenueInput, eventOrganizerInput;
    private Button saveEventButton, cancelEventButton;

    private static final String TAG = "AddEventActivity";

    private static final String[] API_URLS = {
            "http://10.0.2.2/eventnest_api/api.php",
            "http://192.168.0.104/eventnest_api/api.php"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        eventNameInput = findViewById(R.id.eventNameInput);
        eventDateInput = findViewById(R.id.eventDateInput);
        eventVenueInput = findViewById(R.id.eventVenueInput);
        eventOrganizerInput = findViewById(R.id.eventOrganizerInput);

        saveEventButton = findViewById(R.id.saveEventButton);
        cancelEventButton = findViewById(R.id.cancelEventButton);

        saveEventButton.setOnClickListener(v -> saveEvent());
        cancelEventButton.setOnClickListener(v -> finish());
    }

    private void saveEvent() {
        String name = eventNameInput.getText().toString().trim();
        String rawDate = eventDateInput.getText().toString().trim();
        String venue = eventVenueInput.getText().toString().trim();
        String organizer = eventOrganizerInput.getText().toString().trim();

        if (name.isEmpty() || rawDate.isEmpty() || venue.isEmpty() || organizer.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String formattedDate = formatDateForMysql(rawDate);

        saveEventButton.setEnabled(false);
        saveEventButton.setText("Saving to Database...");

        new Thread(() -> {
            boolean success = false;
            String serverMessage = "Could not connect to database server.";

            try {
                String postData = "event_name=" + URLEncoder.encode(name, "UTF-8")
                        + "&event_date=" + URLEncoder.encode(formattedDate, "UTF-8")
                        + "&venue=" + URLEncoder.encode(venue, "UTF-8")
                        + "&organizer=" + URLEncoder.encode(organizer, "UTF-8")
                        + "&status=" + URLEncoder.encode("Upcoming", "UTF-8");

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
                        connection.setRequestProperty("Accept", "application/json");

                        try (OutputStream output = connection.getOutputStream()) {
                            output.write(postData.getBytes(StandardCharsets.UTF_8));
                            output.flush();
                        }

                        int responseCode = connection.getResponseCode();
                        InputStream inputStream = (responseCode >= 200 && responseCode < 400)
                                ? connection.getInputStream()
                                : connection.getErrorStream();

                        StringBuilder response = new StringBuilder();
                        if (inputStream != null) {
                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    response.append(line);
                                }
                            }
                        }

                        Log.d(TAG, "API " + apiUrl + " HTTP Code: " + responseCode + " Response: " + response);

                        if (response.length() > 0) {
                            JSONObject result = new JSONObject(response.toString());
                            boolean isSuccess = result.optBoolean("success", false);
                            String msg = result.optString("message", "Database operation complete.");

                            if (isSuccess) {
                                success = true;
                                serverMessage = msg;
                                break;
                            } else {
                                serverMessage = msg;
                            }
                        } else {
                            serverMessage = "Server returned empty response (HTTP " + responseCode + ")";
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "Error connecting to " + apiUrl, e);
                        serverMessage = "Connection failed: " + e.getMessage();
                    } finally {
                        if (connection != null) {
                            connection.disconnect();
                        }
                    }
                }

            } catch (Exception e) {
                success = false;
                serverMessage = "Error preparing data: " + e.getMessage();
            }

            final boolean finalSuccess = success;
            final String finalMsg = serverMessage;

            runOnUiThread(() -> {
                saveEventButton.setEnabled(true);
                saveEventButton.setText("Save Event");

                if (finalSuccess) {
                    saveEventLocally(name, formattedDate, venue, organizer);
                    Toast.makeText(AddEventActivity.this, "Success: " + finalMsg, Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(AddEventActivity.this, "Save Failed: " + finalMsg, Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    private String formatDateForMysql(String rawDate) {
        if (rawDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return rawDate;
        }
        String cleaned = rawDate.replace('/', '-').replace('.', '-');
        if (cleaned.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return cleaned;
        }
        return rawDate;
    }

    private void saveEventLocally(String name, String date, String venue, String organizer) {
        SharedPreferences prefs = getSharedPreferences("CampusEvents", MODE_PRIVATE);
        String existingEvents = prefs.getString("custom_events", "");

        try {
            JSONArray eventsArray = (!existingEvents.isEmpty()) ? new JSONArray(existingEvents) : new JSONArray();
            JSONObject newEvent = new JSONObject();
            newEvent.put("name", name);
            newEvent.put("date", date);
            newEvent.put("venue", venue);
            newEvent.put("organizer", organizer);

            eventsArray.put(newEvent);
            prefs.edit().putString("custom_events", eventsArray.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
