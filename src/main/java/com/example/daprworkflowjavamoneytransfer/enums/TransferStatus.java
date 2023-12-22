package com.example.daprworkflowjavamoneytransfer.enums;

public enum TransferStatus {
    PENDING("PENDING"),
    ACCEPTED("ACCEPTED"),
    APPROVED("APPROVED"),
    VALIDATED("VALIDATED"),
    COMPLETED("COMPLETED"),
    REJECTED("REJECTED");

    private final String stringValue;

    private TransferStatus(String stringValue) {
        this.stringValue = stringValue;
    }

    public String toString() {
        return stringValue;
    }
}
