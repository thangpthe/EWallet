package com.example.ewallet_thang.models;

public class Transaction {

        private int transactionId;
        private int userId;
        private String transactionType;
        private double amount;
        private String description;
        private String category;
        private String transactionDate;

        public Transaction() {}

        public Transaction(int transactionId, int userId, String transactionType,
                           double amount, String description, String category,
                           String transactionDate) {
            this.transactionId = transactionId;
            this.userId = userId;
            this.transactionType = transactionType;
            this.amount = amount;
            this.description = description;
            this.category = category;
            this.transactionDate = transactionDate;
        }

        // Getters and Setters
        public int getTransactionId() { return transactionId; }
        public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }

        public String getTransactionType() { return transactionType; }
        public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getTransactionDate() { return transactionDate; }
        public void setTransactionDate(String transactionDate) { this.transactionDate = transactionDate; }

        public boolean isIncome() {
            return transactionType.equals("INCOME") || transactionType.equals("DEPOSIT");
        }

        public boolean isExpense() {
            return transactionType.equals("EXPENSE") || transactionType.equals("WITHDRAW");
        }
    }

    class TransactionType {
        public static final String INCOME = "INCOME";
        public static final String EXPENSE = "EXPENSE";
        public static final String DEPOSIT = "DEPOSIT";
        public static final String WITHDRAW = "WITHDRAW";
        public static final String TRANSFER = "TRANSFER";
    }

    class Category {
        public static final String FOOD = "Ăn uống";
        public static final String TRANSPORT = "Di chuyển";
        public static final String SHOPPING = "Mua sắm";
        public static final String ENTERTAINMENT = "Giải trí";
        public static final String BILLS = "Hóa đơn";
        public static final String HEALTH = "Sức khỏe";
        public static final String EDUCATION = "Giáo dục";
        public static final String SALARY = "Lương";
        public static final String BUSINESS = "Kinh doanh";
        public static final String OTHER = "Khác";
    }

