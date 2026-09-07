package com.example.pharmago;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    Button btnPharmacy;
    Button btnMedicine;
    Button btnPrescription;
    Button btnDelivery;

    Button navSearch;
    Button navOrders;
    Button navProfile;

    TextView btnNotification;
    TextView btnProfile;

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

        // Find Pharmacy
        btnPharmacy.setOnClickListener(v -> {
            Intent intent = new Intent(
                    HomeActivity.this,
                    SearchActivity.class
            );
            startActivity(intent);
        });

        // Search Medicine
        btnMedicine.setOnClickListener(v -> {
            Intent intent = new Intent(
                    HomeActivity.this,
                    MedicineSearchActivity.class
            );
            startActivity(intent);
        });

        // Prescription
        btnPrescription.setOnClickListener(v -> {
            Intent intent = new Intent(
                    HomeActivity.this,
                    PrescriptionUploadActivity.class
            );
            startActivity(intent);
        });

        // Delivery
        btnDelivery.setOnClickListener(v -> {
            Intent intent = new Intent(
                    HomeActivity.this,
                    OrderTypeActivity.class
            );
            startActivity(intent);
        });

        // Profile
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(
                    HomeActivity.this,
                    ProfileActivity.class
            );
            startActivity(intent);
        });

        // Notifications
        btnNotification.setOnClickListener(v -> {
            Intent intent = new Intent(
                    HomeActivity.this,
                    NotificationActivity.class
            );
            startActivity(intent);
        });

        // Bottom navigation
        navSearch.setOnClickListener(v -> {
            Intent intent = new Intent(
                    HomeActivity.this,
                    SearchActivity.class
            );
            startActivity(intent);
        });

        navOrders.setOnClickListener(v -> {
            Intent intent = new Intent(
                    HomeActivity.this,
                    OrderHistoryActivity.class
            );
            startActivity(intent);
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(
                    HomeActivity.this,
                    ProfileActivity.class
            );
            startActivity(intent);
        });
    }
}