package com.example.ewallet_thang;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ewallet_thang.adapters.TransactionAdapter;
import com.example.ewallet_thang.models.Transaction;
import com.example.ewallet_thang.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;

    private TextView tvUserName, tvBalance, tvTotalIncome, tvTotalExpense;
    private LinearLayout btnSend, btnReceive, btnScanQR, btnHistory, btnPromotion;
    private RecyclerView rvRecentTransactions;
    private BottomNavigationView bottomNav;

    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;
    private String userPhone;
    private double currentBalance = 0;
    private NumberFormat currencyFormat;

    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactionList;

    private final List<User> defaultUsers = new ArrayList<>();

    // Scanner launcher
    private final ActivityResultLauncher<Intent> qrScannerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            String qrData = result.getData().getStringExtra("SCAN_RESULT");
                            handleQrContent(qrData);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
        userPhone = sharedPreferences.getString("userPhone", "");
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        loadDefaultUsers(); // 🔥 QUAN TRỌNG

        initViews();
        setupListeners();
        loadUserData();
        loadRecentTransactions();
    }

    // ================== USER MẶC ĐỊNH ==================
    private void loadDefaultUsers() {
        defaultUsers.clear();

        defaultUsers.add(new User(10001, "Nguyễn", "Minh Đức", "0901122334", "", 0, ""));
        defaultUsers.add(new User(10002, "Lê", "Hoàng Nam", "0902233445", "", 0, ""));
        defaultUsers.add(new User(10003, "Trần", "Ngọc Vy", "0903344556", "", 0, ""));
        defaultUsers.add(new User(10004, "Phạm", "Gia Bảo", "0904455667", "", 0, ""));
        defaultUsers.add(new User(10005, "Đoàn", "Khánh Linh", "0905566778", "", 0, ""));
    }

    // ================== UI ==================
    private void initViews() {
        tvUserName = findViewById(R.id.tvUserName);
        tvBalance = findViewById(R.id.tvBalance);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);

        btnSend = findViewById(R.id.btnSend);
        btnReceive = findViewById(R.id.btnReceive);
        btnScanQR = findViewById(R.id.btnScanQR);
        btnHistory = findViewById(R.id.btnHistory);
        btnPromotion = findViewById(R.id.btnPromotion);

        rvRecentTransactions = findViewById(R.id.rvRecentTransactions);
        bottomNav = findViewById(R.id.bottomNav);

        transactionList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(this, transactionList);
        rvRecentTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvRecentTransactions.setAdapter(transactionAdapter);
    }

    private void setupListeners() {
        btnSend.setOnClickListener(v -> startActivity(new Intent(this, PaymentActivity.class)));
        btnReceive.setOnClickListener(v -> startActivity(new Intent(this, TopUpActivity.class)));
        btnHistory.setOnClickListener(v -> startActivity(new Intent(this, TransactionHistoryActivity.class)));
        btnScanQR.setOnClickListener(v -> checkCameraPermission());
        btnPromotion.setOnClickListener(v -> startActivity(new Intent(this, PromotionActivity.class)));

        bottomNav.setOnItemSelectedListener(this::onNavigationItemSelected);
    }

    // ================== QR ==================
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            startQrScanner();
        }
    }

    private void startQrScanner() {
        qrScannerLauncher.launch(new Intent(this, CustomScannerActivity.class));
    }

    private void handleQrContent(String qr) {
        if (qr == null) return;

        try {
            String[] parts = qr.split("\\|");

            if (parts.length < 2 || !parts[0].equals("EWALLET")) {
                Toast.makeText(this, "QR không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            int userId = Integer.parseInt(parts[1]);

            // 🔥 TÌM USER TRONG LIST
            for (User user : defaultUsers) {
                if (user.getUserId() == userId) {
                    openTransfer(user);
                    return;
                }
            }

            Toast.makeText(this, "User không tồn tại!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi đọc QR!", Toast.LENGTH_SHORT).show();
        }
    }

    private void openTransfer(User user) {
        Intent i = new Intent(this, TransferConfirmActivity.class);
        i.putExtra("recipientName", user.getFullName());
        i.putExtra("recipientPhone", user.getPhone());
        i.putExtra("currentBalance", currentBalance);
        startActivity(i);
    }

    // ================== DATA ==================
    private void loadUserData() {
        if (userPhone.isEmpty()) return;

        db.collection("users").document(userPhone)
                .addSnapshotListener((doc, e) -> {
                    if (doc != null && doc.exists()) {
                        String name = doc.getString("firstName") + " " + doc.getString("lastName");
                        currentBalance = doc.getDouble("balance") != null ? doc.getDouble("balance") : 0;

                        tvUserName.setText(name);
                        tvBalance.setText(currencyFormat.format(currentBalance));

                        sharedPreferences.edit()
                                .putFloat("balance", (float) currentBalance)
                                .apply();
                    }
                });
    }
//    private void loadRecentTransactions() {
//        db.collection("transactions")
//                .whereEqualTo("userPhone", userPhone)
//                .orderBy("timestamp", Query.Direction.DESCENDING)
//                .limit(5)
//                .addSnapshotListener((value, error) -> {
//                    if (error != null || value == null) return;
//
//                    transactionList.clear();
//
//                    for (DocumentSnapshot doc : value.getDocuments()) {
//                        String type        = doc.getString("type");
//                        Double amount      = doc.getDouble("amount");
//                        String description = doc.getString("description");
//                        String date        = doc.getString("date");
//                        String category    = doc.getString("category");
//
//                        if (type != null && amount != null) {
//                            int id = doc.getId().hashCode();
//                            // Truyền đầy đủ dữ liệu
//                            transactionList.add(new Transaction(id, 0, type, amount, description, category, date));
//                        }
//                    }
//
//                    transactionAdapter.notifyDataSetChanged();
//                });
//    }

    private void loadRecentTransactions() {
        // 1. Load 5 giao dịch gần nhất để hiển thị danh sách
        db.collection("transactions")
                .whereEqualTo("userPhone", userPhone)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(5)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    transactionList.clear();

                    for (DocumentSnapshot doc : value.getDocuments()) {
                        String type        = doc.getString("type");
                        Double amount      = doc.getDouble("amount");
                        String description = doc.getString("description");
                        String date        = doc.getString("date");
                        String category    = doc.getString("category");

                        if (type != null && amount != null) {
                            int id = doc.getId().hashCode();
                            transactionList.add(new Transaction(id, 0, type, amount, description, category, date));
                        }
                    }
                    transactionAdapter.notifyDataSetChanged();
                });

        // 2. Tính tổng thu nhập & tổng chi tiêu từ TẤT CẢ giao dịch
        db.collection("transactions")
                .whereEqualTo("userPhone", userPhone)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    double totalIncome = 0;
                    double totalExpense = 0;

                    for (DocumentSnapshot doc : value.getDocuments()) {
                        String type = doc.getString("type");
                        Double amount = doc.getDouble("amount");

                        if (type == null || amount == null) continue;

                        if ("INCOME".equals(type) || "DEPOSIT".equals(type)) {
                            totalIncome += amount;
                        } else if ("EXPENSE".equals(type) || "WITHDRAW".equals(type) || "TRANSFER".equals(type)) {
                            totalExpense += amount;
                        }
                    }

                    // Hiển thị tổng thu nhập
                    if (tvTotalIncome != null) {
                        tvTotalIncome.setText("+" + currencyFormat.format(totalIncome));
                    }

                    // Hiển thị tổng chi tiêu
                    if (tvTotalExpense != null) {
                        tvTotalExpense.setText("-" + currencyFormat.format(totalExpense));
                    }
                });
    }
    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_home) return true;
        if (item.getItemId() == R.id.nav_statistics)
            startActivity(new Intent(this, StatisticsActivity.class));
        if (item.getItemId() == R.id.nav_notification)
            startActivity(new Intent(this, NotificationActivity.class));
        if (item.getItemId() == R.id.nav_profile)
            startActivity(new Intent(this, ProfileActivity.class));
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            startQrScanner();
        }
    }
}