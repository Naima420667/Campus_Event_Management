package com.example.campuseventmanagement;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class EventPassActivity extends AppCompatActivity {

    TextView backButton;
    TextView passEventTitle, passDateText, passVenueText, passIdText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_pass);

        backButton = findViewById(R.id.backButton);
        passEventTitle = findViewById(R.id.passEventTitle);
        passDateText = findViewById(R.id.passDateText);
        passVenueText = findViewById(R.id.passVenueText);
        passIdText = findViewById(R.id.passIdText);

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        String title = getIntent().getStringExtra("eventName");
        String date = getIntent().getStringExtra("eventDate");
        String venue = getIntent().getStringExtra("eventVenue");
        String passId = getIntent().getStringExtra("passId");

        if (title != null && passEventTitle != null) {
            passEventTitle.setText(title);
        }
        if (date != null && passDateText != null) {
            passDateText.setText(date);
        }
        if (venue != null && passVenueText != null) {
            passVenueText.setText(venue);
        }
        if (passId != null && passIdText != null) {
            passIdText.setText("PASS ID: " + passId);
        }
    }
}