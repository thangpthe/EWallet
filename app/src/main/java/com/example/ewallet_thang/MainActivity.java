package com.example.ewallet_thang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvUserName, tvBalance, tvTotalIncome, tvTotalExpense;
    private MaterialCardView cardSend, cardReceive, cardTopUp, cardWithdraw;
    private RecyclerView rvRecentTransactions;
    private FloatingActionButton fabAddTransaction;
    private BottomNavigationView bottomNav;

    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;
    private int userId;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactionList;
    private NumberFormat currencyFormat;

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
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tvUserName);
        tvBalance = findViewById(R.id.tvBalance);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);

        cardSend = findViewById(R.id.cardSend);
        cardReceive = findViewById(R.id.cardReceive);
        cardTopUp = findViewById(R.id.cardTopUp);
        cardWithdraw = findViewById(R.id.cardWithdraw);

        rvRecentTransactions = findViewById(R.id.rvRecentTransactions);
        fabAddTransaction = findViewById(R.id.fabAddTransaction);
        bottomNav = findViewById(R.id.bottomNav);

        // Setup RecyclerView
        transactionList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(this, transactionList);
        rvRecentTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvRecentTransactions.setAdapter(transactionAdapter);
    }

    private void setupListeners() {
        // ACTIVATED: Transfer functionality
        cardSend.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TransferActivity.class);
            startActivity(intent);
        });

        // ACTIVATED: Top Up functionality
        cardTopUp.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TopUpActivity.class);
            startActivity(intent);
        });
        // TODO: Implement other features
//        cardReceive.setOnClickListener(v -> {
//            Intent intent = new Intent(MainActivity.this, ReceiveActivity.class);
//            startActivity(intent);
//        });
//
//        cardTopUp.setOnClickListener(v -> {
//            Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
//            intent.putExtra("type", "DEPOSIT");
//            startActivity(intent);
//        });
//
//        cardWithdraw.setOnClickListener(v -> {
//            Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
//            intent.putExtra("type", "WITHDRAW");
//            startActivity(intent);
//        });
//
//        fabAddTransaction.setOnClickListener(v -> {
//            Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
//            startActivity(intent);
//        });

        bottomNav.setOnItemSelectedListener(this::onNavigationItemSelected);
    }

    private boolean onNavigationItemSelected(MenuItem item) {
        int itemId = item.getItemId();

//        if (itemId == R.id.nav_home) {
//            return true;
//        } else if (itemId == R.id.nav_transactions) {
//            Intent intent = new Intent(MainActivity.this, TransactionsActivity.class);
//            startActivity(intent);
//            return true;
//        } else if (itemId == R.id.nav_cards) {
//            Intent intent = new Intent(MainActivity.this, CardsActivity.class);
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

        return false;
    }

    private void loadUserData() {
        String firstName = sharedPreferences.getString("firstName", "");
        String lastName = sharedPreferences.getString("lastName", "");

        tvUserName.setText("Xin chào, " + firstName + " " + lastName);

        // Load balance
        double balance = dbHelper.getBalance(userId);
        tvBalance.setText(currencyFormat.format(balance));

        // Update SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("balance", (float) balance);
        editor.apply();

        // Load statistics
        double totalIncome = dbHelper.getTotalIncome(userId);
        double totalExpense = dbHelper.getTotalExpense(userId);

        tvTotalIncome.setText(currencyFormat.format(totalIncome));
        tvTotalExpense.setText(currencyFormat.format(totalExpense));
    }

    private void loadRecentTransactions() {
        transactionList.clear();

        Cursor cursor = dbHelper.getAllTransactions(userId);

        if (cursor != null && cursor.moveToFirst()) {
            int count = 0;
            do {
                if (count >= 5) break; // Show only 5 recent transactions

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}