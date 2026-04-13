//package com.example.ewallet_thang;
//
//import android.app.DatePickerDialog;
//import android.os.Bundle;
//import android.widget.TextView;
//import android.widget.Toast;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.ewallet_thang.database.FirebaseManager;
//import com.google.android.material.button.MaterialButton;
//import com.google.android.material.textfield.TextInputEditText; // Đã đổi sang TextInputEditText
//import com.google.android.material.textfield.TextInputLayout;
//import com.example.ewallet_thang.database.DatabaseHelper;
//
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Locale;
//import java.util.regex.Pattern;
//
//public class RegisterActivity extends AppCompatActivity {
//
//    private TextInputEditText etFirstName, etLastName, etPhone, etBirthDate, etPassword, etConfirmPassword;
//    private TextInputLayout tilFirstName, tilLastName, tilPhone, tilBirthDate, tilPassword, tilConfirmPassword;
//    private MaterialButton btnRegister;
//    private TextView tvLogin;
//    private DatabaseHelper dbHelper;
//    private Calendar calendar;
//    private SimpleDateFormat dateFormat;
//    private FirebaseManager firebaseManager;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_register);
//
////        dbHelper = new DatabaseHelper(this);
//        firebaseManager = new FirebaseManager();
//        calendar = Calendar.getInstance();
//        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//
//        initViews();
//        setupListeners();
//    }
//
//    private void initViews() {
//        tilFirstName = findViewById(R.id.tilFirstName);
//        tilLastName = findViewById(R.id.tilLastName);
//        tilPhone = findViewById(R.id.tilPhone);
//        tilBirthDate = findViewById(R.id.tilBirthDate);
//        tilPassword = findViewById(R.id.tilPassword);
//        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
//
//        etFirstName = findViewById(R.id.etFirstName);
//        etLastName = findViewById(R.id.etLastName);
//        etPhone = findViewById(R.id.etPhone);
//        etBirthDate = findViewById(R.id.etBirthDate);
//        etPassword = findViewById(R.id.etPassword);
//        etConfirmPassword = findViewById(R.id.etConfirmPassword);
//
//        btnRegister = findViewById(R.id.btnRegister);
//        tvLogin = findViewById(R.id.tvLogin);
//    }
//
//    private void setupListeners() {
//        btnRegister.setOnClickListener(v -> handleRegister());
//
//        tvLogin.setOnClickListener(v -> finish());
//
//        // Date picker cho phần chọn ngày sinh
//        etBirthDate.setOnClickListener(v -> showDatePicker());
//    }
//
//    private void showDatePicker() {
//        DatePickerDialog datePickerDialog = new DatePickerDialog(
//                this,
//                (view, year, month, dayOfMonth) -> {
//                    calendar.set(Calendar.YEAR, year);
//                    calendar.set(Calendar.MONTH, month);
//                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
//                    etBirthDate.setText(dateFormat.format(calendar.getTime()));
//                },
//                calendar.get(Calendar.YEAR),
//                calendar.get(Calendar.MONTH),
//                calendar.get(Calendar.DAY_OF_MONTH)
//        );
//
//        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
//
//        Calendar minDate = Calendar.getInstance();
//        minDate.add(Calendar.YEAR, -100);
//        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
//
//        datePickerDialog.show();
//    }
//
//    private void handleRegister() {
//        String firstName = etFirstName.getText() != null ? etFirstName.getText().toString().trim() : "";
//        String lastName = etLastName.getText() != null ? etLastName.getText().toString().trim() : "";
//        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
//        String birthDate = etBirthDate.getText() != null ? etBirthDate.getText().toString().trim() : "";
//        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
//        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";
//
//        // ... (Giữ nguyên các đoạn code if kiểm tra rỗng, kiểm tra độ dài giống cũ) ...
//        // Ví dụ: if (firstName.isEmpty()) { ... return; }
//        if (!password.equals(confirmPassword)) {
//            tilConfirmPassword.setError("Mật khẩu không khớp");
//            return;
//        }
//
//        // Vô hiệu hóa nút bấm và đổi text để người dùng biết app đang xử lý mạng
//        btnRegister.setEnabled(false);
//        btnRegister.setText("Đang xử lý...");
//
//        // Gọi Firebase
//        firebaseManager.registerUser(firstName, lastName, phone, birthDate, password, new FirebaseManager.AuthCallback() {
//            @Override
//            public void onSuccess(User user) {
//                Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
//                finish(); // Quay lại trang đăng nhập
//            }
//
//            @Override
//            public void onFailure(String error) {
//                Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
//                btnRegister.setEnabled(true);
//                btnRegister.setText("Đăng ký");
//            }
//        });
//
//    private boolean isValidPhone(String phone) {
//        Pattern pattern = Pattern.compile("^(0|\\+84)(\\d{9,10})$");
//        return pattern.matcher(phone).matches();
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

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ewallet_thang.database.FirebaseManager;
import com.example.ewallet_thang.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFirstName, etLastName, etPhone,
            etBirthDate, etPassword, etConfirmPassword;
    private TextInputLayout tilFirstName, tilLastName, tilPhone,
            tilBirthDate, tilPassword, tilConfirmPassword;
    private MaterialButton btnRegister;
    private TextView tvLogin;

    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private FirebaseManager firebaseManager;

    // ── [TÍCH HỢP MỚI] ───────────────────────────────────────────────────────
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseManager = new FirebaseManager();
        db              = FirebaseFirestore.getInstance();   // [MỚI]
        calendar        = Calendar.getInstance();
        dateFormat      = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        initViews();
        setupListeners();
    }

    private void initViews() {
        tilFirstName      = findViewById(R.id.tilFirstName);
        tilLastName       = findViewById(R.id.tilLastName);
        tilPhone          = findViewById(R.id.tilPhone);
        tilBirthDate      = findViewById(R.id.tilBirthDate);
        tilPassword       = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        etFirstName      = findViewById(R.id.etFirstName);
        etLastName       = findViewById(R.id.etLastName);
        etPhone          = findViewById(R.id.etPhone);
        etBirthDate      = findViewById(R.id.etBirthDate);
        etPassword       = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);
        tvLogin     = findViewById(R.id.tvLogin);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> handleRegister());
        tvLogin.setOnClickListener(v -> finish());
        etBirthDate.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        DatePickerDialog dlg = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    calendar.set(Calendar.YEAR,         year);
                    calendar.set(Calendar.MONTH,        month);
                    calendar.set(Calendar.DAY_OF_MONTH, day);
                    etBirthDate.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dlg.getDatePicker().setMaxDate(System.currentTimeMillis());
        Calendar min = Calendar.getInstance();
        min.add(Calendar.YEAR, -100);
        dlg.getDatePicker().setMinDate(min.getTimeInMillis());
        dlg.show();
    }

    private void handleRegister() {
        String firstName      = getText(etFirstName);
        String lastName       = getText(etLastName);
        String phone          = getText(etPhone);
        String birthDate      = getText(etBirthDate);
        String password       = getText(etPassword);
        String confirmPassword = getText(etConfirmPassword);

        // Reset lỗi
        tilFirstName.setError(null);  tilLastName.setError(null);
        tilPhone.setError(null);      tilBirthDate.setError(null);
        tilPassword.setError(null);   tilConfirmPassword.setError(null);

        // Validate
        if (firstName.isEmpty())        { tilFirstName.setError("Vui lòng nhập họ");       etFirstName.requestFocus();       return; }
        if (lastName.isEmpty())         { tilLastName.setError("Vui lòng nhập tên");        etLastName.requestFocus();        return; }
        if (phone.isEmpty())            { tilPhone.setError("Vui lòng nhập số điện thoại"); etPhone.requestFocus();           return; }
        if (!isValidPhone(phone))       { tilPhone.setError("Số điện thoại không hợp lệ"); etPhone.requestFocus();           return; }
        if (birthDate.isEmpty())        { tilBirthDate.setError("Vui lòng chọn ngày sinh"); etBirthDate.requestFocus();       return; }
        if (password.isEmpty())         { tilPassword.setError("Vui lòng nhập mật khẩu");  etPassword.requestFocus();        return; }
        if (password.length() < 6)      { tilPassword.setError("Mật khẩu ít nhất 6 ký tự");etPassword.requestFocus();        return; }
        if (confirmPassword.isEmpty())  { tilConfirmPassword.setError("Vui lòng nhập lại"); etConfirmPassword.requestFocus(); return; }
        if (!password.equals(confirmPassword)) { tilConfirmPassword.setError("Mật khẩu không khớp"); etConfirmPassword.requestFocus(); return; }

        btnRegister.setEnabled(false);
        btnRegister.setText("Đang xử lý...");

        firebaseManager.registerUser(firstName, lastName, phone, birthDate, password,
                new FirebaseManager.AuthCallback() {
                    @Override
                    public void onSuccess(User user) {
                        // ── [TÍCH HỢP MỚI] Tặng ưu đãi tân thủ sau khi đăng ký ──────
                        grantNewUserBonuses(phone, firstName, lastName);

                        Toast.makeText(RegisterActivity.this,
                                "Đăng ký thành công! Bạn nhận được voucher 100K chào mừng 🎁",
                                Toast.LENGTH_LONG).show();
                        finish();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
                        btnRegister.setEnabled(true);
                        btnRegister.setText("Đăng ký");
                    }
                });
    }

    // ── [TÍCH HỢP MỚI] Tặng voucher 100K + 50 điểm cho người dùng mới ──────
    private void grantNewUserBonuses(String phone, String firstName, String lastName) {
        String expiryDate = getExpiryDate(30);   // Hết hạn sau 30 ngày
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()).format(new java.util.Date());

        // 1. Tặng voucher 100K xác thực tài khoản
        Map<String, Object> voucher = new HashMap<>();
        voucher.put("userPhone",   phone);
        voucher.put("code",        "WELCOME100");
        voucher.put("value",       100000L);
        voucher.put("type",        "TOPUP");
        voucher.put("description", "Voucher tặng khi xác thực tài khoản");
        voucher.put("isUsed",      false);
        voucher.put("expiryDate",  expiryDate);
        voucher.put("createdAt",   System.currentTimeMillis());

        db.collection("vouchers").add(voucher);

        // 2. Cập nhật user: tặng 50 điểm khởi đầu + đánh dấu đã tặng
        Map<String, Object> updates = new HashMap<>();
        updates.put("points",                50L);
        updates.put("welcomeVoucherGranted", true);
        db.collection("users").document(phone).update(updates);

        // 3. Lưu lịch sử điểm khởi đầu
        Map<String, Object> history = new HashMap<>();
        history.put("userPhone",   phone);
        history.put("description", "Điểm chào mừng người dùng mới");
        history.put("delta",       50);
        history.put("date",        now);
        history.put("timestamp",   System.currentTimeMillis());
        db.collection("pointHistory").add(history);

        // 4. Thêm thông báo chào mừng
        Map<String, Object> notif = new HashMap<>();
        notif.put("userPhone", phone);
        notif.put("title",     "Chào mừng " + firstName + " " + lastName + "! 🎉");
        notif.put("message",   "Bạn nhận được voucher 100.000đ & 50 điểm thưởng. Xem trong mục Ưu đãi!");
        notif.put("type",      "INCOME");
        notif.put("date",      now);
        notif.put("isRead",    false);
        db.collection("notifications").add(notif);
    }

    private String getExpiryDate(int daysFromNow) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, daysFromNow);
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(cal.getTime());
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private boolean isValidPhone(String phone) {
        return Pattern.compile("^(0|\\+84)(\\d{9,10})$").matcher(phone).matches();
    }
}