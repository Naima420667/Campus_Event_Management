package com.example.campuseventmanagement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class RegistrationActivity extends AppCompatActivity {

    private EditText studentName;
    private EditText studentEmail;
    private EditText studentId;
    private EditText studentDepartment;
    private EditText studentPhone;

    private Button registerButton;
    private TextView backButton;

    private static final String TAG = "EventNestAPI";

    private static final String API_URL =
            "http://192.168.0.104/eventnest_api/register.php";

    private static final String PREFS_NAME = "EventNestPrefs";
    private static final String KEY_UNIVERSITY_ID = "university_id";
    private static final String KEY_LOCAL_REGISTRATIONS = "local_registrations_json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        studentName = findViewById(R.id.studentName);
        studentEmail = findViewById(R.id.studentEmail);
        studentId = findViewById(R.id.studentId);
        studentDepartment = findViewById(R.id.studentDepartment);
        studentPhone = findViewById(R.id.studentPhone);

        registerButton = findViewById(R.id.registerButton);
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());

        registerButton.setOnClickListener(v -> registerStudent());
    }

    private void registerStudent() {

        String name = studentName.getText().toString().trim();
        String email = studentEmail.getText().toString().trim();
        String universityId = studentId.getText().toString().trim();
        String department = studentDepartment.getText().toString().trim();
        String phone = studentPhone.getText().toString().trim();

        int eventId = getIntent().getIntExtra("event_id", 1);
        String eventName = getIntent().getStringExtra("event_name");
        if (eventName == null || eventName.trim().isEmpty()) {
            eventName = "Tech Fest 2026";
        }

        if (name.isEmpty() || email.isEmpty()
                || universityId.isEmpty() || department.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please fill all required fields",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            studentEmail.setError("Enter a valid email address");
            studentEmail.requestFocus();
            return;
        }

        registerButton.setEnabled(false);
        registerButton.setText("Registering...");

        final int finalEventId = eventId;
        final String finalEventName = eventName;

        saveRegistrationLocally(name, email, universityId, department, phone, finalEventId, finalEventName);

        new Thread(() -> {

            HttpURLConnection connection = null;

            try {

                URL url = new URL(API_URL);

                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                connection.setDoOutput(true);
                connection.setUseCaches(false);

                connection.setRequestProperty(
                        "Content-Type",
                        "application/x-www-form-urlencoded; charset=UTF-8"
                );

                connection.setRequestProperty(
                        "Accept",
                        "application/json"
                );

                String postData =
                        "name=" + URLEncoder.encode(name, "UTF-8")
                                + "&email=" + URLEncoder.encode(email, "UTF-8")
                                + "&university_id=" + URLEncoder.encode(universityId, "UTF-8")
                                + "&department=" + URLEncoder.encode(department, "UTF-8")
                                + "&phone=" + URLEncoder.encode(phone, "UTF-8")
                                + "&event_id=" + finalEventId;

                byte[] requestData =
                        postData.getBytes(StandardCharsets.UTF_8);

                connection.setFixedLengthStreamingMode(requestData.length);

                try (OutputStream outputStream = connection.getOutputStream()) {
                    outputStream.write(requestData);
                    outputStream.flush();
                }

                int responseCode = connection.getResponseCode();

                InputStream inputStream;

                if (responseCode >= 200 && responseCode < 400) {
                    inputStream = connection.getInputStream();
                } else {
                    inputStream = connection.getErrorStream();
                }

                StringBuilder response = new StringBuilder();

                if (inputStream != null) {

                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(
                                    inputStream,
                                    StandardCharsets.UTF_8
                            ))) {

                        String line;

                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                    }

                    inputStream.close();
                }

                Log.d(TAG, "HTTP code: " + responseCode);
                Log.d(TAG, "Response: " + response);

                JSONObject json = new JSONObject(response.toString());

                boolean success = json.optBoolean("success", false);

                String message = json.optString(
                        "message",
                        "Registration Successful!"
                );

                runOnUiThread(() -> {

                    registerButton.setEnabled(true);
                    registerButton.setText("Register Now");

                    Toast.makeText(
                            RegistrationActivity.this,
                            message,
                            Toast.LENGTH_LONG
                    ).show();

                    navigateToSuccess(finalEventId, finalEventName);
                });

            } catch (Exception e) {

                Log.e(TAG, "Registration API error", e);

                runOnUiThread(() -> {

                    registerButton.setEnabled(true);
                    registerButton.setText("Register Now");

                    Toast.makeText(
                            RegistrationActivity.this,
                            "Registration Saved Successfully!",
                            Toast.LENGTH_LONG
                    ).show();

                    navigateToSuccess(finalEventId, finalEventName);
                });

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }

        }).start();
    }

    private void saveRegistrationLocally(
            String name,
            String email,
            String universityId,
            String department,
            String phone,
            int eventId,
            String eventName
    ) {

        SharedPreferences preferences =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        preferences.edit()
                .putString(KEY_UNIVERSITY_ID, universityId)
                .apply();

        String existingJson =
                preferences.getString(KEY_LOCAL_REGISTRATIONS, "[]");

        try {

            JSONArray array = new JSONArray(existingJson);

            boolean exists = false;

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.optJSONObject(i);
                if (obj != null && obj.optInt("event_id", -1) == eventId) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {

                JSONObject regObj = new JSONObject();

                regObj.put("registration_id", String.valueOf(System.currentTimeMillis() % 100000));
                regObj.put("event_id", String.valueOf(eventId));
                regObj.put("event_name", eventName);
                regObj.put("event_date", "2026-10-15");
                regObj.put("venue", "University Auditorium");
                regObj.put("organizer", "CSE Department");
                regObj.put("student_name", name);
                regObj.put("university_id", universityId);

                array.put(regObj);

                preferences.edit()
                        .putString(KEY_LOCAL_REGISTRATIONS, array.toString())
                        .apply();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error saving local registration", e);
        }
    }

    private void navigateToSuccess(int eventId, String eventName) {

        Intent intent = new Intent(
                RegistrationActivity.this,
                RegistrationSuccessActivity.class
        );

        intent.putExtra("event_id", eventId);
        intent.putExtra("event_name", eventName);

        startActivity(intent);
        finish();
    }
}
