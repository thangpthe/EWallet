package com.example.ewallet_thang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.ewallet_thang.database.DatabaseHelper;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransferActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvCurrentBalance;
    private EditText etSearch;
    private LinearLayout llRecipientList;
    private Button btnContinue;
    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;
    private int currentUserId;
    private double currentBalance;
    private NumberFormat currencyFormat;
    private List<RecipientUser> allRecipients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
        currentUserId = sharedPreferences.getInt("userId", -1);
        currentBalance = sharedPreferences.getFloat("balance", 0);
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        initViews();
        setupListeners();
        loadSampleRecipients(); // Load 2 người dùng mẫu
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
        etSearch = findViewById(R.id.etSearch);
        llRecipientList = findViewById(R.id.llRecipientList);
        btnContinue = findViewById(R.id.btnContinue);

        tvCurrentBalance.setText(currencyFormat.format(currentBalance));
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRecipients(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnContinue.setOnClickListener(v -> {
            // TODO: Add functionality if needed
        });
    }

    private void loadSampleRecipients() {
        allRecipients = new ArrayList<>();
        llRecipientList.removeAllViews();

        // Tạo 2 người dùng mẫu cố định
        RecipientUser user1 = new RecipientUser(2, "Người", "dùng 1", "0901234567");
        RecipientUser user2 = new RecipientUser(3, "Người", "dùng 2", "0907654321");

        allRecipients.add(user1);
        allRecipients.add(user2);

        // Hiển thị 2 người dùng
        addRecipientView(user1);
        addRecipientView(user2);
    }

    private void addRecipientView(RecipientUser recipient) {
        View recipientView = getLayoutInflater().inflate(R.layout.item_recipient, llRecipientList, false);

        CardView cardRecipient = recipientView.findViewById(R.id.cardRecipient);
        TextView tvRecipientName = recipientView.findViewById(R.id.tvRecipientName);
        TextView tvRecipientEmail = recipientView.findViewById(R.id.tvRecipientEmail);

        String fullName = recipient.firstName + " " + recipient.lastName;
        tvRecipientName.setText(fullName);

        // Hiển thị số điện thoại thay vì email
        if (recipient.phone != null && !recipient.phone.isEmpty()) {
            tvRecipientEmail.setText(recipient.phone);
        } else {
            tvRecipientEmail.setVisibility(View.GONE);
        }

        cardRecipient.setOnClickListener(v -> {
            Intent intent = new Intent(TransferActivity.this, TransferConfirmActivity.class);
            intent.putExtra("recipientId", recipient.userId);
            intent.putExtra("recipientName", fullName);
            intent.putExtra("recipientPhone", recipient.phone);
            intent.putExtra("currentBalance", currentBalance);
            startActivity(intent);
        });

        llRecipientList.addView(recipientView);
    }

    private void filterRecipients(String searchText) {
        llRecipientList.removeAllViews();

        if (searchText.isEmpty()) {
            for (RecipientUser recipient : allRecipients) {
                addRecipientView(recipient);
            }
            return;
        }

        String searchLower = searchText.toLowerCase();
        boolean found = false;

        for (RecipientUser recipient : allRecipients) {
            String fullName = (recipient.firstName + " " + recipient.lastName).toLowerCase();
            String phone = recipient.phone != null ? recipient.phone : "";

            if (fullName.contains(searchLower) || phone.contains(searchLower)) {
                addRecipientView(recipient);
                found = true;
            }
        }

        if (!found) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Không tìm thấy người dùng");
            tvEmpty.setPadding(32, 32, 32, 32);
            tvEmpty.setTextSize(14);
            tvEmpty.setTextColor(0xFF757575);
            llRecipientList.addView(tvEmpty);
        }
    }

    private static class RecipientUser {
        int userId;
        String firstName;
        String lastName;
        String phone;

        RecipientUser(int userId, String firstName, String lastName, String phone) {
            this.userId = userId;
            this.firstName = firstName;
            this.lastName = lastName;
            this.phone = phone;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh balance when returning to this screen
        currentBalance = sharedPreferences.getFloat("balance", 0);
        tvCurrentBalance.setText(currencyFormat.format(currentBalance));
    }
}