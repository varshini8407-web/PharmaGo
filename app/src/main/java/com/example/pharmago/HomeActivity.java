package com.example.pharmago;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    Button btnPharmacy, btnMedicine, btnPrescription, btnDelivery;
    Button navSearch, navOrders, navProfile;
    TextView btnNotification, btnProfile, tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnPharmacy = findViewById(R.id.btnPharmacy);
        btnMedicine = findViewById(R.id.btnMedicine);
        btnPrescription = findViewById(R.id.btnPrescription);
        btnDelivery = findViewById(R.id.btnDelivery);

        navSearch = findViewById(R.id.navSearch);
        navOrders = findViewById(R.id.navOrders);
        navProfile = findViewById(R.id.navProfile);

        btnNotification = findViewById(R.id.btnNotification);
        btnProfile = findViewById(R.id.btnProfile);
        tvWelcome = findViewById(R.id.tvWelcome);

        SharedPreferences prefs = getSharedPreferences("PharmaGoPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", "User");
        tvWelcome.setText(getString(R.string.hello_user, username));

        btnPharmacy.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, SearchActivity.class)));
        btnMedicine.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, MedicineSearchActivity.class)));
        btnPrescription.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, PrescriptionUploadActivity.class)));
        btnDelivery.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, OrderTypeActivity.class)));

        btnProfile.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));
        btnNotification.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, NotificationActivity.class)));

        navSearch.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, SearchActivity.class)));
        navOrders.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, OrderHistoryActivity.class)));
        navProfile.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));
    }
}