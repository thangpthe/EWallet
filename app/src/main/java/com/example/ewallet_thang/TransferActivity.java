////package com.example.ewallet_thang;
////
////import android.Manifest;
////import android.content.Intent;
////import android.content.SharedPreferences;
////import android.content.pm.PackageManager;
////import android.database.Cursor;
////import android.os.Bundle;
////import android.text.Editable;
////import android.text.TextWatcher;
////import android.view.View;
////import android.widget.Button;
////import android.widget.EditText;
////import android.widget.ImageView;
////import android.widget.LinearLayout;
////import android.widget.TextView;
////import android.widget.Toast;
////
////import androidx.appcompat.app.AppCompatActivity;
////import androidx.cardview.widget.CardView;
////import androidx.core.app.ActivityCompat;
////import androidx.core.content.ContextCompat;
////
////import com.example.ewallet_thang.database.DatabaseHelper;
////
////import java.text.NumberFormat;
////import java.util.ArrayList;
////import java.util.List;
////import java.util.Locale;
////
////public class TransferActivity extends AppCompatActivity {
////
////    private static final int CAMERA_REQUEST_CODE = 1001;
////
////    private ImageView btnBack, ivScanQr;
////    private TextView tvCurrentBalance;
////    private EditText etSearch;
////    private LinearLayout llRecipientList;
////    private Button btnContinue;
////
////    private DatabaseHelper dbHelper;
////    private SharedPreferences sharedPreferences;
////
////    private int currentUserId;
////    private double currentBalance;
////
////    private NumberFormat currencyFormat;
////
////    private List<RecipientUser> allRecipients;       // User thật trong DB
////    private List<RecipientUser> defaultRecipients;  // 5 người mặc định
////    private List<RecipientUser> combinedList;       // Tổng hợp 2 nguồn
////
////    @Override
////    protected void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////        setContentView(R.layout.activity_transfer);
////
////        dbHelper = new DatabaseHelper(this);
////        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
////
////        currentUserId = sharedPreferences.getInt("userId", -1);
////        currentBalance = sharedPreferences.getFloat("balance", 0);
////
////        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
////
////        initViews();
////        setupListeners();
////        loadDefaultRecipients();   // 🆕 THÊM 5 NGƯỜI MẶC ĐỊNH
////        loadRecipientsFromDB();    // Load user thật từ database
////        mergeLists();              // GỘP 2 LIST LẠI
////        renderAllRecipients(combinedList);
////
////        checkCameraPermission();
////    }
////
////    private void initViews() {
////        btnBack = findViewById(R.id.btnBack);
////        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
////        etSearch = findViewById(R.id.etSearch);
////        llRecipientList = findViewById(R.id.llRecipientList);
////        btnContinue = findViewById(R.id.btnContinue);
////
////        tvCurrentBalance.setText(currencyFormat.format(currentBalance));
////    }
////
////    private void setupListeners() {
////        btnBack.setOnClickListener(v -> finish());
////
////
////        etSearch.addTextChangedListener(new TextWatcher() {
////            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
////            @Override public void afterTextChanged(Editable s) {}
////
////            @Override
////            public void onTextChanged(CharSequence s, int start, int before, int count) {
////                filterRecipients(s.toString());
////            }
////        });
////    }
////
////    private void checkCameraPermission() {
////        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
////                != PackageManager.PERMISSION_GRANTED) {
////
////            ActivityCompat.requestPermissions(
////                    this,
////                    new String[]{Manifest.permission.CAMERA},
////                    CAMERA_REQUEST_CODE
////            );
////        }
////    }
////
////    // =============================================================
////    // 🆕 THÊM 5 NGƯỜI MẶC ĐỊNH (KHÔNG PHỤ THUỘC DATABASE)
////    // =============================================================
////    private void loadDefaultRecipients() {
////        defaultRecipients = new ArrayList<>();
////
////        defaultRecipients.add(new RecipientUser(10001, "Nguyễn Minh", "Đức", "0901122334"));
////        defaultRecipients.add(new RecipientUser(10002, "Lê Hoàng", "Nam", "0902233445"));
////        defaultRecipients.add(new RecipientUser(10003, "Trần Ngọc", "Vy", "0903344556"));
////        defaultRecipients.add(new RecipientUser(10004, "Phạm Gia", "Bảo", "0904455667"));
////        defaultRecipients.add(new RecipientUser(10005, "Đoàn Khánh", "Linh", "0905566778"));
////    }
////
////    // =============================================================
////    // LOAD USER TỪ DATABASE
////    // =============================================================
////    private void loadRecipientsFromDB() {
////        allRecipients = new ArrayList<>();
////
////        Cursor cursor = dbHelper.getAllUsers();
////        if (cursor != null && cursor.moveToFirst()) {
////            do {
////                int id = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
////                if (id == currentUserId) continue;
////
////                String first = cursor.getString(cursor.getColumnIndexOrThrow("first_name"));
////                String last = cursor.getString(cursor.getColumnIndexOrThrow("last_name"));
////                String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
////
////                allRecipients.add(new RecipientUser(id, first, last, phone));
////            } while (cursor.moveToNext());
////            cursor.close();
////        }
////    }
////
////    // =============================================================
////    // GỘP 5 NGƯỜI MẶC ĐỊNH + USER TRONG DB
////    // =============================================================
////    private void mergeLists() {
////        combinedList = new ArrayList<>();
////        combinedList.addAll(defaultRecipients); // luôn có 5 người mặc định
////        combinedList.addAll(allRecipients);     // thêm user thật
////    }
////
////    // =============================================================
////    // HIỂN THỊ DANH SÁCH NGƯỜI NHẬN
////    // =============================================================
////    private void renderAllRecipients(List<RecipientUser> list) {
////        llRecipientList.removeAllViews();
////        for (RecipientUser r : list) addRecipientView(r);
////    }
////
////    private void addRecipientView(RecipientUser recipient) {
////
////        View view = getLayoutInflater().inflate(R.layout.item_recipient, llRecipientList, false);
////
////        TextView tvName = view.findViewById(R.id.tvRecipientName);
////        TextView tvPhone = view.findViewById(R.id.tvRecipientEmail);
////        TextView tvAvatar = view.findViewById(R.id.tvAvatar);
////
////        CardView card = view.findViewById(R.id.cardRecipient);
////
////        String fullName = recipient.firstName + " " + recipient.lastName;
////        tvName.setText(fullName);
////        tvPhone.setText(recipient.phone);
////
////        tvAvatar.setText(recipient.firstName.substring(0,1).toUpperCase());
////
////        card.setOnClickListener(v -> {
////            Intent intent = new Intent(TransferActivity.this, TransferConfirmActivity.class);
////            intent.putExtra("recipientId", recipient.userId);
////            intent.putExtra("recipientName", fullName);
////            intent.putExtra("recipientPhone", recipient.phone);
////            intent.putExtra("currentBalance", currentBalance);
////            startActivity(intent);
////        });
////
////        llRecipientList.addView(view);
////    }
////
////    // =============================================================
////    // TÌM KIẾM
////    // =============================================================
////    private void filterRecipients(String text) {
////        llRecipientList.removeAllViews();
////
////        if (text.isEmpty()) {
////            renderAllRecipients(combinedList);
////            return;
////        }
////
////        String key = text.toLowerCase();
////
////        List<RecipientUser> filtered = new ArrayList<>();
////
////        for (RecipientUser r : combinedList) {
////            String full = (r.firstName + " " + r.lastName).toLowerCase();
////            if (full.contains(key) || r.phone.contains(key)) {
////                filtered.add(r);
////            }
////        }
////
////        if (filtered.isEmpty()) {
////            TextView tv = new TextView(this);
////            tv.setText("Không tìm thấy người dùng");
////            tv.setPadding(32, 32, 32, 32);
////            llRecipientList.addView(tv);
////            return;
////        }
////
////        renderAllRecipients(filtered);
////    }
////
////    private static class RecipientUser {
////        int userId;
////        String firstName, lastName, phone;
////
////        RecipientUser(int id, String fn, String ln, String p) {
////            userId = id;
////            firstName = fn;
////            lastName = ln;
////            phone = p;
////        }
////    }
////
////}
//package com.example.ewallet_thang;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.cardview.widget.CardView;
//
//import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//import java.text.NumberFormat;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Locale;
//
//public class TransferActivity extends AppCompatActivity {
//
//    private ImageView btnBack;
//    private TextView tvCurrentBalance;
//    private EditText etSearch;
//    private LinearLayout llRecipientList;
//
//    private FirebaseFirestore db;
//    private SharedPreferences sharedPreferences;
//
//    private String currentPhone;
//    private double currentBalance;
//    private NumberFormat currencyFormat;
//
//    private List<RecipientUser> allRecipients;
//    private List<RecipientUser> defaultRecipients;
//    private List<RecipientUser> combinedList;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_transfer);
//
//        db = FirebaseFirestore.getInstance();
//        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
//
//        currentPhone = sharedPreferences.getString("userPhone", "");
//        currentBalance = sharedPreferences.getFloat("balance", 0);
//        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
//
//        initViews();
//        setupListeners();
//        loadDefaultRecipients();
//        loadRecipientsFromFirebase();
//    }
//
//    private void initViews() {
//        btnBack = findViewById(R.id.btnBack);
//        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
//        etSearch = findViewById(R.id.etSearch);
//        llRecipientList = findViewById(R.id.llRecipientList);
//        tvCurrentBalance.setText(currencyFormat.format(currentBalance));
//    }
//
//    private void setupListeners() {
//        btnBack.setOnClickListener(v -> finish());
//        etSearch.addTextChangedListener(new TextWatcher() {
//            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            @Override public void afterTextChanged(Editable s) {}
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                filterRecipients(s.toString());
//            }
//        });
//    }
//
//    private void loadDefaultRecipients() {
//        defaultRecipients = new ArrayList<>();
//        defaultRecipients.add(new RecipientUser("0901122334", "Nguyễn Minh", "Đức"));
//        defaultRecipients.add(new RecipientUser("0902233445", "Lê Hoàng", "Nam"));
//        defaultRecipients.add(new RecipientUser("0903344556", "Trần Ngọc", "Vy"));
//    }
//
//    private void loadRecipientsFromFirebase() {
//        allRecipients = new ArrayList<>();
//        combinedList = new ArrayList<>();
//
//        db.collection("users").get().addOnSuccessListener(queryDocumentSnapshots -> {
//            for (DocumentSnapshot doc : queryDocumentSnapshots) {
//                String phone = doc.getId();
//                if (phone.equals(currentPhone)) continue; // Bỏ qua chính mình
//
//                String first = doc.getString("firstName");
//                String last = doc.getString("lastName");
//                allRecipients.add(new RecipientUser(phone, first, last));
//            }
//
//            combinedList.addAll(defaultRecipients);
//            combinedList.addAll(allRecipients);
//            renderAllRecipients(combinedList);
//        });
//    }
//
//    private void renderAllRecipients(List<RecipientUser> list) {
//        llRecipientList.removeAllViews();
//        for (RecipientUser r : list) {
//            View view = getLayoutInflater().inflate(R.layout.item_recipient, llRecipientList, false);
//            TextView tvName = view.findViewById(R.id.tvRecipientName);
//            TextView tvPhone = view.findViewById(R.id.tvRecipientEmail);
//            TextView tvAvatar = view.findViewById(R.id.tvAvatar);
//            CardView card = view.findViewById(R.id.cardRecipient);
//
//            String fullName = r.firstName + " " + r.lastName;
//            tvName.setText(fullName);
//            tvPhone.setText(r.phone);
//            tvAvatar.setText(r.firstName.isEmpty() ? "U" : r.firstName.substring(0,1).toUpperCase());
//
//            card.setOnClickListener(v -> {
//                Intent intent = new Intent(TransferActivity.this, TransferConfirmActivity.class);
//                intent.putExtra("recipientName", fullName);
//                intent.putExtra("recipientPhone", r.phone);
//                intent.putExtra("currentBalance", currentBalance);
//                startActivity(intent);
//            });
//            llRecipientList.addView(view);
//        }
//    }
//
//    private void filterRecipients(String text) {
//        llRecipientList.removeAllViews();
//        if (text.isEmpty()) {
//            renderAllRecipients(combinedList);
//            return;
//        }
//
//        String key = text.toLowerCase();
//        List<RecipientUser> filtered = new ArrayList<>();
//        for (RecipientUser r : combinedList) {
//            String full = (r.firstName + " " + r.lastName).toLowerCase();
//            if (full.contains(key) || r.phone.contains(key)) filtered.add(r);
//        }
//        renderAllRecipients(filtered);
//    }
//
//    public static class RecipientUser {
//        String phone, firstName, lastName;
//        RecipientUser(String p, String fn, String ln) { phone = p; firstName = fn; lastName = ln; }
//    }
//}
package com.example.ewallet_thang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransferActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvCurrentBalance;
    private EditText etSearch;
    private LinearLayout llRecipientList;

    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;

    private String currentPhone;
    private double currentBalance;
    private NumberFormat currencyFormat;

    // Chỉ dùng duy nhất 1 danh sách lấy từ Firebase
    private List<RecipientUser> allRecipients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);

        currentPhone = sharedPreferences.getString("userPhone", "");
        currentBalance = sharedPreferences.getFloat("balance", 0);
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        initViews();
        setupListeners();

        // Gọi Firebase lấy danh sách User thật
        loadRecipientsFromFirebase();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
        etSearch = findViewById(R.id.etSearch);
        llRecipientList = findViewById(R.id.llRecipientList);
        tvCurrentBalance.setText(currencyFormat.format(currentBalance));
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRecipients(s.toString());
            }
        });
    }

    private void loadRecipientsFromFirebase() {
        allRecipients = new ArrayList<>();
        llRecipientList.removeAllViews();

        // Quét toàn bộ collection "users" trên Firestore
        db.collection("users").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                String phone = doc.getId();

                // Bỏ qua chính bản thân mình (Không tự chuyển khoản cho mình)
                if (phone.equals(currentPhone)) continue;

                String first = doc.getString("firstName");
                String last = doc.getString("lastName");

                // Đề phòng data Firebase bị rỗng
                if(first == null) first = "";
                if(last == null) last = "";

                allRecipients.add(new RecipientUser(phone, first, last));
            }

            renderAllRecipients(allRecipients);
        });
    }

    private void renderAllRecipients(List<RecipientUser> list) {
        llRecipientList.removeAllViews();

        if (list.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Chưa có người dùng nào trên hệ thống");
            tvEmpty.setPadding(32, 32, 32, 32);
            llRecipientList.addView(tvEmpty);
            return;
        }

        for (RecipientUser r : list) {
            View view = getLayoutInflater().inflate(R.layout.item_recipient, llRecipientList, false);
            TextView tvName = view.findViewById(R.id.tvRecipientName);
            TextView tvPhone = view.findViewById(R.id.tvRecipientEmail);
            TextView tvAvatar = view.findViewById(R.id.tvAvatar);
            CardView card = view.findViewById(R.id.cardRecipient);

            String fullName = (r.firstName + " " + r.lastName).trim();
            if (fullName.isEmpty()) fullName = "Người dùng ẩn danh";

            tvName.setText(fullName);
            tvPhone.setText(r.phone);

            // Xử lý icon avatar động (Lấy chữ cái đầu tiên)
            String initial = r.firstName.isEmpty() ? (r.lastName.isEmpty() ? "U" : r.lastName.substring(0,1)) : r.firstName.substring(0,1);
            tvAvatar.setText(initial.toUpperCase());

            String finalFullName = fullName;
            card.setOnClickListener(v -> {
                Intent intent = new Intent(TransferActivity.this, TransferConfirmActivity.class);
                intent.putExtra("recipientName", finalFullName);
                intent.putExtra("recipientPhone", r.phone);
                intent.putExtra("currentBalance", currentBalance);
                startActivity(intent);
            });
            llRecipientList.addView(view);
        }
    }

    private void filterRecipients(String text) {
        if (allRecipients == null) return;

        llRecipientList.removeAllViews();
        if (text.isEmpty()) {
            renderAllRecipients(allRecipients);
            return;
        }

        String key = text.toLowerCase();
        List<RecipientUser> filtered = new ArrayList<>();

        for (RecipientUser r : allRecipients) {
            String full = (r.firstName + " " + r.lastName).toLowerCase();
            if (full.contains(key) || r.phone.contains(key)) filtered.add(r);
        }

        renderAllRecipients(filtered);
    }

    public static class RecipientUser {
        String phone, firstName, lastName;
        RecipientUser(String p, String fn, String ln) { phone = p; firstName = fn; lastName = ln; }
    }
}