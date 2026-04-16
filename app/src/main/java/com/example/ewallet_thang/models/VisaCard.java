//package com.example.ewallet_thang.models;
//
//public class VisaCard {
//    private String cardId;        // Firestore document ID
//    private String userPhone;
//    private String cardNumber;    // Lưu đã mask, chỉ 4 số cuối hiển thị
//    private String cardHolder;
//    private String expiry;        // MM/YY
//    private String last4;         // 4 số cuối
//    private String cardBrand;     // VISA / MASTERCARD
//    private boolean isDefault;
//
//    public VisaCard() {}
//
//    public VisaCard(String cardId, String userPhone, String cardNumber,
//                    String cardHolder, String expiry, String cardBrand) {
//        this.cardId     = cardId;
//        this.userPhone  = userPhone;
//        this.cardNumber = cardNumber;
//        this.cardHolder = cardHolder;
//        this.expiry     = expiry;
//        this.cardBrand  = cardBrand;
//        this.last4      = cardNumber.length() >= 4
//                ? cardNumber.substring(cardNumber.length() - 4) : "****";
//        this.isDefault  = false;
//    }
//
//    // ─── Getters / Setters ───────────────────────────────────────
//    public String getCardId()       { return cardId; }
//    public void   setCardId(String v){ cardId = v; }
//
//    public String getUserPhone()       { return userPhone; }
//    public void   setUserPhone(String v){ userPhone = v; }
//
//    public String getCardNumber()       { return cardNumber; }
//    public void   setCardNumber(String v){
//        cardNumber = v;
//        last4 = v.length() >= 4 ? v.substring(v.length() - 4) : "****";
//    }
//
//    public String getCardHolder()       { return cardHolder; }
//    public void   setCardHolder(String v){ cardHolder = v; }
//
//    public String getExpiry()       { return expiry; }
//    public void   setExpiry(String v){ expiry = v; }
//
//    public String getLast4()       { return last4; }
//    public void   setLast4(String v){ last4 = v; }
//
//    public String getCardBrand()       { return cardBrand; }
//    public void   setCardBrand(String v){ cardBrand = v; }
//
//    public boolean isDefault()         { return isDefault; }
//    public void    setDefault(boolean v){ isDefault = v; }
//
//    /** Trả về chuỗi hiển thị, VD: "**** **** **** 4242" */
//    public String getMaskedNumber() {
//        return "**** **** **** " + last4;
//    }
//}

package com.example.ewallet_thang.models;

/**
 * Model thẻ Visa / Mastercard.
 * CHÚ Ý: cardNumber chỉ lưu 4 số cuối (last4) — CVV không bao giờ lưu.
 */
public class VisaCard {

    private String  cardId;
    private String  userPhone;
    private String  cardNumber;   // chỉ 4 số cuối
    private String  cardHolder;
    private String  expiry;       // MM/YY
    private String  cardBrand;    // "VISA" | "MASTERCARD" | "AMEX"
    private boolean isDefault;

    // ── Constructor ───────────────────────────────────────────────
    public VisaCard(String cardId, String userPhone, String cardNumber,
                    String cardHolder, String expiry, String cardBrand) {
        this.cardId     = cardId;
        this.userPhone  = userPhone;
        this.cardNumber = cardNumber;
        this.cardHolder = cardHolder;
        this.expiry     = expiry;
        this.cardBrand  = cardBrand;
    }

    // ── Getters ───────────────────────────────────────────────────
    public String  getCardId()    { return cardId; }
    public String  getUserPhone() { return userPhone; }
    public String  getCardNumber(){ return cardNumber; }
    public String  getCardHolder(){ return cardHolder; }
    public String  getExpiry()    { return expiry; }
    public String  getCardBrand() { return cardBrand; }
    public boolean isDefault()    { return isDefault; }

    /** 4 số cuối — dùng để hiển thị "**** 1234" */
    public String getLast4() {
        if (cardNumber == null) return "????";
        return cardNumber.length() >= 4
                ? cardNumber.substring(cardNumber.length() - 4)
                : cardNumber;
    }

    /** Hiển thị dạng "**** **** **** 1234" */
    public String getMaskedNumber() {
        return "**** **** **** " + getLast4();
    }

    // ── Setters ───────────────────────────────────────────────────
    public void setDefault(boolean def) { this.isDefault = def; }
}