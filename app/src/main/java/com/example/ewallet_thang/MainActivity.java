package com.example.ewallet_thang;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ewallet_thang.database.DatabaseHelper;
import com.example.ewallet_thang.adapters.TransactionAdapter;
import com.example.ewallet_thang.models.Transaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 1001;

    private TextView tvUserName, tvBalance, tvTotalIncome, tvTotalExpense;
    private MaterialCardView cardSend, cardReceive, cardHistory, cardScanQR;
    private RecyclerView rvRecentTransactions;
    private BottomNavigationView bottomNav;

    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;
    private int userId;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactionList;

    private NumberFormat currencyFormat;
    private double currentBalance = 0;

    // ===============================
    // 5 USER MẶC ĐỊNH
    // ===============================
    private final List<RecipientUser> defaultUsers = new ArrayList<>();

    private void loadDefaultUsers() {
        defaultUsers.add(new RecipientUser(10001, "Nguyễn Minh", "Đức", "0901122334"));
        defaultUsers.add(new RecipientUser(10002, "Lê Hoàng", "Nam", "0902233445"));
        defaultUsers.add(new RecipientUser(10003, "Trần Ngọc", "Vy", "0903344556"));
        defaultUsers.add(new RecipientUser(10004, "Phạm Gia", "Bảo", "0904455667"));
        defaultUsers.add(new RecipientUser(10005, "Đoàn Khánh", "Linh", "0905566778"));
    }

    static class RecipientUser {
        int id;
        String firstName, lastName, phone;

        RecipientUser(int id, String fn, String ln, String p) {
            this.id = id;
            this.firstName = fn;
            this.lastName = ln;
            this.phone = p;
        }
    }

    // ===============================

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

        loadDefaultUsers();
        initViews();
        setupListeners();
        loadUserData();
        loadRecentTransactions();
        checkCameraPermission();
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tvUserName);
        tvBalance = findViewById(R.id.tvBalance);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);

        cardSend = findViewById(R.id.cardSend);
        cardReceive = findViewById(R.id.cardReceive);
        cardHistory = findViewById(R.id.cardHistory);
        cardScanQR = findViewById(R.id.cardScanQR);

        bottomNav = findViewById(R.id.bottomNav);

        rvRecentTransactions = findViewById(R.id.rvRecentTransactions);

        transactionList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(this, transactionList);

        rvRecentTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvRecentTransactions.setAdapter(transactionAdapter);
    }

    private void setupListeners() {
        cardSend.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, TransferActivity.class)));
        cardReceive.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, TopUpActivity.class)));
        cardHistory.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, TransactionHistoryActivity.class)));

        cardScanQR.setOnClickListener(v -> startQrScanner());

        bottomNav.setOnItemSelectedListener(this::onNavigationItemSelected);
    }

    private boolean onNavigationItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.nav_home) return true;
        if (item.getItemId() == R.id.nav_notification) {
            startActivity(new Intent(MainActivity.this, NotificationActivity.class));
            return true;
        }
        if (item.getItemId() == R.id.nav_statistics) {
            startActivity(new Intent(MainActivity.this, StatisticsActivity.class));
            return true;
        }
        if (item.getItemId() == R.id.nav_profile) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            return true;
        }
        return false;
    }

    private void loadUserData() {
        String firstName = sharedPreferences.getString("firstName", "");
        String lastName = sharedPreferences.getString("lastName", "");
        tvUserName.setText("Xin chào, " + firstName + " " + lastName);

        currentBalance = dbHelper.getBalance(userId);
        tvBalance.setText(currencyFormat.format(currentBalance));

        sharedPreferences.edit().putFloat("balance", (float) currentBalance).apply();

        tvTotalIncome.setText(currencyFormat.format(dbHelper.getTotalIncome(userId)));
        tvTotalExpense.setText(currencyFormat.format(dbHelper.getTotalExpense(userId)));
    }

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
                String desc = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_DESCRIPTION));
                String cat = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_CATEGORY));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_DATE));

                transactionList.add(new Transaction(id, userId, type, amount, desc, cat, date));
                count++;

            } while (cursor.moveToNext());

            cursor.close();
        }
        transactionAdapter.notifyDataSetChanged();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // =============================== QR CODE ===============================

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
            String[] parts = qr.split("\\|");

            if (parts.length < 4 || !parts[0].equals("EWALLET")) {
                Toast.makeText(this, "Mã QR không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            int id = Integer.parseInt(parts[1]);
            String name = parts[2];
            String phone = parts[3];

            // Nếu là user mặc định (ID >= 10000) → bỏ qua DB
            if (id < 10000) {
                Cursor c = dbHelper.getUserById(id);
                if (c == null || !c.moveToFirst()) {
                    Toast.makeText(this, "Người dùng không tồn tại!", Toast.LENGTH_SHORT).show();
                    return;
                }
                c.close();
            }

            Intent i = new Intent(MainActivity.this, TransferConfirmActivity.class);
            i.putExtra("recipientId", id);
            i.putExtra("recipientName", name);
            i.putExtra("recipientPhone", phone);
            i.putExtra("currentBalance", currentBalance);
            startActivity(i);

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi đọc QR!", Toast.LENGTH_SHORT).show();
        }
    }
}
