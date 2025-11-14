package com.example.ewallet_thang;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.Locale;

public class TransferSuccessActivity extends AppCompatActivity {

    private TextView tvRecipientName, tvAmount, tvDate, tvCategory;
    private Button btnViewTransaction, btnBackToHome;
    private NumberFormat currencyFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_success);

        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        initViews();
        loadTransferData();
        setupListeners();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToMain();
            }
        });
    }

    private void initViews() {
        tvRecipientName = findViewById(R.id.tvRecipientName);
        tvAmount = findViewById(R.id.tvAmount);
        tvDate = findViewById(R.id.tvDate);
        tvCategory = findViewById(R.id.tvCategory);
        btnViewTransaction = findViewById(R.id.btnViewTransaction);
        btnBackToHome = findViewById(R.id.btnBackToHome);
    }

    private void loadTransferData() {
        Intent intent = getIntent();
        String recipientName = intent.getStringExtra("recipientName");
        double amount = intent.getDoubleExtra("amount", 0);
        String date = intent.getStringExtra("date");
        String category = intent.getStringExtra("category");

        tvRecipientName.setText("tới " + recipientName.toUpperCase());
        tvAmount.setText(currencyFormat.format(amount));
        tvDate.setText(date);
        tvCategory.setText(category);
    }

    private void setupListeners() {
        btnViewTransaction.setOnClickListener(v -> navigateToMain());
        btnBackToHome.setOnClickListener(v -> navigateToMain());
    }

    private void navigateToMain() {
        Intent intent = new Intent(TransferSuccessActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
