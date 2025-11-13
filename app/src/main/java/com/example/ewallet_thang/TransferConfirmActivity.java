package com.example.ewallet_thang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ewallet_thang.database.DatabaseHelper;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TransferConfirmActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvRecipientName, tvRecipientEmail;
    private EditText etAmount;
    private Spinner spinnerCategory;
    private Button btnTransfer;

    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;
    private int currentUserId;
    private int recipientId;
    private String recipientName;
    private String recipientEmail;
    private double currentBalance;
    private String selectedCategory = "";
    private NumberFormat currencyFormat;

    private String[] categories = {
            "--Phân loại--",
            "Ăn uống",
            "Học tập",
            "Xăng xe",
            "Thuê nhà",
            "Mua sắm",
            "Giải trí",
            "Y tế",
            "Du lịch",
            "Khác"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_confirm);

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
        currentUserId = sharedPreferences.getInt("userId", -1);
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        // Get data from intent
        Intent intent = getIntent();
        recipientId = intent.getIntExtra("recipientId", -1);
        recipientName = intent.getStringExtra("recipientName");
        recipientEmail = intent.getStringExtra("recipientEmail");
        currentBalance = intent.getDoubleExtra("currentBalance", 0);

        initViews();
        setupListeners();
        setupCategorySpinner();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvRecipientName = findViewById(R.id.tvRecipientName);
        tvRecipientEmail = findViewById(R.id.tvRecipientEmail);
        etAmount = findViewById(R.id.etAmount);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnTransfer = findViewById(R.id.btnTransfer);

        tvRecipientName.setText(recipientName.toUpperCase());
        tvRecipientEmail.setText("số tiền");

        // Set default amount
        etAmount.setText("500000");
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateAmount();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnTransfer.setOnClickListener(v -> processTransfer());
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectedCategory = categories[position];
                } else {
                    selectedCategory = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategory = "";
            }
        });
    }

    private boolean validateAmount() {
        String amountStr = etAmount.getText().toString().trim();

        if (amountStr.isEmpty()) {
            btnTransfer.setEnabled(false);
            return false;
        }

        try {
            double amount = Double.parseDouble(amountStr);

            if (amount <= 0) {
                Toast.makeText(this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                btnTransfer.setEnabled(false);
                return false;
            }

            if (amount > currentBalance) {
                Toast.makeText(this, "Số dư không đủ để chuyển", Toast.LENGTH_SHORT).show();
                btnTransfer.setEnabled(false);
                return false;
            }

            btnTransfer.setEnabled(true);
            return true;

        } catch (NumberFormatException e) {
            btnTransfer.setEnabled(false);
            return false;
        }
    }

    private void processTransfer() {
        if (!validateAmount()) {
            return;
        }

        if (selectedCategory.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn phân loại", Toast.LENGTH_SHORT).show();
            return;
        }

        String amountStr = etAmount.getText().toString().trim();
        double amount = Double.parseDouble(amountStr);

        // Lấy thời gian hiện tại (dùng cho hiển thị sau)
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());

        // Ghi giao dịch cho người gửi (chi tiêu)
        String description = "Chuyển tiền tới " + recipientName;
        long senderTransactionId = dbHelper.addTransaction(
                currentUserId,
                "EXPENSE",
                amount,
                description,
                selectedCategory
        );

        // Ghi giao dịch cho người nhận (thu nhập)
        String firstName = sharedPreferences.getString("firstName", "");
        String lastName = sharedPreferences.getString("lastName", "");
        String senderFullName = firstName + " " + lastName;
        String recipientDescription = "Nhận tiền từ " + senderFullName;

        long recipientTransactionId = dbHelper.addTransaction(
                recipientId,
                "INCOME",
                amount,
                recipientDescription,
                selectedCategory
        );

        // Kiểm tra kết quả
        if (senderTransactionId > 0 && recipientTransactionId > 0) {
            // Cập nhật số dư trong SharedPreferences
            double newBalance = currentBalance - amount;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat("balance", (float) newBalance);
            editor.apply();

            // Chuyển đến màn hình thành công
            Intent intent = new Intent(TransferConfirmActivity.this, TransferSuccessActivity.class);
            intent.putExtra("recipientName", recipientName);
            intent.putExtra("amount", amount);
            intent.putExtra("date", currentDate);
            intent.putExtra("category", selectedCategory);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Chuyển tiền thất bại. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
        }
    }
}