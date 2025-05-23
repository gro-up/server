package com.hamster.gro_up.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class CookieUtil {
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh";

    public static String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public static Cookie createExpiredCookie(String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        return cookie;
    }

    public static Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 운영환경에서만 true
        return cookie;
    }

    public static Cookie createRefreshTokenCookie(String refreshToken){
        return CookieUtil.createCookie(CookieUtil.REFRESH_TOKEN_COOKIE_NAME, refreshToken, 60 * 60 * 24 * 14); // 2주
    }
}
