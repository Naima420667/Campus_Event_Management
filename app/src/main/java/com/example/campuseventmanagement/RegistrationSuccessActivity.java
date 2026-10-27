package com.example.campuseventmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class RegistrationSuccessActivity extends AppCompatActivity {

    Button myRegistrationsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_success);

        myRegistrationsButton = findViewById(R.id.myRegistrationsButton);

        myRegistrationsButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    RegistrationSuccessActivity.this,
                    MyRegistrationsActivity.class
            );

            startActivity(intent);
        });
    }
}