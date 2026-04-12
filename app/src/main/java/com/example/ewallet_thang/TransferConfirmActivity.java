//package com.example.ewallet_thang;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.view.View;
//import android.widget.AdapterView;
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
//public class TransferConfirmActivity extends AppCompatActivity {
//
//    private ImageView btnBack;
//    private TextView tvRecipientName;
//    private EditText etAmount;
//    private Spinner spinnerCategory;
//    private Button btnTransfer;
//
//    private DatabaseHelper dbHelper;
//    private SharedPreferences sharedPreferences;
//
//    private int currentUserId;
//    private int recipientId;
//    private String recipientName;
//    private String recipientPhone;
//    private double currentBalance;
//
//    private String selectedCategory = "";
//    private NumberFormat currencyFormat;
//
//    private String[] categories = {
//            "--Phân loại--",
//            "Ăn uống",
//            "Học tập",
//            "Xăng xe",
//            "Thuê nhà",
//            "Mua sắm",
//            "Giải trí",
//            "Y tế",
//            "Du lịch",
//            "Khác"
//    };
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_transfer_confirm);
//
//        dbHelper = new DatabaseHelper(this);
//        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
//        currentUserId = sharedPreferences.getInt("userId", -1);
//
//        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
//
//        // Nhận dữ liệu từ Intent
//        Intent intent = getIntent();
//        recipientId = intent.getIntExtra("recipientId", -1);
//        recipientName = intent.getStringExtra("recipientName");
//        recipientPhone = intent.getStringExtra("recipientPhone");  // ĐÃ SỬA
//        currentBalance = intent.getDoubleExtra("currentBalance", 0);
//
//        initViews();
//        setupListeners();
//        setupCategorySpinner();
//    }
//
//    private void initViews() {
//        btnBack = findViewById(R.id.btnBack);
//        tvRecipientName = findViewById(R.id.tvRecipientName);
//        etAmount = findViewById(R.id.etAmount);
//        spinnerCategory = findViewById(R.id.spinnerCategory);
//        btnTransfer = findViewById(R.id.btnTransfer);
//
//        // Hiển thị tên người nhận
//        tvRecipientName.setText(recipientName);
//
//        // Đặt sẵn gợi ý số tiền (tuỳ bạn)
//        etAmount.setText("");
//    }
//
//    private void setupListeners() {
//        btnBack.setOnClickListener(v -> finish());
//
//        etAmount.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                validateAmount();
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {}
//        });
//
//        btnTransfer.setOnClickListener(v -> processTransfer());
//    }
//
//    private void setupCategorySpinner() {
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(
//                this,
//                android.R.layout.simple_spinner_item,
//                categories
//        );
//
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerCategory.setAdapter(adapter);
//
//        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                selectedCategory = (position > 0) ? categories[position] : "";
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                selectedCategory = "";
//            }
//        });
//    }
//
//    private boolean validateAmount() {
//        String amountStr = etAmount.getText().toString().trim();
//
//        if (amountStr.isEmpty()) {
//            btnTransfer.setEnabled(false);
//            return false;
//        }
//
//        try {
//            double amount = Double.parseDouble(amountStr);
//
//            if (amount <= 0) {
//                Toast.makeText(this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
//                btnTransfer.setEnabled(false);
//                return false;
//            }
//
//            if (amount > currentBalance) {
//                Toast.makeText(this, "Số dư không đủ để chuyển", Toast.LENGTH_SHORT).show();
//                btnTransfer.setEnabled(false);
//                return false;
//            }
//
//            btnTransfer.setEnabled(true);
//            return true;
//
//        } catch (NumberFormatException e) {
//            btnTransfer.setEnabled(false);
//            return false;
//        }
//    }
//
//    private void processTransfer() {
//        if (!validateAmount()) return;
//
//        if (selectedCategory.isEmpty()) {
//            Toast.makeText(this, "Vui lòng chọn phân loại", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        double amount = Double.parseDouble(etAmount.getText().toString().trim());
//
//        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
//        String currentDate = dateFormat.format(new Date());
//
//        // Giao dịch của người gửi
//        long senderTransactionId = dbHelper.addTransaction(
//                currentUserId,
//                "EXPENSE",
//                amount,
//                "Chuyển tiền tới " + recipientName,
//                selectedCategory
//        );
//
//        // Giao dịch của người nhận (nếu không phải user mặc định)
//        long recipientTransactionId = -1;
//
//        if (recipientId < 10000) {
//            String senderFullName = sharedPreferences.getString("firstName", "") + " " +
//                    sharedPreferences.getString("lastName", "");
//
//            recipientTransactionId = dbHelper.addTransaction(
//                    recipientId,
//                    "INCOME",
//                    amount,
//                    "Nhận tiền từ " + senderFullName,
//                    selectedCategory
//            );
//        }
//
//        // Thông báo
//        if (senderTransactionId != -1) {
//            dbHelper.addNotification(
//                    currentUserId,
//                    "Chi tiêu",
//                    "- " + currencyFormat.format(amount),
//                    "EXPENSE",
//                    (int) senderTransactionId
//            );
//        }
//
//        if (recipientTransactionId != -1) {
//            dbHelper.addNotification(
//                    recipientId,
//                    "Thu nhập",
//                    "+ " + currencyFormat.format(amount),
//                    "INCOME",
//                    (int) recipientTransactionId
//            );
//        }
//
//        // Trừ tiền người gửi
//        double newBalance = currentBalance - amount;
//        sharedPreferences.edit().putFloat("balance", (float) newBalance).apply();
//
//        // Màn hình thành công
//        Intent intent = new Intent(TransferConfirmActivity.this, TransferSuccessActivity.class);
//        intent.putExtra("recipientName", recipientName);
//        intent.putExtra("amount", amount);
//        intent.putExtra("date", currentDate);
//        intent.putExtra("category", selectedCategory);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(intent);
//        finish();
//    }
//}
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

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TransferConfirmActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvRecipientName;
    private EditText etAmount;
    private Spinner spinnerCategory;
    private Button btnTransfer;

    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;

    private String currentPhone;
    private String recipientName;
    private String recipientPhone;
    private double currentBalance;

    private String selectedCategory = "";
    private NumberFormat currencyFormat;

    private String[] categories = {"--Phân loại--", "Ăn uống", "Học tập", "Xăng xe", "Thuê nhà", "Mua sắm", "Giải trí", "Khác"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_confirm);

        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
        currentPhone = sharedPreferences.getString("userPhone", "");
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        Intent intent = getIntent();
        recipientName = intent.getStringExtra("recipientName");
        recipientPhone = intent.getStringExtra("recipientPhone");
        currentBalance = intent.getDoubleExtra("currentBalance", 0);

        initViews();
        setupListeners();
        setupCategorySpinner();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvRecipientName = findViewById(R.id.tvRecipientName);
        etAmount = findViewById(R.id.etAmount);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnTransfer = findViewById(R.id.btnTransfer);

        tvRecipientName.setText(recipientName);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { validateAmount(); }
        });
        btnTransfer.setOnClickListener(v -> processTransfer());
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = (position > 0) ? categories[position] : "";
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { selectedCategory = ""; }
        });
    }

    private boolean validateAmount() {
        String amountStr = etAmount.getText().toString().trim();
        if (amountStr.isEmpty()) { btnTransfer.setEnabled(false); return false; }
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0 || amount > currentBalance) {
                btnTransfer.setEnabled(false); return false;
            }
            btnTransfer.setEnabled(true);
            return true;
        } catch (Exception e) { btnTransfer.setEnabled(false); return false; }
    }

    private void processTransfer() {
        if (!validateAmount() || selectedCategory.isEmpty()) return;

        btnTransfer.setEnabled(false);
        btnTransfer.setText("Đang xử lý...");

        double amount = Double.parseDouble(etAmount.getText().toString().trim());
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String senderFullName = sharedPreferences.getString("firstName", "") + " " + sharedPreferences.getString("lastName", "");

        WriteBatch batch = db.batch();

        // 1. XỬ LÝ NGƯỜI GỬI (Trừ tiền, Thêm Lịch sử, Thêm Thông báo)
        batch.update(db.collection("users").document(currentPhone), "balance", FieldValue.increment(-amount));

        Map<String, Object> senderTx = new HashMap<>();
        senderTx.put("userPhone", currentPhone);
        senderTx.put("type", "TRANSFER");
        senderTx.put("amount", amount);
        senderTx.put("description", "Chuyển tiền tới " + recipientName);
        senderTx.put("category", selectedCategory);
        senderTx.put("date", currentDate);
        senderTx.put("timestamp", System.currentTimeMillis());
        batch.set(db.collection("transactions").document(), senderTx);

        Map<String, Object> senderNotif = new HashMap<>();
        senderNotif.put("userPhone", currentPhone);
        senderNotif.put("title", "Chi tiêu");
        senderNotif.put("message", "- " + currencyFormat.format(amount));
        senderNotif.put("type", "EXPENSE");
        senderNotif.put("date", currentDate);
        senderNotif.put("isRead", false);
        batch.set(db.collection("notifications").document(), senderNotif);

        // 2. XỬ LÝ NGƯỜI NHẬN (Cộng tiền nếu là User thật trên Firebase)
        // Các user mặc định (test) có sdt bắt đầu bằng 0901, 0902... nếu sdt không có trên Firebase, lệnh update sẽ bị bỏ qua an toàn.
        if (recipientPhone != null && !recipientPhone.isEmpty()) {
            batch.update(db.collection("users").document(recipientPhone), "balance", FieldValue.increment(amount));

            Map<String, Object> recTx = new HashMap<>(senderTx);
            recTx.put("userPhone", recipientPhone);
            recTx.put("type", "INCOME");
            recTx.put("description", "Nhận tiền từ " + senderFullName);
            batch.set(db.collection("transactions").document(), recTx);

            Map<String, Object> recNotif = new HashMap<>(senderNotif);
            recNotif.put("userPhone", recipientPhone);
            recNotif.put("title", "Thu nhập");
            recNotif.put("message", "+ " + currencyFormat.format(amount));
            recNotif.put("type", "INCOME");
            batch.set(db.collection("notifications").document(), recNotif);
        }

        // 3. THỰC THI
        batch.commit().addOnSuccessListener(aVoid -> {
            sharedPreferences.edit().putFloat("balance", (float) (currentBalance - amount)).apply();
            Intent intent = new Intent(TransferConfirmActivity.this, TransferSuccessActivity.class);
            intent.putExtra("recipientName", recipientName);
            intent.putExtra("amount", amount);
            intent.putExtra("date", currentDate);
            intent.putExtra("category", selectedCategory);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi mạng!", Toast.LENGTH_SHORT).show();
            btnTransfer.setEnabled(true);
        });
    }
}