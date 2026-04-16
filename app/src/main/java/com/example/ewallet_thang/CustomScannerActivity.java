//package com.example.ewallet_thang;
//
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.net.Uri;
//import android.os.Bundle;
//import android.provider.MediaStore;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.android.material.card.MaterialCardView;
//import com.google.zxing.BinaryBitmap;
//import com.google.zxing.LuminanceSource;
//import com.google.zxing.MultiFormatReader;
//import com.google.zxing.RGBLuminanceSource;
//import com.google.zxing.Result;
//import com.google.zxing.common.HybridBinarizer;
//import com.journeyapps.barcodescanner.CaptureManager;
//import com.journeyapps.barcodescanner.DecoratedBarcodeView;
//
//import java.io.InputStream;
//
//public class CustomScannerActivity extends AppCompatActivity {
//
//    private static final int PICK_IMAGE_REQUEST = 1002;
//
//    private CaptureManager capture;
//    private DecoratedBarcodeView barcodeScannerView;
//    private MaterialCardView btnFlash, btnGallery;
//    private boolean isFlashOn = false;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_custom_scanner);
//
//        barcodeScannerView = findViewById(R.id.zxing_barcode_scanner);
//        btnFlash = findViewById(R.id.btnFlash);
//        btnGallery = findViewById(R.id.btnGallery); // Khai báo nút thư viện mới
//
//        // Khởi tạo trình quản lý quét Camera
//        capture = new CaptureManager(this, barcodeScannerView);
//        capture.initializeFromIntent(getIntent(), savedInstanceState);
//        capture.decode();
//
//        // 1. Xử lý bật/tắt đèn Flash
//        btnFlash.setOnClickListener(v -> {
//            if (isFlashOn) {
//                barcodeScannerView.setTorchOff();
//                isFlashOn = false;
//                btnFlash.setCardBackgroundColor(0x4D000000);
//            } else {
//                barcodeScannerView.setTorchOn();
//                isFlashOn = true;
//                btnFlash.setCardBackgroundColor(0x4DFFFFFF);
//            }
//        });
//
//        // 2. Xử lý mở Thư viện ảnh
//        btnGallery.setOnClickListener(v -> {
//            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            startActivityForResult(intent, PICK_IMAGE_REQUEST);
//        });
//    }
//
//    // 3. Xử lý ảnh được chọn từ Thư viện
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
//            Uri imageUri = data.getData();
//            try {
//                // Đọc ảnh thành Bitmap
//                InputStream inputStream = getContentResolver().openInputStream(imageUri);
//                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//
//                // Chuyển đổi Bitmap thành định dạng mà ZXing đọc được
//                int width = bitmap.getWidth();
//                int height = bitmap.getHeight();
//                int[] pixels = new int[width * height];
//                bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
//
//                LuminanceSource source = new RGBLuminanceSource(width, height, pixels);
//                BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
//
//                // Giải mã QR Code
//                MultiFormatReader reader = new MultiFormatReader();
//                Result result = reader.decode(binaryBitmap);
//
//                // Nếu đọc thành công, giả lập Intent trả về cho MainActivity
//                Intent resultIntent = new Intent();
//                resultIntent.putExtra("SCAN_RESULT", result.getText());
//                setResult(RESULT_OK, resultIntent);
//                finish(); // Đóng màn hình quét và quay về Trang chủ xử lý giao dịch
//
//            } catch (Exception e) {
//                // Không tìm thấy mã QR trong ảnh
//                Toast.makeText(this, "Không tìm thấy mã QR hợp lệ trong ảnh này!", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    // Các hàm vòng đời BẮT BUỘC phải có để Camera hoạt động
//    @Override
//    protected void onResume() { super.onResume(); capture.onResume(); }
//
//    @Override
//    protected void onPause() { super.onPause(); capture.onPause(); }
//
//    @Override
//    protected void onDestroy() { super.onDestroy(); capture.onDestroy(); }
//
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        capture.onSaveInstanceState(outState);
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        capture.onRequestPermissionsResult(requestCode, permissions, grantResults);
//    }
//}

package com.example.ewallet_thang;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Size;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * QR Scanner hiện đại dùng CameraX + ML Kit Barcode (Google).
 * Tương tự MoMo, ZaloPay, VNPay.
 *
 * Tính năng:
 *  - Preview full-screen mượt với CameraX
 *  - Scan real-time qua ML Kit (nhanh hơn ZXing ~3-5x)
 *  - Animation laser quét chạy liên tục
 *  - Bật/tắt đèn flash
 *  - Chọn ảnh từ thư viện → giải mã QR offline
 *  - Haptic feedback khi scan thành công
 *  - Tự động xử lý permission camera
 */
public class CustomScannerActivity extends AppCompatActivity {

    // ── Request codes ──────────────────────────────────────────────────────────
    public static final String EXTRA_SCAN_RESULT = "SCAN_RESULT";

    // ── Views ──────────────────────────────────────────────────────────────────
    private PreviewView previewView;
    private View        laserLine;
    private View        overlayTop, overlayBottom, overlayLeft, overlayRight;
    private ImageButton btnBack;
    private MaterialCardView btnFlash, btnGallery;
    private TextView    tvHint;

    // ── CameraX ────────────────────────────────────────────────────────────────
    private ProcessCameraProvider cameraProvider;
    private Camera                camera;
    private ExecutorService       cameraExecutor;
    private BarcodeScanner        barcodeScanner;

    // ── State ──────────────────────────────────────────────────────────────────
    private boolean isFlashOn    = false;
    private boolean hasScanned   = false; // chặn scan nhiều lần liên tiếp
    private ValueAnimator laserAnimator;

    // ── Launchers ──────────────────────────────────────────────────────────────
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startCamera();
                else {
                    Toast.makeText(this, "Cần quyền Camera để quét QR", Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) decodeQRFromGallery(uri);
            });

    // ══════════════════════════════════════════════════════════════════════════
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_scanner);

        initViews();
        setupListeners();

        cameraExecutor = Executors.newSingleThreadExecutor();
        initBarcodeScanner();

        // Kiểm tra permission Camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    // ── Init ──────────────────────────────────────────────────────────────────
    private void initViews() {
        previewView   = findViewById(R.id.previewView);
        laserLine     = findViewById(R.id.laserLine);
        overlayTop    = findViewById(R.id.overlayTop);
        overlayBottom = findViewById(R.id.overlayBottom);
        overlayLeft   = findViewById(R.id.overlayLeft);
        overlayRight  = findViewById(R.id.overlayRight);
        btnBack       = findViewById(R.id.btnBack);
        btnFlash      = findViewById(R.id.btnFlash);
        btnGallery    = findViewById(R.id.btnGallery);
//        tvHint        = findViewById(R.id.tvHint);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

//        btnFlash.setOnClickListener(v -> {
//            if (camera == null) return;
//            isFlashOn = !isFlashOn;
//            camera.getCameraControl().enableTorch(isFlashOn);
//            // Đổi màu nút khi bật đèn
//            btnFlash.setCardBackgroundColor(isFlashOn ? 0xCCFFFFFF : 0x4D000000);
//            ImageButton icon = btnFlash.findViewById(R.id.ivFlashIcon);
//            if (icon != null) icon.setImageResource(
//                    isFlashOn ? R.drawable.ic_flash_on : R.drawable.ic_flash_off);
//        });

        btnGallery.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
    }

    // ── ML Kit ────────────────────────────────────────────────────────────────
    private void initBarcodeScanner() {
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)  // chỉ QR để nhanh hơn
                .build();
        barcodeScanner = BarcodeScanning.getClient(options);
    }

    // ── CameraX ───────────────────────────────────────────────────────────────
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);

        future.addListener(() -> {
            try {
                cameraProvider = future.get();
                bindCameraUseCases();
                startLaserAnimation();
            } catch (Exception e) {
                Toast.makeText(this, "Không thể khởi động camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases() {
        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        // Preview — full screen, mượt
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // ImageAnalysis — gửi frame cho ML Kit
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeImage);

        // Unbind trước khi bind lại
        cameraProvider.unbindAll();
        camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalysis);
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void analyzeImage(@NonNull ImageProxy imageProxy) {
        if (hasScanned) {
            imageProxy.close();
            return;
        }

        if (imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }

        InputImage inputImage = InputImage.fromMediaImage(
                imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees()
        );

        barcodeScanner.process(inputImage)
                .addOnSuccessListener(barcodes -> {
                    if (!barcodes.isEmpty() && !hasScanned) {
                        Barcode barcode = barcodes.get(0);
                        String rawValue = barcode.getRawValue();
                        if (rawValue != null && !rawValue.isEmpty()) {
                            hasScanned = true;
                            onQRCodeDetected(rawValue);
                        }
                    }
                })
                .addOnCompleteListener(task -> imageProxy.close());
    }

    // ── Xử lý kết quả QR ─────────────────────────────────────────────────────
    private void onQRCodeDetected(String result) {
        // Haptic feedback giống app ví điện tử thật
        previewView.performHapticFeedback(HapticFeedbackConstants.CONFIRM);

        // Dừng laser animation
        stopLaserAnimation();

        // Hiện feedback thành công


        // Delay nhỏ để user thấy feedback rồi mới trả kết quả
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_SCAN_RESULT, result);
            setResult(RESULT_OK, resultIntent);
            finish();
        }, 400);
    }

    // ── Giải mã QR từ ảnh thư viện ───────────────────────────────────────────
    private void decodeQRFromGallery(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap == null) {
                Toast.makeText(this, "Không thể đọc ảnh", Toast.LENGTH_SHORT).show();
                return;
            }

            InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
            barcodeScanner.process(inputImage)
                    .addOnSuccessListener(barcodes -> {
                        if (!barcodes.isEmpty()) {
                            String rawValue = barcodes.get(0).getRawValue();
                            if (rawValue != null && !rawValue.isEmpty()) {
                                onQRCodeDetected(rawValue);
                            } else {
                                Toast.makeText(this, "Mã QR không hợp lệ", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Không tìm thấy mã QR trong ảnh này",
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi đọc ảnh: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
        } catch (Exception e) {
            Toast.makeText(this, "Không thể mở ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    // ── Laser animation ───────────────────────────────────────────────────────
    private void startLaserAnimation() {
        // Chờ layout xong mới lấy được chiều cao vùng scan
        previewView.post(() -> {
            View scanFrame = findViewById(R.id.scanFrame);
            if (scanFrame == null || laserLine == null) return;

            int frameHeight = scanFrame.getHeight();
            int laserHeight = laserLine.getHeight();
            int travel      = frameHeight - laserHeight;

            laserAnimator = ValueAnimator.ofInt(0, travel);
            laserAnimator.setDuration(1800);
            laserAnimator.setRepeatCount(ValueAnimator.INFINITE);
            laserAnimator.setRepeatMode(ValueAnimator.REVERSE);
            laserAnimator.setInterpolator(new LinearInterpolator());
            laserAnimator.addUpdateListener(anim -> {
                int translationY = (int) anim.getAnimatedValue();
                laserLine.setTranslationY(translationY);
            });
            laserAnimator.start();
        });
    }

    private void stopLaserAnimation() {
        if (laserAnimator != null) {
            laserAnimator.cancel();
            laserAnimator = null;
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    @Override
    protected void onResume() {
        super.onResume();
        hasScanned = false; // Reset khi quay lại màn hình
        if (laserAnimator == null) startLaserAnimation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLaserAnimation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLaserAnimation();
        cameraExecutor.shutdown();
        if (barcodeScanner != null) barcodeScanner.close();
    }
}