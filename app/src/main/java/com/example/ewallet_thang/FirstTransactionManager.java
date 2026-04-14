package com.example.ewallet_thang;

import android.content.SharedPreferences;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Quản lý logic ưu đãi giao dịch đầu tiên.
 *
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │  QUY TẮC ƯU ĐÃI                                                    │
 * │                                                                     │
 * │  • Giao dịch đầu tiên (nạp HOẶC chuyển HOẶC thanh toán):          │
 * │    → Tặng voucher FIRST100K giảm 100.000đ (loại TRANSFER)         │
 * │    → Tặng 100 điểm thưởng khởi đầu                                 │
 * │    → Chỉ tặng DUY NHẤT 1 LẦN, kiểm tra qua SharedPrefs + Firestore│
 * │                                                                     │
 * │  • Các giao dịch sau:                                               │
 * │    → Tích điểm bình thường: 10.000đ = 1 điểm                      │
 * │    → Voucher chỉ áp dụng nếu đủ điều kiện (loại, hạn dùng, chưa  │
 * │      sử dụng) — không tự động phát thêm                            │
 * └─────────────────────────────────────────────────────────────────────┘
 *
 * Cách dùng trong TopUpActivity / TransferActivity / PaymentActivity:
 *
 *   boolean isFirst = FirstTransactionManager.isFirstTransaction(prefs, userPhone);
 *   if (isFirst) {
 *       FirstTransactionManager.addBonusToBatch(db, batch, userPhone, date);
 *       FirstTransactionManager.markCompleted(prefs, userPhone);
 *   }
 *   long pts = FirstTransactionManager.calcPoints(deductAmount);
 *   // ... commit batch ...
 *   // Truyền isFirst và pts vào SuccessBottomSheet
 */
public class FirstTransactionManager {

    private static final String PREF_KEY_PREFIX = "hasCompletedFirstTx_";

    /** Kiểm tra user đã có giao dịch đầu tiên chưa (ưu tiên SharedPrefs, nhanh hơn) */
    public static boolean isFirstTransaction(SharedPreferences prefs, String userPhone) {
        return !prefs.getBoolean(PREF_KEY_PREFIX + userPhone, false);
    }

    /** Đánh dấu local đã hoàn thành giao dịch đầu tiên */
    public static void markCompleted(SharedPreferences prefs, String userPhone) {
        prefs.edit().putBoolean(PREF_KEY_PREFIX + userPhone, true).apply();
    }

    /**
     * Thêm vào batch:
     * - Voucher FIRST100K (giảm 100.000đ, loại TRANSFER, hết hạn 30 ngày)
     * - +100 điểm thưởng
     * - Lịch sử điểm
     * - Cờ hasCompletedFirstTx = true trên Firestore (để đồng bộ đa thiết bị)
     */
    public static void addBonusToBatch(FirebaseFirestore db, WriteBatch batch,
                                       String userPhone, String date) {
        String expiry = getExpiryDate(30);

        // Voucher 100K
        Map<String, Object> voucher = new HashMap<>();
        voucher.put("userPhone",   userPhone);
        voucher.put("code",        "FIRST100K");
        voucher.put("value",       100_000L);
        voucher.put("type",        "TRANSFER");   // dùng cho chuyển tiền / thanh toán
        voucher.put("description", "🎁 Ưu đãi giao dịch đầu tiên – giảm 100.000đ");
        voucher.put("isUsed",      false);
        voucher.put("expiryDate",  expiry);
        voucher.put("createdAt",   System.currentTimeMillis());
        batch.set(db.collection("vouchers").document(), voucher);

        // +100 điểm
        batch.update(db.collection("users").document(userPhone),
                "points", FieldValue.increment(100));

        // Cờ Firestore (phòng trường hợp user đăng nhập thiết bị khác)
        batch.update(db.collection("users").document(userPhone),
                "hasCompletedFirstTx", true);

        // Lịch sử điểm
        Map<String, Object> ph = new HashMap<>();
        ph.put("userPhone",   userPhone);
        ph.put("description", "🎁 Thưởng giao dịch đầu tiên → +100 điểm");
        ph.put("delta",       100);
        ph.put("date",        date);
        ph.put("timestamp",   System.currentTimeMillis());
        batch.set(db.collection("pointHistory").document(), ph);
    }

    /**
     * Tính số điểm tích lũy từ một giao dịch.
     * Công thức: 10.000đ = 1 điểm (làm tròn xuống).
     */
    public static long calcPoints(double amount) {
        return (long)(amount / 10_000);
    }

    /**
     * Thêm lịch sử điểm vào batch (dùng sau khi calcPoints > 0).
     */
    public static void addPointHistoryToBatch(FirebaseFirestore db, WriteBatch batch,
                                              String userPhone, long points,
                                              String description, String date) {
        if (points <= 0) return;
        batch.update(db.collection("users").document(userPhone),
                "points", FieldValue.increment(points));

        Map<String, Object> ph = new HashMap<>();
        ph.put("userPhone",   userPhone);
        ph.put("description", description + " → +" + points + " điểm");
        ph.put("delta",       points);
        ph.put("date",        date);
        ph.put("timestamp",   System.currentTimeMillis());
        batch.set(db.collection("pointHistory").document(), ph);
    }

    private static String getExpiryDate(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, days);
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.getTime());
    }
}