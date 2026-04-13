package com.example.ewallet_thang;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

public class NewUserBonusActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvStep1Status, tvStep2Status, tvStep3Status;
    private TextView tvCountdownDay, tvCountdownHour, tvCountdownMin;
    private View dotStep1, dotStep2, dotStep3;

    private FirebaseFirestore db;
    private SharedPreferences prefs;
    private String userPhone;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user_bonus);

        db    = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
        userPhone = prefs.getString("userPhone", "");

        initViews();
        btnBack.setOnClickListener(v -> finish());
        loadUserProgress();
        startCountDown();
    }

    private void initViews() {
        btnBack          = findViewById(R.id.btnBack);
        tvStep1Status    = findViewById(R.id.tvStep1Status);
        tvStep2Status    = findViewById(R.id.tvStep2Status);
        tvStep3Status    = findViewById(R.id.tvStep3Status);
        tvCountdownDay   = findViewById(R.id.tvCountdownDay);
        tvCountdownHour  = findViewById(R.id.tvCountdownHour);
        tvCountdownMin   = findViewById(R.id.tvCountdownMin);
        dotStep1         = findViewById(R.id.dotStep1);
        dotStep2         = findViewById(R.id.dotStep2);
        dotStep3         = findViewById(R.id.dotStep3);
    }

    // ── Load tiến trình của user ──────────────────────────────────────────────
    private void loadUserProgress() {
        db.collection("users").document(userPhone).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) return;

            // Bước 1: Tạo tài khoản – luôn hoàn thành nếu đang ở màn hình này
            setStepDone(tvStep1Status, dotStep1);

            // Bước 2: Xác thực TK (welcomeVoucherGranted = true)
            Boolean welcomed = doc.getBoolean("welcomeVoucherGranted");
            if (Boolean.TRUE.equals(welcomed)) {
                setStepDone(tvStep2Status, dotStep2);
            } else {
                setStepActive(tvStep2Status, dotStep2);
            }

            // Bước 3: Giao dịch đầu tiên (firstTransactionCashback = true)
            Boolean firstTxDone = doc.getBoolean("firstTransactionCashback");
            if (Boolean.TRUE.equals(firstTxDone)) {
                setStepDone(tvStep3Status, dotStep3);
            } else if (Boolean.TRUE.equals(welcomed)) {
                setStepActive(tvStep3Status, dotStep3);
            } else {
                setStepPending(tvStep3Status, dotStep3);
            }
        });
    }

    private void setStepDone(TextView tv, View dot) {
        tv.setText("✓ Hoàn thành");
        tv.setTextColor(0xFF00C48C);
        if (dot != null) dot.setBackgroundColor(0xFF00C48C);
    }

    private void setStepActive(TextView tv, View dot) {
        tv.setText("Đang chờ...");
        tv.setTextColor(0xFF3D5AFE);
        if (dot != null) dot.setBackgroundColor(0xFF3D5AFE);
    }

    private void setStepPending(TextView tv, View dot) {
        tv.setText("Chưa mở khóa");
        tv.setTextColor(0xFF9EA8C2);
        if (dot != null) dot.setBackgroundColor(0xFFDDE0FF);
    }

    // ── Đếm ngược 7 ngày kể từ khi đăng ký ─────────────────────────────────
    private void startCountDown() {
        long registeredAt = prefs.getLong("registeredAt_" + userPhone,
                System.currentTimeMillis());
        long deadline = registeredAt + TimeUnit.DAYS.toMillis(7);
        long remaining = deadline - System.currentTimeMillis();

        if (remaining <= 0) {
            tvCountdownDay.setText("00");
            tvCountdownHour.setText("00");
            tvCountdownMin.setText("00");
            return;
        }

        countDownTimer = new CountDownTimer(remaining, 60_000) {
            @Override
            public void onTick(long ms) {
                long days  = TimeUnit.MILLISECONDS.toDays(ms);
                long hours = TimeUnit.MILLISECONDS.toHours(ms) % 24;
                long mins  = TimeUnit.MILLISECONDS.toMinutes(ms) % 60;
                tvCountdownDay.setText(String.format("%02d", days));
                tvCountdownHour.setText(String.format("%02d", hours));
                tvCountdownMin.setText(String.format("%02d", mins));
            }
            @Override public void onFinish() {
                tvCountdownDay.setText("00");
                tvCountdownHour.setText("00");
                tvCountdownMin.setText("00");
            }
        }.start();
    }

    // ── Hoàn tiền 50% giao dịch đầu (gọi từ TopUpActivity/TransferConfirm) ──
    /**
     * Gọi method này khi user thực hiện giao dịch đầu tiên.
     * Hoàn 50% tối đa 50.000đ vào ví.
     */
    public static void applyFirstTransactionCashback(
            FirebaseFirestore db,
            SharedPreferences prefs,
            String userPhone,
            double txAmount) {

        db.collection("users").document(userPhone).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) return;
            Boolean done = doc.getBoolean("firstTransactionCashback");
            if (Boolean.TRUE.equals(done)) return;  // Đã áp dụng rồi

            double cashback = Math.min(txAmount * 0.5, 50000);
            if (cashback <= 0) return;

            // Cộng tiền hoàn vào ví
            db.collection("users").document(userPhone)
                    .update("balance",
                            com.google.firebase.firestore.FieldValue.increment(cashback),
                            "firstTransactionCashback", true);

            // Thêm thông báo
            java.util.Map<String, Object> notif = new java.util.HashMap<>();
            notif.put("userPhone", userPhone);
            notif.put("title", "Thu nhập");
            notif.put("message", "Hoàn tiền giao dịch đầu: +"
                    + java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"))
                    .format(cashback) + "đ");
            notif.put("type", "INCOME");
            notif.put("date", new java.text.SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                    .format(new java.util.Date()));
            notif.put("isRead", false);
            db.collection("notifications").add(notif);

            // Ghi lịch sử giao dịch hoàn tiền
            java.util.Map<String, Object> tx = new java.util.HashMap<>();
            tx.put("userPhone",   userPhone);
            tx.put("type",        "INCOME");
            tx.put("amount",      cashback);
            tx.put("description", "Hoàn 50% giao dịch đầu tiên (Ưu đãi tân thủ)");
            tx.put("category",    "Hoàn tiền");
            tx.put("date",
                    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                            java.util.Locale.getDefault()).format(new java.util.Date()));
            tx.put("timestamp",   System.currentTimeMillis());
            db.collection("transactions").add(tx);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}