package com.example.ewallet_thang;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ewallet_thang.adapters.TransactionHistoryAdapter;
import com.example.ewallet_thang.database.DatabaseHelper;
import com.example.ewallet_thang.models.Transaction;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionHistoryActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvTotalTransactions, tvTotalAmount;
    private ChipGroup chipGroupFilter;
    private Chip chipAll, chipIncome, chipExpense, chipTransfer;
    private RecyclerView rvTransactions;
    private LinearLayout tvEmptyState;

    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;
    private int userId;
    private TransactionHistoryAdapter adapter;
    private List<Transaction> transactionList;
    private List<Transaction> allTransactions;
    private NumberFormat currencyFormat;

    private String currentFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getInt("userId", -1);
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        initViews();
        setupListeners();
        loadAllTransactions();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTotalTransactions = findViewById(R.id.tvTotalTransactions);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);
        chipAll = findViewById(R.id.chipAll);
        chipIncome = findViewById(R.id.chipIncome);
        chipExpense = findViewById(R.id.chipExpense);
        chipTransfer = findViewById(R.id.chipTransfer);
        rvTransactions = findViewById(R.id.rvTransactions);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        // Setup RecyclerView
        transactionList = new ArrayList<>();
        allTransactions = new ArrayList<>();
        adapter = new TransactionHistoryAdapter(this, transactionList);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                chipAll.setChecked(true);
                return;
            }

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipAll) {
                currentFilter = "ALL";
            } else if (checkedId == R.id.chipIncome) {
                currentFilter = "INCOME";
            } else if (checkedId == R.id.chipExpense) {
                currentFilter = "EXPENSE";
            } else if (checkedId == R.id.chipTransfer) {
                currentFilter = "TRANSFER";
            }
            filterTransactions();
        });
    }

    private void loadAllTransactions() {
        allTransactions.clear();
        Cursor cursor = dbHelper.getAllTransactions(userId);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_ID));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_TYPE));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_AMOUNT));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_DESCRIPTION));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_CATEGORY));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_DATE));

                Transaction transaction = new Transaction(id, userId, type, amount, description, category, date);
                allTransactions.add(transaction);
            } while (cursor.moveToNext());

            cursor.close();
        }

        filterTransactions();
    }

    private void filterTransactions() {
        transactionList.clear();

        if (currentFilter.equals("ALL")) {
            transactionList.addAll(allTransactions);
        } else {
            for (Transaction transaction : allTransactions) {
                if (currentFilter.equals("INCOME") &&
                        (transaction.getTransactionType().equals("INCOME") || transaction.getTransactionType().equals("DEPOSIT"))) {
                    transactionList.add(transaction);
                } else if (currentFilter.equals("EXPENSE") &&
                        (transaction.getTransactionType().equals("EXPENSE") || transaction.getTransactionType().equals("WITHDRAW"))) {
                    transactionList.add(transaction);
                } else if (currentFilter.equals("TRANSFER") && transaction.getTransactionType().equals("TRANSFER")) {
                    transactionList.add(transaction);
                }
            }
        }

        updateStatistics();
        adapter.notifyDataSetChanged();

        // Show/hide empty state
        if (transactionList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvTransactions.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvTransactions.setVisibility(View.VISIBLE);
        }
    }

    private void updateStatistics() {
        int totalCount = transactionList.size();
        double totalAmount = 0;

        for (Transaction transaction : transactionList) {
            if (transaction.getTransactionType().equals("INCOME") || transaction.getTransactionType().equals("DEPOSIT")) {
                totalAmount += transaction.getAmount();
            } else if (transaction.getTransactionType().equals("EXPENSE") ||
                    transaction.getTransactionType().equals("WITHDRAW") ||
                    transaction.getTransactionType().equals("TRANSFER")) {
                totalAmount -= transaction.getAmount();
            }
        }

        tvTotalTransactions.setText(totalCount + " giao dịch");

        String amountText = currencyFormat.format(Math.abs(totalAmount));
        if (totalAmount >= 0) {
            tvTotalAmount.setText("+" + amountText);
            tvTotalAmount.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvTotalAmount.setText("-" + amountText);
            tvTotalAmount.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}