package com.hamster.gro_up.util;

public enum TokenType {
    ACCESS(60 * 60 * 1000L),         // 1시간
    REFRESH(14 * 24 * 60 * 60 * 1000L); // 2주

    private final long expireMs;

    TokenType(long expireMs) {
        this.expireMs = expireMs;
    }

    public long getExpireMs() {
        return expireMs;
    }
}
