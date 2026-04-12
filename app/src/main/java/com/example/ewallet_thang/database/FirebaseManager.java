package com.example.ewallet_thang.database;

import com.example.ewallet_thang.models.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirebaseManager {
    private FirebaseFirestore db;

    public FirebaseManager() {
        db = FirebaseFirestore.getInstance();
    }

    // Interface (Callback) để trả kết quả về cho Activity
    public interface AuthCallback {
        void onSuccess(User user);
        void onFailure(String error);
    }

    // ==========================================
    // 1. ĐĂNG KÝ (REGISTER)
    // ==========================================
    public void registerUser(String firstName, String lastName, String phone, String birthDate, String password, AuthCallback callback) {
        // B1: Kiểm tra xem số điện thoại (Document) này đã tồn tại chưa
        db.collection("users").document(phone).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    callback.onFailure("Số điện thoại này đã được đăng ký!");
                } else {
                    // B2: Nếu chưa tồn tại -> Tạo user mới
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("firstName", firstName);
                    userMap.put("lastName", lastName);
                    userMap.put("phone", phone);
                    userMap.put("birthDate", birthDate);
                    userMap.put("password", password); // Lưu ý: Thực tế nên mã hóa, ở đây làm đơn giản theo luồng của bạn
                    userMap.put("balance", 0.0); // Tặng 0đ khi mới tạo tài khoản
                    userMap.put("createdAt", System.currentTimeMillis());

                    // Lưu vào Firestore
                    db.collection("users").document(phone).set(userMap)
                            .addOnSuccessListener(aVoid -> {
                                // Trả về đối tượng User khi thành công
                                User newUser = new User(0, firstName, lastName, phone, birthDate, 0.0, "");
                                callback.onSuccess(newUser);
                            })
                            .addOnFailureListener(e -> callback.onFailure("Lỗi tạo tài khoản: " + e.getMessage()));
                }
            } else {
                callback.onFailure("Lỗi kết nối máy chủ!");
            }
        });
    }

    // ==========================================
    // 2. ĐĂNG NHẬP (LOGIN)
    // ==========================================
    public void loginUser(String phone, String password, AuthCallback callback) {
        // Tìm user theo số điện thoại
        db.collection("users").document(phone).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    // Lấy mật khẩu từ Database ra so sánh
                    String dbPassword = document.getString("password");

                    if (password.equals(dbPassword)) {
                        // Đăng nhập thành công, tạo object User để lưu session
                        User user = new User();
                        user.setPhone(phone);
                        user.setFirstName(document.getString("firstName"));
                        user.setLastName(document.getString("lastName"));
                        // Gán thêm các trường khác nếu cần...

                        callback.onSuccess(user);
                    } else {
                        callback.onFailure("Mật khẩu không chính xác!");
                    }
                } else {
                    callback.onFailure("Tài khoản không tồn tại!");
                }
            } else {
                callback.onFailure("Lỗi kết nối máy chủ!");
            }
        });
    }
}