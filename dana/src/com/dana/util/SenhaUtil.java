package com.dana.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Gera o hash da senha (SHA-256) para nunca guardar a senha em texto puro.
 * Para um projeto real, o correto seria BCrypt com "salt"; aqui SHA-256 basta.
 */
public final class SenhaUtil {

    private SenhaUtil() {
    }

    public static String hash(String senha) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(senha.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 não disponível", e);
        }
    }
}
