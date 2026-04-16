package com.example.ewallet_thang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ewallet_thang.models.VisaCard;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Màn hình quản lý thẻ Visa:
 *  - Hiển thị danh sách thẻ đã lưu
 *  - Thêm thẻ mới
 *  - Xoá thẻ
 *  - Trả kết quả chọn thẻ về cho PaymentActivity
 */
public class VisaCardActivity extends AppCompatActivity {

    /** Khi mở để chọn thẻ, truyền extra này = true */
    public static final String EXTRA_PICK_MODE = "pickMode";

    private ImageButton     btnBack;
    private LinearLayout    llCards;
    private Button          btnAddCard;
    private TextView        tvEmpty;

    private FirebaseFirestore db;
    private SharedPreferences prefs;
    private String            userPhone;

    private final List<VisaCard> cards = new ArrayList<>();
    private boolean pickMode = false;

    // ─────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visa_card);

        db        = FirebaseFirestore.getInstance();
        prefs     = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
        userPhone = prefs.getString("userPhone", "");
        pickMode  = getIntent().getBooleanExtra(EXTRA_PICK_MODE, false);

        initViews();
        loadCards();
    }

    private void initViews() {
        btnBack    = findViewById(R.id.btnBack);
        llCards    = findViewById(R.id.llCards);
        btnAddCard = findViewById(R.id.btnAddCard);
        tvEmpty    = findViewById(R.id.tvEmpty);

        btnBack.setOnClickListener(v -> finish());
        btnAddCard.setOnClickListener(v -> showAddCardDialog());
    }

    // ─── Load danh sách thẻ từ Firestore ────────────────────────
    private void loadCards() {
        db.collection("visaCards")
                .whereEqualTo("userPhone", userPhone)
                .get()
                .addOnSuccessListener(snap -> {
                    cards.clear();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        VisaCard c = new VisaCard(
                                doc.getId(),
                                doc.getString("userPhone"),
                                doc.getString("cardNumber") != null ? doc.getString("cardNumber") : "",
                                doc.getString("cardHolder") != null ? doc.getString("cardHolder") : "",
                                doc.getString("expiry")     != null ? doc.getString("expiry")     : "",
                                doc.getString("cardBrand")  != null ? doc.getString("cardBrand")  : "VISA"
                        );
                        Boolean def = doc.getBoolean("isDefault");
                        c.setDefault(def != null && def);
                        cards.add(c);
                    }
                    renderCards();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải danh sách thẻ", Toast.LENGTH_SHORT).show());
    }

    // ─── Vẽ danh sách thẻ ───────────────────────────────────────
    private void renderCards() {
        llCards.removeAllViews();

        if (cards.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }
        tvEmpty.setVisibility(View.GONE);

        LayoutInflater inf = LayoutInflater.from(this);
        for (VisaCard card : cards) {
            View row = inf.inflate(R.layout.item_visa_card, llCards, false);

            TextView tvMasked  = row.findViewById(R.id.tvCardNumber);
            TextView tvHolder  = row.findViewById(R.id.tvCardHolder);
            TextView tvExpiry  = row.findViewById(R.id.tvCardExpiry);
            TextView tvBrand   = row.findViewById(R.id.tvCardBrand);
            Button   btnDelete = row.findViewById(R.id.btnDeleteCard);
            Button   btnSelect = row.findViewById(R.id.btnSelectCard);

            tvMasked.setText(card.getMaskedNumber());
            tvHolder.setText(card.getCardHolder().toUpperCase());
            tvExpiry.setText("Hết hạn: " + card.getExpiry());
            tvBrand.setText(card.getCardBrand());

            // Ẩn / hiện nút tuỳ mode
            btnSelect.setVisibility(pickMode ? View.VISIBLE : View.GONE);
            btnDelete.setVisibility(View.VISIBLE);

            btnSelect.setOnClickListener(v -> returnCard(card));
            btnDelete.setOnClickListener(v -> confirmDelete(card));

            // Click vào card row cũng chọn thẻ nếu ở pickMode
            if (pickMode) {
                row.setOnClickListener(v -> returnCard(card));
            }

            llCards.addView(row);
        }
    }

    // ─── Trả thẻ về cho Activity gọi ───────────────────────────
    private void returnCard(VisaCard card) {
        Intent result = new Intent();
        result.putExtra("cardId",     card.getCardId());
        result.putExtra("last4",      card.getLast4());
        result.putExtra("cardHolder", card.getCardHolder());
        result.putExtra("expiry",     card.getExpiry());
        result.putExtra("cardBrand",  card.getCardBrand());
        setResult(RESULT_OK, result);
        finish();
    }

    // ─── Dialog thêm thẻ mới ────────────────────────────────────
    private void showAddCardDialog() {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_visa_card, null);

        TextInputEditText etNumber = view.findViewById(R.id.etCardNumber);
        TextInputEditText etHolder = view.findViewById(R.id.etCardHolder);
        TextInputEditText etExpiry = view.findViewById(R.id.etExpiry);
        TextInputEditText etCvv    = view.findViewById(R.id.etCvv);
        TextView          tvBrandPreview = view.findViewById(R.id.tvBrandPreview);

        // Auto-format số thẻ theo nhóm 4
        etNumber.addTextChangedListener(new TextWatcher() {
            boolean isFormatting = false;
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;
                String raw    = s.toString().replace(" ", "");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < raw.length() && i < 16; i++) {
                    if (i > 0 && i % 4 == 0) sb.append(' ');
                    sb.append(raw.charAt(i));
                }
                etNumber.setText(sb.toString());
                etNumber.setSelection(sb.length());

                // Xác định brand
                String brand = detectBrand(raw);
                tvBrandPreview.setText(brand);
                isFormatting = false;
            }
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
        });

        // Auto-format MM/YY
        etExpiry.addTextChangedListener(new TextWatcher() {
            boolean isFormatting = false;
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;
                String raw = s.toString().replace("/", "");
                if (raw.length() > 2) {
                    String formatted = raw.substring(0, 2) + "/" + raw.substring(2, Math.min(raw.length(), 4));
                    etExpiry.setText(formatted);
                    etExpiry.setSelection(formatted.length());
                }
                isFormatting = false;
            }
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
        });

        new AlertDialog.Builder(this)
                .setTitle("Thêm thẻ Visa / Mastercard")
                .setView(view)
                .setPositiveButton("Lưu thẻ", (dialog, which) -> {
                    String rawNumber = etNumber.getText() != null
                            ? etNumber.getText().toString().replace(" ", "").trim() : "";
                    String holder    = etHolder.getText() != null
                            ? etHolder.getText().toString().trim() : "";
                    String expiry    = etExpiry.getText() != null
                            ? etExpiry.getText().toString().trim() : "";
                    String cvv       = etCvv.getText()    != null
                            ? etCvv.getText().toString().trim() : "";

                    if (!validateCard(rawNumber, holder, expiry, cvv)) return;

                    saveCard(rawNumber, holder, expiry, detectBrand(rawNumber));
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    // ─── Validate cơ bản ─────────────────────────────────────────
    private boolean validateCard(String number, String holder, String expiry, String cvv) {
        if (number.length() < 16) {
            Toast.makeText(this, "Số thẻ không hợp lệ (cần 16 chữ số)", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (holder.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên chủ thẻ", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!expiry.matches("\\d{2}/\\d{2}")) {
            Toast.makeText(this, "Ngày hết hạn không hợp lệ (MM/YY)", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (cvv.length() < 3) {
            Toast.makeText(this, "CVV không hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // ─── Lưu thẻ vào Firestore (KHÔNG lưu CVV) ──────────────────
    private void saveCard(String number, String holder, String expiry, String brand) {
        // Chỉ lưu 4 số cuối để bảo mật
        String last4 = number.substring(number.length() - 4);

        Map<String, Object> data = new HashMap<>();
        data.put("userPhone",  userPhone);
        data.put("cardNumber", last4);          // chỉ lưu last4
        data.put("cardHolder", holder);
        data.put("expiry",     expiry);
        data.put("cardBrand",  brand);
        data.put("isDefault",  cards.isEmpty()); // thẻ đầu tiên = mặc định
        data.put("createdAt",  System.currentTimeMillis());

        db.collection("visaCards").add(data)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, "Đã thêm thẻ thành công!", Toast.LENGTH_SHORT).show();
                    loadCards();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi lưu thẻ: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ─── Xác nhận xoá thẻ ────────────────────────────────────────
    private void confirmDelete(VisaCard card) {
        new AlertDialog.Builder(this)
                .setTitle("Xoá thẻ")
                .setMessage("Bạn có chắc muốn xoá thẻ **** " + card.getLast4() + "?")
                .setPositiveButton("Xoá", (d, w) ->
                        db.collection("visaCards").document(card.getCardId()).delete()
                                .addOnSuccessListener(v -> {
                                    Toast.makeText(this, "Đã xoá thẻ", Toast.LENGTH_SHORT).show();
                                    loadCards();
                                }))
                .setNegativeButton("Huỷ", null)
                .show();
    }

    // ─── Detect brand từ đầu số thẻ ─────────────────────────────
    private String detectBrand(String number) {
        if (number.startsWith("4"))                 return "VISA";
        if (number.startsWith("5") || number.startsWith("2")) return "MASTERCARD";
        if (number.startsWith("3"))                 return "AMEX";
        return "VISA";
    }
}