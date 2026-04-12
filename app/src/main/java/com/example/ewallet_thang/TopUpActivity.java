//////package com.example.ewallet_thang;
//////
//////import android.content.Intent;
//////import android.content.SharedPreferences;
//////import android.os.Bundle;
//////import android.text.Editable;
//////import android.text.TextWatcher;
//////import android.view.View;
//////import android.widget.ArrayAdapter;
//////import android.widget.Button;
//////import android.widget.EditText;
//////import android.widget.ImageView;
//////import android.widget.Spinner;
//////import android.widget.TextView;
//////import android.widget.Toast;
//////
//////import androidx.appcompat.app.AppCompatActivity;
//////
//////import com.example.ewallet_thang.database.DatabaseHelper;
//////
//////import java.text.NumberFormat;
//////import java.text.SimpleDateFormat;
//////import java.util.Date;
//////import java.util.Locale;
//////
//////public class TopUpActivity extends AppCompatActivity {
//////
//////    private TextView tvCurrentBalance;
//////    private Spinner spinnerBank;
//////    private EditText etAmount;
//////    private Button btnTopUp;
//////    private ImageView btnBack;
//////
//////    private DatabaseHelper dbHelper;
//////    private SharedPreferences sharedPreferences;
//////    private int userId;
//////    private double currentBalance;
//////    private NumberFormat currencyFormat;
//////
//////    // Danh sách ngân hàng
//////    private String[] banks = {
//////            "--Ngân hàng--",
//////            "Vietcombank",
//////            "MB Bank",
//////            "Vietinbank",
//////            "TP Bank",
//////            "Techcombank",
//////            "ACB",
//////            "VPBank",
//////            "Agribank",
//////            "BIDV",
//////            "Sacombank"
//////    };
//////
//////    @Override
//////    protected void onCreate(Bundle savedInstanceState) {
//////        super.onCreate(savedInstanceState);
//////        setContentView(R.layout.activity_topup);
//////
//////        dbHelper = new DatabaseHelper(this);
//////        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
//////        userId = sharedPreferences.getInt("userId", -1);
//////        currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
//////
//////        initViews();
//////        loadCurrentBalance();
//////        setupListeners();
//////    }
//////
//////    private void initViews() {
//////        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
//////        spinnerBank = findViewById(R.id.spinnerBank);
//////        etAmount = findViewById(R.id.etAmount);
//////        btnTopUp = findViewById(R.id.btnTopUp);
//////        btnBack = findViewById(R.id.btnBack);
//////
//////        // Setup spinner với danh sách ngân hàng
//////        ArrayAdapter<String> adapter = new ArrayAdapter<>(
//////                this,
//////                android.R.layout.simple_spinner_item,
//////                banks
//////        );
//////        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//////        spinnerBank.setAdapter(adapter);
//////
//////        // Ẩn nút Nạp ban đầu
//////        btnTopUp.setVisibility(View.GONE);
//////    }
//////
//////    private void loadCurrentBalance() {
//////        currentBalance = dbHelper.getBalance(userId);
//////        tvCurrentBalance.setText(currencyFormat.format(currentBalance));
//////    }
//////
//////    private void setupListeners() {
//////        btnBack.setOnClickListener(v -> finish());
//////
//////        // Lắng nghe thay đổi trong EditText
//////        etAmount.addTextChangedListener(new TextWatcher() {
//////            @Override
//////            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//////
//////            @Override
//////            public void onTextChanged(CharSequence s, int start, int before, int count) {
//////                checkAndShowButton();
//////            }
//////
//////            @Override
//////            public void afterTextChanged(Editable s) {}
//////        });
//////
//////        btnTopUp.setOnClickListener(v -> processTopUp());
//////    }
//////
//////    private void checkAndShowButton() {
//////        String selectedBank = spinnerBank.getSelectedItem().toString();
//////        String amountStr = etAmount.getText().toString().trim();
//////
//////        // Hiện nút Nạp nếu đã chọn ngân hàng và nhập số tiền
//////        if (!selectedBank.equals("--Ngân hàng--") && !amountStr.isEmpty()) {
//////            btnTopUp.setVisibility(View.VISIBLE);
//////        } else {
//////            btnTopUp.setVisibility(View.GONE);
//////        }
//////    }
//////
//////    private void processTopUp() {
//////        String selectedBank = spinnerBank.getSelectedItem().toString();
//////        String amountStr = etAmount.getText().toString().trim();
//////
//////        // Kiểm tra ngân hàng
//////        if (selectedBank.equals("--Ngân hàng--")) {
//////            Toast.makeText(this, "Vui lòng chọn ngân hàng", Toast.LENGTH_SHORT).show();
//////            return;
//////        }
//////
//////        // Kiểm tra số tiền
//////        if (amountStr.isEmpty()) {
//////            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
//////            return;
//////        }
//////
//////        double amount;
//////        try {
//////            amount = Double.parseDouble(amountStr);
//////        } catch (NumberFormatException e) {
//////            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
//////            return;
//////        }
//////
//////        if (amount <= 0) {
//////            Toast.makeText(this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
//////            return;
//////        }
//////
//////        // Tạo giao dịch nạp tiền
//////        String description = "Nạp tiền từ " + selectedBank;
//////        long result = dbHelper.addTransaction(userId, "DEPOSIT", amount, description, "Nạp tiền");
//////        // Sau dòng: long result = dbHelper.addTransaction(...)
//////        if (result != -1) {
//////            // Tạo thông báo
//////            dbHelper.addNotification(userId,
//////                    "Nạp tiền thành công",
//////                    "Bạn vừa nạp " + currencyFormat.format(amount) + " từ " + selectedBank,
//////                    "TRANSACTION",
//////                    (int) result);
//////
//////            // ... code tiếp theo
//////        }
//////        if (result != -1) {
//////            // Lấy ngày hiện tại
//////            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
//////            String currentDate = sdf.format(new Date());
//////
//////            // Chuyển sang màn hình thành công
//////            Intent intent = new Intent(TopUpActivity.this, TopUpSuccessActivity.class);
//////            intent.putExtra("bank", selectedBank);
//////            intent.putExtra("amount", amount);
//////            intent.putExtra("date", currentDate);
//////            startActivity(intent);
//////            finish();
//////        } else {
//////            Toast.makeText(this, "Nạp tiền thất bại", Toast.LENGTH_SHORT).show();
//////        }
//////    }
//////
//////    @Override
//////    protected void onDestroy() {
//////        super.onDestroy();
//////        if (dbHelper != null) {
//////            dbHelper.close();
//////        }
//////    }
//////}
////
////package com.example.ewallet_thang;
////
////import android.content.Intent;
////import android.content.SharedPreferences;
////import android.os.Bundle;
////import android.text.Editable;
////import android.text.TextWatcher;
////import android.view.View;
////import android.widget.ArrayAdapter;
////import android.widget.Button;
////import android.widget.EditText;
////import android.widget.ImageView;
////import android.widget.Spinner;
////import android.widget.TextView;
////import android.widget.Toast;
////
////import androidx.appcompat.app.AppCompatActivity;
////
////import com.example.ewallet_thang.database.DatabaseHelper;
////
////import java.text.NumberFormat;
////import java.text.SimpleDateFormat;
////import java.util.Date;
////import java.util.Locale;
////
////public class TopUpActivity extends AppCompatActivity {
////
////    private TextView tvCurrentBalance;
////    private Spinner spinnerBank;
////    private EditText etAmount;
////    private Button btnTopUp;
////    private ImageView btnBack;
////
////    private DatabaseHelper dbHelper;
////    private SharedPreferences sharedPreferences;
////    private int userId;
////    private double currentBalance;
////    private NumberFormat currencyFormat;
////
////    // Danh sách ngân hàng
////    private String[] banks = {
////            "--Ngân hàng--",
////            "Vietcombank",
////            "MB Bank",
////            "Vietinbank",
////            "TP Bank",
////            "Techcombank",
////            "ACB",
////            "VPBank",
////            "Agribank",
////            "BIDV",
////            "Sacombank"
////    };
////
////    @Override
////    protected void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////        setContentView(R.layout.activity_topup);
////
////        dbHelper = new DatabaseHelper(this);
////        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
////        userId = sharedPreferences.getInt("userId", -1);
////        currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
////
////        initViews();
////        loadCurrentBalance();
////        setupListeners();
////    }
////
////    private void initViews() {
////        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
////        spinnerBank = findViewById(R.id.spinnerBank);
////        etAmount = findViewById(R.id.etAmount);
////        btnTopUp = findViewById(R.id.btnTopUp);
////        btnBack = findViewById(R.id.btnBack);
////
////        // Setup spinner với danh sách ngân hàng
////        ArrayAdapter<String> adapter = new ArrayAdapter<>(
////                this,
////                android.R.layout.simple_spinner_item,
////                banks
////        );
////        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
////        spinnerBank.setAdapter(adapter);
////
////        // Ẩn nút Nạp ban đầu
////        btnTopUp.setVisibility(View.GONE);
////    }
////
////    private void loadCurrentBalance() {
////        // ✅ LẤY SỐ DƯ TỪ DATABASE (nguồn đáng tin cậy nhất)
////        currentBalance = dbHelper.getBalance(userId);
////
////        // ✅ CẬP NHẬT SHAREDPREFERENCES
////        SharedPreferences.Editor editor = sharedPreferences.edit();
////        editor.putFloat("balance", (float) currentBalance);
////        editor.apply();
////
////        // ✅ HIỂN THỊ
////        tvCurrentBalance.setText(currencyFormat.format(currentBalance));
////    }
////
////    private void setupListeners() {
////        btnBack.setOnClickListener(v -> finish());
////
////        // Lắng nghe thay đổi trong EditText
////        etAmount.addTextChangedListener(new TextWatcher() {
////            @Override
////            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
////
////            @Override
////            public void onTextChanged(CharSequence s, int start, int before, int count) {
////                checkAndShowButton();
////            }
////
////            @Override
////            public void afterTextChanged(Editable s) {}
////        });
////
////        btnTopUp.setOnClickListener(v -> processTopUp());
////    }
////
////    private void checkAndShowButton() {
////        String selectedBank = spinnerBank.getSelectedItem().toString();
////        String amountStr = etAmount.getText().toString().trim();
////
////        // Hiện nút Nạp nếu đã chọn ngân hàng và nhập số tiền
////        if (!selectedBank.equals("--Ngân hàng--") && !amountStr.isEmpty()) {
////            btnTopUp.setVisibility(View.VISIBLE);
////        } else {
////            btnTopUp.setVisibility(View.GONE);
////        }
////    }
////
////    private void processTopUp() {
////        String selectedBank = spinnerBank.getSelectedItem().toString();
////        String amountStr = etAmount.getText().toString().trim();
////
////        // Kiểm tra ngân hàng
////        if (selectedBank.equals("--Ngân hàng--")) {
////            Toast.makeText(this, "Vui lòng chọn ngân hàng", Toast.LENGTH_SHORT).show();
////            return;
////        }
////
////        // Kiểm tra số tiền
////        if (amountStr.isEmpty()) {
////            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
////            return;
////        }
////
////        double amount;
////        try {
////            amount = Double.parseDouble(amountStr);
////        } catch (NumberFormatException e) {
////            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
////            return;
////        }
////
////        if (amount <= 0) {
////            Toast.makeText(this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
////            return;
////        }
////
////        // Tạo giao dịch nạp tiền
////        String description = "Nạp tiền từ " + selectedBank;
////        long result = dbHelper.addTransaction(userId, "DEPOSIT", amount, description, "Nạp tiền");
////
////        if (result != -1) {
////            // ✅ LẤY SỐ DƯ MỚI TỪ DATABASE SAU KHI NẠP
////            double newBalance = dbHelper.getBalance(userId);
////
////            // ✅ CẬP NHẬT VÀO SHAREDPREFERENCES
////            SharedPreferences.Editor editor = sharedPreferences.edit();
////            editor.putFloat("balance", (float) newBalance);
////            editor.apply();
////
////            // Tạo thông báo
////            dbHelper.addNotification(userId,
////                    "Thu nhập",
////                    currencyFormat.format(amount),
////                    "INCOME",
////                    (int) result);
////
////            // Lấy ngày hiện tại
////            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
////            String currentDate = sdf.format(new Date());
////
////            // Chuyển sang màn hình thành công
////            Intent intent = new Intent(TopUpActivity.this, TopUpSuccessActivity.class);
////            intent.putExtra("bank", selectedBank);
////            intent.putExtra("amount", amount);
////            intent.putExtra("date", currentDate);
////            startActivity(intent);
////            finish();
////        } else {
////            Toast.makeText(this, "Nạp tiền thất bại", Toast.LENGTH_SHORT).show();
////        }
////    }
////
////    @Override
////    protected void onResume() {
////        super.onResume();
////        loadCurrentBalance();
////    }
////
////    @Override
////    protected void onDestroy() {
////        super.onDestroy();
////        if (dbHelper != null) {
////            dbHelper.close();
////        }
////    }
////}
//
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
//import androidx.cardview.widget.CardView;
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
//    // Quick amount buttons
//    private CardView btn50k, btn100k, btn200k, btn500k;
//
//    private DatabaseHelper dbHelper;
//    private SharedPreferences sharedPreferences;
//    private int userId;
//    private double currentBalance;
//    private NumberFormat currencyFormat;
//    private NumberFormat plainFormat;
//
//    private long selectedAmount = 0;
//
//    private String[] banks = {
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
//        plainFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
//
//        initViews();
//        loadCurrentBalance();
//        setupListeners();
//        selectQuickAmount(100000, btn100k); // default select 100k
//    }
//
//    private void initViews() {
//        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
//        spinnerBank = findViewById(R.id.spinnerBank);
//        etAmount = findViewById(R.id.etAmount);
//        btnTopUp = findViewById(R.id.btnTopUp);
//        btnBack = findViewById(R.id.btnBack);
//
//        btn50k = findViewById(R.id.btn50k);
//        btn100k = findViewById(R.id.btn100k);
//        btn200k = findViewById(R.id.btn200k);
//        btn500k = findViewById(R.id.btn500k);
//
//        // Setup spinner
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(
//                this,
//                android.R.layout.simple_spinner_item,
//                banks
//        );
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerBank.setAdapter(adapter);
//    }
//
//    private void loadCurrentBalance() {
//        currentBalance = dbHelper.getBalance(userId);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putFloat("balance", (float) currentBalance);
//        editor.apply();
//
//        String balanceStr = currencyFormat.format(currentBalance);
//        tvCurrentBalance.setText("**** 4829  •  " + balanceStr + "đ");
//    }
//
//    private void setupListeners() {
//        btnBack.setOnClickListener(v -> finish());
//
//        btn50k.setOnClickListener(v -> selectQuickAmount(50000, btn50k));
//        btn100k.setOnClickListener(v -> selectQuickAmount(100000, btn100k));
//        btn200k.setOnClickListener(v -> selectQuickAmount(200000, btn200k));
//        btn500k.setOnClickListener(v -> selectQuickAmount(500000, btn500k));
//
//        etAmount.addTextChangedListener(new TextWatcher() {
//            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            @Override public void afterTextChanged(Editable s) {}
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                String raw = s.toString().replace(".", "").replace(",", "").trim();
//                if (!raw.isEmpty()) {
//                    try {
//                        selectedAmount = Long.parseLong(raw);
//                        updateButtonText();
//                        clearQuickAmountHighlight();
//                    } catch (NumberFormatException e) {
//                        selectedAmount = 0;
//                    }
//                }
//            }
//        });
//
//        btnTopUp.setOnClickListener(v -> processTopUp());
//    }
//
//    private void selectQuickAmount(long amount, CardView selectedBtn) {
//        selectedAmount = amount;
//
//        // Reset all to white
//        btn50k.setCardBackgroundColor(getResources().getColor(android.R.color.white));
//        btn100k.setCardBackgroundColor(getResources().getColor(android.R.color.white));
//        btn200k.setCardBackgroundColor(getResources().getColor(android.R.color.white));
//        btn500k.setCardBackgroundColor(getResources().getColor(android.R.color.white));
//
//        // Set text colors back to dark
//        setQuickBtnColor(btn50k, "#0D0F3C");
//        setQuickBtnColor(btn100k, "#0D0F3C");
//        setQuickBtnColor(btn200k, "#0D0F3C");
//        setQuickBtnColor(btn500k, "#0D0F3C");
//
//        // Highlight selected
//        selectedBtn.setCardBackgroundColor(0xFFE8EAFF);
//        setQuickBtnColor(selectedBtn, "#3D5AFE");
//
//        // Update EditText display
//        etAmount.setText(currencyFormat.format(amount));
//        updateButtonText();
//    }
//
//    private void setQuickBtnColor(CardView card, String hexColor) {
//        View child = card.getChildAt(0);
//        if (child instanceof TextView) {
//            ((TextView) child).setTextColor(android.graphics.Color.parseColor(hexColor));
//        }
//    }
//
//    private void clearQuickAmountHighlight() {
//        btn50k.setCardBackgroundColor(getResources().getColor(android.R.color.white));
//        btn100k.setCardBackgroundColor(getResources().getColor(android.R.color.white));
//        btn200k.setCardBackgroundColor(getResources().getColor(android.R.color.white));
//        btn500k.setCardBackgroundColor(getResources().getColor(android.R.color.white));
//
//        setQuickBtnColor(btn50k, "#0D0F3C");
//        setQuickBtnColor(btn100k, "#0D0F3C");
//        setQuickBtnColor(btn200k, "#0D0F3C");
//        setQuickBtnColor(btn500k, "#0D0F3C");
//    }
//
//    private void updateButtonText() {
//        if (selectedAmount > 0) {
//            btnTopUp.setText("Nạp tiền  •  " + currencyFormat.format(selectedAmount) + "đ");
//        } else {
//            btnTopUp.setText("Nạp tiền");
//        }
//    }
//
//    private void processTopUp() {
//        if (selectedAmount <= 0) {
//            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        String selectedBank = spinnerBank.getSelectedItem() != null
//                ? spinnerBank.getSelectedItem().toString()
//                : "Ngân hàng";
//
//        double amount = (double) selectedAmount;
//        String description = "Nạp tiền từ " + selectedBank;
//        long result = dbHelper.addTransaction(userId, "DEPOSIT", amount, description, "Nạp tiền");
//
//        if (result != -1) {
//            double newBalance = dbHelper.getBalance(userId);
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.putFloat("balance", (float) newBalance);
//            editor.apply();
//
//            dbHelper.addNotification(userId,
//                    "Thu nhập",
//                    currencyFormat.format(amount),
//                    "INCOME",
//                    (int) result);
//
//            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
//            String currentDate = sdf.format(new Date());
//
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
//    protected void onResume() {
//        super.onResume();
//        loadCurrentBalance();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (dbHelper != null) dbHelper.close();
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
import androidx.cardview.widget.CardView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TopUpActivity extends AppCompatActivity {

    private TextView tvCurrentBalance;
    private Spinner spinnerBank;
    private EditText etAmount;
    private Button btnTopUp;
    private ImageView btnBack;
    private CardView btn50k, btn100k, btn200k, btn500k;

    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;
    private String userPhone;
    private double currentBalance;
    private NumberFormat currencyFormat;
    private long selectedAmount = 0;

    private String[] banks = {"Vietcombank", "MB Bank", "Vietinbank", "TP Bank", "Techcombank", "ACB", "VPBank", "Agribank", "BIDV", "Sacombank"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topup);

        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
        userPhone = sharedPreferences.getString("userPhone", "");
        currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

        initViews();
        loadCurrentBalance();
        setupListeners();
        selectQuickAmount(100000, btn100k);
    }

    private void initViews() {
        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
        spinnerBank = findViewById(R.id.spinnerBank);
        etAmount = findViewById(R.id.etAmount);
        btnTopUp = findViewById(R.id.btnTopUp);
        btnBack = findViewById(R.id.btnBack);

        btn50k = findViewById(R.id.btn50k);
        btn100k = findViewById(R.id.btn100k);
        btn200k = findViewById(R.id.btn200k);
        btn500k = findViewById(R.id.btn500k);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, banks);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBank.setAdapter(adapter);
    }

    private void loadCurrentBalance() {
        currentBalance = sharedPreferences.getFloat("balance", 0);
        String balanceStr = currencyFormat.format(currentBalance);
        tvCurrentBalance.setText("**** 4829  •  " + balanceStr + "đ");
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btn50k.setOnClickListener(v -> selectQuickAmount(50000, btn50k));
        btn100k.setOnClickListener(v -> selectQuickAmount(100000, btn100k));
        btn200k.setOnClickListener(v -> selectQuickAmount(200000, btn200k));
        btn500k.setOnClickListener(v -> selectQuickAmount(500000, btn500k));

        etAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String raw = s.toString().replace(".", "").replace(",", "").trim();
                if (!raw.isEmpty()) {
                    try {
                        selectedAmount = Long.parseLong(raw);
                        updateButtonText();
                    } catch (NumberFormatException e) { selectedAmount = 0; }
                }
            }
        });

        btnTopUp.setOnClickListener(v -> processTopUp());
    }

    private void selectQuickAmount(long amount, CardView selectedBtn) {
        selectedAmount = amount;
        etAmount.setText(currencyFormat.format(amount));
        updateButtonText();
    }

    private void updateButtonText() {
        if (selectedAmount > 0) btnTopUp.setText("Nạp tiền  •  " + currencyFormat.format(selectedAmount) + "đ");
        else btnTopUp.setText("Nạp tiền");
    }

    private void processTopUp() {
        if (selectedAmount <= 0) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        btnTopUp.setEnabled(false);
        btnTopUp.setText("Đang xử lý...");

        String selectedBank = spinnerBank.getSelectedItem() != null ? spinnerBank.getSelectedItem().toString() : "Ngân hàng";
        double amount = (double) selectedAmount;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        WriteBatch batch = db.batch();

        // 1. Cập nhật số dư User
        batch.update(db.collection("users").document(userPhone), "balance", FieldValue.increment(amount));

        // 2. Tạo lịch sử giao dịch
        Map<String, Object> trans = new HashMap<>();
        trans.put("userPhone", userPhone);
        trans.put("type", "DEPOSIT");
        trans.put("amount", amount);
        trans.put("description", "Nạp tiền từ " + selectedBank);
        trans.put("category", "Nạp tiền");
        trans.put("date", currentDate);
        trans.put("timestamp", System.currentTimeMillis());
        batch.set(db.collection("transactions").document(), trans);

        // 3. Tạo thông báo
        Map<String, Object> notif = new HashMap<>();
        notif.put("userPhone", userPhone);
        notif.put("title", "Thu nhập");
        notif.put("message", "+ " + currencyFormat.format(amount));
        notif.put("type", "INCOME");
        notif.put("date", currentDate);
        notif.put("isRead", false);
        batch.set(db.collection("notifications").document(), notif);

        // Thực thi đồng loạt
        batch.commit().addOnSuccessListener(aVoid -> {
            Intent intent = new Intent(TopUpActivity.this, TopUpSuccessActivity.class);
            intent.putExtra("bank", selectedBank);
            intent.putExtra("amount", amount);
            intent.putExtra("date", currentDate);
            startActivity(intent);
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi mạng!", Toast.LENGTH_SHORT).show();
            btnTopUp.setEnabled(true);
        });
    }
}