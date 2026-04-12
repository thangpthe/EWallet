////package com.example.ewallet_thang;
////
////import android.content.Intent;
////import android.content.SharedPreferences;
////import android.database.Cursor;
////import android.os.Bundle;
////import android.view.MenuItem;
////import android.view.View;
////import android.widget.ImageView;
////import android.widget.LinearLayout;
////import android.widget.TextView;
////import androidx.appcompat.app.AppCompatActivity;
////import androidx.recyclerview.widget.LinearLayoutManager;
////import androidx.recyclerview.widget.RecyclerView;
////import com.example.ewallet_thang.adapters.NotificationAdapter;
////import com.example.ewallet_thang.database.DatabaseHelper;
////import com.example.ewallet_thang.models.Notification;
////import com.google.android.material.bottomnavigation.BottomNavigationView;
////import java.util.ArrayList;
////import java.util.List;
////
////public class NotificationActivity extends AppCompatActivity {
////
////    private ImageView btnBack;
////    private TextView tvMarkAllRead;
////    private RecyclerView rvNotifications;
////    private LinearLayout layoutEmpty;
////    private NotificationAdapter notificationAdapter;
////    private List<Notification> notificationList;
////    private DatabaseHelper dbHelper;
////    private SharedPreferences sharedPreferences;
////    private int userId;
////    private BottomNavigationView bottomNav;
////
////    @Override
////    protected void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////        setContentView(R.layout.activity_notification);
////
////        dbHelper = new DatabaseHelper(this);
////        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
////        userId = sharedPreferences.getInt("userId", -1);
////
////        initViews();
////        setupListeners();
////        loadNotifications();
////    }
////
////    private void initViews() {
////        btnBack = findViewById(R.id.btnBack);
////        tvMarkAllRead = findViewById(R.id.tvMarkAllRead);
////        layoutEmpty = findViewById(R.id.layoutEmpty);
////        rvNotifications = findViewById(R.id.rvNotifications);
////        bottomNav = findViewById(R.id.bottomNav);
////
////        notificationList = new ArrayList<>();
////        notificationAdapter = new NotificationAdapter(this, notificationList, this::onNotificationClick);
////        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
////        rvNotifications.setAdapter(notificationAdapter);
////
////        // Set selected item
////        bottomNav.setSelectedItemId(R.id.nav_notification);
////    }
////
////    private void setupListeners() {
////        btnBack.setOnClickListener(v -> finish());
////
////        tvMarkAllRead.setOnClickListener(v -> markAllAsRead());
////
////        bottomNav.setOnItemSelectedListener(this::onNavigationItemSelected);
////    }
////
////    private boolean onNavigationItemSelected(MenuItem item) {
////        int itemId = item.getItemId();
////
////        if (itemId == R.id.nav_home) {
////            finish();
////            return true;
////        } else if (itemId == R.id.nav_notification) {
////            return true;
////        }
////        else if (itemId == R.id.nav_statistics) {
////            Intent intent = new Intent(NotificationActivity.this, StatisticsActivity.class);
////            startActivity(intent);
////            return true;
////        } else if (itemId == R.id.nav_profile) {
////            Intent intent = new Intent(NotificationActivity.this, ProfileActivity.class);
////            startActivity(intent);
////            return true;
////        }
////        return false;
////    }
////
////    private void loadNotifications() {
////        notificationList.clear();
////
////        Cursor cursor = dbHelper.getAllNotifications(userId);
////
////        if (cursor != null && cursor.moveToFirst()) {
////            do {
////                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTI_ID));
////                String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTI_TITLE));
////                String message = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTI_MESSAGE));
////                String type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTI_TYPE));
////                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTI_DATE));
////                int isRead = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTI_IS_READ));
////                int relatedId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTI_RELATED_ID));
////
////                Notification notification = new Notification(id, userId, title, message,
////                        type, date, isRead == 1, relatedId);
////                notificationList.add(notification);
////
////            } while (cursor.moveToNext());
////
////            cursor.close();
////        }
////
////        if (notificationList.isEmpty()) {
////            layoutEmpty.setVisibility(View.VISIBLE);
////            findViewById(R.id.scrollContent).setVisibility(View.GONE);
////        } else {
////            layoutEmpty.setVisibility(View.GONE);
////            findViewById(R.id.scrollContent).setVisibility(View.VISIBLE);
////        }
////
////        notificationAdapter.notifyDataSetChanged();
////    }
////
////    private void onNotificationClick(Notification notification) {
////        if (!notification.isRead()) {
////            dbHelper.markNotificationAsRead(notification.getNotificationId());
////            loadNotifications();
////        }
////    }
////
////    private void markAllAsRead() {
////        dbHelper.markAllNotificationsAsRead(userId);
////        loadNotifications();
////    }
////
////    @Override
////    protected void onDestroy() {
////        super.onDestroy();
////        if (dbHelper != null) {
////            dbHelper.close();
////        }
////    }
////}
//package com.example.ewallet_thang;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.database.Cursor;
//import android.os.Bundle;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//import com.example.ewallet_thang.adapters.NotificationAdapter;
//import com.example.ewallet_thang.database.DatabaseHelper;
//import com.example.ewallet_thang.models.Notification;
//import com.google.android.material.bottomnavigation.BottomNavigationView;
//import java.util.ArrayList;
//import java.util.List;
//
//public class NotificationActivity extends AppCompatActivity {
//
//    private ImageButton btnBack; // Đổi thành ImageButton theo layout mới
//    private TextView tvMarkAllRead;
//    private RecyclerView rvNotifications;
//    private LinearLayout layoutEmpty;
//    private NotificationAdapter notificationAdapter;
//    private List<Notification> notificationList;
//    private DatabaseHelper dbHelper;
//    private SharedPreferences sharedPreferences;
//    private int userId;
//    private BottomNavigationView bottomNav;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_notification);
//
//        dbHelper = new DatabaseHelper(this);
//        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
//        userId = sharedPreferences.getInt("userId", -1);
//
//        initViews();
//        setupListeners();
//        loadNotifications();
//    }
//
//    private void initViews() {
//        btnBack = findViewById(R.id.btnBack);
//        tvMarkAllRead = findViewById(R.id.tvMarkAllRead);
//        layoutEmpty = findViewById(R.id.layoutEmpty);
//        rvNotifications = findViewById(R.id.rvNotifications);
//        bottomNav = findViewById(R.id.bottomNav);
//
//        notificationList = new ArrayList<>();
//        notificationAdapter = new NotificationAdapter(this, notificationList, this::onNotificationClick);
//        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
//        rvNotifications.setAdapter(notificationAdapter);
//
//        // Set selected item
//        if(bottomNav != null) {
//            bottomNav.setSelectedItemId(R.id.nav_notification);
//        }
//    }
//
//    private void setupListeners() {
//        if(btnBack != null) btnBack.setOnClickListener(v -> finish());
//        if(tvMarkAllRead != null) tvMarkAllRead.setOnClickListener(v -> markAllAsRead());
//        if(bottomNav != null) bottomNav.setOnItemSelectedListener(this::onNavigationItemSelected);
//    }
//
//    private boolean onNavigationItemSelected(MenuItem item) {
//        int itemId = item.getItemId();
//
//        if (itemId == R.id.nav_home) {
//            finish();
//            return true;
//        } else if (itemId == R.id.nav_notification) {
//            return true;
//        }
//        else if (itemId == R.id.nav_statistics) {
//            Intent intent = new Intent(NotificationActivity.this, StatisticsActivity.class);
//            startActivity(intent);
//            finish(); // Tùy chọn: Đóng activity hiện tại để tránh stack quá nhiều
//            return true;
//        } else if (itemId == R.id.nav_profile) {
//            Intent intent = new Intent(NotificationActivity.this, ProfileActivity.class);
//            startActivity(intent);
//            finish(); // Tùy chọn
//            return true;
//        }
//        return false;
//    }
//
//    private void loadNotifications() {
//        notificationList.clear();
//
//        Cursor cursor = dbHelper.getAllNotifications(userId);
//
//        if (cursor != null && cursor.moveToFirst()) {
//            do {
//                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTI_ID));
//                String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTI_TITLE));
//                String message = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTI_MESSAGE));
//                String type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTI_TYPE));
//                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTI_DATE));
//                int isRead = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTI_IS_READ));
//                int relatedId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTI_RELATED_ID));
//
//                Notification notification = new Notification(id, userId, title, message,
//                        type, date, isRead == 1, relatedId);
//                notificationList.add(notification);
//
//            } while (cursor.moveToNext());
//
//            cursor.close();
//        }
//
//        // ĐÃ FIX LỖI Ở ĐÂY
//        if (notificationList.isEmpty()) {
//            if(layoutEmpty != null) layoutEmpty.setVisibility(View.VISIBLE);
//            if(rvNotifications != null) rvNotifications.setVisibility(View.GONE);
//        } else {
//            if(layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);
//            if(rvNotifications != null) rvNotifications.setVisibility(View.VISIBLE);
//        }
//
//        notificationAdapter.notifyDataSetChanged();
//    }
//
//    private void onNotificationClick(Notification notification) {
//        if (!notification.isRead()) {
//            dbHelper.markNotificationAsRead(notification.getNotificationId());
//            loadNotifications();
//        }
//    }
//
//    private void markAllAsRead() {
//        dbHelper.markAllNotificationsAsRead(userId);
//        loadNotifications();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (dbHelper != null) {
//            dbHelper.close();
//        }
//    }
//}
package com.example.ewallet_thang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ewallet_thang.adapters.NotificationAdapter;
import com.example.ewallet_thang.models.Notification;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvMarkAllRead;
    private RecyclerView rvNotifications;
    private LinearLayout layoutEmpty;
    private BottomNavigationView bottomNav;

    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;
    private String userPhone;

    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;
    private Map<Integer, String> notificationIdMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("EWalletPrefs", MODE_PRIVATE);
        userPhone = sharedPreferences.getString("userPhone", "");

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
        notificationIdMap = new HashMap<>();

        notificationAdapter = new NotificationAdapter(this, notificationList, this::onNotificationClick);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(notificationAdapter);

        if(bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_notification);
    }

    private void setupListeners() {
        if(btnBack != null) btnBack.setOnClickListener(v -> finish());
        if(tvMarkAllRead != null) tvMarkAllRead.setOnClickListener(v -> markAllAsRead());
        if(bottomNav != null) bottomNav.setOnItemSelectedListener(this::onNavigationItemSelected);
    }

    private boolean onNavigationItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.nav_home) {
            finish();
            return true;
        } else if (itemId == R.id.nav_notification) {
            return true;
        } else if (itemId == R.id.nav_statistics) {
            startActivity(new Intent(NotificationActivity.this, StatisticsActivity.class));
            finish();
            return true;
        } else if (itemId == R.id.nav_profile) {
            startActivity(new Intent(NotificationActivity.this, ProfileActivity.class));
            finish();
            return true;
        }
        return false;
    }

    private void loadNotifications() {
        db.collection("notifications").whereEqualTo("userPhone", userPhone).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    notificationList.clear();
                    notificationIdMap.clear();

                    List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                    docs.sort((d1, d2) -> {
                        String date1 = d1.getString("date");
                        String date2 = d2.getString("date");
                        if (date1 == null) return 1;
                        if (date2 == null) return -1;
                        return date2.compareTo(date1);
                    });

                    for (DocumentSnapshot doc : docs) {
                        String title = doc.getString("title");
                        String message = doc.getString("message");
                        String type = doc.getString("type");
                        String date = doc.getString("date");
                        Boolean isReadObj = doc.getBoolean("isRead");
                        boolean isRead = isReadObj != null ? isReadObj : false;

                        int dummyId = doc.getId().hashCode();
                        notificationIdMap.put(dummyId, doc.getId());

                        Notification notification = new Notification(dummyId, 0, title, message, type, date, isRead, 0);
                        notificationList.add(notification);
                    }

                    if (notificationList.isEmpty()) {
                        if (layoutEmpty != null) layoutEmpty.setVisibility(View.VISIBLE);
                        if (rvNotifications != null) rvNotifications.setVisibility(View.GONE);
                    } else {
                        if (layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);
                        if (rvNotifications != null) rvNotifications.setVisibility(View.VISIBLE);
                    }
                    notificationAdapter.notifyDataSetChanged();
                });
    }

    private void onNotificationClick(Notification notification) {
        if (!notification.isRead()) {
            String docId = notificationIdMap.get(notification.getNotificationId());
            if (docId != null) {
                db.collection("notifications").document(docId).update("isRead", true)
                        .addOnSuccessListener(aVoid -> loadNotifications());
            }
        }
    }

    private void markAllAsRead() {
        db.collection("notifications").whereEqualTo("userPhone", userPhone).whereEqualTo("isRead", false)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.update(doc.getReference(), "isRead", true);
                    }
                    batch.commit().addOnSuccessListener(aVoid -> loadNotifications());
                });
    }
}