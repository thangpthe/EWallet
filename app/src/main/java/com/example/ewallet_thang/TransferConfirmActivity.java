////package com.example.ewallet_thang;
////
////import android.content.Intent;
////import android.content.SharedPreferences;
////import android.os.Bundle;
////import android.text.Editable;
////import android.text.TextWatcher;
////import android.view.View;
////import android.widget.AdapterView;
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
////public class TransferConfirmActivity extends AppCompatActivity {
////
////    private ImageView btnBack;
////    private TextView tvRecipientName;
////    private EditText etAmount;
////    private Spinner spinnerCategory;
////    private Button btnTransfer;
////
////    private DatabaseHelper dbHelper;
////    private SharedPreferences sharedPreferences;
////
////    private int currentUserId;
////    private int recipientId;
////    private String recipientName;
////    private String recipientPhone;
////    private double currentBalance;
////
////    private String selectedCategory = "";
////    private NumberFormat currencyFormat;
////
////    private String[] categories = {
////            "--Phân loại--",
////            "Ăn uống",
////            "Học tập",
////            "Xăng xe",
////            "Thuê nhà",
////            "Mua sắm",
////            "Giải trí",
////            "Y tế",
////            "Du lịch",
////            "Khác"
////    };
////
////    @Override
////    protected void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////        setContentView(R.layout.activity_transfer_confirm);
////
////        dbHelper = new DatabaseHelper(this);
////        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
////        currentUserId = sharedPreferences.getInt("userId", -1);
////
////        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
////
////        // Nhận dữ liệu từ Intent
////        Intent intent = getIntent();
////        recipientId = intent.getIntExtra("recipientId", -1);
////        recipientName = intent.getStringExtra("recipientName");
////        recipientPhone = intent.getStringExtra("recipientPhone");  // ĐÃ SỬA
////        currentBalance = intent.getDoubleExtra("currentBalance", 0);
////
////        initViews();
////        setupListeners();
////        setupCategorySpinner();
////    }
////
////    private void initViews() {
////        btnBack = findViewById(R.id.btnBack);
////        tvRecipientName = findViewById(R.id.tvRecipientName);
////        etAmount = findViewById(R.id.etAmount);
////        spinnerCategory = findViewById(R.id.spinnerCategory);
////        btnTransfer = findViewById(R.id.btnTransfer);
////
////        // Hiển thị tên người nhận
////        tvRecipientName.setText(recipientName);
////
////        // Đặt sẵn gợi ý số tiền (tuỳ bạn)
////        etAmount.setText("");
////    }
////
////    private void setupListeners() {
////        btnBack.setOnClickListener(v -> finish());
////
////        etAmount.addTextChangedListener(new TextWatcher() {
////            @Override
////            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
////
////            @Override
////            public void onTextChanged(CharSequence s, int start, int before, int count) {
////                validateAmount();
////            }
////
////            @Override
////            public void afterTextChanged(Editable s) {}
////        });
////
////        btnTransfer.setOnClickListener(v -> processTransfer());
////    }
////
////    private void setupCategorySpinner() {
////        ArrayAdapter<String> adapter = new ArrayAdapter<>(
////                this,
////                android.R.layout.simple_spinner_item,
////                categories
////        );
////
////        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
////        spinnerCategory.setAdapter(adapter);
////
////        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
////            @Override
////            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
////                selectedCategory = (position > 0) ? categories[position] : "";
////            }
////
////            @Override
////            public void onNothingSelected(AdapterView<?> parent) {
////                selectedCategory = "";
////            }
////        });
////    }
////
////    private boolean validateAmount() {
////        String amountStr = etAmount.getText().toString().trim();
////
////        if (amountStr.isEmpty()) {
////            btnTransfer.setEnabled(false);
////            return false;
////        }
////
////        try {
////            double amount = Double.parseDouble(amountStr);
////
////            if (amount <= 0) {
////                Toast.makeText(this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
////                btnTransfer.setEnabled(false);
////                return false;
////            }
////
////            if (amount > currentBalance) {
////                Toast.makeText(this, "Số dư không đủ để chuyển", Toast.LENGTH_SHORT).show();
////                btnTransfer.setEnabled(false);
////                return false;
////            }
////
////            btnTransfer.setEnabled(true);
////            return true;
////
////        } catch (NumberFormatException e) {
////            btnTransfer.setEnabled(false);
////            return false;
////        }
////    }
////
////    private void processTransfer() {
////        if (!validateAmount()) return;
////
////        if (selectedCategory.isEmpty()) {
////            Toast.makeText(this, "Vui lòng chọn phân loại", Toast.LENGTH_SHORT).show();
////            return;
////        }
////
////        double amount = Double.parseDouble(etAmount.getText().toString().trim());
////
////        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
////        String currentDate = dateFormat.format(new Date());
////
////        // Giao dịch của người gửi
////        long senderTransactionId = dbHelper.addTransaction(
////                currentUserId,
////                "EXPENSE",
////                amount,
////                "Chuyển tiền tới " + recipientName,
////                selectedCategory
////        );
////
////        // Giao dịch của người nhận (nếu không phải user mặc định)
////        long recipientTransactionId = -1;
////
////        if (recipientId < 10000) {
////            String senderFullName = sharedPreferences.getString("firstName", "") + " " +
////                    sharedPreferences.getString("lastName", "");
////
////            recipientTransactionId = dbHelper.addTransaction(
////                    recipientId,
////                    "INCOME",
////                    amount,
////                    "Nhận tiền từ " + senderFullName,
////                    selectedCategory
////            );
////        }
////
////        // Thông báo
////        if (senderTransactionId != -1) {
////            dbHelper.addNotification(
////                    currentUserId,
////                    "Chi tiêu",
////                    "- " + currencyFormat.format(amount),
////                    "EXPENSE",
////                    (int) senderTransactionId
////            );
////        }
////
////        if (recipientTransactionId != -1) {
////            dbHelper.addNotification(
////                    recipientId,
////                    "Thu nhập",
////                    "+ " + currencyFormat.format(amount),
////                    "INCOME",
////                    (int) recipientTransactionId
////            );
////        }
////
////        // Trừ tiền người gửi
////        double newBalance = currentBalance - amount;
////        sharedPreferences.edit().putFloat("balance", (float) newBalance).apply();
////
////        // Màn hình thành công
////        Intent intent = new Intent(TransferConfirmActivity.this, TransferSuccessActivity.class);
////        intent.putExtra("recipientName", recipientName);
////        intent.putExtra("amount", amount);
////        intent.putExtra("date", currentDate);
////        intent.putExtra("category", selectedCategory);
////        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
////        startActivity(intent);
////        finish();
////    }
////}
//package com.example.ewallet_thang;
//
//import android.app.Activity;
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
//import com.google.firebase.firestore.FieldValue;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.WriteBatch;
//
//import java.text.NumberFormat;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Locale;
//import java.util.Map;
//
//public class TransferConfirmActivity extends AppCompatActivity {
//
//    // ── Request code cho màn hình chọn voucher ───────────────────────────────
//    private static final int REQUEST_VOUCHER = 2002;
//
//    private ImageView btnBack;
//    private TextView  tvRecipientName;
//    private EditText  etAmount;
//    private Spinner   spinnerCategory;
//    private Button    btnTransfer;
//
//    // ── [TÍCH HỢP MỚI] UI voucher ────────────────────────────────────────────
//    private TextView tvVoucherApplied;
//    private Button   btnSelectVoucher, btnRemoveVoucher;
//
//    private FirebaseFirestore db;
//    private SharedPreferences sharedPreferences;
//
//    private String currentPhone;
//    private String recipientName;
//    private String recipientPhone;
//    private double currentBalance;
//
//    private String selectedCategory = "";
//    private NumberFormat currencyFormat;
//
//    // ── [TÍCH HỢP MỚI] Trạng thái voucher ───────────────────────────────────
//    private String appliedVoucherId    = null;
//    private long   appliedVoucherValue = 0;
//
//    private String[] categories = {
//            "--Phân loại--","Ăn uống","Học tập","Xăng xe",
//            "Thuê nhà","Mua sắm","Giải trí","Khác"
//    };
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_transfer_confirm);
//
//        db            = FirebaseFirestore.getInstance();
//        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
//        currentPhone  = sharedPreferences.getString("userPhone", "");
//        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
//
//        Intent intent = getIntent();
//        recipientName  = intent.getStringExtra("recipientName");
//        recipientPhone = intent.getStringExtra("recipientPhone");
//        currentBalance = intent.getDoubleExtra("currentBalance", 0);
//
//        initViews();
//        setupListeners();
//        setupCategorySpinner();
//    }
//
//    private void initViews() {
//        btnBack         = findViewById(R.id.btnBack);
//        tvRecipientName = findViewById(R.id.tvRecipientName);
//        etAmount        = findViewById(R.id.etAmount);
//        spinnerCategory = findViewById(R.id.spinnerCategory);
//        btnTransfer     = findViewById(R.id.btnTransfer);
//
//        // ── [TÍCH HỢP MỚI] ────────────────────────────────────────────────
//        tvVoucherApplied = findViewById(R.id.tvVoucherApplied);
//        btnSelectVoucher = findViewById(R.id.btnSelectVoucher);
//        btnRemoveVoucher = findViewById(R.id.btnRemoveVoucher);
//
//        tvRecipientName.setText(recipientName);
//    }
//
//    private void setupListeners() {
//        btnBack.setOnClickListener(v -> finish());
//
//        etAmount.addTextChangedListener(new TextWatcher() {
//            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
//            @Override public void afterTextChanged(Editable s) {}
//            @Override
//            public void onTextChanged(CharSequence s, int st, int b, int c) {
//                validateAmount();
//            }
//        });
//
//        // ── [TÍCH HỢP MỚI] Nút chọn / xóa voucher ────────────────────────
//        if (btnSelectVoucher != null) {
//            btnSelectVoucher.setOnClickListener(v -> {
//                Intent i = new Intent(this, VoucherActivity.class);
//                startActivityForResult(i, REQUEST_VOUCHER);
//            });
//        }
//        if (btnRemoveVoucher != null) {
//            btnRemoveVoucher.setOnClickListener(v -> removeVoucher());
//        }
//
//        btnTransfer.setOnClickListener(v -> processTransfer());
//    }
//
//    // ── [TÍCH HỢP MỚI] Nhận kết quả chọn voucher ────────────────────────────
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_VOUCHER && resultCode == Activity.RESULT_OK && data != null) {
//            String voucherType = data.getStringExtra("voucherType");
//
//            // Chỉ chấp nhận voucher loại TRANSFER hoặc ANY tại màn hình chuyển khoản
//            if ("TOPUP".equals(voucherType)) {
//                Toast.makeText(this,
//                        "Voucher này chỉ dùng được khi nạp tiền!",
//                        Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            appliedVoucherId    = data.getStringExtra("voucherId");
//            appliedVoucherValue = data.getLongExtra("voucherValue", 0);
//            String code         = data.getStringExtra("voucherCode");
//
//            if (tvVoucherApplied != null) {
//                tvVoucherApplied.setText("🎟 " + code + " (-"
//                        + currencyFormat.format(appliedVoucherValue) + "đ)");
//                tvVoucherApplied.setVisibility(View.VISIBLE);
//            }
//            if (btnRemoveVoucher != null)
//                btnRemoveVoucher.setVisibility(View.VISIBLE);
//            if (btnSelectVoucher != null)
//                btnSelectVoucher.setText("Đổi voucher");
//
//            Toast.makeText(this,
//                    "Áp dụng voucher thành công!", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void removeVoucher() {
//        appliedVoucherId    = null;
//        appliedVoucherValue = 0;
//        if (tvVoucherApplied != null) tvVoucherApplied.setVisibility(View.GONE);
//        if (btnRemoveVoucher != null) btnRemoveVoucher.setVisibility(View.GONE);
//        if (btnSelectVoucher != null) btnSelectVoucher.setText("Chọn voucher");
//    }
//
//    private void setupCategorySpinner() {
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(
//                this, android.R.layout.simple_spinner_item, categories);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerCategory.setAdapter(adapter);
//        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
//                selectedCategory = (pos > 0) ? categories[pos] : "";
//            }
//            @Override public void onNothingSelected(AdapterView<?> p) { selectedCategory = ""; }
//        });
//    }
//
//    private boolean validateAmount() {
//        String amountStr = etAmount.getText().toString().trim();
//        if (amountStr.isEmpty()) { btnTransfer.setEnabled(false); return false; }
//        try {
//            double amount = Double.parseDouble(amountStr);
//            // ── [TÍCH HỢP MỚI] Trừ voucher khi kiểm tra số dư ────────────
//            double realCost = Math.max(0, amount - appliedVoucherValue);
//            if (amount <= 0 || realCost > currentBalance) {
//                btnTransfer.setEnabled(false); return false;
//            }
//            btnTransfer.setEnabled(true);
//            return true;
//        } catch (Exception e) { btnTransfer.setEnabled(false); return false; }
//    }
//
//    private void processTransfer() {
//        if (!validateAmount() || selectedCategory.isEmpty()) {
//            if (selectedCategory.isEmpty())
//                Toast.makeText(this, "Vui lòng chọn phân loại", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        btnTransfer.setEnabled(false);
//        btnTransfer.setText("Đang xử lý...");
//
//        double amountInput  = Double.parseDouble(etAmount.getText().toString().trim());
//        // ── [TÍCH HỢP MỚI] Số tiền thực sự trừ sau voucher ─────────────────
//        double amountToDeduct = Math.max(0, amountInput - appliedVoucherValue);
//
//        String currentDate   = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
//                Locale.getDefault()).format(new Date());
//        String senderFullName = sharedPreferences.getString("firstName", "")
//                + " " + sharedPreferences.getString("lastName", "");
//
//        WriteBatch batch = db.batch();
//
//        // 1. Trừ tiền người gửi (trừ amountToDeduct sau voucher)
//        batch.update(db.collection("users").document(currentPhone),
//                "balance", FieldValue.increment(-amountToDeduct));
//
//        // 2. Giao dịch người gửi
//        Map<String, Object> senderTx = new HashMap<>();
//        senderTx.put("userPhone",  currentPhone);
//        senderTx.put("type",       "TRANSFER");
//        senderTx.put("amount",     amountToDeduct);
//        senderTx.put("description","Chuyển tiền tới " + recipientName
//                + (appliedVoucherValue > 0
//                ? " (Voucher -" + currencyFormat.format(appliedVoucherValue) + "đ)" : ""));
//        senderTx.put("category",   selectedCategory);
//        senderTx.put("date",       currentDate);
//        senderTx.put("timestamp",  System.currentTimeMillis());
//        batch.set(db.collection("transactions").document(), senderTx);
//
//        // 3. Thông báo người gửi
//        Map<String, Object> senderNotif = new HashMap<>();
//        senderNotif.put("userPhone", currentPhone);
//        senderNotif.put("title",  "Chi tiêu");
//        senderNotif.put("message","- " + currencyFormat.format(amountToDeduct));
//        senderNotif.put("type",   "EXPENSE");
//        senderNotif.put("date",   currentDate);
//        senderNotif.put("isRead", false);
//        batch.set(db.collection("notifications").document(), senderNotif);
//
//        // 4. Người nhận nhận full amountInput (voucher là người gửi chịu)
//        if (recipientPhone != null && !recipientPhone.isEmpty()) {
//            batch.update(db.collection("users").document(recipientPhone),
//                    "balance", FieldValue.increment(amountInput));
//
//            Map<String, Object> recTx = new HashMap<>();
//            recTx.put("userPhone",  recipientPhone);
//            recTx.put("type",       "INCOME");
//            recTx.put("amount",     amountInput);
//            recTx.put("description","Nhận tiền từ " + senderFullName);
//            recTx.put("category",   selectedCategory);
//            recTx.put("date",       currentDate);
//            recTx.put("timestamp",  System.currentTimeMillis());
//            batch.set(db.collection("transactions").document(), recTx);
//
//            Map<String, Object> recNotif = new HashMap<>();
//            recNotif.put("userPhone", recipientPhone);
//            recNotif.put("title",  "Thu nhập");
//            recNotif.put("message","+ " + currencyFormat.format(amountInput));
//            recNotif.put("type",   "INCOME");
//            recNotif.put("date",   currentDate);
//            recNotif.put("isRead", false);
//            batch.set(db.collection("notifications").document(), recNotif);
//        }
//
//        // 5. Đánh dấu voucher đã dùng (nếu có)
//        if (appliedVoucherId != null) {
//            batch.update(db.collection("vouchers").document(appliedVoucherId),
//                    "isUsed", true);
//        }
//
//        batch.commit().addOnSuccessListener(aVoid -> {
//            // ── [TÍCH HỢP MỚI] Tích điểm cho người gửi ──────────────────
//            PointsActivity.addPointsForTransaction(
//                    db, currentPhone, amountToDeduct,
//                    "Chuyển tiền tới " + recipientName);
//
//            // ── [TÍCH HỢP MỚI] Cashback tân thủ (giao dịch đầu tiên) ────
//            NewUserBonusActivity.applyFirstTransactionCashback(
//                    db, sharedPreferences, currentPhone, amountToDeduct);
//
//            sharedPreferences.edit()
//                    .putFloat("balance", (float)(currentBalance - amountToDeduct))
//                    .apply();
//
//            Intent intent = new Intent(this, TransferSuccessActivity.class);
//            intent.putExtra("recipientName", recipientName);
//            intent.putExtra("amount",        amountToDeduct);
//            intent.putExtra("date",          currentDate);
//            intent.putExtra("category",      selectedCategory);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(intent);
//            finish();
//        }).addOnFailureListener(e -> {
//            Toast.makeText(this, "Lỗi mạng!", Toast.LENGTH_SHORT).show();
//            btnTransfer.setEnabled(true);
//            btnTransfer.setText("XÁC NHẬN CHUYỂN");
//        });
//    }
//}

package com.example.ewallet_thang;

import android.app.Activity;
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

    private static final int REQUEST_VOUCHER = 2002;

    private ImageView btnBack;
    private TextView  tvRecipientName;
    private EditText  etAmount;
    private Spinner   spinnerCategory;
    private Button    btnTransfer;

    private TextView tvVoucherApplied;
    private Button   btnSelectVoucher, btnRemoveVoucher;

    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;

    private String currentPhone;
    private String recipientName;
    private String recipientPhone;
    private double currentBalance;

    private String selectedCategory    = "";
    private NumberFormat currencyFormat;

    private String appliedVoucherId    = null;
    private long   appliedVoucherValue = 0;

    private String[] categories = {
            "--Phân loại--","Ăn uống","Học tập","Xăng xe",
            "Thuê nhà","Mua sắm","Giải trí","Khác"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_confirm);

        db             = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
        currentPhone   = sharedPreferences.getString("userPhone", "");
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        Intent intent  = getIntent();
        recipientName  = intent.getStringExtra("recipientName");
        recipientPhone = intent.getStringExtra("recipientPhone");
        currentBalance = intent.getDoubleExtra("currentBalance", 0);

        initViews();
        setupListeners();
        setupCategorySpinner();
    }

    private void initViews() {
        btnBack         = findViewById(R.id.btnBack);
        tvRecipientName = findViewById(R.id.tvRecipientName);
        etAmount        = findViewById(R.id.etAmount);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnTransfer     = findViewById(R.id.btnTransfer);

        tvVoucherApplied = findViewById(R.id.tvVoucherApplied);
        btnSelectVoucher = findViewById(R.id.btnSelectVoucher);
        btnRemoveVoucher = findViewById(R.id.btnRemoveVoucher);

        tvRecipientName.setText(recipientName);

        // Đặt avatar từ ký tự đầu tên
        TextView tvAvatar = findViewById(R.id.tvAvatar);
        if (tvAvatar != null && recipientName != null && !recipientName.isEmpty()) {
            tvAvatar.setText(String.valueOf(recipientName.charAt(0)).toUpperCase());
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        etAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                validateAmount();
            }
        });

        if (btnSelectVoucher != null) {
            btnSelectVoucher.setOnClickListener(v -> {
                Intent i = new Intent(this, VoucherActivity.class);
                startActivityForResult(i, REQUEST_VOUCHER);
            });
        }
        if (btnRemoveVoucher != null) {
            btnRemoveVoucher.setOnClickListener(v -> removeVoucher());
        }

        btnTransfer.setOnClickListener(v -> processTransfer());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VOUCHER && resultCode == Activity.RESULT_OK && data != null) {
            String voucherType = data.getStringExtra("voucherType");

            if ("TOPUP".equals(voucherType)) {
                Toast.makeText(this, "Voucher này chỉ dùng được khi nạp tiền!", Toast.LENGTH_SHORT).show();
                return;
            }

            appliedVoucherId    = data.getStringExtra("voucherId");
            appliedVoucherValue = data.getLongExtra("voucherValue", 0);
            String code         = data.getStringExtra("voucherCode");

            if (tvVoucherApplied != null) {
                tvVoucherApplied.setText("🎟 " + code + " (-" + currencyFormat.format(appliedVoucherValue) + "đ)");
                tvVoucherApplied.setVisibility(View.VISIBLE);
            }
            if (btnRemoveVoucher != null) btnRemoveVoucher.setVisibility(View.VISIBLE);
            if (btnSelectVoucher != null) btnSelectVoucher.setText("Đổi voucher");

            Toast.makeText(this, "Áp dụng voucher thành công!", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeVoucher() {
        appliedVoucherId    = null;
        appliedVoucherValue = 0;
        if (tvVoucherApplied != null) tvVoucherApplied.setVisibility(View.GONE);
        if (btnRemoveVoucher != null) btnRemoveVoucher.setVisibility(View.GONE);
        if (btnSelectVoucher != null) btnSelectVoucher.setText("Chọn voucher");
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                selectedCategory = (pos > 0) ? categories[pos] : "";
            }
            @Override public void onNothingSelected(AdapterView<?> p) { selectedCategory = ""; }
        });
    }

    private boolean validateAmount() {
        String amountStr = etAmount.getText().toString().trim();
        if (amountStr.isEmpty()) { btnTransfer.setEnabled(false); return false; }
        try {
            double amount   = Double.parseDouble(amountStr);
            double realCost = Math.max(0, amount - appliedVoucherValue);
            if (amount <= 0 || realCost > currentBalance) {
                btnTransfer.setEnabled(false); return false;
            }
            btnTransfer.setEnabled(true);
            return true;
        } catch (Exception e) { btnTransfer.setEnabled(false); return false; }
    }

    private void processTransfer() {
        if (!validateAmount() || selectedCategory.isEmpty()) {
            if (selectedCategory.isEmpty())
                Toast.makeText(this, "Vui lòng chọn phân loại", Toast.LENGTH_SHORT).show();
            return;
        }

        btnTransfer.setEnabled(false);
        btnTransfer.setText("Đang xử lý...");

        double amountInput    = Double.parseDouble(etAmount.getText().toString().trim());
        double amountToDeduct = Math.max(0, amountInput - appliedVoucherValue);

        String currentDate    = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()).format(new Date());
        String senderFullName = sharedPreferences.getString("firstName", "")
                + " " + sharedPreferences.getString("lastName", "");

        // Tính điểm tích lũy: 10.000đ = 1 điểm
        long pointsEarned = (long)(amountToDeduct / 10_000);

        WriteBatch batch = db.batch();

        // 1. Trừ ví người gửi
        batch.update(db.collection("users").document(currentPhone),
                "balance", FieldValue.increment(-amountToDeduct));

        // 2. Cộng điểm người gửi
        if (pointsEarned > 0) {
            batch.update(db.collection("users").document(currentPhone),
                    "points", FieldValue.increment(pointsEarned));

            Map<String, Object> ph = new HashMap<>();
            ph.put("userPhone",   currentPhone);
            ph.put("description", "Chuyển tiền tới " + recipientName + " → +" + pointsEarned + " điểm");
            ph.put("delta",       pointsEarned);
            ph.put("date",        currentDate);
            ph.put("timestamp",   System.currentTimeMillis());
            batch.set(db.collection("pointHistory").document(), ph);
        }

        // 3. Giao dịch người gửi
        Map<String, Object> senderTx = new HashMap<>();
        senderTx.put("userPhone",  currentPhone);
        senderTx.put("type",       "TRANSFER");
        senderTx.put("amount",     amountToDeduct);
        senderTx.put("description","Chuyển tiền tới " + recipientName
                + (appliedVoucherValue > 0
                ? " (Voucher -" + currencyFormat.format(appliedVoucherValue) + "đ)" : ""));
        senderTx.put("category",   selectedCategory);
        senderTx.put("date",       currentDate);
        senderTx.put("timestamp",  System.currentTimeMillis());
        batch.set(db.collection("transactions").document(), senderTx);

        // 4. Thông báo người gửi
        Map<String, Object> senderNotif = new HashMap<>();
        senderNotif.put("userPhone", currentPhone);
        senderNotif.put("title",  "Chi tiêu");
        senderNotif.put("message","- " + currencyFormat.format(amountToDeduct));
        senderNotif.put("type",   "EXPENSE");
        senderNotif.put("date",   currentDate);
        senderNotif.put("isRead", false);
        batch.set(db.collection("notifications").document(), senderNotif);

        // 5. Cộng tiền người nhận (nhận full amountInput, voucher là người gửi chịu)
        if (recipientPhone != null && !recipientPhone.isEmpty()) {
            batch.update(db.collection("users").document(recipientPhone),
                    "balance", FieldValue.increment(amountInput));

            Map<String, Object> recTx = new HashMap<>();
            recTx.put("userPhone",  recipientPhone);
            recTx.put("type",       "INCOME");
            recTx.put("amount",     amountInput);
            recTx.put("description","Nhận tiền từ " + senderFullName);
            recTx.put("category",   selectedCategory);
            recTx.put("date",       currentDate);
            recTx.put("timestamp",  System.currentTimeMillis());
            batch.set(db.collection("transactions").document(), recTx);

            Map<String, Object> recNotif = new HashMap<>();
            recNotif.put("userPhone", recipientPhone);
            recNotif.put("title",  "Thu nhập");
            recNotif.put("message","+ " + currencyFormat.format(amountInput));
            recNotif.put("type",   "INCOME");
            recNotif.put("date",   currentDate);
            recNotif.put("isRead", false);
            batch.set(db.collection("notifications").document(), recNotif);
        }

        // 6. Đánh dấu voucher đã dùng
        if (appliedVoucherId != null) {
            batch.update(db.collection("vouchers").document(appliedVoucherId), "isUsed", true);
        }

        final double fd = amountToDeduct;
        final long   fp = pointsEarned;

        batch.commit().addOnSuccessListener(aVoid -> {

            // Cập nhật local balance
            sharedPreferences.edit()
                    .putFloat("balance", (float)(currentBalance - fd))
                    .apply();

            // ── Hiện SuccessBottomSheet trực tiếp — KHÔNG qua TransferSuccessActivity ──
            SuccessBottomSheet sheet = SuccessBottomSheet.newInstance(
                    SuccessBottomSheet.TYPE_TRANSFER,
                    fd,
                    recipientName,
                    currentDate,
                    selectedCategory,
                    fp,
                    false   // first-tx bonus xử lý riêng ở NewUserBonusActivity
            );
            sheet.setOnDismissListener(goHome -> {
                if (goHome) {
                    startActivity(new Intent(this, MainActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                }
                finish();
            });
            sheet.show(getSupportFragmentManager(), "success");

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi mạng!", Toast.LENGTH_SHORT).show();
            btnTransfer.setEnabled(true);
            btnTransfer.setText("Xác nhận chuyển");
        });
    }
}