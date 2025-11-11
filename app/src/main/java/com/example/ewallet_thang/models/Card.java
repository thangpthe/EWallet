package com.example.ewallet_thang.models;

public class Card {

        private int cardId;
        private int userId;
        private String cardNumber;
        private String cardHolder;
        private String cardType;
        private String expiryDate;

        public Card() {}

        public Card(int cardId, int userId, String cardNumber, String cardHolder,
                    String cardType, String expiryDate) {
            this.cardId = cardId;
            this.userId = userId;
            this.cardNumber = cardNumber;
            this.cardHolder = cardHolder;
            this.cardType = cardType;
            this.expiryDate = expiryDate;
        }

        // Getters and Setters
        public int getCardId() { return cardId; }
        public void setCardId(int cardId) { this.cardId = cardId; }

        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }

        public String getCardNumber() { return cardNumber; }
        public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

        public String getCardHolder() { return cardHolder; }
        public void setCardHolder(String cardHolder) { this.cardHolder = cardHolder; }

        public String getCardType() { return cardType; }
        public void setCardType(String cardType) { this.cardType = cardType; }

        public String getExpiryDate() { return expiryDate; }
        public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

        public String getMaskedCardNumber() {
            if (cardNumber.length() >= 4) {
                return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
            }
            return cardNumber;
        }
    }

