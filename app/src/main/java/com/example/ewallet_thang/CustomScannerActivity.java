package com.example.ewallet_thang;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.io.InputStream;

public class CustomScannerActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1002;

    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;
    private MaterialCardView btnFlash, btnGallery;
    private boolean isFlashOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_scanner);

        barcodeScannerView = findViewById(R.id.zxing_barcode_scanner);
        btnFlash = findViewById(R.id.btnFlash);
        btnGallery = findViewById(R.id.btnGallery); // Khai báo nút thư viện mới

        // Khởi tạo trình quản lý quét Camera
        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.decode();

        // 1. Xử lý bật/tắt đèn Flash
        btnFlash.setOnClickListener(v -> {
            if (isFlashOn) {
                barcodeScannerView.setTorchOff();
                isFlashOn = false;
                btnFlash.setCardBackgroundColor(0x4D000000);
            } else {
                barcodeScannerView.setTorchOn();
                isFlashOn = true;
                btnFlash.setCardBackgroundColor(0x4DFFFFFF);
            }
        });

        // 2. Xử lý mở Thư viện ảnh
        btnGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });
    }

    // 3. Xử lý ảnh được chọn từ Thư viện
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                // Đọc ảnh thành Bitmap
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                // Chuyển đổi Bitmap thành định dạng mà ZXing đọc được
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                int[] pixels = new int[width * height];
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

                LuminanceSource source = new RGBLuminanceSource(width, height, pixels);
                BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

                // Giải mã QR Code
                MultiFormatReader reader = new MultiFormatReader();
                Result result = reader.decode(binaryBitmap);

                // Nếu đọc thành công, giả lập Intent trả về cho MainActivity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("SCAN_RESULT", result.getText());
                setResult(RESULT_OK, resultIntent);
                finish(); // Đóng màn hình quét và quay về Trang chủ xử lý giao dịch

            } catch (Exception e) {
                // Không tìm thấy mã QR trong ảnh
                Toast.makeText(this, "Không tìm thấy mã QR hợp lệ trong ảnh này!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Các hàm vòng đời BẮT BUỘC phải có để Camera hoạt động
    @Override
    protected void onResume() { super.onResume(); capture.onResume(); }

    @Override
    protected void onPause() { super.onPause(); capture.onPause(); }

    @Override
    protected void onDestroy() { super.onDestroy(); capture.onDestroy(); }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}