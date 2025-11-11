package com.example.ewallet_thang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.example.ewallet_thang.database.DatabaseHelper;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private TextInputLayout tilUsername, tilPassword;
    private MaterialButton btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);

        // Check if user is already logged in
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            navigateToMain();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        etUsername = findViewById(R.id.etUsername);
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
            Toast.makeText(this, "Chức năng quên mật khẩu đang phát triển",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Reset errors
        tilUsername.setError(null);
        tilPassword.setError(null);

        // Validation
        if (username.isEmpty()) {
            tilUsername.setError("Vui lòng nhập số điện thoại");
            etUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            tilPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            tilPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            etPassword.requestFocus();
            return;
        }

        // Check login credentials
        Cursor cursor = dbHelper.loginUser(username, password);

        if (cursor != null && cursor.moveToFirst()) {
            // Login successful
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID));
            String firstName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FIRST_NAME));
            String lastName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LAST_NAME));
            double balance = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BALANCE));

            cursor.close();

            // Save login session
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", true);
            editor.putInt("userId", userId);
            editor.putString("firstName", firstName);
            editor.putString("lastName", lastName);
            editor.putString("phone", username);
            editor.putFloat("balance", (float) balance);
            editor.apply();

            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
            navigateToMain();

        } else {
            // Login failed
            if (cursor != null) cursor.close();

            // Check if user exists
            if (!dbHelper.checkUserExists(username)) {
                tilUsername.setError("Số điện thoại chưa được đăng ký");
                etUsername.requestFocus();
            } else {
                tilPassword.setError("Mật khẩu không đúng");
                etPassword.requestFocus();
            }
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}