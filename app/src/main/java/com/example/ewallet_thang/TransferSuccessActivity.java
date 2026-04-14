package com.example.ewallet_thang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * TransferSuccessActivity — KHÔNG CÒN LÀM MÀN HÌNH RIÊNG.
 *
 * Activity này chỉ còn để tương thích ngược với code cũ (nếu còn nơi nào
 * gọi startActivity với TransferSuccessActivity). Nó sẽ đọc Intent extras
 * và hiện SuccessBottomSheet rồi kết thúc.
 *
 * ─────────────────────────────────────────────────────────────────────────
 * CÁCH DÙNG MỚI (trong TransferActivity, TopUpActivity, PaymentActivity):
 *
 *   SuccessBottomSheet sheet = SuccessBottomSheet.newInstance(
 *       SuccessBottomSheet.TYPE_TRANSFER,  // TYPE_TOPUP / TYPE_PAYMENT
 *       deductAmount,
 *       recipientNameOrBankName,
 *       dateString,
 *       categoryLabel,           // null nếu là nạp tiền
 *       pointsEarned,            // 0 nếu không có
 *       isFirstTransaction       // true nếu giao dịch đầu tiên
 *   );
 *   sheet.setOnDismissListener(goHome -> {
 *       if (goHome) {
 *           startActivity(new Intent(this, MainActivity.class)
 *               .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
 *       }
 *       finish();
 *   });
 *   sheet.show(getSupportFragmentManager(), "success");
 * ─────────────────────────────────────────────────────────────────────────
 */
public class TransferSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Layout tối giản — BottomSheet sẽ phủ lên
        setContentView(R.layout.activity_transfer_success);

        SharedPreferences prefs = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
        String userPhone = prefs.getString("userPhone", "");

        Intent intent       = getIntent();
        String recipient    = intent.getStringExtra("recipientName");
        double amount       = intent.getDoubleExtra("amount", 0);
        String date         = intent.getStringExtra("date");
        String category     = intent.getStringExtra("category");
        long   points       = intent.getLongExtra("points", 0);
        boolean isFirstTx   = intent.getBooleanExtra("isFirstTx", false);

        // Xác định loại từ category
        int type = SuccessBottomSheet.TYPE_TRANSFER;
        if ("Nạp tiền".equals(category)) {
            type = SuccessBottomSheet.TYPE_TOPUP;
        }

        SuccessBottomSheet sheet = SuccessBottomSheet.newInstance(
                type,
                amount,
                recipient != null ? recipient : "",
                date != null ? date : "",
                category,
                points,
                isFirstTx
        );
        sheet.setOnDismissListener(goHome -> {
            if (goHome) {
                startActivity(new Intent(this, MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
            finish();
        });
        sheet.show(getSupportFragmentManager(), "success");
    }
}