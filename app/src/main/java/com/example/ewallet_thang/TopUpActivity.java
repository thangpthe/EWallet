////////package com.example.ewallet_thang;
////////
////////import android.content.Intent;
////////import android.content.SharedPreferences;
////////import android.os.Bundle;
////////import android.text.Editable;
////////import android.text.TextWatcher;
////////import android.view.View;
////////import android.widget.ArrayAdapter;
////////import android.widget.Button;
////////import android.widget.EditText;
////////import android.widget.ImageView;
////////import android.widget.Spinner;
////////import android.widget.TextView;
////////import android.widget.Toast;
////////
////////import androidx.appcompat.app.AppCompatActivity;
////////
////////import com.example.ewallet_thang.database.DatabaseHelper;
////////
////////import java.text.NumberFormat;
////////import java.text.SimpleDateFormat;
////////import java.util.Date;
////////import java.util.Locale;
////////
////////public class TopUpActivity extends AppCompatActivity {
////////
////////    private TextView tvCurrentBalance;
////////    private Spinner spinnerBank;
////////    private EditText etAmount;
////////    private Button btnTopUp;
////////    private ImageView btnBack;
////////
////////    private DatabaseHelper dbHelper;
////////    private SharedPreferences sharedPreferences;
////////    private int userId;
////////    private double currentBalance;
////////    private NumberFormat currencyFormat;
////////
////////    // Danh sách ngân hàng
////////    private String[] banks = {
////////            "--Ngân hàng--",
////////            "Vietcombank",
////////            "MB Bank",
////////            "Vietinbank",
////////            "TP Bank",
////////            "Techcombank",
////////            "ACB",
////////            "VPBank",
////////            "Agribank",
////////            "BIDV",
////////            "Sacombank"
////////    };
////////
////////    @Override
////////    protected void onCreate(Bundle savedInstanceState) {
////////        super.onCreate(savedInstanceState);
////////        setContentView(R.layout.activity_topup);
////////
////////        dbHelper = new DatabaseHelper(this);
////////        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
////////        userId = sharedPreferences.getInt("userId", -1);
////////        currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
////////
////////        initViews();
////////        loadCurrentBalance();
////////        setupListeners();
////////    }
////////
////////    private void initViews() {
////////        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
////////        spinnerBank = findViewById(R.id.spinnerBank);
////////        etAmount = findViewById(R.id.etAmount);
////////        btnTopUp = findViewById(R.id.btnTopUp);
////////        btnBack = findViewById(R.id.btnBack);
////////
////////        // Setup spinner với danh sách ngân hàng
////////        ArrayAdapter<String> adapter = new ArrayAdapter<>(
////////                this,
////////                android.R.layout.simple_spinner_item,
////////                banks
////////        );
////////        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
////////        spinnerBank.setAdapter(adapter);
////////
////////        // Ẩn nút Nạp ban đầu
////////        btnTopUp.setVisibility(View.GONE);
////////    }
////////
////////    private void loadCurrentBalance() {
////////        currentBalance = dbHelper.getBalance(userId);
////////        tvCurrentBalance.setText(currencyFormat.format(currentBalance));
////////    }
////////
////////    private void setupListeners() {
////////        btnBack.setOnClickListener(v -> finish());
////////
////////        // Lắng nghe thay đổi trong EditText
////////        etAmount.addTextChangedListener(new TextWatcher() {
////////            @Override
////////            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
////////
////////            @Override
////////            public void onTextChanged(CharSequence s, int start, int before, int count) {
////////                checkAndShowButton();
////////            }
////////
////////            @Override
////////            public void afterTextChanged(Editable s) {}
////////        });
////////
////////        btnTopUp.setOnClickListener(v -> processTopUp());
////////    }
////////
////////    private void checkAndShowButton() {
////////        String selectedBank = spinnerBank.getSelectedItem().toString();
////////        String amountStr = etAmount.getText().toString().trim();
////////
////////        // Hiện nút Nạp nếu đã chọn ngân hàng và nhập số tiền
////////        if (!selectedBank.equals("--Ngân hàng--") && !amountStr.isEmpty()) {
////////            btnTopUp.setVisibility(View.VISIBLE);
////////        } else {
////////            btnTopUp.setVisibility(View.GONE);
////////        }
////////    }
////////
////////    private void processTopUp() {
////////        String selectedBank = spinnerBank.getSelectedItem().toString();
////////        String amountStr = etAmount.getText().toString().trim();
////////
////////        // Kiểm tra ngân hàng
////////        if (selectedBank.equals("--Ngân hàng--")) {
////////            Toast.makeText(this, "Vui lòng chọn ngân hàng", Toast.LENGTH_SHORT).show();
////////            return;
////////        }
////////
////////        // Kiểm tra số tiền
////////        if (amountStr.isEmpty()) {
////////            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
////////            return;
////////        }
////////
////////        double amount;
////////        try {
////////            amount = Double.parseDouble(amountStr);
////////        } catch (NumberFormatException e) {
////////            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
////////            return;
////////        }
////////
////////        if (amount <= 0) {
////////            Toast.makeText(this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
////////            return;
////////        }
////////
////////        // Tạo giao dịch nạp tiền
////////        String description = "Nạp tiền từ " + selectedBank;
////////        long result = dbHelper.addTransaction(userId, "DEPOSIT", amount, description, "Nạp tiền");
////////        // Sau dòng: long result = dbHelper.addTransaction(...)
////////        if (result != -1) {
////////            // Tạo thông báo
////////            dbHelper.addNotification(userId,
////////                    "Nạp tiền thành công",
////////                    "Bạn vừa nạp " + currencyFormat.format(amount) + " từ " + selectedBank,
////////                    "TRANSACTION",
////////                    (int) result);
////////
////////            // ... code tiếp theo
////////        }
////////        if (result != -1) {
////////            // Lấy ngày hiện tại
////////            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
////////            String currentDate = sdf.format(new Date());
////////
////////            // Chuyển sang màn hình thành công
////////            Intent intent = new Intent(TopUpActivity.this, TopUpSuccessActivity.class);
////////            intent.putExtra("bank", selectedBank);
////////            intent.putExtra("amount", amount);
////////            intent.putExtra("date", currentDate);
////////            startActivity(intent);
////////            finish();
////////        } else {
////////            Toast.makeText(this, "Nạp tiền thất bại", Toast.LENGTH_SHORT).show();
////////        }
////////    }
////////
////////    @Override
////////    protected void onDestroy() {
////////        super.onDestroy();
////////        if (dbHelper != null) {
////////            dbHelper.close();
////////        }
////////    }
////////}
//////
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
//////        // ✅ LẤY SỐ DƯ TỪ DATABASE (nguồn đáng tin cậy nhất)
//////        currentBalance = dbHelper.getBalance(userId);
//////
//////        // ✅ CẬP NHẬT SHAREDPREFERENCES
//////        SharedPreferences.Editor editor = sharedPreferences.edit();
//////        editor.putFloat("balance", (float) currentBalance);
//////        editor.apply();
//////
//////        // ✅ HIỂN THỊ
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
//////
//////        if (result != -1) {
//////            // ✅ LẤY SỐ DƯ MỚI TỪ DATABASE SAU KHI NẠP
//////            double newBalance = dbHelper.getBalance(userId);
//////
//////            // ✅ CẬP NHẬT VÀO SHAREDPREFERENCES
//////            SharedPreferences.Editor editor = sharedPreferences.edit();
//////            editor.putFloat("balance", (float) newBalance);
//////            editor.apply();
//////
//////            // Tạo thông báo
//////            dbHelper.addNotification(userId,
//////                    "Thu nhập",
//////                    currencyFormat.format(amount),
//////                    "INCOME",
//////                    (int) result);
//////
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
//////    protected void onResume() {
//////        super.onResume();
//////        loadCurrentBalance();
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
////import androidx.cardview.widget.CardView;
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
////    // Quick amount buttons
////    private CardView btn50k, btn100k, btn200k, btn500k;
////
////    private DatabaseHelper dbHelper;
////    private SharedPreferences sharedPreferences;
////    private int userId;
////    private double currentBalance;
////    private NumberFormat currencyFormat;
////    private NumberFormat plainFormat;
////
////    private long selectedAmount = 0;
////
////    private String[] banks = {
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
////        plainFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
////
////        initViews();
////        loadCurrentBalance();
////        setupListeners();
////        selectQuickAmount(100000, btn100k); // default select 100k
////    }
////
////    private void initViews() {
////        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
////        spinnerBank = findViewById(R.id.spinnerBank);
////        etAmount = findViewById(R.id.etAmount);
////        btnTopUp = findViewById(R.id.btnTopUp);
////        btnBack = findViewById(R.id.btnBack);
////
////        btn50k = findViewById(R.id.btn50k);
////        btn100k = findViewById(R.id.btn100k);
////        btn200k = findViewById(R.id.btn200k);
////        btn500k = findViewById(R.id.btn500k);
////
////        // Setup spinner
////        ArrayAdapter<String> adapter = new ArrayAdapter<>(
////                this,
////                android.R.layout.simple_spinner_item,
////                banks
////        );
////        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
////        spinnerBank.setAdapter(adapter);
////    }
////
////    private void loadCurrentBalance() {
////        currentBalance = dbHelper.getBalance(userId);
////        SharedPreferences.Editor editor = sharedPreferences.edit();
////        editor.putFloat("balance", (float) currentBalance);
////        editor.apply();
////
////        String balanceStr = currencyFormat.format(currentBalance);
////        tvCurrentBalance.setText("**** 4829  •  " + balanceStr + "đ");
////    }
////
////    private void setupListeners() {
////        btnBack.setOnClickListener(v -> finish());
////
////        btn50k.setOnClickListener(v -> selectQuickAmount(50000, btn50k));
////        btn100k.setOnClickListener(v -> selectQuickAmount(100000, btn100k));
////        btn200k.setOnClickListener(v -> selectQuickAmount(200000, btn200k));
////        btn500k.setOnClickListener(v -> selectQuickAmount(500000, btn500k));
////
////        etAmount.addTextChangedListener(new TextWatcher() {
////            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
////            @Override public void afterTextChanged(Editable s) {}
////
////            @Override
////            public void onTextChanged(CharSequence s, int start, int before, int count) {
////                String raw = s.toString().replace(".", "").replace(",", "").trim();
////                if (!raw.isEmpty()) {
////                    try {
////                        selectedAmount = Long.parseLong(raw);
////                        updateButtonText();
////                        clearQuickAmountHighlight();
////                    } catch (NumberFormatException e) {
////                        selectedAmount = 0;
////                    }
////                }
////            }
////        });
////
////        btnTopUp.setOnClickListener(v -> processTopUp());
////    }
////
////    private void selectQuickAmount(long amount, CardView selectedBtn) {
////        selectedAmount = amount;
////
////        // Reset all to white
////        btn50k.setCardBackgroundColor(getResources().getColor(android.R.color.white));
////        btn100k.setCardBackgroundColor(getResources().getColor(android.R.color.white));
////        btn200k.setCardBackgroundColor(getResources().getColor(android.R.color.white));
////        btn500k.setCardBackgroundColor(getResources().getColor(android.R.color.white));
////
////        // Set text colors back to dark
////        setQuickBtnColor(btn50k, "#0D0F3C");
////        setQuickBtnColor(btn100k, "#0D0F3C");
////        setQuickBtnColor(btn200k, "#0D0F3C");
////        setQuickBtnColor(btn500k, "#0D0F3C");
////
////        // Highlight selected
////        selectedBtn.setCardBackgroundColor(0xFFE8EAFF);
////        setQuickBtnColor(selectedBtn, "#3D5AFE");
////
////        // Update EditText display
////        etAmount.setText(currencyFormat.format(amount));
////        updateButtonText();
////    }
////
////    private void setQuickBtnColor(CardView card, String hexColor) {
////        View child = card.getChildAt(0);
////        if (child instanceof TextView) {
////            ((TextView) child).setTextColor(android.graphics.Color.parseColor(hexColor));
////        }
////    }
////
////    private void clearQuickAmountHighlight() {
////        btn50k.setCardBackgroundColor(getResources().getColor(android.R.color.white));
////        btn100k.setCardBackgroundColor(getResources().getColor(android.R.color.white));
////        btn200k.setCardBackgroundColor(getResources().getColor(android.R.color.white));
////        btn500k.setCardBackgroundColor(getResources().getColor(android.R.color.white));
////
////        setQuickBtnColor(btn50k, "#0D0F3C");
////        setQuickBtnColor(btn100k, "#0D0F3C");
////        setQuickBtnColor(btn200k, "#0D0F3C");
////        setQuickBtnColor(btn500k, "#0D0F3C");
////    }
////
////    private void updateButtonText() {
////        if (selectedAmount > 0) {
////            btnTopUp.setText("Nạp tiền  •  " + currencyFormat.format(selectedAmount) + "đ");
////        } else {
////            btnTopUp.setText("Nạp tiền");
////        }
////    }
////
////    private void processTopUp() {
////        if (selectedAmount <= 0) {
////            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
////            return;
////        }
////
////        String selectedBank = spinnerBank.getSelectedItem() != null
////                ? spinnerBank.getSelectedItem().toString()
////                : "Ngân hàng";
////
////        double amount = (double) selectedAmount;
////        String description = "Nạp tiền từ " + selectedBank;
////        long result = dbHelper.addTransaction(userId, "DEPOSIT", amount, description, "Nạp tiền");
////
////        if (result != -1) {
////            double newBalance = dbHelper.getBalance(userId);
////            SharedPreferences.Editor editor = sharedPreferences.edit();
////            editor.putFloat("balance", (float) newBalance);
////            editor.apply();
////
////            dbHelper.addNotification(userId,
////                    "Thu nhập",
////                    currencyFormat.format(amount),
////                    "INCOME",
////                    (int) result);
////
////            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
////            String currentDate = sdf.format(new Date());
////
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
////        if (dbHelper != null) dbHelper.close();
////    }
////}
//
//package com.example.ewallet_thang;
//
//import android.app.Activity;
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
//public class TopUpActivity extends AppCompatActivity {
//
//    // ── Request code cho màn hình chọn voucher ───────────────────────────────
//    private static final int REQUEST_VOUCHER = 2001;
//
//    private TextView tvCurrentBalance;
//    private Spinner  spinnerBank;
//    private EditText etAmount;
//    private Button   btnTopUp;
//    private ImageView btnBack;
//    private CardView btn50k, btn100k, btn200k, btn500k;
//
//    // ── [TÍCH HỢP MỚI] UI voucher ────────────────────────────────────────────
//    private CardView  cardVoucherSection;
//    private TextView  tvVoucherApplied;
//    private Button    btnSelectVoucher, btnRemoveVoucher;
//
//    private FirebaseFirestore  db;
//    private SharedPreferences  sharedPreferences;
//    private String             userPhone;
//    private double             currentBalance;
//    private NumberFormat       currencyFormat;
//    private long               selectedAmount = 0;
//
//    // ── [TÍCH HỢP MỚI] Trạng thái voucher ───────────────────────────────────
//    private String  appliedVoucherId   = null;
//    private long    appliedVoucherValue = 0;
//
//    private String[] banks = {
//            "Vietcombank","MB Bank","Vietinbank","TP Bank",
//            "Techcombank","ACB","VPBank","Agribank","BIDV","Sacombank"
//    };
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_topup);
//
//        db            = FirebaseFirestore.getInstance();
//        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
//        userPhone     = sharedPreferences.getString("userPhone", "");
//        currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
//
//        initViews();
//        loadCurrentBalance();
//        setupListeners();
//        selectQuickAmount(100000, btn100k);
//    }
//
//    private void initViews() {
//        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
//        spinnerBank      = findViewById(R.id.spinnerBank);
//        etAmount         = findViewById(R.id.etAmount);
//        btnTopUp         = findViewById(R.id.btnTopUp);
//        btnBack          = findViewById(R.id.btnBack);
//
//        btn50k  = findViewById(R.id.btn50k);
//        btn100k = findViewById(R.id.btn100k);
//        btn200k = findViewById(R.id.btn200k);
//        btn500k = findViewById(R.id.btn500k);
//
//        // ── [TÍCH HỢP MỚI] Voucher UI ─────────────────────────────────────
//        cardVoucherSection = findViewById(R.id.cardVoucherSection);
//        tvVoucherApplied   = findViewById(R.id.tvVoucherApplied);
//        btnSelectVoucher   = findViewById(R.id.btnSelectVoucher);
//        btnRemoveVoucher   = findViewById(R.id.btnRemoveVoucher);
//
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(
//                this, android.R.layout.simple_spinner_item, banks);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerBank.setAdapter(adapter);
//    }
//
//    private void loadCurrentBalance() {
//        currentBalance = sharedPreferences.getFloat("balance", 0);
//        tvCurrentBalance.setText("**** 4829  •  "
//                + currencyFormat.format(currentBalance) + "đ");
//    }
//
//    private void setupListeners() {
//        btnBack.setOnClickListener(v -> finish());
//        btn50k.setOnClickListener(v  -> selectQuickAmount(50000,  btn50k));
//        btn100k.setOnClickListener(v -> selectQuickAmount(100000, btn100k));
//        btn200k.setOnClickListener(v -> selectQuickAmount(200000, btn200k));
//        btn500k.setOnClickListener(v -> selectQuickAmount(500000, btn500k));
//
//        etAmount.addTextChangedListener(new TextWatcher() {
//            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
//            @Override public void afterTextChanged(Editable s) {}
//            @Override
//            public void onTextChanged(CharSequence s, int st, int b, int c) {
//                String raw = s.toString().replace(".", "").replace(",", "").trim();
//                if (!raw.isEmpty()) {
//                    try {
//                        selectedAmount = Long.parseLong(raw);
//                        updateButtonText();
//                    } catch (NumberFormatException e) { selectedAmount = 0; }
//                }
//            }
//        });
//
//        // ── [TÍCH HỢP MỚI] Nút chọn / xóa voucher ────────────────────────
//        if (btnSelectVoucher != null) {
//            btnSelectVoucher.setOnClickListener(v -> {
//                Intent intent = new Intent(this, VoucherActivity.class);
//                startActivityForResult(intent, REQUEST_VOUCHER);
//            });
//        }
//        if (btnRemoveVoucher != null) {
//            btnRemoveVoucher.setOnClickListener(v -> removeVoucher());
//        }
//
//        btnTopUp.setOnClickListener(v -> processTopUp());
//    }
//
//    // ── [TÍCH HỢP MỚI] Nhận kết quả chọn voucher ────────────────────────────
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_VOUCHER && resultCode == Activity.RESULT_OK && data != null) {
//            String voucherType = data.getStringExtra("voucherType");
//
//            // Chỉ chấp nhận voucher loại TOPUP tại màn hình nạp tiền
//            if (!"TOPUP".equals(voucherType)) {
//                Toast.makeText(this,
//                        "Voucher này chỉ dùng được khi chuyển khoản!",
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
//            updateButtonText();
//            Toast.makeText(this,
//                    "Áp dụng voucher thành công!", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void removeVoucher() {
//        appliedVoucherId    = null;
//        appliedVoucherValue = 0;
//        if (tvVoucherApplied  != null) tvVoucherApplied.setVisibility(View.GONE);
//        if (btnRemoveVoucher  != null) btnRemoveVoucher.setVisibility(View.GONE);
//        if (btnSelectVoucher  != null) btnSelectVoucher.setText("Chọn voucher");
//        updateButtonText();
//    }
//
//    private void selectQuickAmount(long amount, CardView selectedBtn) {
//        selectedAmount = amount;
//        etAmount.setText(currencyFormat.format(amount));
//        updateButtonText();
//    }
//
//    private void updateButtonText() {
//        long finalAmount = selectedAmount - appliedVoucherValue;
//        if (finalAmount < 0) finalAmount = 0;
//
//        if (appliedVoucherValue > 0 && selectedAmount > 0) {
//            btnTopUp.setText("Nạp " + currencyFormat.format(selectedAmount)
//                    + " - " + currencyFormat.format(appliedVoucherValue)
//                    + "đ = " + currencyFormat.format(finalAmount) + "đ");
//        } else if (selectedAmount > 0) {
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
//        btnTopUp.setEnabled(false);
//        btnTopUp.setText("Đang xử lý...");
//
//        String selectedBank = spinnerBank.getSelectedItem() != null
//                ? spinnerBank.getSelectedItem().toString() : "Ngân hàng";
//
//        // ── [TÍCH HỢP MỚI] Tính tiền thực nạp sau khi trừ voucher ─────────
//        double actualAmount = selectedAmount - appliedVoucherValue;
//        if (actualAmount < 0) actualAmount = 0;
//        double totalCreditAmount = selectedAmount; // Số tiền cộng vào ví = gốc (voucher là giảm phí)
//
//        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
//                Locale.getDefault()).format(new Date());
//
//        WriteBatch batch = db.batch();
//
//        // 1. Cộng tiền vào ví (cộng full selectedAmount – voucher là giảm chi phí nạp)
//        batch.update(db.collection("users").document(userPhone),
//                "balance", FieldValue.increment(totalCreditAmount));
//
//        // 2. Ghi giao dịch
//        Map<String, Object> trans = new HashMap<>();
//        trans.put("userPhone",   userPhone);
//        trans.put("type",        "DEPOSIT");
//        trans.put("amount",      totalCreditAmount);
//        trans.put("description", "Nạp tiền từ " + selectedBank
//                + (appliedVoucherValue > 0
//                ? " (Voucher -" + currencyFormat.format(appliedVoucherValue) + "đ)" : ""));
//        trans.put("category",    "Nạp tiền");
//        trans.put("date",        date);
//        trans.put("timestamp",   System.currentTimeMillis());
//        batch.set(db.collection("transactions").document(), trans);
//
//        // 3. Thông báo
//        Map<String, Object> notif = new HashMap<>();
//        notif.put("userPhone", userPhone);
//        notif.put("title",  "Thu nhập");
//        notif.put("message","+ " + currencyFormat.format(totalCreditAmount) + "đ");
//        notif.put("type",   "INCOME");
//        notif.put("date",   date);
//        notif.put("isRead", false);
//        batch.set(db.collection("notifications").document(), notif);
//
//        // 4. Đánh dấu voucher đã dùng (nếu có)
//        if (appliedVoucherId != null) {
//            batch.update(db.collection("vouchers").document(appliedVoucherId),
//                    "isUsed", true);
//        }
//
//        batch.commit().addOnSuccessListener(aVoid -> {
//            // ── [TÍCH HỢP MỚI] Tích điểm sau khi nạp tiền thành công ──────
//            PointsActivity.addPointsForTransaction(
//                    db, userPhone, totalCreditAmount,
//                    "Nạp tiền từ " + selectedBank);
//
//            // ── [TÍCH HỢP MỚI] Cashback tân thủ (giao dịch đầu tiên) ──────
//            NewUserBonusActivity.applyFirstTransactionCashback(
//                    db, sharedPreferences, userPhone, totalCreditAmount);
//
//            Intent intent = new Intent(this, TopUpSuccessActivity.class);
//            intent.putExtra("bank",   selectedBank);
//            intent.putExtra("amount", totalCreditAmount);
//            intent.putExtra("date",   date);
//            startActivity(intent);
//            finish();
//        }).addOnFailureListener(e -> {
//            Toast.makeText(this, "Lỗi mạng!", Toast.LENGTH_SHORT).show();
//            btnTopUp.setEnabled(true);
//            updateButtonText();
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

    private static final int REQUEST_VOUCHER = 2001;

    private TextView tvCurrentBalance;
    private Spinner  spinnerBank;
    private EditText etAmount;
    private Button   btnTopUp;
    private ImageView btnBack;
    private CardView btn50k, btn100k, btn200k, btn500k;

    // Voucher UI
    private CardView  cardVoucherSection;
    private TextView  tvVoucherApplied;
    private Button    btnSelectVoucher, btnRemoveVoucher;

    private FirebaseFirestore  db;
    private SharedPreferences  sharedPreferences;
    private String             userPhone;
    private double             currentBalance;
    private NumberFormat       currencyFormat;
    private long               selectedAmount = 0;

    // Voucher state
    private String  appliedVoucherId   = null;
    private long    appliedVoucherValue = 0;

    private String[] banks = {
            "Vietcombank","MB Bank","Vietinbank","TP Bank",
            "Techcombank","ACB","VPBank","Agribank","BIDV","Sacombank"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topup);

        db            = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
        userPhone     = sharedPreferences.getString("userPhone", "");
        currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

        initViews();
        loadCurrentBalance();
        setupListeners();
        selectQuickAmount(100000, btn100k);
    }

    private void initViews() {
        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
        spinnerBank      = findViewById(R.id.spinnerBank);
        etAmount         = findViewById(R.id.etAmount);
        btnTopUp         = findViewById(R.id.btnTopUp);
        btnBack          = findViewById(R.id.btnBack);

        btn50k  = findViewById(R.id.btn50k);
        btn100k = findViewById(R.id.btn100k);
        btn200k = findViewById(R.id.btn200k);
        btn500k = findViewById(R.id.btn500k);

        cardVoucherSection = findViewById(R.id.cardVoucherSection);
        tvVoucherApplied   = findViewById(R.id.tvVoucherApplied);
        btnSelectVoucher   = findViewById(R.id.btnSelectVoucher);
        btnRemoveVoucher   = findViewById(R.id.btnRemoveVoucher);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, banks);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spinnerBank != null) spinnerBank.setAdapter(adapter);
    }

    private void loadCurrentBalance() {
        currentBalance = sharedPreferences.getFloat("balance", 0);
        db.collection("users").document(userPhone).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Double bal = doc.getDouble("balance");
                        if (bal != null) {
                            currentBalance = bal;

                            sharedPreferences.edit().putFloat("balance", bal.floatValue()).apply();
                        }
                    }
                    updateBalanceDisplay();
                })
                .addOnFailureListener(e -> updateBalanceDisplay());
    }

    private void updateBalanceDisplay() {
        if (tvCurrentBalance != null) {
            tvCurrentBalance.setText("**** 4829  •  " + currencyFormat.format(currentBalance) + "đ");
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        if (btn50k  != null) btn50k.setOnClickListener(v  -> selectQuickAmount(50000,  btn50k));
        if (btn100k != null) btn100k.setOnClickListener(v -> selectQuickAmount(100000, btn100k));
        if (btn200k != null) btn200k.setOnClickListener(v -> selectQuickAmount(200000, btn200k));
        if (btn500k != null) btn500k.setOnClickListener(v -> selectQuickAmount(500000, btn500k));

        if (etAmount != null) {
            etAmount.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void afterTextChanged(Editable s) {}
                @Override
                public void onTextChanged(CharSequence s, int st, int b, int c) {
                    String raw = s.toString().replace(".", "").replace(",", "").trim();
                    if (!raw.isEmpty()) {
                        try {
                            selectedAmount = Long.parseLong(raw);
                            clearQuickHighlight();
                            updateButtonText();
                        } catch (NumberFormatException ignored) { selectedAmount = 0; }
                    } else {
                        selectedAmount = 0;
                        updateButtonText();
                    }
                }
            });
        }

        if (btnSelectVoucher != null) {
            btnSelectVoucher.setOnClickListener(v -> {
                Intent intent = new Intent(this, VoucherActivity.class);
                startActivityForResult(intent, REQUEST_VOUCHER);
            });
        }
        if (btnRemoveVoucher != null) {
            btnRemoveVoucher.setOnClickListener(v -> removeVoucher());
        }

        btnTopUp.setOnClickListener(v -> processTopUp());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VOUCHER && resultCode == Activity.RESULT_OK && data != null) {
            String voucherType = data.getStringExtra("voucherType");

            if (!"TOPUP".equals(voucherType)) {
                Toast.makeText(this, "Voucher này chỉ dùng khi chuyển khoản!", Toast.LENGTH_SHORT).show();
                return;
            }

            appliedVoucherId    = data.getStringExtra("voucherId");
            appliedVoucherValue = data.getLongExtra("voucherValue", 0);
            String code         = data.getStringExtra("voucherCode");

            if (tvVoucherApplied != null) {
                tvVoucherApplied.setText("🎟 " + code + "  (-" + currencyFormat.format(appliedVoucherValue) + "đ)");
                tvVoucherApplied.setVisibility(View.VISIBLE);
            }
            if (btnRemoveVoucher != null) btnRemoveVoucher.setVisibility(View.VISIBLE);
            if (btnSelectVoucher != null) btnSelectVoucher.setText("Đổi voucher");

            updateButtonText();
            Toast.makeText(this, "Áp dụng voucher thành công! 🎉", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeVoucher() {
        appliedVoucherId    = null;
        appliedVoucherValue = 0;
        if (tvVoucherApplied != null) tvVoucherApplied.setVisibility(View.GONE);
        if (btnRemoveVoucher != null) btnRemoveVoucher.setVisibility(View.GONE);
        if (btnSelectVoucher != null) btnSelectVoucher.setText("Chọn voucher");
        updateButtonText();
    }

    private void selectQuickAmount(long amount, CardView selectedBtn) {
        selectedAmount = amount;
        clearQuickHighlight();
        if (selectedBtn != null) {
            selectedBtn.setCardBackgroundColor(0xFFE8EAFF);
            setCardTextColor(selectedBtn, "#3D5AFE");
        }
        if (etAmount != null) etAmount.setText(currencyFormat.format(amount));
        updateButtonText();
    }

    private void clearQuickHighlight() {
        int white = 0xFFFFFFFF;
        String dark = "#0D0F3C";
        if (btn50k  != null) { btn50k.setCardBackgroundColor(white);  setCardTextColor(btn50k,  dark); }
        if (btn100k != null) { btn100k.setCardBackgroundColor(white); setCardTextColor(btn100k, dark); }
        if (btn200k != null) { btn200k.setCardBackgroundColor(white); setCardTextColor(btn200k, dark); }
        if (btn500k != null) { btn500k.setCardBackgroundColor(white); setCardTextColor(btn500k, dark); }
    }

    private void setCardTextColor(CardView card, String hexColor) {
        View child = card.getChildAt(0);
        if (child instanceof TextView) {
            ((TextView) child).setTextColor(android.graphics.Color.parseColor(hexColor));
        }
    }

    private void updateButtonText() {
        long finalAmount = Math.max(0, selectedAmount - appliedVoucherValue);
        if (appliedVoucherValue > 0 && selectedAmount > 0) {
            btnTopUp.setText("Nạp " + currencyFormat.format(selectedAmount)
                    + "đ  →  bạn trả " + currencyFormat.format(finalAmount) + "đ");
        } else if (selectedAmount > 0) {
            btnTopUp.setText("Nạp tiền  •  " + currencyFormat.format(selectedAmount) + "đ");
        } else {
            btnTopUp.setText("Nạp tiền");
        }
    }

    private void processTopUp() {
        if (selectedAmount <= 0) {
            Toast.makeText(this, "Vui lòng chọn hoặc nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedBank = (spinnerBank != null && spinnerBank.getSelectedItem() != null)
                ? spinnerBank.getSelectedItem().toString() : "Ngân hàng";

        double creditAmount = selectedAmount; // Số tiền cộng vào ví
        // appliedVoucherValue là số tiền được giảm chi phí nạp (ví dụ: nạp 100k, có voucher 20k → chỉ trả 80k ngân hàng)
        // Ví vẫn cộng 100k

        btnTopUp.setEnabled(false);
        btnTopUp.setText("Đang xử lý...");

        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String desc = "Nạp tiền từ " + selectedBank
                + (appliedVoucherValue > 0
                ? " (Voucher -" + currencyFormat.format(appliedVoucherValue) + "đ)" : "");

        WriteBatch batch = db.batch();

        // 1. Cộng tiền vào ví
        batch.update(db.collection("users").document(userPhone),
                "balance", FieldValue.increment(creditAmount));

        // 2. Giao dịch
        Map<String, Object> trans = new HashMap<>();
        trans.put("userPhone",   userPhone);
        trans.put("type",        "DEPOSIT");
        trans.put("amount",      creditAmount);
        trans.put("description", desc);
        trans.put("category",    "Nạp tiền");
        trans.put("date",        date);
        trans.put("timestamp",   System.currentTimeMillis());
        batch.set(db.collection("transactions").document(), trans);

        // 3. Thông báo
        Map<String, Object> notif = new HashMap<>();
        notif.put("userPhone", userPhone);
        notif.put("title",  "Thu nhập");
        notif.put("message","+ " + currencyFormat.format(creditAmount) + "đ");
        notif.put("type",   "INCOME");
        notif.put("date",   date);
        notif.put("isRead", false);
        batch.set(db.collection("notifications").document(), notif);

        // 4. Đánh dấu voucher đã dùng
        if (appliedVoucherId != null) {
            batch.update(db.collection("vouchers").document(appliedVoucherId), "isUsed", true);
        }

        batch.commit().addOnSuccessListener(aVoid -> {
            // ✅ Tích điểm: 10.000đ = 1 điểm
            PointsActivity.addPointsForTransaction(
                    db, userPhone, creditAmount, "Nạp tiền từ " + selectedBank);

            // ✅ Cashback 50% giao dịch đầu tiên (tối đa 50.000đ)
            NewUserBonusActivity.applyFirstTransactionCashback(
                    db, sharedPreferences, userPhone, creditAmount);

            sharedPreferences.edit()
                    .putFloat("balance", (float)(currentBalance + creditAmount))
                    .apply();

            Intent intent = new Intent(this, TopUpSuccessActivity.class);
            intent.putExtra("bank",   selectedBank);
            intent.putExtra("amount", creditAmount);
            intent.putExtra("date",   date);
            startActivity(intent);
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            btnTopUp.setEnabled(true);
            updateButtonText();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCurrentBalance();
    }
}