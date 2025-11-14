package com.example.ewallet_thang.models;

public class Notification {
    private int notificationId;
    private int userId;
    private String title;
    private String message;
    private String type; // TRANSACTION, SYSTEM, PROMOTION
    private String date;
    private boolean isRead;
    private int relatedId; // ID của transaction liên quan (nếu có)

    public Notification() {}

    public Notification(int notificationId, int userId, String title,
                        String message, String type, String date,
                        boolean isRead, int relatedId) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.date = date;
        this.isRead = isRead;
        this.relatedId = relatedId;
    }

    // Getters and Setters
    public int getNotificationId() { return notificationId; }
    public void setNotificationId(int notificationId) { this.notificationId = notificationId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public int getRelatedId() { return relatedId; }
    public void setRelatedId(int relatedId) { this.relatedId = relatedId; }
}