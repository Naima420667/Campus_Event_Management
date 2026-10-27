package com.example.campuseventmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EventsActivity extends AppCompatActivity {

    Button viewEvent1, viewEvent2, viewEvent3, viewEvent4, viewEvent5, viewEvent6, viewEvent7, viewEvent8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_events);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewEvent1 = findViewById(R.id.viewEvent1);
        viewEvent2 = findViewById(R.id.viewEvent2);
        viewEvent3 = findViewById(R.id.viewEvent3);
        viewEvent4 = findViewById(R.id.viewEvent4);
        viewEvent5 = findViewById(R.id.viewEvent5);
        viewEvent6 = findViewById(R.id.viewEvent6);
        viewEvent7 = findViewById(R.id.viewEvent7);
        viewEvent8 = findViewById(R.id.viewEvent8);

        if (viewEvent1 != null) viewEvent1.setOnClickListener(v -> openEventDetails("tech"));
        if (viewEvent2 != null) viewEvent2.setOnClickListener(v -> openEventDetails("programming"));
        if (viewEvent3 != null) viewEvent3.setOnClickListener(v -> openEventDetails("cultural"));
        if (viewEvent4 != null) viewEvent4.setOnClickListener(v -> openEventDetails("debate"));
        if (viewEvent5 != null) viewEvent5.setOnClickListener(v -> openEventDetails("robotics"));
        if (viewEvent6 != null) viewEvent6.setOnClickListener(v -> openEventDetails("career"));
        if (viewEvent7 != null) viewEvent7.setOnClickListener(v -> openEventDetails("sports"));
        if (viewEvent8 != null) viewEvent8.setOnClickListener(v -> openEventDetails("art"));
    }

    private void openEventDetails(String eventCode) {
        Intent intent = new Intent(EventsActivity.this, EventDetailsActivity.class);
        intent.putExtra("event", eventCode);
        startActivity(intent);
    }
}