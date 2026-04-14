package com.example.ewallet_thang;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {

    private static final int REQUEST_VOUCHER = 3001;

    private ImageButton btnBack;
    private CardView tabElectricity, tabWater, tabMobile, tabInternet;
    private TextView tvTabElectricity, tvTabWater, tvTabMobile, tvTabInternet;

    private EditText etProviderCode, etAmount, etNote;
    private TextView tvCurrentBalance, tvProviderLabel, tvAmountLabel;

    private Button btnQuick50k, btnQuick100k, btnQuick200k, btnQuick500k;

    private com.google.android.material.card.MaterialCardView cardVoucherUI;
    private TextView tvVoucherApplied;
    private Button btnSelectVoucher, btnRemoveVoucher;

    private TextView tvSummaryAmount, tvSummaryDiscount, tvSummaryFinal;
    private LinearLayout layoutDiscount;
    private Button btnPay;

    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;
    private String userPhone;
    private double currentBalance;
    private NumberFormat currencyFormat;

    private String selectedCategory = "ELECTRICITY";
    private String appliedVoucherId = null;
    private long appliedVoucherValue = 0;

    private static final String[][] CATEGORIES = {
            {"ELECTRICITY", "Tiền điện",    "Mã khách hàng (VD: PE123)", "Số tiền cần thanh toán (VNĐ)"},
            {"WATER",       "Tiền nước",    "Mã danh bộ (VD: WA456)",    "Số tiền cần thanh toán (VNĐ)"},
            {"MOBILE",      "Nạp tiền ĐT",  "Số điện thoại nạp",         "Mệnh giá (VNĐ)"},
            {"INTERNET",    "Cước Internet", "Mã hợp đồng / SĐT",         "Cước phí (VNĐ)"},
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
        userPhone = sharedPreferences.getString("userPhone", "");
        currentBalance = sharedPreferences.getFloat("balance", 0);
        currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

        initViews();
        setupListeners();
        selectTab(0);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);

        tabElectricity = findViewById(R.id.tabElectricity);
        tabWater       = findViewById(R.id.tabWater);
        tabMobile      = findViewById(R.id.tabInternet);
        tabInternet    = findViewById(R.id.tabTransfer);

        tvTabElectricity = findViewById(R.id.tvTabElectricity);
        tvTabWater       = findViewById(R.id.tvTabWater);
        tvTabMobile      = findViewById(R.id.tvTabInternet);
        tvTabInternet    = findViewById(R.id.tvTabTransfer);

        etProviderCode   = findViewById(R.id.etProviderCode);
        etAmount         = findViewById(R.id.etAmount);
        etNote           = findViewById(R.id.etNote);
        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
        tvProviderLabel  = findViewById(R.id.tvProviderLabel);
        tvAmountLabel    = findViewById(R.id.tvAmountLabel);

        btnQuick50k  = findViewById(R.id.btnQuick50k);
        btnQuick100k = findViewById(R.id.btnQuick100k);
        btnQuick200k = findViewById(R.id.btnQuick200k);
        btnQuick500k = findViewById(R.id.btnQuick500k);

        cardVoucherUI    = findViewById(R.id.cardVoucher);
        tvVoucherApplied = findViewById(R.id.tvVoucherApplied);
        btnSelectVoucher = findViewById(R.id.btnSelectVoucher);
        btnRemoveVoucher = findViewById(R.id.btnRemoveVoucher);

        tvSummaryAmount   = findViewById(R.id.tvSummaryAmount);
        tvSummaryDiscount = findViewById(R.id.tvSummaryDiscount);
        tvSummaryFinal    = findViewById(R.id.tvSummaryFinal);
        layoutDiscount    = findViewById(R.id.layoutDiscount);
        btnPay            = findViewById(R.id.btnPay);

        tvTabMobile.setText("Nạp ĐT");
        tvTabInternet.setText("Internet");
        tvCurrentBalance.setText("Số dư: " + currencyFormat.format(currentBalance) + "đ");
        etAmount.setEnabled(true);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        tabElectricity.setOnClickListener(v -> selectTab(0));
        tabWater.setOnClickListener(v -> selectTab(1));
        tabMobile.setOnClickListener(v -> selectTab(2));
        tabInternet.setOnClickListener(v -> selectTab(3));

        btnQuick50k.setOnClickListener(v  -> setQuickAmount(50_000));
        btnQuick100k.setOnClickListener(v -> setQuickAmount(100_000));
        btnQuick200k.setOnClickListener(v -> setQuickAmount(200_000));
        btnQuick500k.setOnClickListener(v -> setQuickAmount(500_000));

        etAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { updateSummary(); }
        });

        btnSelectVoucher.setOnClickListener(v -> {
            Intent i = new Intent(this, VoucherActivity.class);
            startActivityForResult(i, REQUEST_VOUCHER);
        });
        btnRemoveVoucher.setOnClickListener(v -> removeVoucher());
        btnPay.setOnClickListener(v -> processPayment());
    }

    private void setQuickAmount(long amount) {
        etAmount.setText(String.valueOf(amount));
        etAmount.setSelection(etAmount.getText().length());
    }

    private void selectTab(int index) {
        CardView[] tabs  = {tabElectricity, tabWater, tabMobile, tabInternet};
        TextView[] texts = {tvTabElectricity, tvTabWater, tvTabMobile, tvTabInternet};

        for (int i = 0; i < tabs.length; i++) {
            tabs[i].setCardBackgroundColor(0xFFEEF0FF);
            texts[i].setTextColor(0xFF5063C8);
        }
        tabs[index].setCardBackgroundColor(0xFF3D5AFE);
        texts[index].setTextColor(0xFFFFFFFF);

        selectedCategory = CATEGORIES[index][0];
        tvProviderLabel.setText(CATEGORIES[index][2]);
        tvAmountLabel.setText(CATEGORIES[index][3]);
        etProviderCode.setText("");
        etAmount.setText("");
        etAmount.setEnabled(true);
        removeVoucher();

        if (cardVoucherUI != null) {
            cardVoucherUI.setVisibility(
                    selectedCategory.equals("MOBILE") ? View.GONE : View.VISIBLE);
        }
    }

    private void updateSummary() {
        String raw = etAmount.getText().toString().trim();
        double amount = 0;
        try { amount = Double.parseDouble(raw); } catch (Exception ignored) {}

        double discount    = Math.min(appliedVoucherValue, amount);
        double finalAmount = amount - discount;

        tvSummaryAmount.setText(currencyFormat.format(amount) + "đ");
        tvSummaryFinal.setText(currencyFormat.format(finalAmount) + "đ");

        if (discount > 0 && amount > 0) {
            layoutDiscount.setVisibility(View.VISIBLE);
            tvSummaryDiscount.setText("-" + currencyFormat.format(discount) + "đ");
        } else {
            layoutDiscount.setVisibility(View.GONE);
        }
        btnPay.setText(amount > 0
                ? "Thanh toán " + currencyFormat.format(finalAmount) + "đ"
                : "Thanh toán");
    }

    private void removeVoucher() {
        appliedVoucherId    = null;
        appliedVoucherValue = 0;
        if (tvVoucherApplied != null) tvVoucherApplied.setVisibility(View.GONE);
        if (btnRemoveVoucher != null) btnRemoveVoucher.setVisibility(View.GONE);
        if (btnSelectVoucher != null) btnSelectVoucher.setText("Chọn voucher");
        updateSummary();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VOUCHER && resultCode == Activity.RESULT_OK && data != null) {
            appliedVoucherId    = data.getStringExtra("voucherId");
            appliedVoucherValue = data.getLongExtra("voucherValue", 0);
            String code         = data.getStringExtra("voucherCode");

            tvVoucherApplied.setText("🎟 " + code + "  -" + currencyFormat.format(appliedVoucherValue) + "đ");
            tvVoucherApplied.setVisibility(View.VISIBLE);
            btnRemoveVoucher.setVisibility(View.VISIBLE);
            btnSelectVoucher.setText("Đổi voucher");
            updateSummary();
        }
    }

    private void processPayment() {
        String code = etProviderCode.getText().toString().trim();
        String raw  = etAmount.getText().toString().trim();

        if (code.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã dịch vụ", Toast.LENGTH_SHORT).show();
            return;
        }
        if (raw.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }
        double amount = Double.parseDouble(raw);
        if (amount <= 0) {
            Toast.makeText(this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
            return;
        }

        double discount = Math.min(appliedVoucherValue, amount);
        double deduct   = amount - discount;

        if (deduct > currentBalance) {
            Toast.makeText(this, "Số dư không đủ! Cần " + currencyFormat.format(deduct) + "đ",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnPay.setEnabled(false);
        btnPay.setText("Đang xử lý...");

        // ── Kiểm tra giao dịch đầu tiên (dùng local flag để tránh query thêm) ──
        boolean isFirstTx = !sharedPreferences.getBoolean("hasCompletedFirstTx_" + userPhone, false);

        String date           = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        long   pointsEarned   = (long)(deduct / 10_000);

        WriteBatch batch = db.batch();

        // Trừ ví
        batch.update(db.collection("users").document(userPhone),
                "balance", FieldValue.increment(-deduct));

        // Cộng điểm nếu > 0
        if (pointsEarned > 0) {
            batch.update(db.collection("users").document(userPhone),
                    "points", FieldValue.increment(pointsEarned));

            Map<String, Object> ph = new HashMap<>();
            ph.put("userPhone",   userPhone);
            ph.put("description", getCategoryLabel() + ": " + code + " → +" + pointsEarned + " điểm");
            ph.put("delta",       pointsEarned);
            ph.put("date",        date);
            ph.put("timestamp",   System.currentTimeMillis());
            batch.set(db.collection("pointHistory").document(), ph);
        }

        // Lưu giao dịch
        Map<String, Object> tx = new HashMap<>();
        tx.put("userPhone",      userPhone);
        tx.put("type",           "EXPENSE");
        tx.put("amount",         deduct);
        tx.put("originalAmount", amount);
        tx.put("discount",       discount);
        tx.put("description",    getCategoryLabel() + ": " + code);
        tx.put("category",       getCategoryLabel());
        tx.put("note",           etNote.getText().toString().trim());
        tx.put("date",           date);
        tx.put("timestamp",      System.currentTimeMillis());
        batch.set(db.collection("transactions").document(), tx);

        // Đánh dấu voucher đã dùng
        if (appliedVoucherId != null) {
            batch.update(db.collection("vouchers").document(appliedVoucherId), "isUsed", true);
        }

        // ── Ưu đãi giao dịch đầu tiên ──────────────────────────────────────────
        if (isFirstTx) {
            addFirstTransactionBonus(batch, date);
            batch.update(db.collection("users").document(userPhone), "hasCompletedFirstTx", true);
        }

        final double  fd  = deduct;
        final long    fp  = pointsEarned;
        final boolean fit = isFirstTx;

        batch.commit().addOnSuccessListener(aVoid -> {
            sharedPreferences.edit()
                    .putFloat("balance", (float)(currentBalance - fd))
                    .putBoolean("hasCompletedFirstTx_" + userPhone, true)
                    .apply();

            showSuccessSheet(fd, code, date, fp, fit);

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Thanh toán thất bại, thử lại!", Toast.LENGTH_SHORT).show();
            btnPay.setEnabled(true);
            updateSummary();
        });
    }

    private void showSuccessSheet(double amount, String code, String date,
                                  long points, boolean isFirstTx) {
        SuccessBottomSheet sheet = SuccessBottomSheet.newInstance(
                SuccessBottomSheet.TYPE_PAYMENT,
                amount,
                getCategoryLabel() + ": " + code,
                date,
                getCategoryLabel(),
                points,
                isFirstTx
        );
        sheet.setOnDismissListener(goHome -> {
            if (goHome) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            finish();
        });
        sheet.show(getSupportFragmentManager(), "success");
    }

    /**
     * Thêm vào batch: voucher 100K + 100 điểm khởi đầu cho giao dịch đầu tiên.
     */
    private void addFirstTransactionBonus(WriteBatch batch, String date) {
        // Voucher 100K áp dụng cho giao dịch tiếp theo
        Map<String, Object> voucher = new HashMap<>();
        voucher.put("userPhone",   userPhone);
        voucher.put("code",        "FIRST100K");
        voucher.put("value",       100_000L);
        voucher.put("type",        "TRANSFER");
        voucher.put("description", "🎁 Ưu đãi giao dịch đầu tiên – giảm 100.000đ");
        voucher.put("isUsed",      false);
        voucher.put("expiryDate",  getExpiryDate(30));
        voucher.put("createdAt",   System.currentTimeMillis());
        batch.set(db.collection("vouchers").document(), voucher);

        // +100 điểm thưởng
        batch.update(db.collection("users").document(userPhone),
                "points", FieldValue.increment(100));

        Map<String, Object> ph = new HashMap<>();
        ph.put("userPhone",   userPhone);
        ph.put("description", "🎁 Thưởng giao dịch đầu tiên → +100 điểm");
        ph.put("delta",       100);
        ph.put("date",        date);
        ph.put("timestamp",   System.currentTimeMillis());
        batch.set(db.collection("pointHistory").document(), ph);
    }

    private String getExpiryDate(int days) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_MONTH, days);
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.getTime());
    }

    private String getCategoryLabel() {
        for (String[] cat : CATEGORIES) {
            if (cat[0].equals(selectedCategory)) return cat[1];
        }
        return "Thanh toán";
    }
}