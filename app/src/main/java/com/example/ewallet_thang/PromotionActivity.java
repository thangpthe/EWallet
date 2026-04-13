package com.example.ewallet_thang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.firestore.FirebaseFirestore;

public class PromotionActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private CardView cardVoucher, cardPoints, cardNewUser;
    private TextView tvNewBadge;

    private FirebaseFirestore db;
    private SharedPreferences prefs;
    private String userPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion);

        db = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
        userPhone = prefs.getString("userPhone", "");

        initViews();
        setupListeners();
        checkNewUserStatus();
    }

    private void initViews() {
        btnBack     = findViewById(R.id.btnBack);
        cardVoucher = findViewById(R.id.cardVoucher);
        cardPoints  = findViewById(R.id.cardPoints);
        cardNewUser = findViewById(R.id.cardNewUser);
        tvNewBadge  = findViewById(R.id.tvNewBadge);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        cardVoucher.setOnClickListener(v ->
                startActivity(new Intent(this, VoucherActivity.class)));

        cardPoints.setOnClickListener(v ->
                startActivity(new Intent(this, PointsActivity.class)));

        cardNewUser.setOnClickListener(v ->
                startActivity(new Intent(this, NewUserBonusActivity.class)));
    }

    private void checkNewUserStatus() {
        // Kiểm tra xem user đã nhận ưu đãi tân thủ chưa
        boolean isNewUserBonusClaimed = prefs.getBoolean("newUserBonusClaimed_" + userPhone, false);

        if (!isNewUserBonusClaimed) {
            // Hiển thị badge "Mới" trên mục ưu đãi tân thủ
            if (tvNewBadge != null) tvNewBadge.setVisibility(android.view.View.VISIBLE);

            // Nếu là user mới hoàn toàn chưa tặng voucher 100K, tặng ngay
            grantWelcomeVoucherIfNeeded();
        }
    }

    private void grantWelcomeVoucherIfNeeded() {
        db.collection("users").document(userPhone).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                Boolean welcomeGranted = doc.getBoolean("welcomeVoucherGranted");
                if (welcomeGranted == null || !welcomeGranted) {
                    // Tạo voucher 100K chào mừng trong Firestore
                    java.util.Map<String, Object> voucher = new java.util.HashMap<>();
                    voucher.put("userPhone", userPhone);
                    voucher.put("code", "WELCOME100");
                    voucher.put("value", 100000);
                    voucher.put("type", "TOPUP");           // Áp dụng khi nạp tiền
                    voucher.put("description", "Voucher tặng khi xác thực tài khoản");
                    voucher.put("isUsed", false);
                    voucher.put("expiryDate", getExpiryDate(30)); // Hết hạn sau 30 ngày
                    voucher.put("createdAt", System.currentTimeMillis());

                    db.collection("vouchers").add(voucher).addOnSuccessListener(ref -> {
                        // Đánh dấu đã tặng voucher chào mừng
                        db.collection("users").document(userPhone)
                                .update("welcomeVoucherGranted", true);

                        // Tặng 50 điểm khởi đầu
                        db.collection("users").document(userPhone)
                                .update("points",
                                        com.google.firebase.firestore.FieldValue.increment(50));
                    });
                }
            }
        });
    }

    private String getExpiryDate(int daysFromNow) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_MONTH, daysFromNow);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy",
                java.util.Locale.getDefault());
        return sdf.format(cal.getTime());
    }
}