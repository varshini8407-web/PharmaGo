package com.example.pharmago;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.pharmago.api.ApiService;
import com.example.pharmago.api.RetrofitClient;
import com.example.pharmago.models.Pharmacy;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    EditText etSearchPharmacy;
    Button btnSearch;
    LinearLayout pharmacyContainer;
    ProgressBar progressBar;
    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        etSearchPharmacy = findViewById(R.id.etSearchPharmacy);
        btnSearch = findViewById(R.id.btnSearch);
        pharmacyContainer = findViewById(R.id.pharmacyContainer);
        progressBar = findViewById(R.id.progressBar);

        // Using auth client as /api/pharmacies requires JWT
        apiService = RetrofitClient.getClientWithAuth(this).create(ApiService.class);

        btnSearch.setOnClickListener(v -> {
            String searchText = etSearchPharmacy.getText().toString().trim();
            loadPharmacies(searchText);
        });

        loadPharmacies(null);
    }

    private void loadPharmacies(String query) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        pharmacyContainer.removeAllViews();
        
        apiService.getAllPharmacies().enqueue(new Callback<List<Pharmacy>>() {
            @Override
            public void onResponse(Call<List<Pharmacy>> call, Response<List<Pharmacy>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    displayPharmacies(response.body(), query);
                } else {
                    String error = "Failed to load pharmacies";
                    if (response.code() == 401) error = "Session expired. Please login again.";
                    Toast.makeText(SearchActivity.this, error, Toast.LENGTH_SHORT).show();
                    if (response.code() == 401) {
                        Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Pharmacy>> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(SearchActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayPharmacies(List<Pharmacy> pharmacies, String query) {
        pharmacyContainer.removeAllViews();
        boolean found = false;

        for (Pharmacy pharmacy : pharmacies) {
            if (query != null && !query.isEmpty()) {
                String q = query.toLowerCase();
                if (!pharmacy.getName().toLowerCase().contains(q) &&
                    !pharmacy.getAddress().toLowerCase().contains(q)) {
                    continue;
                }
            }
            addPharmacy(pharmacy);
            found = true;
        }

        if (!found) {
            TextView noResult = new TextView(this);
            noResult.setText("No pharmacies available at the moment.");
            noResult.setTextSize(16);
            noResult.setPadding(20, 40, 20, 40);
            noResult.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            pharmacyContainer.addView(noResult);
        }
    }

    private void addPharmacy(Pharmacy pharmacy) {
        View card = getLayoutInflater().inflate(R.layout.item_pharmacy, pharmacyContainer, false);
        
        TextView name = card.findViewById(R.id.tvPharmacyName);
        TextView address = card.findViewById(R.id.tvPharmacyAddress);
        TextView phone = card.findViewById(R.id.tvPharmacyPhone);
        TextView status = card.findViewById(R.id.tvPharmacyStatus);
        Button btnViewMedicines = card.findViewById(R.id.btnViewMedicines);

        name.setText(pharmacy.getName());
        address.setText("📍 " + pharmacy.getAddress());
        phone.setText("📞 " + (pharmacy.getPhoneNumber() != null ? pharmacy.getPhoneNumber() : "N/A"));
        status.setText(pharmacy.isActive() ? "Active" : "Inactive");
        status.setTextColor(ContextCompat.getColor(this, 
                pharmacy.isActive() ? android.R.color.holo_green_dark : android.R.color.holo_red_dark));

        btnViewMedicines.setOnClickListener(v -> {
            Intent intent = new Intent(SearchActivity.this, MedicineSearchActivity.class);
            intent.putExtra("pharmacyId", pharmacy.getId());
            intent.putExtra("pharmacyName", pharmacy.getName());
            startActivity(intent);
        });

        pharmacyContainer.addView(card);
    }
}