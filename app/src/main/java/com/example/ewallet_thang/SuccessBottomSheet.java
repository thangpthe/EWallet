package com.example.ewallet_thang;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Bottom Sheet thông báo giao dịch thành công.
 * Dùng chung cho: Nạp tiền, Chuyển tiền, Thanh toán hóa đơn.
 *
 * Cách gọi:
 *   SuccessBottomSheet sheet = SuccessBottomSheet.newInstance(
 *       SuccessBottomSheet.TYPE_TOPUP,   // hoặc TYPE_TRANSFER / TYPE_PAYMENT
 *       500_000,
 *       "Vietcombank",                   // recipientOrBank
 *       "2025-06-01 10:30:00",
 *       "Nạp tiền",                      // category (có thể null)
 *       100,                             // pointsEarned (0 nếu không có)
 *       true                             // isFirstTransaction
 *   );
 *   sheet.show(getSupportFragmentManager(), "success");
 */
public class SuccessBottomSheet extends BottomSheetDialogFragment {

    // ── Loại giao dịch ────────────────────────────────────────────────────────
    public static final int TYPE_TOPUP    = 0;
    public static final int TYPE_TRANSFER = 1;
    public static final int TYPE_PAYMENT  = 2;

    // ── Argument keys ─────────────────────────────────────────────────────────
    private static final String ARG_TYPE        = "type";
    private static final String ARG_AMOUNT      = "amount";
    private static final String ARG_RECIPIENT   = "recipient";
    private static final String ARG_DATE        = "date";
    private static final String ARG_CATEGORY    = "category";
    private static final String ARG_POINTS      = "points";
    private static final String ARG_FIRST_TX    = "firstTx";

    // ── Listener để activity xử lý sau khi đóng ───────────────────────────────
    public interface OnDismissListener {
        /** goHome = true → về trang chủ; false → giao dịch mới */
        void onDismiss(boolean goHome);
    }

    private OnDismissListener dismissListener;

    // ── Factory ───────────────────────────────────────────────────────────────
    public static SuccessBottomSheet newInstance(int type, double amount,
                                                 String recipientOrBank,
                                                 String date, String category,
                                                 long pointsEarned,
                                                 boolean isFirstTransaction) {
        SuccessBottomSheet f = new SuccessBottomSheet();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE,      type);
        args.putDouble(ARG_AMOUNT, amount);
        args.putString(ARG_RECIPIENT, recipientOrBank);
        args.putString(ARG_DATE,   date);
        args.putString(ARG_CATEGORY, category);
        args.putLong(ARG_POINTS,   pointsEarned);
        args.putBoolean(ARG_FIRST_TX, isFirstTransaction);
        f.setArguments(args);
        return f;
    }

    public void setOnDismissListener(OnDismissListener l) { this.dismissListener = l; }

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Không cancel được (phải nhấn nút)
        setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_success, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = requireArguments();
        int    type       = args.getInt(ARG_TYPE, TYPE_TRANSFER);
        double amount     = args.getDouble(ARG_AMOUNT, 0);
        String recipient  = args.getString(ARG_RECIPIENT, "");
        String date       = args.getString(ARG_DATE, "");
        String category   = args.getString(ARG_CATEGORY, "");
        long   points     = args.getLong(ARG_POINTS, 0);
        boolean firstTx   = args.getBoolean(ARG_FIRST_TX, false);

        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));

        // ── Views ──────────────────────────────────────────────────────────────
        TextView tvIcon        = view.findViewById(R.id.tvSuccessIcon);
        TextView tvTitle       = view.findViewById(R.id.tvSuccessTitle);
        TextView tvSubtitle    = view.findViewById(R.id.tvSuccessSubtitle);
        TextView tvAmount      = view.findViewById(R.id.tvSuccessAmount);
        TextView tvRowLabel1   = view.findViewById(R.id.tvRowLabel1);
        TextView tvRowValue1   = view.findViewById(R.id.tvRowValue1);
        TextView tvRowLabel2   = view.findViewById(R.id.tvRowLabel2);
        TextView tvRowValue2   = view.findViewById(R.id.tvRowValue2);
        TextView tvRowLabel3   = view.findViewById(R.id.tvRowLabel3);
        TextView tvRowValue3   = view.findViewById(R.id.tvRowValue3);
        View     layoutRow3    = view.findViewById(R.id.layoutRow3);
        View     layoutBonus   = view.findViewById(R.id.layoutBonus);
        TextView tvBonusText   = view.findViewById(R.id.tvBonusText);
        MaterialButton btnNew  = view.findViewById(R.id.btnNewTransaction);
        MaterialButton btnHome = view.findViewById(R.id.btnBackToHome);

        // ── Nội dung theo loại giao dịch ───────────────────────────────────────
        switch (type) {
            case TYPE_TOPUP:
                tvIcon.setText("💰");
                tvTitle.setText("Nạp tiền thành công!");
                tvSubtitle.setText("Số dư đã được cập nhật");
                tvAmount.setText("+" + fmt.format(amount) + "đ");
                tvAmount.setTextColor(0xFF00C48C); // xanh lá

                tvRowLabel1.setText("Nguồn tiền");
                tvRowValue1.setText(recipient);
                tvRowLabel2.setText("Thời gian");
                tvRowValue2.setText(date);
                layoutRow3.setVisibility(View.GONE);
                break;

            case TYPE_TRANSFER:
                tvIcon.setText("✈️");
                tvTitle.setText("Chuyển tiền thành công!");
                tvSubtitle.setText("Giao dịch đã được xử lý");
                tvAmount.setText("-" + fmt.format(amount) + "đ");
                tvAmount.setTextColor(0xFFEF4444); // đỏ

                tvRowLabel1.setText("Người nhận");
                tvRowValue1.setText(recipient.toUpperCase());
                tvRowLabel2.setText("Thời gian");
                tvRowValue2.setText(date);
                tvRowLabel3.setText("Phân loại");
                tvRowValue3.setText(category != null ? category : "Chuyển tiền");
                layoutRow3.setVisibility(View.VISIBLE);
                break;

            case TYPE_PAYMENT:
                tvIcon.setText("⚡");
                tvTitle.setText("Thanh toán thành công!");
                tvSubtitle.setText("Hóa đơn đã được xử lý");
                tvAmount.setText("-" + fmt.format(amount) + "đ");
                tvAmount.setTextColor(0xFFEF4444);

                tvRowLabel1.setText("Dịch vụ");
                tvRowValue1.setText(recipient);
                tvRowLabel2.setText("Thời gian");
                tvRowValue2.setText(date);
                tvRowLabel3.setText("Phân loại");
                tvRowValue3.setText(category != null ? category : "Thanh toán");
                layoutRow3.setVisibility(View.VISIBLE);
                break;
        }

        // ── Banner ưu đãi ──────────────────────────────────────────────────────
        if (firstTx) {
            // Giao dịch đầu tiên → hiện banner đặc biệt
            layoutBonus.setVisibility(View.VISIBLE);
            tvBonusText.setText("🎁  Giao dịch đầu tiên! Bạn vừa nhận voucher 100K chào mừng.");
        } else if (points > 0) {
            // Có điểm tích lũy → hiện số điểm
            layoutBonus.setVisibility(View.VISIBLE);
            tvBonusText.setText("⭐  Bạn vừa tích được +" + points + " điểm từ giao dịch này!");
        } else {
            layoutBonus.setVisibility(View.GONE);
        }

        // ── Buttons ────────────────────────────────────────────────────────────
        btnNew.setOnClickListener(v -> {
            dismiss();
            if (dismissListener != null) dismissListener.onDismiss(false);
        });
        btnHome.setOnClickListener(v -> {
            dismiss();
            if (dismissListener != null) dismissListener.onDismiss(true);
        });
    }
}