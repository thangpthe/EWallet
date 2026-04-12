//package com.example.ewallet_thang;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.database.Cursor;
//import android.os.Bundle;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.ewallet_thang.database.FirebaseManager;
//import com.google.android.material.button.MaterialButton;
//import com.google.android.material.textfield.TextInputLayout;
//import com.example.ewallet_thang.database.DatabaseHelper;
//
//public class LoginActivity extends AppCompatActivity {
//
//    private EditText etUsername, etPassword;
//    private TextInputLayout tilUsername, tilPassword;
//    private MaterialButton btnLogin;
//    private TextView tvRegister, tvForgotPassword;
////    private DatabaseHelper dbHelper;
//    private FirebaseManager firebaseManager;
//    private SharedPreferences sharedPreferences;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_login);
//
////        dbHelper = new DatabaseHelper(this);
//        firebaseManager = new FirebaseManager();
//        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
//
//        // Check if user is already logged in
//        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
//            navigateToMain();
//            return;
//        }
//
//        initViews();
//        setupListeners();
//    }
//
//    private void initViews() {
//        tilUsername = findViewById(R.id.tilUsername);
//        tilPassword = findViewById(R.id.tilPassword);
//        etUsername = findViewById(R.id.etUsername);
//        etPassword = findViewById(R.id.etPassword);
//        btnLogin = findViewById(R.id.btnLogin);
//        tvRegister = findViewById(R.id.tvRegister);
//        tvForgotPassword = findViewById(R.id.tvForgotPassword);
//    }
//
//    private void setupListeners() {
//        btnLogin.setOnClickListener(v -> handleLogin());
//
//        tvRegister.setOnClickListener(v -> {
//            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
//            startActivity(intent);
//        });
//
//        tvForgotPassword.setOnClickListener(v -> {
//            Toast.makeText(this, "Chức năng quên mật khẩu đang phát triển",
//                    Toast.LENGTH_SHORT).show();
//        });
//    }
//
//    private void handleLogin() {
//        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
//        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
//
//        if (phone.isEmpty() || password.isEmpty()) {
//            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        btnLogin.setEnabled(false);
//        btnLogin.setText("Đang đăng nhập...");
//
//        firebaseManager.loginUser(phone, password, new FirebaseManager.AuthCallback() {
//            @Override
//            public void onSuccess(User user) {
//                // Lưu thông tin phiên đăng nhập
//                SharedPreferences.Editor editor = sharedPreferences.edit();
//                // Vì dùng phone làm ID nên ta lưu phone thay cho userId kiểu INT cũ
//                editor.putString("userPhone", user.getPhone());
//                editor.putString("firstName", user.getFirstName());
//                editor.putString("lastName", user.getLastName());
//                editor.apply();
//
//                Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
//                navigateToMain();
//            }
//
//            @Override
//            public void onFailure(String error) {
//                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
//                btnLogin.setEnabled(true);
//                btnLogin.setText("Đăng nhập");
//            }
//        });
//    }
//
//    private void navigateToMain() {
//        Intent intent = new Intent(LoginActivity.this, com.example.ewallet_thang.MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
//        finish();
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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ewallet_thang.database.FirebaseManager;
import com.example.ewallet_thang.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etPhone, etPassword;
    private TextInputLayout tilPhone, tilPassword;
    private MaterialButton btnLogin;
    private TextView tvRegister, tvForgotPassword;

    private FirebaseManager firebaseManager;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseManager = new FirebaseManager();
        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);

        // Đã sửa lại check theo Firebase (Dùng Số điện thoại làm ID)
        String userPhone = sharedPreferences.getString("userPhone", "");
        if (!userPhone.isEmpty()) {
            navigateToMain();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        tilPhone = findViewById(R.id.tilPhone);
        tilPassword = findViewById(R.id.tilPassword);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng quên mật khẩu đang phát triển!", Toast.LENGTH_SHORT).show();
        });
    }

    private void handleLogin() {
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        tilPhone.setError(null);
        tilPassword.setError(null);

        if (phone.isEmpty()) {
            tilPhone.setError("Vui lòng nhập số điện thoại");
            etPhone.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            tilPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Đang đăng nhập...");

        firebaseManager.loginUser(phone, password, new FirebaseManager.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                // Lưu session đăng nhập bằng Phone
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("userPhone", user.getPhone());
                editor.putString("firstName", user.getFirstName());
                editor.putString("lastName", user.getLastName());
                editor.apply();

                Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                navigateToMain();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                btnLogin.setEnabled(true);
                btnLogin.setText("Đăng nhập");
            }
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}