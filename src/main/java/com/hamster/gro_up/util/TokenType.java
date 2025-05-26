package com.hamster.gro_up.util;

public enum TokenType {
    ACCESS(14 * 24 * 60 * 60 * 1000L),  // 2주
    REFRESH(2 * 14 * 24 * 60 * 60 * 1000L); // 4주

    private final long expireMs;

    TokenType(long expireMs) {
        this.expireMs = expireMs;
    }

    public long getExpireMs() {
        return expireMs;
    }
}
