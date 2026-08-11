package com.cibertec.galcom.security;

import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;

public class Constants {
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String SECRET_TEXT = "GALCOM2026CibertecGALCOM2026Cibertec";
    public static final long EXPIRATION_TIME = 3_600_000;

    public static Key getSigningKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
