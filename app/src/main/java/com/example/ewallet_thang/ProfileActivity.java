//package com.example.ewallet_thang;
//
//import android.app.AlertDialog;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.database.Cursor;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.cardview.widget.CardView; // ĐÃ FIX: Dùng CardView thay vì MaterialCardView
//
//import com.example.ewallet_thang.database.DatabaseHelper;
//import com.google.android.material.bottomnavigation.BottomNavigationView;
//import com.google.android.material.textfield.TextInputEditText;
//
//import java.text.NumberFormat;
//import java.util.Locale;
//
//public class ProfileActivity extends AppCompatActivity {
//
//    // Khai báo thêm tvProfileInitial để làm Avatar động
//    private TextView tvProfileInitial, tvProfileName, tvProfilePhone, tvProfileBirthDate, tvAccountBalance;
//    private CardView cardChangePassword, cardSettings; // ĐÃ FIX kiểu View
//    private Button btnLogout;
//    private BottomNavigationView bottomNav;
//
//    private DatabaseHelper dbHelper;
//    private SharedPreferences sharedPreferences;
//    private int userId;
//    private NumberFormat currencyFormat;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_profile);
//
//        dbHelper = new DatabaseHelper(this);
//        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
//        userId = sharedPreferences.getInt("userId", -1);
//        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
//
//        if (userId == -1) {
//            logout();
//            return;
//        }
//
//        initViews();
//        loadProfileData();
//        setupListeners();
//    }
//
//    private void initViews() {
//        // Ánh xạ đầy đủ theo XML mới
//        tvProfileInitial = findViewById(R.id.tvProfileInitial);
//        tvProfileName = findViewById(R.id.tvProfileName);
//        tvProfilePhone = findViewById(R.id.tvProfilePhone);
//        tvProfileBirthDate = findViewById(R.id.tvProfileBirthDate);
//        tvAccountBalance = findViewById(R.id.tvAccountBalance);
//
//        cardChangePassword = findViewById(R.id.cardChangePassword);
//        cardSettings = findViewById(R.id.cardSettings);
//
//        btnLogout = findViewById(R.id.btnLogout);
//        bottomNav = findViewById(R.id.bottomNav);
//    }
//
//    private void loadProfileData() {
//        Cursor cursor = dbHelper.getUserById(userId);
//        if (cursor != null && cursor.moveToFirst()) {
//            String firstName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FIRST_NAME));
//            String lastName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LAST_NAME));
//            String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE));
//            String birthDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BIRTH_DATE));
//            double balance = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BALANCE));
//
//            String fullName = firstName + " " + lastName;
//            tvProfileName.setText(fullName);
//            tvProfilePhone.setText(phone);
//            tvProfileBirthDate.setText("Ngày sinh: " + birthDate);
//            tvAccountBalance.setText(currencyFormat.format(balance));
//
//            // Logic tạo Avatar động từ chữ cái đầu tiên của Tên
//            if (!firstName.isEmpty()) {
//                tvProfileInitial.setText(String.valueOf(firstName.charAt(0)).toUpperCase());
//            } else if (!lastName.isEmpty()) {
//                tvProfileInitial.setText(String.valueOf(lastName.charAt(0)).toUpperCase());
//            }
//
//            cursor.close();
//        }
//    }
//
//    private void setupListeners() {
//        if (cardChangePassword != null) {
//            cardChangePassword.setOnClickListener(v -> showChangePasswordDialog());
//        }
//
//        if (cardSettings != null) {
//            cardSettings.setOnClickListener(v -> {
//                Toast.makeText(this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show();
//            });
//        }
//
//        if (btnLogout != null) {
//            btnLogout.setOnClickListener(v -> showLogoutConfirmDialog());
//        }
//
//        if (bottomNav != null) {
//            bottomNav.setSelectedItemId(R.id.nav_profile);
//            bottomNav.setOnItemSelectedListener(item -> {
//                int itemId = item.getItemId();
//                if (itemId == R.id.nav_home) {
//                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                    startActivity(intent);
//                    finish();
//                    return true;
//                } else if (itemId == R.id.nav_notification) {
//                    Intent intent = new Intent(ProfileActivity.this, NotificationActivity.class);
//                    startActivity(intent);
//                    finish();
//                    return true;
//                } else if (itemId == R.id.nav_statistics) {
//                    Intent intent = new Intent(ProfileActivity.this, StatisticsActivity.class);
//                    startActivity(intent);
//                    finish();
//                    return true;
//                } else if (itemId == R.id.nav_profile) {
//                    return true;
//                }
//                return false;
//            });
//        }
//    }
//
//    private void showChangePasswordDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
//
//        // Dùng TextInputEditText thay vì EditText cho chuẩn Material Design
//        TextInputEditText etOldPassword = dialogView.findViewById(R.id.etOldPassword);
//        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
//        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);
//
//        builder.setView(dialogView)
//                .setTitle("Đổi mật khẩu")
//                .setPositiveButton("Xác nhận", (dialog, which) -> {
//                    String oldPassword = etOldPassword.getText() != null ? etOldPassword.getText().toString().trim() : "";
//                    String newPassword = etNewPassword.getText() != null ? etNewPassword.getText().toString().trim() : "";
//                    String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";
//
//                    if (validatePasswordChange(oldPassword, newPassword, confirmPassword)) {
//                        changePassword(oldPassword, newPassword);
//                    }
//                })
//                .setNegativeButton("Hủy", null)
//                .show();
//    }
//
//    private boolean validatePasswordChange(String oldPassword, String newPassword, String confirmPassword) {
//        if (oldPassword.isEmpty()) {
//            Toast.makeText(this, "Vui lòng nhập mật khẩu cũ", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//
//        if (newPassword.isEmpty()) {
//            Toast.makeText(this, "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//
//        if (newPassword.length() < 6) {
//            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//
//        if (!newPassword.equals(confirmPassword)) {
//            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//
//        return true;
//    }
//
//    private void changePassword(String oldPassword, String newPassword) {
//        String phone = sharedPreferences.getString("phone", "");
//
//        // Verify old password
//        Cursor cursor = dbHelper.loginUser(phone, oldPassword);
//        if (cursor != null && cursor.moveToFirst()) {
//            cursor.close();
//
//            // Update password
//            if (dbHelper.updatePassword(userId, newPassword)) {
//                Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(this, "Đổi mật khẩu thất bại", Toast.LENGTH_SHORT).show();
//            }
//        } else {
//            if (cursor != null) cursor.close();
//            Toast.makeText(this, "Mật khẩu cũ không đúng", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void showLogoutConfirmDialog() {
//        new AlertDialog.Builder(this)
//                .setTitle("Đăng xuất")
//                .setMessage("Bạn có chắc muốn đăng xuất?")
//                .setPositiveButton("Đăng xuất", (dialog, which) -> logout())
//                .setNegativeButton("Hủy", null)
//                .show();
//    }
//
//    private void logout() {
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.clear();
//        editor.apply();
//
//        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
//        finish();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        loadProfileData(); // Cập nhật lại data mỗi khi quay lại màn hình này
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

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileInitial, tvProfileName, tvProfilePhone, tvProfileBirthDate, tvAccountBalance;
    private CardView cardChangePassword, cardSettings;
    private Button btnLogout;
    private BottomNavigationView bottomNav;

    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;
    private String userPhone;
    private NumberFormat currencyFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
        userPhone = sharedPreferences.getString("userPhone", "");
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        if (userPhone.isEmpty()) {
            logout();
            return;
        }

        initViews();
        loadProfileData();
        setupListeners();
    }

    private void initViews() {
        tvProfileInitial = findViewById(R.id.tvProfileInitial);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);
        tvProfileBirthDate = findViewById(R.id.tvProfileBirthDate);
        tvAccountBalance = findViewById(R.id.tvAccountBalance);

        cardChangePassword = findViewById(R.id.cardChangePassword);
        cardSettings = findViewById(R.id.cardSettings);
        btnLogout = findViewById(R.id.btnLogout);
        bottomNav = findViewById(R.id.bottomNav);
    }

    private void loadProfileData() {
        db.collection("users").document(userPhone).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String firstName = doc.getString("firstName");
                String lastName = doc.getString("lastName");
                String phone = doc.getString("phone");
                String birthDate = doc.getString("birthDate");
                Double balance = doc.getDouble("balance");
                if (balance == null) balance = 0.0;

                String fullName = firstName + " " + lastName;
                tvProfileName.setText(fullName);
                tvProfilePhone.setText(phone);
                tvProfileBirthDate.setText("Ngày sinh: " + birthDate);
                tvAccountBalance.setText(currencyFormat.format(balance));

                if (firstName != null && !firstName.isEmpty()) {
                    tvProfileInitial.setText(String.valueOf(firstName.charAt(0)).toUpperCase());
                } else if (lastName != null && !lastName.isEmpty()) {
                    tvProfileInitial.setText(String.valueOf(lastName.charAt(0)).toUpperCase());
                }
            }
        });
    }

    private void setupListeners() {
        if (cardChangePassword != null) cardChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        if (cardSettings != null) cardSettings.setOnClickListener(v -> Toast.makeText(this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show());
        if (btnLogout != null) btnLogout.setOnClickListener(v -> showLogoutConfirmDialog());

        if (bottomNav != null) {
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
                    startActivity(new Intent(ProfileActivity.this, NotificationActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_statistics) {
                    startActivity(new Intent(ProfileActivity.this, StatisticsActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    return true;
                }
                return false;
            });
        }
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);

        TextInputEditText etOldPassword = dialogView.findViewById(R.id.etOldPassword);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);

        builder.setView(dialogView)
                .setTitle("Đổi mật khẩu")
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    String oldPass = etOldPassword.getText() != null ? etOldPassword.getText().toString().trim() : "";
                    String newPass = etNewPassword.getText() != null ? etNewPassword.getText().toString().trim() : "";
                    String confirmPass = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

                    if (validatePasswordChange(oldPass, newPass, confirmPass)) {
                        changePassword(oldPass, newPass);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private boolean validatePasswordChange(String oldPassword, String newPassword, String confirmPassword) {
        if (oldPassword.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
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
        db.collection("users").document(userPhone).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String currentPass = doc.getString("password");
                if (oldPassword.equals(currentPass)) {
                    db.collection("users").document(userPhone).update("password", newPassword)
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(this, "Mật khẩu cũ không đúng", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showLogoutConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> logout())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void logout() {
        sharedPreferences.edit().clear().apply();
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
}