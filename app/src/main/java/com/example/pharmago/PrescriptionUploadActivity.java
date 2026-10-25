package com.example.pharmago;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pharmago.api.ApiService;
import com.example.pharmago.api.RetrofitClient;
import com.example.pharmago.models.PrescriptionResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrescriptionUploadActivity extends AppCompatActivity {

    TextView tvOrderDetails, tvPrescriptionMessage;
    Button btnUploadPrescription, btnContinue;
    ProgressBar progressBar;

    String medicineName, pharmacyName, price, orderType;
    Long medicineId, pharmacyId, prescriptionId = null;
    boolean requiresPrescription;
    Uri prescriptionUri = null;
    ApiService apiService;

    ActivityResultLauncher<String[]> prescriptionPicker =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    uri -> {
                        if (uri != null) {
                            prescriptionUri = uri;
                            tvPrescriptionMessage.setText("Selected: " + getFileName(uri));
                            btnContinue.setEnabled(true);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription_upload);

        tvOrderDetails = findViewById(R.id.tvOrderDetails);
        tvPrescriptionMessage = findViewById(R.id.tvPrescriptionMessage);
        btnUploadPrescription = findViewById(R.id.btnUploadPrescription);
        btnContinue = findViewById(R.id.btnContinue);
        progressBar = findViewById(R.id.progressBar);

        apiService = RetrofitClient.getClientWithAuth(this).create(ApiService.class);

        Intent intent = getIntent();
        if (intent != null) {
            medicineName = intent.getStringExtra("medicineName");
            pharmacyName = intent.getStringExtra("pharmacyName");
            price = intent.getStringExtra("price");
            orderType = intent.getStringExtra("orderType");
            medicineId = intent.getLongExtra("medicineId", -1L);
            pharmacyId = intent.getLongExtra("pharmacyId", -1L);
            requiresPrescription = intent.getBooleanExtra("requiresPrescription", false);
        }

        tvOrderDetails.setText(String.format("Medicine: %s\nPharmacy: %s\nType: %s", 
                medicineName, pharmacyName, orderType));

        if (requiresPrescription) {
            tvPrescriptionMessage.setText("A valid prescription is required.");
            btnContinue.setEnabled(false);
        } else {
            tvPrescriptionMessage.setText("Prescription not required for this item.");
            btnUploadPrescription.setVisibility(View.GONE);
            btnContinue.setEnabled(true);
        }

        btnUploadPrescription.setOnClickListener(v -> 
            prescriptionPicker.launch(new String[]{"image/*", "application/pdf"})
        );

        btnContinue.setOnClickListener(v -> {
            if (requiresPrescription && prescriptionUri != null && prescriptionId == null) {
                uploadPrescription();
            } else {
                navigateToSummary();
            }
        });
    }

    private void uploadPrescription() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        btnContinue.setEnabled(false);

        try {
            File file = uriToFile(prescriptionUri);
            String mimeType = getContentResolver().getType(prescriptionUri);
            if (mimeType == null) mimeType = "application/octet-stream";
            
            RequestBody requestFile = RequestBody.create(file, MediaType.parse(mimeType));
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            apiService.uploadPrescription(body).enqueue(new Callback<PrescriptionResponse>() {
                @Override
                public void onResponse(@NonNull Call<PrescriptionResponse> call, @NonNull Response<PrescriptionResponse> response) {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        prescriptionId = response.body().getId();
                        Toast.makeText(PrescriptionUploadActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                        navigateToSummary();
                    } else {
                        btnContinue.setEnabled(true);
                        Toast.makeText(PrescriptionUploadActivity.this, "Upload failed: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<PrescriptionResponse> call, @NonNull Throwable t) {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    btnContinue.setEnabled(true);
                    Toast.makeText(PrescriptionUploadActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            btnContinue.setEnabled(true);
            Toast.makeText(this, "Error processing file", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToSummary() {
        Intent intent = new Intent(this, OrderSummaryActivity.class);
        intent.putExtra("medicineName", medicineName);
        intent.putExtra("pharmacyName", pharmacyName);
        intent.putExtra("price", price);
        intent.putExtra("orderType", orderType);
        intent.putExtra("medicineId", medicineId);
        intent.putExtra("pharmacyId", pharmacyId);
        intent.putExtra("prescriptionId", prescriptionId);
        intent.putExtra("prescriptionRequired", requiresPrescription);
        intent.putExtra("prescriptionUploaded", prescriptionId != null);
        startActivity(intent);
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) result = cursor.getString(index);
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) result = result.substring(cut + 1);
        }
        return result;
    }

    private File uriToFile(Uri uri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        File tempFile = new File(getCacheDir(), getFileName(uri));
        try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[4096];
            int read;
            while (inputStream != null && (read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
        } finally {
            if (inputStream != null) inputStream.close();
        }
        return tempFile;
    }
}