package com.example.pharmago;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pharmago.api.ApiService;
import com.example.pharmago.api.RetrofitClient;
import com.example.pharmago.models.OrderResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryActivity extends AppCompatActivity {

    LinearLayout orderContainer;
    TextView tvEmptyHistory;
    ProgressBar progressBar;
    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        orderContainer = findViewById(R.id.orderContainer);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);
        progressBar = findViewById(R.id.progressBar);

        apiService = RetrofitClient.getClientWithAuth(this).create(ApiService.class);

        loadOrderHistoryFromServer();
    }

    private void loadOrderHistoryFromServer() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        orderContainer.removeAllViews();

        apiService.getMyOrders().enqueue(new Callback<List<OrderResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<OrderResponse>> call, @NonNull Response<List<OrderResponse>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<OrderResponse> orders = response.body();
                    if (orders.isEmpty()) {
                        tvEmptyHistory.setVisibility(View.VISIBLE);
                    } else {
                        tvEmptyHistory.setVisibility(View.GONE);
                        for (OrderResponse order : orders) {
                            addOrderCard(order);
                        }
                    }
                } else if (response.code() == 401) {
                    Toast.makeText(OrderHistoryActivity.this, R.string.session_expired, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(OrderHistoryActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(OrderHistoryActivity.this, "Failed to load orders: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<OrderResponse>> call, @NonNull Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(OrderHistoryActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                tvEmptyHistory.setText("Unable to connect to server.");
                tvEmptyHistory.setVisibility(View.VISIBLE);
            }
        });
    }

    private void addOrderCard(OrderResponse order) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(30, 25, 30, 25);
        card.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
        card.setElevation(4);
        card.setClickable(true);
        card.setFocusable(true);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 24);
        card.setLayoutParams(params);

        addText(card, "Order #" + order.getId(), 20, 0xFF176B4D, true);
        addText(card, "Status: " + order.getStatus(), 16, 0xFF202124, true);
        addText(card, "Type: " + order.getType(), 14, 0xFF5F6368, false);
        addText(card, "Date: " + order.getOrderDate(), 14, 0xFF5F6368, false);

        card.setOnClickListener(v -> {
            if ("DELIVERY".equalsIgnoreCase(order.getType())) {
                Intent intent = new Intent(OrderHistoryActivity.this, DeliveryTrackingActivity.class);
                intent.putExtra("orderId", order.getId());
                startActivity(intent);
            } else {
                Intent intent = new Intent(OrderHistoryActivity.this, PickupQRActivity.class);
                intent.putExtra("orderId", order.getId());
                // Pass some basic info if available to avoid extra fetch if not needed
                intent.putExtra("pharmacyName", order.getPharmacyName());
                intent.putExtra("price", order.getTotalPrice() != null ? "₹" + order.getTotalPrice() : "N/A");
                startActivity(intent);
            }
        });

        orderContainer.addView(card);
    }

    private void addText(LinearLayout parent, String text, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setGravity(Gravity.START);
        view.setPadding(0, 5, 0, 5);
        if (bold) view.setTypeface(null, android.graphics.Typeface.BOLD);
        parent.addView(view);
    }
}