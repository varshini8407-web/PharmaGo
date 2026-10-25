package com.example.pharmago;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.pharmago.api.ApiService;
import com.example.pharmago.api.RetrofitClient;
import com.example.pharmago.models.DeliveryResponse;
import com.example.pharmago.models.OrderResponse;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeliveryTrackingActivity extends AppCompatActivity {

    TextView tvDeliveryStatus, tvDeliveryDetails;
    TextView tvOrderConfirmed, tvPreparing;
    TextView tvOutForDelivery, tvDelivered;
    Button btnDeliveryHome;
    ProgressBar progressBar;
    ApiService apiService;
    Long orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_tracking);

        tvDeliveryStatus = findViewById(R.id.tvDeliveryStatus);
        tvDeliveryDetails = findViewById(R.id.tvDeliveryDetails);
        tvOrderConfirmed = findViewById(R.id.tvOrderConfirmed);
        tvPreparing = findViewById(R.id.tvPreparing);
        tvOutForDelivery = findViewById(R.id.tvOutForDelivery);
        tvDelivered = findViewById(R.id.tvDelivered);
        btnDeliveryHome = findViewById(R.id.btnDeliveryHome);
        progressBar = findViewById(R.id.progressBar);

        apiService = RetrofitClient.getClientWithAuth(this).create(ApiService.class);

        orderId = getIntent().getLongExtra("orderId", -1L);

        if (orderId != -1L) {
            fetchOrderDetails();
            fetchTrackingDetails();
        } else {
            tvDeliveryStatus.setText("Demo Tracking Mode");
            tvDeliveryDetails.setText("No active order ID found for tracking.");
        }

        btnDeliveryHome.setOnClickListener(v -> {
            Intent homeIntent = new Intent(DeliveryTrackingActivity.this, HomeActivity.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(homeIntent);
            finish();
        });
    }

    private void fetchOrderDetails() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        apiService.getOrderDetails(orderId).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderResponse> call, @NonNull Response<OrderResponse> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    OrderResponse order = response.body();
                    StringBuilder itemDetails = new StringBuilder();
                    itemDetails.append("Pharmacy: ").append(order.getPharmacyName()).append("\n");
                    if (order.getItems() != null) {
                        for (OrderResponse.OrderItem item : order.getItems()) {
                            itemDetails.append("- ").append(item.getMedicineName()).append(" x").append(item.getQuantity()).append("\n");
                        }
                    }
                    itemDetails.append("Total: ₹").append(order.getTotalPrice());
                    tvDeliveryDetails.setText(itemDetails.toString());
                } else if (response.code() == 401) {
                    handleUnauthorized();
                }
            }

            @Override
            public void onFailure(@NonNull Call<OrderResponse> call, @NonNull Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void fetchTrackingDetails() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        apiService.getDeliveryTracking(orderId).enqueue(new Callback<DeliveryResponse>() {
            @Override
            public void onResponse(@NonNull Call<DeliveryResponse> call, @NonNull Response<DeliveryResponse> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    updateTrackingUI(response.body());
                } else if (response.code() == 404) {
                    tvDeliveryStatus.setText("Tracking Pending");
                    tvDeliveryDetails.append("\n\nDelivery has not started yet.");
                } else if (response.code() == 401) {
                    handleUnauthorized();
                } else {
                    tvDeliveryStatus.setText("Tracking Status Unavailable");
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeliveryResponse> call, @NonNull Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(DeliveryTrackingActivity.this, "Tracking Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTrackingUI(DeliveryResponse delivery) {
        tvDeliveryStatus.setText(String.format(Locale.getDefault(), "Status: %s", delivery.getStatus()));
        
        String currentText = tvDeliveryDetails.getText().toString();
        String addressInfo = String.format(Locale.getDefault(), "\n\nDelivery Address: %s\nEst. Arrival: %s", 
                delivery.getDeliveryAddress(), delivery.getEstimatedArrival());
        tvDeliveryDetails.setText(currentText + addressInfo);

        String status = delivery.getStatus().toUpperCase();

        resetStep(tvOrderConfirmed, "○ Order Confirmed");
        resetStep(tvPreparing, "○ Preparing Your Order");
        resetStep(tvOutForDelivery, "○ Out for Delivery");
        resetStep(tvDelivered, "○ Delivered");

        if (status.contains("CONFIRMED") || status.contains("PREPARING") || status.contains("OUT") || status.contains("DELIVERED")) {
            markActive(tvOrderConfirmed, "✓ Order Confirmed");
        }
        
        if (status.contains("PREPARING") || status.contains("OUT") || status.contains("DELIVERED")) {
            markActive(tvPreparing, "● Preparing Your Order");
        }

        if (status.contains("OUT") || status.contains("DELIVERED")) {
            markActive(tvOutForDelivery, "● Out for Delivery");
        }

        if (status.contains("DELIVERED")) {
            markActive(tvDelivered, "✓ Delivered");
        }
    }

    private void handleUnauthorized() {
        Toast.makeText(this, R.string.session_expired, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void markActive(TextView view, String text) {
        view.setText(text);
        view.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        view.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void resetStep(TextView view, String text) {
        view.setText(text);
        view.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        view.setTypeface(null, android.graphics.Typeface.NORMAL);
    }
}