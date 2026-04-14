package com.example.ewallet_thang;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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

public class TopUpActivity extends AppCompatActivity {

    private static final int REQUEST_VOUCHER = 2001;

    private TextView tvCurrentBalance;
    private Spinner  spinnerBank;
    private EditText etAmount;
    private Button   btnTopUp;
    private ImageView btnBack;
    private CardView btn50k, btn100k, btn200k, btn500k;

    // Voucher UI
    private CardView  cardVoucherSection;
    private TextView  tvVoucherApplied;
    private Button    btnSelectVoucher, btnRemoveVoucher;

    private FirebaseFirestore  db;
    private SharedPreferences  sharedPreferences;
    private String             userPhone;
    private double             currentBalance;
    private NumberFormat       currencyFormat;
    private long               selectedAmount = 0;

    // Voucher state
    private String  appliedVoucherId   = null;
    private long    appliedVoucherValue = 0;

    private String[] banks = {
            "Vietcombank","MB Bank","Vietinbank","TP Bank",
            "Techcombank","ACB","VPBank","Agribank","BIDV","Sacombank"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topup);

        db            = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
        userPhone     = sharedPreferences.getString("userPhone", "");
        currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

        initViews();
        loadCurrentBalance();
        setupListeners();
        selectQuickAmount(100000, btn100k);
    }

    private void initViews() {
        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
        spinnerBank      = findViewById(R.id.spinnerBank);
        etAmount         = findViewById(R.id.etAmount);
        btnTopUp         = findViewById(R.id.btnTopUp);
        btnBack          = findViewById(R.id.btnBack);

        btn50k  = findViewById(R.id.btn50k);
        btn100k = findViewById(R.id.btn100k);
        btn200k = findViewById(R.id.btn200k);
        btn500k = findViewById(R.id.btn500k);

        cardVoucherSection = findViewById(R.id.cardVoucherSection);
        tvVoucherApplied   = findViewById(R.id.tvVoucherApplied);
        btnSelectVoucher   = findViewById(R.id.btnSelectVoucher);
        btnRemoveVoucher   = findViewById(R.id.btnRemoveVoucher);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, banks);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spinnerBank != null) spinnerBank.setAdapter(adapter);
    }

    private void loadCurrentBalance() {
        currentBalance = sharedPreferences.getFloat("balance", 0);
        db.collection("users").document(userPhone).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Double bal = doc.getDouble("balance");
                        if (bal != null) {
                            currentBalance = bal;
                            sharedPreferences.edit().putFloat("balance", bal.floatValue()).apply();
                        }
                    }
                    updateBalanceDisplay();
                })
                .addOnFailureListener(e -> updateBalanceDisplay());
    }

    private void updateBalanceDisplay() {
        if (tvCurrentBalance != null) {
            tvCurrentBalance.setText("**** 4829  •  " + currencyFormat.format(currentBalance) + "đ");
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        if (btn50k  != null) btn50k.setOnClickListener(v  -> selectQuickAmount(50000,  btn50k));
        if (btn100k != null) btn100k.setOnClickListener(v -> selectQuickAmount(100000, btn100k));
        if (btn200k != null) btn200k.setOnClickListener(v -> selectQuickAmount(200000, btn200k));
        if (btn500k != null) btn500k.setOnClickListener(v -> selectQuickAmount(500000, btn500k));

        if (etAmount != null) {
            etAmount.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void afterTextChanged(Editable s) {}
                @Override
                public void onTextChanged(CharSequence s, int st, int b, int c) {
                    String raw = s.toString().replace(".", "").replace(",", "").trim();
                    if (!raw.isEmpty()) {
                        try {
                            selectedAmount = Long.parseLong(raw);
                            clearQuickHighlight();
                            updateButtonText();
                        } catch (NumberFormatException ignored) { selectedAmount = 0; }
                    } else {
                        selectedAmount = 0;
                        updateButtonText();
                    }
                }
            });
        }

        if (btnSelectVoucher != null) {
            btnSelectVoucher.setOnClickListener(v -> {
                Intent intent = new Intent(this, VoucherActivity.class);
                startActivityForResult(intent, REQUEST_VOUCHER);
            });
        }
        if (btnRemoveVoucher != null) {
            btnRemoveVoucher.setOnClickListener(v -> removeVoucher());
        }

        btnTopUp.setOnClickListener(v -> processTopUp());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VOUCHER && resultCode == Activity.RESULT_OK && data != null) {
            String voucherType = data.getStringExtra("voucherType");

            if (!"TOPUP".equals(voucherType)) {
                Toast.makeText(this, "Voucher này chỉ dùng khi chuyển khoản!", Toast.LENGTH_SHORT).show();
                return;
            }

            appliedVoucherId    = data.getStringExtra("voucherId");
            appliedVoucherValue = data.getLongExtra("voucherValue", 0);
            String code         = data.getStringExtra("voucherCode");

            if (tvVoucherApplied != null) {
                tvVoucherApplied.setText("🎟 " + code + "  (-" + currencyFormat.format(appliedVoucherValue) + "đ)");
                tvVoucherApplied.setVisibility(View.VISIBLE);
            }
            if (btnRemoveVoucher != null) btnRemoveVoucher.setVisibility(View.VISIBLE);
            if (btnSelectVoucher != null) btnSelectVoucher.setText("Đổi voucher");

            updateButtonText();
            Toast.makeText(this, "Áp dụng voucher thành công! 🎉", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeVoucher() {
        appliedVoucherId    = null;
        appliedVoucherValue = 0;
        if (tvVoucherApplied != null) tvVoucherApplied.setVisibility(View.GONE);
        if (btnRemoveVoucher != null) btnRemoveVoucher.setVisibility(View.GONE);
        if (btnSelectVoucher != null) btnSelectVoucher.setText("Chọn voucher");
        updateButtonText();
    }

    private void selectQuickAmount(long amount, CardView selectedBtn) {
        selectedAmount = amount;
        clearQuickHighlight();
        if (selectedBtn != null) {
            selectedBtn.setCardBackgroundColor(0xFFE8EAFF);
            setCardTextColor(selectedBtn, "#3D5AFE");
        }
        if (etAmount != null) etAmount.setText(currencyFormat.format(amount));
        updateButtonText();
    }

    private void clearQuickHighlight() {
        int white = 0xFFFFFFFF;
        String dark = "#0D0F3C";
        if (btn50k  != null) { btn50k.setCardBackgroundColor(white);  setCardTextColor(btn50k,  dark); }
        if (btn100k != null) { btn100k.setCardBackgroundColor(white); setCardTextColor(btn100k, dark); }
        if (btn200k != null) { btn200k.setCardBackgroundColor(white); setCardTextColor(btn200k, dark); }
        if (btn500k != null) { btn500k.setCardBackgroundColor(white); setCardTextColor(btn500k, dark); }
    }

    private void setCardTextColor(CardView card, String hexColor) {
        View child = card.getChildAt(0);
        if (child instanceof TextView) {
            ((TextView) child).setTextColor(android.graphics.Color.parseColor(hexColor));
        }
    }

    private void updateButtonText() {
        long finalAmount = Math.max(0, selectedAmount - appliedVoucherValue);
        if (appliedVoucherValue > 0 && selectedAmount > 0) {
            btnTopUp.setText("Nạp " + currencyFormat.format(selectedAmount)
                    + "đ  →  bạn trả " + currencyFormat.format(finalAmount) + "đ");
        } else if (selectedAmount > 0) {
            btnTopUp.setText("Nạp tiền  •  " + currencyFormat.format(selectedAmount) + "đ");
        } else {
            btnTopUp.setText("Nạp tiền");
        }
    }

    private void processTopUp() {
        if (selectedAmount <= 0) {
            Toast.makeText(this, "Vui lòng chọn hoặc nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedBank = (spinnerBank != null && spinnerBank.getSelectedItem() != null)
                ? spinnerBank.getSelectedItem().toString() : "Ngân hàng";

        double creditAmount = selectedAmount;

        btnTopUp.setEnabled(false);
        btnTopUp.setText("Đang xử lý...");

        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String desc = "Nạp tiền từ " + selectedBank
                + (appliedVoucherValue > 0
                ? " (Voucher -" + currencyFormat.format(appliedVoucherValue) + "đ)" : "");

        // Kiểm tra giao dịch đầu tiên
        boolean isFirstTx = !sharedPreferences.getBoolean("hasCompletedFirstTx_" + userPhone, false);
        long pointsEarned = (long)(creditAmount / 10_000);

        WriteBatch batch = db.batch();

        // 1. Cộng tiền vào ví
        batch.update(db.collection("users").document(userPhone),
                "balance", FieldValue.increment(creditAmount));

        // 2. Cộng điểm nếu > 0
        if (pointsEarned > 0) {
            batch.update(db.collection("users").document(userPhone),
                    "points", FieldValue.increment(pointsEarned));

            Map<String, Object> ph = new HashMap<>();
            ph.put("userPhone",   userPhone);
            ph.put("description", "Nạp tiền từ " + selectedBank + " → +" + pointsEarned + " điểm");
            ph.put("delta",       pointsEarned);
            ph.put("date",        date);
            ph.put("timestamp",   System.currentTimeMillis());
            batch.set(db.collection("pointHistory").document(), ph);
        }

        // 3. Giao dịch
        Map<String, Object> trans = new HashMap<>();
        trans.put("userPhone",   userPhone);
        trans.put("type",        "DEPOSIT");
        trans.put("amount",      creditAmount);
        trans.put("description", desc);
        trans.put("category",    "Nạp tiền");
        trans.put("date",        date);
        trans.put("timestamp",   System.currentTimeMillis());
        batch.set(db.collection("transactions").document(), trans);

        // 4. Thông báo
        Map<String, Object> notif = new HashMap<>();
        notif.put("userPhone", userPhone);
        notif.put("title",  "Thu nhập");
        notif.put("message","+ " + currencyFormat.format(creditAmount) + "đ");
        notif.put("type",   "INCOME");
        notif.put("date",   date);
        notif.put("isRead", false);
        batch.set(db.collection("notifications").document(), notif);

        // 5. Đánh dấu voucher đã dùng
        if (appliedVoucherId != null) {
            batch.update(db.collection("vouchers").document(appliedVoucherId), "isUsed", true);
        }

        // 6. Ưu đãi giao dịch đầu tiên
        if (isFirstTx) {
            addFirstTransactionBonus(batch, date);
            batch.update(db.collection("users").document(userPhone), "hasCompletedFirstTx", true);
        }

        final double  fd  = creditAmount;
        final long    fp  = pointsEarned;
        final boolean fit = isFirstTx;

        batch.commit().addOnSuccessListener(aVoid -> {
            sharedPreferences.edit()
                    .putFloat("balance", (float)(currentBalance + fd))
                    .putBoolean("hasCompletedFirstTx_" + userPhone, true)
                    .apply();

            // Hiển thị SuccessBottomSheet (nhất quán với Transfer và Payment)
            showSuccessSheet(fd, selectedBank, date, fp, fit);

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            btnTopUp.setEnabled(true);
            updateButtonText();
        });
    }

    private void showSuccessSheet(double amount, String bank, String date,
                                  long points, boolean isFirstTx) {
        SuccessBottomSheet sheet = SuccessBottomSheet.newInstance(
                SuccessBottomSheet.TYPE_TOPUP,
                amount,
                bank,
                date,
                "Nạp tiền",
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

    private void addFirstTransactionBonus(WriteBatch batch, String date) {
        // Voucher 100K
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

    @Override
    protected void onResume() {
        super.onResume();
        loadCurrentBalance();
    }
}