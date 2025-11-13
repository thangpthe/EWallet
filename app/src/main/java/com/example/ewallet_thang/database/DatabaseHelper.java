package com.example.ewallet_thang.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "EWallet.db";
    private static final int DATABASE_VERSION = 1;

    // Table Users
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "user_id";
    public static final String COL_FIRST_NAME = "first_name";
    public static final String COL_LAST_NAME = "last_name";
    public static final String COL_PHONE = "phone";
    public static final String COL_BIRTH_DATE = "birth_date";
    public static final String COL_PASSWORD = "password";
    public static final String COL_BALANCE = "balance";
    public static final String COL_CREATED_AT = "created_at";

    // Table Transactions
    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String COL_TRANS_ID = "transaction_id";
    public static final String COL_TRANS_USER_ID = "user_id";
    public static final String COL_TRANS_TYPE = "transaction_type";
    public static final String COL_TRANS_AMOUNT = "amount";
    public static final String COL_TRANS_DESCRIPTION = "description";
    public static final String COL_TRANS_DATE = "transaction_date";
    public static final String COL_TRANS_CATEGORY = "category";

    // Table Cards
    public static final String TABLE_CARDS = "cards";
    public static final String COL_CARD_ID = "card_id";
    public static final String COL_CARD_USER_ID = "user_id";
    public static final String COL_CARD_NUMBER = "card_number";
    public static final String COL_CARD_HOLDER = "card_holder";
    public static final String COL_CARD_TYPE = "card_type";
    public static final String COL_CARD_EXPIRY = "expiry_date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_FIRST_NAME + " TEXT NOT NULL, " +
                COL_LAST_NAME + " TEXT NOT NULL, " +
                COL_PHONE + " TEXT UNIQUE NOT NULL, " +
                COL_BIRTH_DATE + " TEXT NOT NULL, " +
                COL_PASSWORD + " TEXT NOT NULL, " +
                COL_BALANCE + " REAL DEFAULT 0, " +
                COL_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP)";
        db.execSQL(createUsersTable);

        // Create Transactions table
        String createTransactionsTable = "CREATE TABLE " + TABLE_TRANSACTIONS + " (" +
                COL_TRANS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TRANS_USER_ID + " INTEGER NOT NULL, " +
                COL_TRANS_TYPE + " TEXT NOT NULL, " +
                COL_TRANS_AMOUNT + " REAL NOT NULL, " +
                COL_TRANS_DESCRIPTION + " TEXT, " +
                COL_TRANS_CATEGORY + " TEXT, " +
                COL_TRANS_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(" + COL_TRANS_USER_ID + ") REFERENCES " +
                TABLE_USERS + "(" + COL_USER_ID + "))";
        db.execSQL(createTransactionsTable);

        // Create Cards table
        String createCardsTable = "CREATE TABLE " + TABLE_CARDS + " (" +
                COL_CARD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CARD_USER_ID + " INTEGER NOT NULL, " +
                COL_CARD_NUMBER + " TEXT NOT NULL, " +
                COL_CARD_HOLDER + " TEXT NOT NULL, " +
                COL_CARD_TYPE + " TEXT NOT NULL, " +
                COL_CARD_EXPIRY + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + COL_CARD_USER_ID + ") REFERENCES " +
                TABLE_USERS + "(" + COL_USER_ID + "))";
        db.execSQL(createCardsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CARDS);
        onCreate(db);
    }

    // Hash password using SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password;
        }
    }

    // User Operations
    public long registerUser(String firstName, String lastName, String phone,
                             String birthDate, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FIRST_NAME, firstName);
        values.put(COL_LAST_NAME, lastName);
        values.put(COL_PHONE, phone);
        values.put(COL_BIRTH_DATE, birthDate);
        values.put(COL_PASSWORD, hashPassword(password));
        values.put(COL_BALANCE, 10000000.0);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result;
    }

    public boolean checkUserExists(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                COL_PHONE + "=?", new String[]{phone},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public Cursor loginUser(String phone, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS, null,
                COL_PHONE + "=? AND " + COL_PASSWORD + "=?",
                new String[]{phone, hashPassword(password)},
                null, null, null);
    }

    public Cursor getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS, null,
                COL_USER_ID + "=?", new String[]{String.valueOf(userId)},
                null, null, null);
    }

    public boolean updateBalance(int userId, double newBalance) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_BALANCE, newBalance);

        int result = db.update(TABLE_USERS, values,
                COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
        db.close();
        return result > 0;
    }

    public double getBalance(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_BALANCE},
                COL_USER_ID + "=?", new String[]{String.valueOf(userId)},
                null, null, null);

        double balance = 0.0;
        if (cursor.moveToFirst()) {
            balance = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return balance;
    }

    // Transaction Operations
    public long addTransaction(int userId, String type, double amount,
                               String description, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TRANS_USER_ID, userId);
        values.put(COL_TRANS_TYPE, type);
        values.put(COL_TRANS_AMOUNT, amount);
        values.put(COL_TRANS_DESCRIPTION, description);
        values.put(COL_TRANS_CATEGORY, category);

        long result = db.insert(TABLE_TRANSACTIONS, null, values);

        // Update user balance
        double currentBalance = getBalance(userId);
        if (type.equals("INCOME") || type.equals("DEPOSIT")) {
            updateBalance(userId, currentBalance + amount);
        } else if (type.equals("EXPENSE") || type.equals("WITHDRAW")) {
            updateBalance(userId, currentBalance - amount);
        }

        db.close();
        return result;
    }

    public Cursor getAllTransactions(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_TRANSACTIONS, null,
                COL_TRANS_USER_ID + "=?", new String[]{String.valueOf(userId)},
                null, null, COL_TRANS_DATE + " DESC");
    }

    public Cursor getTransactionsByType(int userId, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_TRANSACTIONS, null,
                COL_TRANS_USER_ID + "=? AND " + COL_TRANS_TYPE + "=?",
                new String[]{String.valueOf(userId), type},
                null, null, COL_TRANS_DATE + " DESC");
    }

    public Cursor getTransactionsByDateRange(int userId, String startDate, String endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_TRANSACTIONS, null,
                COL_TRANS_USER_ID + "=? AND " + COL_TRANS_DATE + " BETWEEN ? AND ?",
                new String[]{String.valueOf(userId), startDate, endDate},
                null, null, COL_TRANS_DATE + " DESC");
    }

    // Card Operations
    public long addCard(int userId, String cardNumber, String cardHolder,
                        String cardType, String expiryDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CARD_USER_ID, userId);
        values.put(COL_CARD_NUMBER, cardNumber);
        values.put(COL_CARD_HOLDER, cardHolder);
        values.put(COL_CARD_TYPE, cardType);
        values.put(COL_CARD_EXPIRY, expiryDate);

        long result = db.insert(TABLE_CARDS, null, values);
        db.close();
        return result;
    }

    public Cursor getAllCards(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_CARDS, null,
                COL_CARD_USER_ID + "=?", new String[]{String.valueOf(userId)},
                null, null, null);
    }

    public boolean deleteCard(int cardId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_CARDS,
                COL_CARD_ID + "=?", new String[]{String.valueOf(cardId)});
        db.close();
        return result > 0;
    }

    // Statistics
    public double getTotalIncome(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + COL_TRANS_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                        " WHERE " + COL_TRANS_USER_ID + "=? AND (" +
                        COL_TRANS_TYPE + "='INCOME' OR " + COL_TRANS_TYPE + "='DEPOSIT')",
                new String[]{String.valueOf(userId)});

        double total = 0.0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return total;
    }

    public double getTotalExpense(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + COL_TRANS_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                        " WHERE " + COL_TRANS_USER_ID + "=? AND (" +
                        COL_TRANS_TYPE + "='EXPENSE' OR " + COL_TRANS_TYPE + "='WITHDRAW')",
                new String[]{String.valueOf(userId)});

        double total = 0.0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return total;
    }

    public Cursor getExpenseByCategory(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT " + COL_TRANS_CATEGORY + ", SUM(" + COL_TRANS_AMOUNT + ") as total " +
                        "FROM " + TABLE_TRANSACTIONS +
                        " WHERE " + COL_TRANS_USER_ID + "=? AND " +
                        COL_TRANS_TYPE + "='EXPENSE' " +
                        "GROUP BY " + COL_TRANS_CATEGORY,
                new String[]{String.valueOf(userId)});
    }
    public Cursor getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " ORDER BY " + COL_FIRST_NAME + " ASC";
        return db.rawQuery(query, null);
    }
}