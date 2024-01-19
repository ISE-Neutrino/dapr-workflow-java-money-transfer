package com.example.accountservice.enums;

public enum ApprovalResult {
    APPROVED("APPROVED"),
    REJECTED("REJECTED");
    
    private final String stringValue;

    private ApprovalResult(String stringValue) {
        this.stringValue = stringValue;
    }

    public String toString() {
        return stringValue;
    }
}
