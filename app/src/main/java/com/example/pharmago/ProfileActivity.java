package com.example.pharmago;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    TextView tvProfileUsername, tvProfileEmail, tvProfileId;
    Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvProfileUsername = findViewById(R.id.tvProfileUsername);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfileId = findViewById(R.id.tvProfileId);
        btnLogout = findViewById(R.id.btnLogout);

        // Load data from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("PharmaGoPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", "Not available");
        String email = prefs.getString("email", "Not available");
        long userId = prefs.getLong("userId", -1L);

        tvProfileUsername.setText(username);
        tvProfileEmail.setText(email);
        tvProfileId.setText(userId == -1L ? "N/A" : String.valueOf(userId));

        btnLogout.setOnClickListener(v -> {
            // Clear preferences and go to Login
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}