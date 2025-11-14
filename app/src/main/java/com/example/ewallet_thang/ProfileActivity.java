// ==================== 1. ProfileActivity.java - Nâng cấp ====================
package com.example.ewallet_thang;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ewallet_thang.database.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import java.text.NumberFormat;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfilePhone, tvProfileBirthDate, tvAccountBalance;
    private MaterialCardView cardChangePassword, cardAccountInfo, cardSettings;
    private Button btnLogout;
    private BottomNavigationView bottomNav;
    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;
    private int userId;
    private NumberFormat currencyFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getInt("userId", -1);
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        if (userId == -1) {
            logout();
            return;
        }

        initViews();
        loadProfileData();
        setupListeners();
    }

    private void initViews() {
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);
        tvProfileBirthDate = findViewById(R.id.tvProfileBirthDate);
        tvAccountBalance = findViewById(R.id.tvAccountBalance);

        cardChangePassword = findViewById(R.id.cardChangePassword);
//        cardAccountInfo = findViewById(R.id.cardAccountInfo);
        cardSettings = findViewById(R.id.cardSettings);

        btnLogout = findViewById(R.id.btnLogout);
        bottomNav = findViewById(R.id.bottomNav);
    }

    private void loadProfileData() {
        Cursor cursor = dbHelper.getUserById(userId);
        if (cursor != null && cursor.moveToFirst()) {
            String firstName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FIRST_NAME));
            String lastName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LAST_NAME));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE));
            String birthDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BIRTH_DATE));
            double balance = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BALANCE));

            tvProfileName.setText(firstName + " " + lastName);
            tvProfilePhone.setText(phone);
            tvProfileBirthDate.setText(birthDate);
            tvAccountBalance.setText(currencyFormat.format(balance));

            cursor.close();
        }
    }

    private void setupListeners() {
        // Đổi mật khẩu
        cardChangePassword.setOnClickListener(v -> showChangePasswordDialog());


        // Cài đặt
        cardSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show();
        });

        // Nút Đăng xuất
        btnLogout.setOnClickListener(v -> showLogoutConfirmDialog());

        // Bottom Nav
        bottomNav.setSelectedItemId(R.id.nav_profile);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_notification) {
                Intent intent = new Intent(ProfileActivity.this, NotificationActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_statistics) {
                Intent intent = new Intent(ProfileActivity.this, StatisticsActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);

        EditText etOldPassword = dialogView.findViewById(R.id.etOldPassword);
        EditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        EditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);

        builder.setView(dialogView)
                .setTitle("Đổi mật khẩu")
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    String oldPassword = etOldPassword.getText().toString().trim();
                    String newPassword = etNewPassword.getText().toString().trim();
                    String confirmPassword = etConfirmPassword.getText().toString().trim();

                    if (validatePasswordChange(oldPassword, newPassword, confirmPassword)) {
                        changePassword(oldPassword, newPassword);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private boolean validatePasswordChange(String oldPassword, String newPassword, String confirmPassword) {
        if (oldPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu cũ", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (newPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void changePassword(String oldPassword, String newPassword) {
        String phone = sharedPreferences.getString("phone", "");

        // Verify old password
        Cursor cursor = dbHelper.loginUser(phone, oldPassword);
        if (cursor != null && cursor.moveToFirst()) {
            cursor.close();

            // Update password
            if (dbHelper.updatePassword(userId, newPassword)) {
                Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Đổi mật khẩu thất bại", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (cursor != null) cursor.close();
            Toast.makeText(this, "Mật khẩu cũ không đúng", Toast.LENGTH_SHORT).show();
        }
    }

//    private void showAccountInfoDialog() {
//        Cursor cursor = dbHelper.getUserById(userId);
//        if (cursor != null && cursor.moveToFirst()) {
//            String createdAt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CREATED_AT));
//            String userName = cursor.getString(cursor.)
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setTitle("Thông tin tài khoản")
//                    .setMessage("Tài khoản: " +  + "\n" +
//                            "Ngày tạo: " + createdAt + "\n" +
//                            "Loại tài khoản: Cá nhân")
//                    .setPositiveButton("OK", null)
//                    .show();
//
//            cursor.close();
//        }
//    }

    private void showLogoutConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> logout())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}