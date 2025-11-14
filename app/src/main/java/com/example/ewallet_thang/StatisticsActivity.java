// ==================== 2. StatisticsActivity.java - Nâng cấp ====================
package com.example.ewallet_thang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ewallet_thang.adapters.CategoryStatAdapter;
import com.example.ewallet_thang.database.DatabaseHelper;
import com.example.ewallet_thang.models.CategoryStat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {

    private TextView tvStatTotalIncome, tvStatTotalExpense, tvStatBalance, tvStatSavings;
    private RecyclerView rvCategoryStats;
    private BottomNavigationView bottomNav;
    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;
    private int userId;
    private NumberFormat currencyFormat;
    private CategoryStatAdapter categoryStatAdapter;
    private List<CategoryStat> categoryStatList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getInt("userId", -1);
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        initViews();
        loadStatistics();
        loadCategoryStatistics();
        setupBottomNav();
    }

    private void initViews() {
        tvStatTotalIncome = findViewById(R.id.tvStatTotalIncome);
        tvStatTotalExpense = findViewById(R.id.tvStatTotalExpense);
        tvStatBalance = findViewById(R.id.tvStatBalance);
        tvStatSavings = findViewById(R.id.tvStatSavings);
        rvCategoryStats = findViewById(R.id.rvCategoryStats);
        bottomNav = findViewById(R.id.bottomNav);

        // Setup RecyclerView
        categoryStatList = new ArrayList<>();
        categoryStatAdapter = new CategoryStatAdapter(this, categoryStatList);
        rvCategoryStats.setLayoutManager(new LinearLayoutManager(this));
        rvCategoryStats.setAdapter(categoryStatAdapter);
    }

    private void loadStatistics() {
        double totalIncome = dbHelper.getTotalIncome(userId);
        double totalExpense = dbHelper.getTotalExpense(userId);
        double balance = dbHelper.getBalance(userId);
        double savings = totalIncome - totalExpense;

        tvStatTotalIncome.setText(currencyFormat.format(totalIncome));
        tvStatTotalExpense.setText(currencyFormat.format(totalExpense));
        tvStatBalance.setText(currencyFormat.format(balance));
        tvStatSavings.setText(currencyFormat.format(savings));
    }

    private void loadCategoryStatistics() {
        categoryStatList.clear();

        Cursor cursor = dbHelper.getExpenseByCategory(userId);
        double totalExpense = dbHelper.getTotalExpense(userId);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String category = cursor.getString(0);
                double amount = cursor.getDouble(1);
                double percentage = (totalExpense > 0) ? (amount / totalExpense * 100) : 0;

                CategoryStat stat = new CategoryStat(category, amount, percentage);
                categoryStatList.add(stat);

            } while (cursor.moveToNext());
            cursor.close();
        }

        categoryStatAdapter.notifyDataSetChanged();
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_statistics);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(StatisticsActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_notification) {
                Intent intent = new Intent(StatisticsActivity.this, NotificationActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_statistics) {
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(StatisticsActivity.this, ProfileActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
