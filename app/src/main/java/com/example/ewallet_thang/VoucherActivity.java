package com.example.ewallet_thang;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class VoucherActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ChipGroup chipGroup;
    private Chip chipAvailable, chipUsed, chipExpired;
    private RecyclerView rvVouchers;
    private LinearLayout layoutEmpty;
    private TextView tvVoucherCount;

    private FirebaseFirestore db;
    private SharedPreferences prefs;
    private String userPhone;

    private VoucherAdapter adapter;
    private List<VoucherModel> allVouchers = new ArrayList<>();
    private String currentFilter = "AVAILABLE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voucher);

        db = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
        userPhone = prefs.getString("userPhone", "");

        initViews();
        setupListeners();
        loadVouchers();
    }

    private void initViews() {
        btnBack       = findViewById(R.id.btnBack);
        chipGroup     = findViewById(R.id.chipGroup);
        chipAvailable = findViewById(R.id.chipAvailable);
        chipUsed      = findViewById(R.id.chipUsed);
        chipExpired   = findViewById(R.id.chipExpired);
        rvVouchers    = findViewById(R.id.rvVouchers);
        layoutEmpty   = findViewById(R.id.layoutEmpty);
        tvVoucherCount = findViewById(R.id.tvVoucherCount);

        adapter = new VoucherAdapter(this, new ArrayList<>(), this::onVoucherApply);
        rvVouchers.setLayoutManager(new LinearLayoutManager(this));
        rvVouchers.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipAvailable)   currentFilter = "AVAILABLE";
            else if (id == R.id.chipUsed)   currentFilter = "USED";
            else if (id == R.id.chipExpired) currentFilter = "EXPIRED";
            filterAndShow();
        });
    }

    private void loadVouchers() {
        db.collection("vouchers")
                .whereEqualTo("userPhone", userPhone)
                .get()
                .addOnSuccessListener(snapshots -> {
                    allVouchers.clear();
                    for (DocumentSnapshot doc : snapshots) {
                        String code    = doc.getString("code");
                        String desc    = doc.getString("description");
                        long   value   = doc.getLong("value") != null ? doc.getLong("value") : 0;
                        String expiry  = doc.getString("expiryDate");
                        boolean isUsed = Boolean.TRUE.equals(doc.getBoolean("isUsed"));
                        String type    = doc.getString("type");  // TOPUP / TRANSFER

                        String status = isUsed ? "USED" : isExpired(expiry) ? "EXPIRED" : "AVAILABLE";
                        allVouchers.add(new VoucherModel(doc.getId(), code, desc, value, expiry, status, type));
                    }
                    filterAndShow();
                });
    }

    private void filterAndShow() {
        List<VoucherModel> filtered = new ArrayList<>();
        int availableCount = 0;

        for (VoucherModel v : allVouchers) {
            if (v.status.equals("AVAILABLE")) availableCount++;
            if (v.status.equals(currentFilter)) filtered.add(v);
        }

        tvVoucherCount.setText(availableCount + " voucher khả dụng");

        if (filtered.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvVouchers.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvVouchers.setVisibility(View.VISIBLE);
            adapter.updateData(filtered);
        }
    }

    private void onVoucherApply(VoucherModel voucher) {
        // Truyền mã voucher về màn hình nạp tiền / chuyển khoản
        android.content.Intent result = new android.content.Intent();
        result.putExtra("voucherCode",  voucher.code);
        result.putExtra("voucherValue", voucher.value);
        result.putExtra("voucherId",    voucher.id);
        result.putExtra("voucherType",  voucher.type);
        setResult(RESULT_OK, result);
        finish();
    }

    private boolean isExpired(String expiryDate) {
        if (expiryDate == null || expiryDate.isEmpty()) return false;
        try {
            java.text.SimpleDateFormat sdf =
                    new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            java.util.Date expiry = sdf.parse(expiryDate);
            return expiry != null && expiry.before(new java.util.Date());
        } catch (Exception e) { return false; }
    }

    // ─── Inner model ───────────────────────────────────────────────────────────
    public static class VoucherModel {
        public String id, code, description, expiryDate, status, type;
        public long value;

        public VoucherModel(String id, String code, String description,
                            long value, String expiryDate, String status, String type) {
            this.id = id; this.code = code; this.description = description;
            this.value = value; this.expiryDate = expiryDate;
            this.status = status; this.type = type;
        }
    }

    // ─── Adapter ───────────────────────────────────────────────────────────────
    public static class VoucherAdapter
            extends RecyclerView.Adapter<VoucherAdapter.VH> {

        interface OnApply { void apply(VoucherModel v); }

        private android.content.Context ctx;
        private List<VoucherModel> list;
        private OnApply listener;
        private java.text.NumberFormat fmt =
                java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));

        public VoucherAdapter(android.content.Context ctx,
                              List<VoucherModel> list, OnApply listener) {
            this.ctx = ctx; this.list = list; this.listener = listener;
        }

        public void updateData(List<VoucherModel> newList) {
            list.clear(); list.addAll(newList); notifyDataSetChanged();
        }

        @Override
        public VH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View v = android.view.LayoutInflater.from(ctx)
                    .inflate(R.layout.item_voucher, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            VoucherModel vm = list.get(pos);
            h.tvCode.setText(vm.code);
            h.tvDesc.setText(vm.description != null ? vm.description : "");
            h.tvValue.setText(fmt.format(vm.value) + " ₫");
            h.tvExpiry.setText("HSD: " + (vm.expiryDate != null ? vm.expiryDate : "--"));

            // Loại voucher
            if ("TOPUP".equals(vm.type)) {
                h.tvType.setText("Nạp tiền");
                h.tvType.setBackgroundResource(R.drawable.badge_blue);
            } else {
                h.tvType.setText("Chuyển khoản");
                h.tvType.setBackgroundResource(R.drawable.badge_green);
            }

            if ("AVAILABLE".equals(vm.status)) {
                h.btnApply.setVisibility(android.view.View.VISIBLE);
                h.tvUsedTag.setVisibility(android.view.View.GONE);
                h.btnApply.setOnClickListener(v -> listener.apply(vm));
                h.itemView.setAlpha(1f);
            } else {
                h.btnApply.setVisibility(android.view.View.GONE);
                h.tvUsedTag.setVisibility(android.view.View.VISIBLE);
                h.tvUsedTag.setText("USED".equals(vm.status) ? "Đã dùng" : "Hết hạn");
                h.itemView.setAlpha(0.5f);
            }
        }

        @Override public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvCode, tvDesc, tvValue, tvExpiry, tvType, tvUsedTag;
            android.widget.Button btnApply;
            VH(android.view.View v) {
                super(v);
                tvCode   = v.findViewById(R.id.tvVoucherCode);
                tvDesc   = v.findViewById(R.id.tvVoucherDesc);
                tvValue  = v.findViewById(R.id.tvVoucherValue);
                tvExpiry = v.findViewById(R.id.tvVoucherExpiry);
                tvType   = v.findViewById(R.id.tvVoucherType);
                tvUsedTag = v.findViewById(R.id.tvUsedTag);
                btnApply = v.findViewById(R.id.btnApplyVoucher);
            }
        }
    }
}