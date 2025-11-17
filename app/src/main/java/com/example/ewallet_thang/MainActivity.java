//package com.example.ewallet_thang;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.database.Cursor;
//import android.os.Bundle;
//import android.view.MenuItem;
//import android.widget.TextView;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.view.WindowCompat;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.ewallet_thang.LoginActivity;
//import com.google.android.material.bottomnavigation.BottomNavigationView;
//import com.google.android.material.card.MaterialCardView;
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//import com.example.ewallet_thang.database.DatabaseHelper;
//import com.example.ewallet_thang.adapters.TransactionAdapter;
//import com.example.ewallet_thang.models.Transaction;
//import java.text.NumberFormat;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Locale;
//
//public class MainActivity extends AppCompatActivity {
//
//    private TextView tvUserName, tvBalance, tvTotalIncome, tvTotalExpense;
//    private MaterialCardView cardSend, cardReceive, cardHistory;
//    private RecyclerView rvRecentTransactions;
//    private FloatingActionButton fabAddTransaction;
//    private BottomNavigationView bottomNav;
//
//    private DatabaseHelper dbHelper;
//    private SharedPreferences sharedPreferences;
//    private int userId;
//    private TransactionAdapter transactionAdapter;
//    private List<Transaction> transactionList;
//    private NumberFormat currencyFormat;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
//        setContentView(R.layout.activity_main);
//
//        dbHelper = new DatabaseHelper(this);
//        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
//        userId = sharedPreferences.getInt("userId", -1);
//
//        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
//
//        if (userId == -1) {
//            navigateToLogin();
//            return;
//        }
//
//        initViews();
//        setupListeners();
//        loadUserData();
//        loadRecentTransactions();
//    }
//
//    private void initViews() {
//        tvUserName = findViewById(R.id.tvUserName);
//        tvBalance = findViewById(R.id.tvBalance);
//        tvTotalIncome = findViewById(R.id.tvTotalIncome);
//        tvTotalExpense = findViewById(R.id.tvTotalExpense);
//
//        cardSend = findViewById(R.id.cardSend);
//        cardReceive = findViewById(R.id.cardReceive);
//        cardHistory = findViewById(R.id.cardHistory);
//        bottomNav = findViewById(R.id.bottomNav);
//
//        rvRecentTransactions = findViewById(R.id.rvRecentTransactions);
//        //fabAddTransaction = findViewById(R.id.fabAddTransaction);
//
//        // Setup RecyclerView
//        transactionList = new ArrayList<>();
//        transactionAdapter = new TransactionAdapter(this, transactionList);
//        rvRecentTransactions.setLayoutManager(new LinearLayoutManager(this));
//        rvRecentTransactions.setAdapter(transactionAdapter);
//    }
//
//    private void updateNotificationBadge() {
//        int unreadCount = dbHelper.getUnreadNotificationCount(userId);
//
//        if (unreadCount > 0) {
//            bottomNav.getOrCreateBadge(R.id.nav_notification).setNumber(unreadCount);
//        } else {
//            bottomNav.removeBadge(R.id.nav_notification);
//        }
//    }
//
//    private void setupListeners() {
//        // ACTIVATED: Transfer functionality
//        cardSend.setOnClickListener(v -> {
//            Intent intent = new Intent(MainActivity.this, TransferActivity.class);
//            startActivity(intent);
//        });
//
//        // ACTIVATED: Top Up functionality
//        cardReceive.setOnClickListener(v -> {
//            Intent intent = new Intent(MainActivity.this, TopUpActivity.class);
//            startActivity(intent);
//        });
//
//        // ACTIVATED: Transaction History functionality
//        cardHistory.setOnClickListener(v -> {
//            Intent intent = new Intent(MainActivity.this, TransactionHistoryActivity.class);
//            startActivity(intent);
//        });
//
//        bottomNav.setOnItemSelectedListener(this::onNavigationItemSelected);
//    }
//
//    private boolean onNavigationItemSelected(MenuItem item) {
//        int itemId = item.getItemId();
//
//        if (itemId == R.id.nav_home) {
//            return true;
//        } else if (itemId == R.id.nav_notification) {
//            Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
//            startActivity(intent);
//            return true;
//        } else if (itemId == R.id.nav_statistics) {
//            Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
//            startActivity(intent);
//            return true;
//        } else if (itemId == R.id.nav_profile) {
//            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
//            startActivity(intent);
//            return true;
//        }
//        return false;
//    }
//
//    private void loadUserData() {
//        String firstName = sharedPreferences.getString("firstName", "");
//        String lastName = sharedPreferences.getString("lastName", "");
//
//        tvUserName.setText("Xin chào, " + firstName + " " + lastName);
//
//        double balance = dbHelper.getBalance(userId);
//        tvBalance.setText(currencyFormat.format(balance));
//
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putFloat("balance", (float) balance);
//        editor.apply();
//
//        // Load statistics
//        double totalIncome = dbHelper.getTotalIncome(userId);
//        double totalExpense = dbHelper.getTotalExpense(userId);
//
//        tvTotalIncome.setText(currencyFormat.format(totalIncome));
//        tvTotalExpense.setText(currencyFormat.format(totalExpense));
//    }
//
//    private void loadRecentTransactions() {
//        transactionList.clear();
//
//        Cursor cursor = dbHelper.getAllTransactions(userId);
//
//        if (cursor != null && cursor.moveToFirst()) {
//            int count = 0;
//            do {
//                if (count >= 5) break;
//                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_ID));
//                String type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_TYPE));
//                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_AMOUNT));
//                String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_DESCRIPTION));
//                String category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_CATEGORY));
//                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_DATE));
//
//                Transaction transaction = new Transaction(id, userId, type, amount, description, category, date);
//                transactionList.add(transaction);
//                count++;
//
//            } while (cursor.moveToNext());
//
//            cursor.close();
//        }
//
//        transactionAdapter.notifyDataSetChanged();
//    }
//
//    private void navigateToLogin() {
//        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
//        finish();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        loadUserData();
//        loadRecentTransactions();
//        updateNotificationBadge();
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

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast; // <-- THÊM MỚI

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat; // <-- THÊM MỚI
import androidx.core.content.ContextCompat; // <-- THÊM MỚI
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ewallet_thang.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.ewallet_thang.database.DatabaseHelper;
import com.example.ewallet_thang.adapters.TransactionAdapter;
import com.example.ewallet_thang.models.Transaction;
import com.google.zxing.integration.android.IntentIntegrator; // <-- THÊM MỚI
import com.google.zxing.integration.android.IntentResult; // <-- THÊM MỚI

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 1001; // <-- THÊM MỚI

    private TextView tvUserName, tvBalance, tvTotalIncome, tvTotalExpense;
    // SỬA: Thêm cardScanQR
    private MaterialCardView cardSend, cardReceive, cardHistory, cardScanQR;
    private RecyclerView rvRecentTransactions;
    private BottomNavigationView bottomNav;

    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;
    private int userId;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactionList;
    private NumberFormat currencyFormat;

    private double currentBalance = 0; // <-- THÊM MỚI (để dùng cho QR)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getInt("userId", -1);

        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        if (userId == -1) {
            navigateToLogin();
            return;
        }

        initViews();
        setupListeners();
        loadUserData();
        loadRecentTransactions();
        checkCameraPermission(); // <-- THÊM MỚI
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tvUserName);
        tvBalance = findViewById(R.id.tvBalance);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);

        cardSend = findViewById(R.id.cardSend);
        cardReceive = findViewById(R.id.cardReceive);
        cardHistory = findViewById(R.id.cardHistory);
        cardScanQR = findViewById(R.id.cardScanQR); // <-- THÊM MỚI
        bottomNav = findViewById(R.id.bottomNav);

        rvRecentTransactions = findViewById(R.id.rvRecentTransactions);

        // Setup RecyclerView
        transactionList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(this, transactionList);
        rvRecentTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvRecentTransactions.setAdapter(transactionAdapter);
    }

    private void updateNotificationBadge() {
        int unreadCount = dbHelper.getUnreadNotificationCount(userId);

        if (unreadCount > 0) {
            bottomNav.getOrCreateBadge(R.id.nav_notification).setNumber(unreadCount);
        } else {
            bottomNav.removeBadge(R.id.nav_notification);
        }
    }

    private void setupListeners() {
        // ACTIVATED: Transfer functionality
        cardSend.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TransferActivity.class);
            startActivity(intent);
        });

        // ACTIVATED: Top Up functionality
        cardReceive.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TopUpActivity.class);
            startActivity(intent);
        });

        // ACTIVATED: Transaction History functionality
        cardHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TransactionHistoryActivity.class);
            startActivity(intent);
        });

        // ACTIVATED: QR Scan functionality
        cardScanQR.setOnClickListener(v -> startQrScanner()); // <-- THÊM MỚI

        bottomNav.setOnItemSelectedListener(this::onNavigationItemSelected);
    }

    // ... (Hàm onNavigationItemSelected giữ nguyên)
    private boolean onNavigationItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            return true;
        } else if (itemId == R.id.nav_notification) {
            Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.nav_statistics) {
            Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.nav_profile) {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
            return true;
        }
        return false;
    }


    private void loadUserData() {
        String firstName = sharedPreferences.getString("firstName", "");
        String lastName = sharedPreferences.getString("lastName", "");

        tvUserName.setText("Xin chào, " + firstName + " " + lastName);

        // SỬA: Gán vào biến global
        currentBalance = dbHelper.getBalance(userId);
        tvBalance.setText(currencyFormat.format(currentBalance));

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("balance", (float) currentBalance);
        editor.apply();

        // Load statistics
        double totalIncome = dbHelper.getTotalIncome(userId);
        double totalExpense = dbHelper.getTotalExpense(userId);

        tvTotalIncome.setText(currencyFormat.format(totalIncome));
        tvTotalExpense.setText(currencyFormat.format(totalExpense));
    }

    // ... (Hàm loadRecentTransactions giữ nguyên)
    private void loadRecentTransactions() {
        transactionList.clear();
        Cursor cursor = dbHelper.getAllTransactions(userId);
        if (cursor != null && cursor.moveToFirst()) {
            int count = 0;
            do {
                if (count >= 5) break;
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_ID));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_TYPE));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_AMOUNT));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_DESCRIPTION));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_CATEGORY));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_DATE));
                Transaction transaction = new Transaction(id, userId, type, amount, description, category, date);
                transactionList.add(transaction);
                count++;
            } while (cursor.moveToNext());
            cursor.close();
        }
        transactionAdapter.notifyDataSetChanged();
    }

    // ... (Hàm navigateToLogin giữ nguyên)
    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
        loadRecentTransactions();
        updateNotificationBadge();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    // =============================================================
    // CÁC HÀM MỚI (SAO CHÉP TỪ TRANSFERACTIVITY)
    // =============================================================

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_REQUEST_CODE
            );
        }
    }

    private void startQrScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Đưa mã QR vào khung hình");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Đã hủy quét mã QR", Toast.LENGTH_SHORT).show();
            } else {
                handleQrContent(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleQrContent(String qr) {
        try {
            String[] p = qr.split("\\|");

            if (p.length < 4 || !p[0].equals("EWALLET")) {
                Toast.makeText(this, "Mã QR không đúng định dạng!", Toast.LENGTH_SHORT).show();
                return;
            }

            int id = Integer.parseInt(p[1]);
            String name = p[2];
            String phone = p[3];

            // Nếu là user mặc định (ID >= 10000) thì bỏ qua việc kiểm tra DB
            if (id < 10000) {
                Cursor c = dbHelper.getUserById(id);
                if (c == null || !c.moveToFirst()) {
                    Toast.makeText(this, "Người dùng không tồn tại!", Toast.LENGTH_SHORT).show();
                    return;
                }
                c.close();
            }

            // Đây chính là "ra thông tin giao dịch" (màn hình xác nhận)
            Intent intent = new Intent(MainActivity.this, TransferConfirmActivity.class);
            intent.putExtra("recipientId", id);
            intent.putExtra("recipientName", name);
            intent.putExtra("recipientPhone", phone);
            // SỬA: Lấy balance từ biến global đã lưu
            intent.putExtra("currentBalance", currentBalance);
            startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi đọc mã QR", Toast.LENGTH_SHORT).show();
        }
    }
}