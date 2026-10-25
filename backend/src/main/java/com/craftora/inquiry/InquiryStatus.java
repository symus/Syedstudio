package com.craftora.inquiry;

public enum InquiryStatus {
    NEW,
    IN_PROGRESS,
    QUOTED,
    ACCEPTED,
    COMPLETED,
    DECLINED;

    public boolean isOpen() {
        return this != COMPLETED && this != DECLINED;
    }
}
