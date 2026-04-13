package com.example.ewallet_thang;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PointsActivity extends AppCompatActivity {

    // ─── Cấu hình bảng đổi điểm ──────────────────────────────────────────────
    static final int[]  GIFT_VALUES  = {20000, 30000, 50000, 100000};
    static final int[]  GIFT_POINTS  = {40,    60,    100,   200};

    private ImageButton btnBack;
    private TextView    tvTotalPoints, tvPointsProgress, tvNextTarget;
    private ProgressBar progressPoints;
    private CardView    cardGift0, cardGift1, cardGift2, cardGift3;
    private RecyclerView rvHistory;

    private FirebaseFirestore db;
    private SharedPreferences prefs;
    private String userPhone;
    private long currentPoints = 0;

    private PointHistoryAdapter historyAdapter;
    private List<PointHistory>  historyList = new ArrayList<>();
    private NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_points);

        db    = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
        userPhone = prefs.getString("userPhone", "");

        initViews();
        setupListeners();
        loadPointsData();
    }

    private void initViews() {
        btnBack         = findViewById(R.id.btnBack);
        tvTotalPoints   = findViewById(R.id.tvTotalPoints);
        tvPointsProgress = findViewById(R.id.tvPointsProgress);
        tvNextTarget    = findViewById(R.id.tvNextTarget);
        progressPoints  = findViewById(R.id.progressPoints);
        cardGift0       = findViewById(R.id.cardGift0);
        cardGift1       = findViewById(R.id.cardGift1);
        cardGift2       = findViewById(R.id.cardGift2);
        cardGift3       = findViewById(R.id.cardGift3);
        rvHistory       = findViewById(R.id.rvPointHistory);

        historyAdapter = new PointHistoryAdapter(historyList);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(historyAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        cardGift0.setOnClickListener(v -> confirmRedeem(0));
        cardGift1.setOnClickListener(v -> confirmRedeem(1));
        cardGift2.setOnClickListener(v -> confirmRedeem(2));
        cardGift3.setOnClickListener(v -> confirmRedeem(3));
    }

    // ── Load điểm + lịch sử ──────────────────────────────────────────────────
    private void loadPointsData() {
        db.collection("users").document(userPhone)
                .addSnapshotListener((snap, err) -> {
                    if (snap == null || !snap.exists()) return;
                    Long pts = snap.getLong("points");
                    currentPoints = pts != null ? pts : 0;
                    updatePointsUI();
                });

        db.collection("pointHistory")
                .whereEqualTo("userPhone", userPhone)
                .get()
                .addOnSuccessListener(snapshots -> {
                    historyList.clear();
                    List<DocumentSnapshot> docs = snapshots.getDocuments();
                    // Sắp xếp mới nhất lên đầu
                    docs.sort((a, b) -> {
                        Long ta = a.getLong("timestamp");
                        Long tb = b.getLong("timestamp");
                        if (ta == null) return 1;
                        if (tb == null) return -1;
                        return tb.compareTo(ta);
                    });
                    for (DocumentSnapshot doc : docs) {
                        String desc    = doc.getString("description");
                        int    delta   = doc.getLong("delta") != null
                                ? doc.getLong("delta").intValue() : 0;
                        String date    = doc.getString("date");
                        historyList.add(new PointHistory(desc, delta, date));
                    }
                    historyAdapter.notifyDataSetChanged();
                });
    }

    private void updatePointsUI() {
        tvTotalPoints.setText(fmt.format(currentPoints));

        // Tìm mốc tiếp theo
        int nextTarget = -1;
        for (int p : GIFT_POINTS) {
            if (currentPoints < p) { nextTarget = p; break; }
        }

        if (nextTarget == -1) {
            tvNextTarget.setText("Bạn đủ điểm đổi tất cả phần thưởng!");
            progressPoints.setProgress(100);
            tvPointsProgress.setText("");
        } else {
            int prevTarget = 0;
            for (int p : GIFT_POINTS) {
                if (p < nextTarget) prevTarget = p;
            }
            long range   = nextTarget - prevTarget;
            long done    = currentPoints - prevTarget;
            int  percent = range > 0 ? (int) (done * 100 / range) : 0;
            progressPoints.setProgress(percent);
            tvNextTarget.setText("Cần thêm " + (nextTarget - currentPoints)
                    + " điểm để đổi voucher "
                    + fmt.format(getVoucherValueForPoints(nextTarget)) + "đ");
            tvPointsProgress.setText(currentPoints + "/" + nextTarget);
        }

        // Cập nhật trạng thái các nút đổi quà
        updateGiftButtons();
    }

    private void updateGiftButtons() {
        CardView[] cards = {cardGift0, cardGift1, cardGift2, cardGift3};
        for (int i = 0; i < cards.length; i++) {
            TextView tvBtn = cards[i].findViewWithTag("btnRedeem_" + i);
            if (tvBtn == null) continue;
            if (currentPoints >= GIFT_POINTS[i]) {
                tvBtn.setEnabled(true);
                tvBtn.setAlpha(1f);
            } else {
                tvBtn.setEnabled(false);
                tvBtn.setAlpha(0.45f);
            }
        }
    }

    private long getVoucherValueForPoints(int points) {
        for (int i = 0; i < GIFT_POINTS.length; i++) {
            if (GIFT_POINTS[i] == points) return GIFT_VALUES[i];
        }
        return 0;
    }

    // ── Xác nhận đổi quà ─────────────────────────────────────────────────────
    private void confirmRedeem(int index) {
        if (currentPoints < GIFT_POINTS[index]) {
            Toast.makeText(this,
                    "Bạn cần " + GIFT_POINTS[index] + " điểm để đổi phần thưởng này",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đổi điểm")
                .setMessage("Dùng " + GIFT_POINTS[index] + " điểm để nhận voucher "
                        + fmt.format(GIFT_VALUES[index]) + "đ?")
                .setPositiveButton("Xác nhận", (d, w) -> redeemPoints(index))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void redeemPoints(int index) {
        int  cost  = GIFT_POINTS[index];
        long value = GIFT_VALUES[index];
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()).format(new Date());
        String expiryDate = getExpiryDate(30);

        WriteBatch batch = db.batch();

        // 1. Trừ điểm
        batch.update(db.collection("users").document(userPhone),
                "points", FieldValue.increment(-cost));

        // 2. Cấp voucher mới
        Map<String, Object> voucher = new HashMap<>();
        voucher.put("userPhone",   userPhone);
        voucher.put("code",        "REDEEM" + value / 1000 + "K"
                + System.currentTimeMillis() % 10000);
        voucher.put("value",       value);
        voucher.put("type",        "TRANSFER");  // Dùng khi chuyển / nạp
        voucher.put("description", "Đổi " + cost + " điểm – voucher "
                + fmt.format(value) + "đ");
        voucher.put("isUsed",      false);
        voucher.put("expiryDate",  expiryDate);
        voucher.put("createdAt",   System.currentTimeMillis());
        batch.set(db.collection("vouchers").document(), voucher);

        // 3. Lưu lịch sử điểm
        Map<String, Object> history = new HashMap<>();
        history.put("userPhone",   userPhone);
        history.put("description", "Đổi voucher " + fmt.format(value) + "đ");
        history.put("delta",       -cost);
        history.put("date",        date);
        history.put("timestamp",   System.currentTimeMillis());
        batch.set(db.collection("pointHistory").document(), history);

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(this,
                    "Đã đổi thành công! Voucher đã được thêm vào ví của bạn.",
                    Toast.LENGTH_LONG).show();
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Lỗi khi đổi điểm: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
    }

    private String getExpiryDate(int days) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_MONTH, days);
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(cal.getTime());
    }

    // ─── Tích điểm từ giao dịch (gọi từ TopUpActivity / TransferConfirmActivity) ──
    /**
     * Gọi method này sau khi giao dịch thành công để cộng điểm.
     * 10.000đ = 1 điểm.
     */
    public static void addPointsForTransaction(FirebaseFirestore db,
                                               String userPhone,
                                               double amount,
                                               String description) {
        long points = (long) (amount / 10000);
        if (points <= 0) return;

        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()).format(new Date());

        WriteBatch batch = db.batch();

        // Cộng điểm cho user
        batch.update(db.collection("users").document(userPhone),
                "points", FieldValue.increment(points));

        // Lưu lịch sử
        Map<String, Object> history = new HashMap<>();
        history.put("userPhone",   userPhone);
        history.put("description", description + " → +" + points + " điểm");
        history.put("delta",       points);
        history.put("date",        date);
        history.put("timestamp",   System.currentTimeMillis());
        batch.set(db.collection("pointHistory").document(), history);

        batch.commit();
    }

    // ─── Inner model ─────────────────────────────────────────────────────────
    static class PointHistory {
        String description, date;
        int delta;
        PointHistory(String desc, int delta, String date) {
            this.description = desc; this.delta = delta; this.date = date;
        }
    }

    // ─── Adapter lịch sử điểm ────────────────────────────────────────────────
    class PointHistoryAdapter extends RecyclerView.Adapter<PointHistoryAdapter.VH> {
        private List<PointHistory> items;
        PointHistoryAdapter(List<PointHistory> items) { this.items = items; }

        @Override
        public VH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(PointsActivity.this)
                    .inflate(R.layout.item_point_history, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            PointHistory ph = items.get(pos);
            h.tvDesc.setText(ph.description != null ? ph.description : "");
            h.tvDate.setText(ph.date != null ? ph.date : "");
            if (ph.delta >= 0) {
                h.tvDelta.setText("+" + ph.delta + " điểm");
                h.tvDelta.setTextColor(0xFF00C48C);
                h.dot.setBackgroundColor(0xFF00C48C);
            } else {
                h.tvDelta.setText(ph.delta + " điểm");
                h.tvDelta.setTextColor(0xFFFF9800);
                h.dot.setBackgroundColor(0xFFFF9800);
            }
        }

        @Override public int getItemCount() { return items.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvDesc, tvDate, tvDelta;
            View dot;
            VH(View v) {
                super(v);
                tvDesc  = v.findViewById(R.id.tvPointDesc);
                tvDate  = v.findViewById(R.id.tvPointDate);
                tvDelta = v.findViewById(R.id.tvPointDelta);
                dot     = v.findViewById(R.id.viewDot);
            }
        }
    }
}