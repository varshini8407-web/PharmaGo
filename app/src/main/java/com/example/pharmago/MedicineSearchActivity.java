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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.pharmago.api.ApiService;
import com.example.pharmago.api.RetrofitClient;
import com.example.pharmago.models.Medicine;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MedicineSearchActivity extends AppCompatActivity {

    EditText etSearchMedicine;
    Button btnSearchMedicine;
    LinearLayout medicineContainer;
    ProgressBar progressBar;
    String pharmacyName;
    Long pharmacyId;
    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_search);

        etSearchMedicine = findViewById(R.id.etSearchMedicine);
        btnSearchMedicine = findViewById(R.id.btnSearchMedicine);
        medicineContainer = findViewById(R.id.medicineContainer);
        progressBar = findViewById(R.id.progressBar);

        pharmacyName = getIntent().getStringExtra("pharmacyName");
        pharmacyId = getIntent().getLongExtra("pharmacyId", -1L);

        TextView pharmacyTitle = findViewById(R.id.tvPharmacyName);
        pharmacyTitle.setText(pharmacyName != null ? pharmacyName : "All Medicines");

        apiService = RetrofitClient.getClientWithAuth(this).create(ApiService.class);

        btnSearchMedicine.setOnClickListener(v -> {
            String searchText = etSearchMedicine.getText().toString().trim();
            loadMedicines(searchText);
        });

        loadMedicines(null);
    }

    private void loadMedicines(String query) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        medicineContainer.removeAllViews();
        
        Call<List<Medicine>> call;
        if (query != null && !query.isEmpty()) {
            call = apiService.searchMedicines(query);
        } else {
            call = apiService.getAllMedicines();
        }

        call.enqueue(new Callback<List<Medicine>>() {
            @Override
            public void onResponse(@NonNull Call<List<Medicine>> call, @NonNull Response<List<Medicine>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    displayMedicines(response.body());
                } else {
                    Toast.makeText(MedicineSearchActivity.this, "Failed to load medicines", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Medicine>> call, @NonNull Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(MedicineSearchActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayMedicines(List<Medicine> medicines) {
        medicineContainer.removeAllViews();
        if (medicines.isEmpty()) {
            TextView noResult = new TextView(this);
            noResult.setText("No medicines found.");
            noResult.setTextSize(16);
            noResult.setPadding(20, 40, 20, 40);
            noResult.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            medicineContainer.addView(noResult);
            return;
        }

        for (Medicine medicine : medicines) {
            addMedicine(medicine);
        }
    }

    private void addMedicine(Medicine medicine) {
        View card = getLayoutInflater().inflate(R.layout.item_medicine, medicineContainer, false);
        
        TextView name = card.findViewById(R.id.tvMedicineName);
        TextView desc = card.findViewById(R.id.tvMedicineDesc);
        TextView category = card.findViewById(R.id.tvMedicineCategory);
        TextView prescription = card.findViewById(R.id.tvPrescriptionStatus);
        Button selectButton = card.findViewById(R.id.btnSelectMedicine);

        name.setText(medicine.getName());
        desc.setText(medicine.getDescription());
        category.setText(String.format("Category: %s", medicine.getCategory()));
        
        if (medicine.isRequiresPrescription()) {
            prescription.setText("⚠️ Prescription Required");
            prescription.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        } else {
            prescription.setText("✅ Over the Counter");
            prescription.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        }

        selectButton.setOnClickListener(v -> {
            Intent intent = new Intent(MedicineSearchActivity.this, OrderTypeActivity.class);
            intent.putExtra("medicineId", medicine.getId());
            intent.putExtra("medicineName", medicine.getName());
            intent.putExtra("pharmacyName", pharmacyName);
            intent.putExtra("pharmacyId", pharmacyId);
            intent.putExtra("price", medicine.getPrice());
            intent.putExtra("requiresPrescription", medicine.isRequiresPrescription());
            startActivity(intent);
        });

        medicineContainer.addView(card);
    }
}