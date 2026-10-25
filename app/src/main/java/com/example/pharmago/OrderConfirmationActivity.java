package com.example.pharmago;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pharmago.api.ApiService;
import com.example.pharmago.api.RetrofitClient;
import com.example.pharmago.models.OrderRequest;
import com.example.pharmago.models.OrderResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderConfirmationActivity extends AppCompatActivity {

    private static final String TAG = "OrderConfirmation";

    TextView tvConfirmationMessage, tvConfirmedOrderDetails;
    Button btnContinueOrder, btnViewOrderHistory;
    ProgressBar progressBar;

    String medicineName, pharmacyName, price, orderType, paymentMethod, deliveryAddress;
    Long medicineId, pharmacyId, prescriptionId, serverOrderId;
    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        tvConfirmationMessage = findViewById(R.id.tvConfirmationMessage);
        tvConfirmedOrderDetails = findViewById(R.id.tvConfirmedOrderDetails);
        btnContinueOrder = findViewById(R.id.btnContinueOrder);
        btnViewOrderHistory = findViewById(R.id.btnViewOrderHistory);
        progressBar = findViewById(R.id.progressBar);

        apiService = RetrofitClient.getClientWithAuth(this).create(ApiService.class);

        Intent intent = getIntent();
        if (intent != null) {
            medicineName = intent.getStringExtra("medicineName");
            pharmacyName = intent.getStringExtra("pharmacyName");
            price = intent.getStringExtra("price");
            orderType = intent.getStringExtra("orderType");
            paymentMethod = intent.getStringExtra("paymentMethod");
            deliveryAddress = intent.getStringExtra("deliveryAddress");
            medicineId = intent.getLongExtra("medicineId", -1L);
            pharmacyId = intent.getLongExtra("pharmacyId", -1L);
            
            if (intent.hasExtra("prescriptionId")) {
                long pId = intent.getLongExtra("prescriptionId", -1L);
                prescriptionId = (pId == -1L) ? null : pId;
            }
        }

        tvConfirmationMessage.setText(R.string.processing_order);
        btnContinueOrder.setEnabled(false);

        String details = getString(R.string.order_details_format,
                medicineName != null ? medicineName : "N/S",
                pharmacyName != null ? pharmacyName : "N/S",
                orderType != null ? orderType : "N/S",
                price != null ? price : "N/S",
                paymentMethod != null ? paymentMethod : "N/S");
        tvConfirmedOrderDetails.setText(details);

        placeOrderOnServer();

        btnContinueOrder.setOnClickListener(v -> {
            Class<?> destination =
                    "TAKEAWAY".equalsIgnoreCase(orderType)
                            ? PickupQRActivity.class
                            : DeliveryTrackingActivity.class;

            Intent nextIntent = new Intent(OrderConfirmationActivity.this, destination);
            nextIntent.putExtra("orderId", serverOrderId);
            nextIntent.putExtra("medicineName", medicineName);
            nextIntent.putExtra("pharmacyName", pharmacyName);
            nextIntent.putExtra("price", price);
            startActivity(nextIntent);
        });

        btnViewOrderHistory.setOnClickListener(v ->
                startActivity(new Intent(OrderConfirmationActivity.this, OrderHistoryActivity.class))
        );
    }

    private void placeOrderOnServer() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        List<OrderRequest.OrderItemRequest> items = new ArrayList<>();
        if (medicineId != null && medicineId != -1L) {
            items.add(new OrderRequest.OrderItemRequest(medicineId, 1));
        }

        // Use deliveryAddress passed from Summary if available, else fallback to user profile address
        String finalAddress = deliveryAddress;
        if (finalAddress == null || finalAddress.isEmpty()) {
            SharedPreferences userPrefs = getSharedPreferences("PharmaGoPrefs", MODE_PRIVATE);
            finalAddress = userPrefs.getString("userAddress", null);
        }

        // Normalize order type for backend Enum (TAKEAWAY / DELIVERY)
        String backendOrderType = "TAKEAWAY";
        if (orderType != null && (orderType.equalsIgnoreCase("DELIVERY") || orderType.toLowerCase().contains("delivery"))) {
            backendOrderType = "DELIVERY";
        }

        OrderRequest orderRequest = new OrderRequest(
                pharmacyId != null && pharmacyId != -1L ? pharmacyId : null,
                items,
                backendOrderType,
                prescriptionId,
                finalAddress
        );

        apiService.placeOrder(orderRequest).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderResponse> call, @NonNull Response<OrderResponse> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                btnContinueOrder.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    serverOrderId = response.body().getId();
                    tvConfirmationMessage.setText(R.string.order_success_msg);
                    saveOrderLocally("Confirmed", serverOrderId);
                } else {
                    tvConfirmationMessage.setText(getString(R.string.order_failed_server, response.code()));
                    saveOrderLocally("Failed on Server", null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<OrderResponse> call, @NonNull Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                btnContinueOrder.setEnabled(true);
                tvConfirmationMessage.setText(R.string.order_connection_error);
                Toast.makeText(OrderConfirmationActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Order placement failed", t);
            }
        });
    }

    private void saveOrderLocally(String status, Long sOrderId) {
        SharedPreferences preferences = getSharedPreferences("PharmaGoOrders", MODE_PRIVATE);
        try {
            JSONArray orders = new JSONArray(preferences.getString("orders", "[]"));
            JSONObject order = new JSONObject();
            order.put("medicineName", medicineName == null ? "Medicine" : medicineName);
            order.put("pharmacyName", pharmacyName == null ? "Pharmacy" : pharmacyName);
            order.put("price", price == null ? "N/A" : price);
            order.put("orderType", orderType == null ? "TAKEAWAY" : orderType);
            order.put("paymentMethod", paymentMethod == null ? "N/A" : paymentMethod);
            order.put("status", status);
            order.put("orderId", sOrderId != null ? sOrderId.toString() : "PG-" + System.currentTimeMillis());
            orders.put(order);
            preferences.edit().putString("orders", orders.toString()).apply();
        } catch (Exception e) {
            Log.e(TAG, "Failed to save order locally", e);
        }
    }
}