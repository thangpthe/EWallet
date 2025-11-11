package com.example.ewallet_thang;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.example.ewallet_thang.database.DatabaseHelper;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etPhone, etBirthDate, etPassword, etConfirmPassword;
    private TextInputLayout tilFirstName, tilLastName, tilPhone, tilBirthDate, tilPassword, tilConfirmPassword;
    private MaterialButton btnRegister;
    private TextView tvLogin;
    private DatabaseHelper dbHelper;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        initViews();
        setupListeners();
    }

    private void initViews() {
        tilFirstName = findViewById(R.id.tilFirstName);
        tilLastName = findViewById(R.id.tilLastName);
        tilPhone = findViewById(R.id.tilPhone);
        tilBirthDate = findViewById(R.id.tilBirthDate);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etPhone = findViewById(R.id.etPhone);
        etBirthDate = findViewById(R.id.etBirthDate);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> handleRegister());

        tvLogin.setOnClickListener(v -> finish());

        // Date picker for birth date
        etBirthDate.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    etBirthDate.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Set max date to today (user must be born before today)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        // Set min date to 100 years ago
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -100);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());

        datePickerDialog.show();
    }

    private void handleRegister() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Reset errors
        tilFirstName.setError(null);
        tilLastName.setError(null);
        tilPhone.setError(null);
        tilBirthDate.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        // Validation
        if (firstName.isEmpty()) {
            tilFirstName.setError("Vui lòng nhập họ");
            etFirstName.requestFocus();
            return;
        }

        if (lastName.isEmpty()) {
            tilLastName.setError("Vui lòng nhập tên");
            etLastName.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            tilPhone.setError("Vui lòng nhập số điện thoại");
            etPhone.requestFocus();
            return;
        }

        if (!isValidPhone(phone)) {
            tilPhone.setError("Số điện thoại không hợp lệ");
            etPhone.requestFocus();
            return;
        }

        // Check if phone already exists
        if (dbHelper.checkUserExists(phone)) {
            tilPhone.setError("Số điện thoại đã được đăng ký");
            etPhone.requestFocus();
            return;
        }

        if (birthDate.isEmpty()) {
            tilBirthDate.setError("Vui lòng chọn ngày sinh");
            etBirthDate.requestFocus();
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

        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.setError("Vui lòng nhập lại mật khẩu");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Mật khẩu không khớp");
            etConfirmPassword.requestFocus();
            return;
        }

        // Register user
        long result = dbHelper.registerUser(firstName, lastName, phone, birthDate, password);

        if (result != -1) {
            Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Đăng ký thất bại. Vui lòng thử lại!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidPhone(String phone) {
        // Vietnamese phone number pattern
        Pattern pattern = Pattern.compile("^(0|\\+84)(\\d{9,10})$");
        return pattern.matcher(phone).matches();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}