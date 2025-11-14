package com.example.ewallet_thang;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ewallet_thang.adapters.NotificationAdapter;
import com.example.ewallet_thang.database.DatabaseHelper;
import com.example.ewallet_thang.models.Notification;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvMarkAllRead;
    private RecyclerView rvNotifications;
    private LinearLayout layoutEmpty;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;
    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;
    private int userId;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getInt("userId", -1);

        initViews();
        setupListeners();
        loadNotifications();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvMarkAllRead = findViewById(R.id.tvMarkAllRead);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        rvNotifications = findViewById(R.id.rvNotifications);
        bottomNav = findViewById(R.id.bottomNav);

        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(this, notificationList, this::onNotificationClick);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(notificationAdapter);

        // Set selected item
        bottomNav.setSelectedItemId(R.id.nav_notification);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        tvMarkAllRead.setOnClickListener(v -> markAllAsRead());

        bottomNav.setOnItemSelectedListener(this::onNavigationItemSelected);
    }

    private boolean onNavigationItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            finish();
            return true;
        } else if (itemId == R.id.nav_notification) {
            return true;
        }
        return false;
    }

    private void loadNotifications() {
        notificationList.clear();

        Cursor cursor = dbHelper.getAllNotifications(userId);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTI_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTI_TITLE));
                String message = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTI_MESSAGE));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTI_TYPE));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTI_DATE));
                int isRead = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTI_IS_READ));
                int relatedId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTI_RELATED_ID));

                Notification notification = new Notification(id, userId, title, message,
                        type, date, isRead == 1, relatedId);
                notificationList.add(notification);

            } while (cursor.moveToNext());

            cursor.close();
        }

        if (notificationList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            findViewById(R.id.scrollContent).setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            findViewById(R.id.scrollContent).setVisibility(View.VISIBLE);
        }

        notificationAdapter.notifyDataSetChanged();
    }

    private void onNotificationClick(Notification notification) {
        if (!notification.isRead()) {
            dbHelper.markNotificationAsRead(notification.getNotificationId());
            loadNotifications();
        }
    }

    private void markAllAsRead() {
        dbHelper.markAllNotificationsAsRead(userId);
        loadNotifications();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}