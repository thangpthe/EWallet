//package com.example.ewallet_thang;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.view.View;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.Spinner;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.ewallet_thang.database.DatabaseHelper;
//
//import java.text.NumberFormat;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Locale;
//
//public class TopUpActivity extends AppCompatActivity {
//
//    private TextView tvCurrentBalance;
//    private Spinner spinnerBank;
//    private EditText etAmount;
//    private Button btnTopUp;
//    private ImageView btnBack;
//
//    private DatabaseHelper dbHelper;
//    private SharedPreferences sharedPreferences;
//    private int userId;
//    private double currentBalance;
//    private NumberFormat currencyFormat;
//
//    // Danh sách ngân hàng
//    private String[] banks = {
//            "--Ngân hàng--",
//            "Vietcombank",
//            "MB Bank",
//            "Vietinbank",
//            "TP Bank",
//            "Techcombank",
//            "ACB",
//            "VPBank",
//            "Agribank",
//            "BIDV",
//            "Sacombank"
//    };
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_topup);
//
//        dbHelper = new DatabaseHelper(this);
//        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
//        userId = sharedPreferences.getInt("userId", -1);
//        currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
//
//        initViews();
//        loadCurrentBalance();
//        setupListeners();
//    }
//
//    private void initViews() {
//        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
//        spinnerBank = findViewById(R.id.spinnerBank);
//        etAmount = findViewById(R.id.etAmount);
//        btnTopUp = findViewById(R.id.btnTopUp);
//        btnBack = findViewById(R.id.btnBack);
//
//        // Setup spinner với danh sách ngân hàng
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(
//                this,
//                android.R.layout.simple_spinner_item,
//                banks
//        );
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerBank.setAdapter(adapter);
//
//        // Ẩn nút Nạp ban đầu
//        btnTopUp.setVisibility(View.GONE);
//    }
//
//    private void loadCurrentBalance() {
//        currentBalance = dbHelper.getBalance(userId);
//        tvCurrentBalance.setText(currencyFormat.format(currentBalance));
//    }
//
//    private void setupListeners() {
//        btnBack.setOnClickListener(v -> finish());
//
//        // Lắng nghe thay đổi trong EditText
//        etAmount.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                checkAndShowButton();
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {}
//        });
//
//        btnTopUp.setOnClickListener(v -> processTopUp());
//    }
//
//    private void checkAndShowButton() {
//        String selectedBank = spinnerBank.getSelectedItem().toString();
//        String amountStr = etAmount.getText().toString().trim();
//
//        // Hiện nút Nạp nếu đã chọn ngân hàng và nhập số tiền
//        if (!selectedBank.equals("--Ngân hàng--") && !amountStr.isEmpty()) {
//            btnTopUp.setVisibility(View.VISIBLE);
//        } else {
//            btnTopUp.setVisibility(View.GONE);
//        }
//    }
//
//    private void processTopUp() {
//        String selectedBank = spinnerBank.getSelectedItem().toString();
//        String amountStr = etAmount.getText().toString().trim();
//
//        // Kiểm tra ngân hàng
//        if (selectedBank.equals("--Ngân hàng--")) {
//            Toast.makeText(this, "Vui lòng chọn ngân hàng", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Kiểm tra số tiền
//        if (amountStr.isEmpty()) {
//            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        double amount;
//        try {
//            amount = Double.parseDouble(amountStr);
//        } catch (NumberFormatException e) {
//            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (amount <= 0) {
//            Toast.makeText(this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Tạo giao dịch nạp tiền
//        String description = "Nạp tiền từ " + selectedBank;
//        long result = dbHelper.addTransaction(userId, "DEPOSIT", amount, description, "Nạp tiền");
//        // Sau dòng: long result = dbHelper.addTransaction(...)
//        if (result != -1) {
//            // Tạo thông báo
//            dbHelper.addNotification(userId,
//                    "Nạp tiền thành công",
//                    "Bạn vừa nạp " + currencyFormat.format(amount) + " từ " + selectedBank,
//                    "TRANSACTION",
//                    (int) result);
//
//            // ... code tiếp theo
//        }
//        if (result != -1) {
//            // Lấy ngày hiện tại
//            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
//            String currentDate = sdf.format(new Date());
//
//            // Chuyển sang màn hình thành công
//            Intent intent = new Intent(TopUpActivity.this, TopUpSuccessActivity.class);
//            intent.putExtra("bank", selectedBank);
//            intent.putExtra("amount", amount);
//            intent.putExtra("date", currentDate);
//            startActivity(intent);
//            finish();
//        } else {
//            Toast.makeText(this, "Nạp tiền thất bại", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (dbHelper != null) {
//            dbHelper.close();
//        }
//    }
//}

package com.example.ewallet_thang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
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

public class TopUpActivity extends AppCompatActivity {

    private TextView tvCurrentBalance;
    private Spinner spinnerBank;
    private EditText etAmount;
    private Button btnTopUp;
    private ImageView btnBack;

    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;
    private int userId;
    private double currentBalance;
    private NumberFormat currencyFormat;

    // Danh sách ngân hàng
    private String[] banks = {
            "--Ngân hàng--",
            "Vietcombank",
            "MB Bank",
            "Vietinbank",
            "TP Bank",
            "Techcombank",
            "ACB",
            "VPBank",
            "Agribank",
            "BIDV",
            "Sacombank"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topup);

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getInt("userId", -1);
        currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

        initViews();
        loadCurrentBalance();
        setupListeners();
    }

    private void initViews() {
        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
        spinnerBank = findViewById(R.id.spinnerBank);
        etAmount = findViewById(R.id.etAmount);
        btnTopUp = findViewById(R.id.btnTopUp);
        btnBack = findViewById(R.id.btnBack);

        // Setup spinner với danh sách ngân hàng
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                banks
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBank.setAdapter(adapter);

        // Ẩn nút Nạp ban đầu
        btnTopUp.setVisibility(View.GONE);
    }

    private void loadCurrentBalance() {
        // ✅ LẤY SỐ DƯ TỪ DATABASE (nguồn đáng tin cậy nhất)
        currentBalance = dbHelper.getBalance(userId);

        // ✅ CẬP NHẬT SHAREDPREFERENCES
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("balance", (float) currentBalance);
        editor.apply();

        // ✅ HIỂN THỊ
        tvCurrentBalance.setText(currencyFormat.format(currentBalance));
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Lắng nghe thay đổi trong EditText
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkAndShowButton();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnTopUp.setOnClickListener(v -> processTopUp());
    }

    private void checkAndShowButton() {
        String selectedBank = spinnerBank.getSelectedItem().toString();
        String amountStr = etAmount.getText().toString().trim();

        // Hiện nút Nạp nếu đã chọn ngân hàng và nhập số tiền
        if (!selectedBank.equals("--Ngân hàng--") && !amountStr.isEmpty()) {
            btnTopUp.setVisibility(View.VISIBLE);
        } else {
            btnTopUp.setVisibility(View.GONE);
        }
    }

    private void processTopUp() {
        String selectedBank = spinnerBank.getSelectedItem().toString();
        String amountStr = etAmount.getText().toString().trim();

        // Kiểm tra ngân hàng
        if (selectedBank.equals("--Ngân hàng--")) {
            Toast.makeText(this, "Vui lòng chọn ngân hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra số tiền
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount <= 0) {
            Toast.makeText(this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo giao dịch nạp tiền
        String description = "Nạp tiền từ " + selectedBank;
        long result = dbHelper.addTransaction(userId, "DEPOSIT", amount, description, "Nạp tiền");

        if (result != -1) {
            // ✅ LẤY SỐ DƯ MỚI TỪ DATABASE SAU KHI NẠP
            double newBalance = dbHelper.getBalance(userId);

            // ✅ CẬP NHẬT VÀO SHAREDPREFERENCES
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat("balance", (float) newBalance);
            editor.apply();

            // Tạo thông báo
            dbHelper.addNotification(userId,
                    "Thu nhập",
                    currencyFormat.format(amount),
                    "INCOME",
                    (int) result);

            // Lấy ngày hiện tại
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String currentDate = sdf.format(new Date());

            // Chuyển sang màn hình thành công
            Intent intent = new Intent(TopUpActivity.this, TopUpSuccessActivity.class);
            intent.putExtra("bank", selectedBank);
            intent.putExtra("amount", amount);
            intent.putExtra("date", currentDate);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Nạp tiền thất bại", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCurrentBalance();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}