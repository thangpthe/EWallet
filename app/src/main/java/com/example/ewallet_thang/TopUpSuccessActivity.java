package com.example.ewallet_thang;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.Locale;

public class TopUpSuccessActivity extends AppCompatActivity {

    private TextView tvBank, tvAmount, tvDate;
    private Button btnBack;

    private String bank;
    private double amount;
    private String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topup_success);

        // Nhận dữ liệu từ Intent
        bank = getIntent().getStringExtra("bank");
        amount = getIntent().getDoubleExtra("amount", 0);
        date = getIntent().getStringExtra("date");

        initViews();
        displayTransactionInfo();
        setupListeners();

        // ✅ Xử lý nút back theo chuẩn mới
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToMain();
            }
        });
    }

    private void initViews() {
        tvBank = findViewById(R.id.tvBank);
        tvAmount = findViewById(R.id.tvAmount);
        tvDate = findViewById(R.id.tvDate);
        btnBack = findViewById(R.id.btnBackToHome);
    }

    private void displayTransactionInfo() {
        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvBank.setText("Ngân hàng " + bank);
        tvAmount.setText(currencyFormat.format(amount));
        tvDate.setText(date);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> navigateToMain());
    }

    private void navigateToMain() {
        Intent intent = new Intent(TopUpSuccessActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
