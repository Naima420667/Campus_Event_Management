package com.example.campuseventmanagement;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EventReminderActivity extends Activity {

    private static final int NOTIFICATION_PERMISSION_CODE = 200;
    private Button reminderButton;

    private static final String[] API_URLS = {
            "http://10.0.2.2/eventnest_api/api.php",
            "http://192.168.0.104/eventnest_api/api.php"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(24, 30, 24, 30);
        main.setGravity(Gravity.CENTER);
        main.setBackgroundColor(Color.parseColor("#F0F7FF"));

        TextView title = new TextView(this);
        title.setText("⏰ Event Reminders");
        title.setTextSize(26);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.parseColor("#172554"));
        title.setGravity(Gravity.CENTER);
        main.addView(title);

        TextView description = new TextView(this);
        description.setText(
                "Schedule automatic reminders for upcoming campus events.\n\n" +
                        "Reminder notification time: 9:00 AM on the day before an event."
        );
        description.setTextSize(15);
        description.setTextColor(Color.parseColor("#64748B"));
        description.setPadding(0, 20, 0, 24);
        main.addView(description);

        reminderButton = new Button(this);
        reminderButton.setText("Enable Event Reminders");
        reminderButton.setAllCaps(false);
        reminderButton.setTextSize(16);
        reminderButton.setTypeface(null, Typeface.BOLD);
        reminderButton.setTextColor(Color.WHITE);
        reminderButton.setPadding(20, 22, 20, 22);
        reminderButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#3155D4")));

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        reminderButton.setLayoutParams(btnParams);

        main.addView(reminderButton);

        reminderButton.setOnClickListener(v -> requestPermissionAndLoad());

        setContentView(main);
    }

    private void requestPermissionAndLoad() {
        if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(
                        this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_CODE
            );
            return;
        }

        scheduleReminders();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            scheduleReminders();
        } else {
            Toast.makeText(this, "Notification permission is required", Toast.LENGTH_LONG).show();
        }
    }

    private void scheduleReminders() {
        Toast.makeText(this, "Scheduling event reminders...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            int scheduled = 0;
            JSONArray events = null;

            // 1. Fetch from API
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
                    if (trimResp.startsWith("[")) {
                        events = new JSONArray(trimResp);
                    } else if (trimResp.startsWith("{")) {
                        JSONObject json = new JSONObject(trimResp);
                        events = json.optJSONArray("events");
                    }
                    if (events != null && events.length() > 0) break;
                } catch (Exception ignored) {
                } finally {
                    if (connection != null) connection.disconnect();
                }
            }

            if (events != null) {
                for (int i = 0; i < events.length(); i++) {
                    JSONObject event = events.optJSONObject(i);
                    if (event != null) {
                        String name = event.optString("event_name", "Campus Event");
                        String date = event.optString("event_date", "");
                        int id = event.optInt("event_id", i + 1);

                        if (scheduleSingleReminder(id, name, date)) {
                            scheduled++;
                        }
                    }
                }
            }

            // 2. Schedule for custom events
            SharedPreferences prefs = getSharedPreferences("CampusEvents", MODE_PRIVATE);
            String customJson = prefs.getString("custom_events", "");
            if (!customJson.isEmpty()) {
                try {
                    JSONArray array = new JSONArray(customJson);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        String name = obj.optString("name", "Custom Event");
                        String date = obj.optString("date", "");
                        int id = 500 + i;

                        if (scheduleSingleReminder(id, name, date)) {
                            scheduled++;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            final int result = scheduled;
            runOnUiThread(() -> Toast.makeText(
                    this,
                    "Successfully scheduled " + result + " event reminder(s)!",
                    Toast.LENGTH_LONG
            ).show());
        }).start();
    }

    private boolean scheduleSingleReminder(int id, String name, String date) {
        if (date == null || date.isEmpty()) return false;

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date eventDate = formatter.parse(date);
            if (eventDate == null) return false;

            Calendar alarmTime = Calendar.getInstance();
            alarmTime.setTime(eventDate);
            alarmTime.add(Calendar.DAY_OF_MONTH, -1);
            alarmTime.set(Calendar.HOUR_OF_DAY, 9);
            alarmTime.set(Calendar.MINUTE, 0);
            alarmTime.set(Calendar.SECOND, 0);
            alarmTime.set(Calendar.MILLISECOND, 0);

            if (alarmTime.getTimeInMillis() <= System.currentTimeMillis()) {
                return false;
            }

            Intent intent = new Intent(this, EventReminderReceiver.class);
            intent.putExtra("event_name", name);
            intent.putExtra("event_id", id);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this, id, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pendingIntent);
                    } else {
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pendingIntent);
                    }
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pendingIntent);
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}