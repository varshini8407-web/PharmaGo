package com.example.pharmago;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText etPhone, etPassword;
    Button btnLogin;
    TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> {

            String phone = etPhone.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (phone.isEmpty()) {
                etPhone.setError("Enter phone number");
                etPhone.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                etPassword.setError("Enter password");
                etPassword.requestFocus();
                return;
            }

            Toast.makeText(
                    MainActivity.this,
                    "Login successful",
                    Toast.LENGTH_SHORT
            ).show();

            Intent intent = new Intent(
                    MainActivity.this,
                    HomeActivity.class
            );

            startActivity(intent);
        });

        tvRegister.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    RegisterActivity.class
            );

            startActivity(intent);
        });
    }
}