package com.dana.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Hash de senha com PBKDF2 + "salt" aleatório (já vem no JDK, sem biblioteca extra).
 * Formato guardado: iteracoes:salt:hash (salt e hash em Base64). A senha pura nunca é guardada.
 */
public final class SenhaUtil {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACOES = 65_536;
    private static final int TAMANHO_SALT = 16;
    private static final int TAMANHO_CHAVE = 256;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private SenhaUtil() {
    }

    public static String gerarHash(String senha) {
        byte[] salt = new byte[TAMANHO_SALT];
        ALEATORIO.nextBytes(salt);
        byte[] hash = derivar(senha, salt, ITERACOES);
        Base64.Encoder b64 = Base64.getEncoder();
        return ITERACOES + ":" + b64.encodeToString(salt) + ":" + b64.encodeToString(hash);
    }

    public static boolean verificar(String senha, String armazenado) {
        String[] partes = armazenado.split(":");
        if (partes.length != 3) {
            return false;
        }
        Base64.Decoder b64 = Base64.getDecoder();
        byte[] salt = b64.decode(partes[1]);
        byte[] esperado = b64.decode(partes[2]);
        byte[] calculado = derivar(senha, salt, Integer.parseInt(partes[0]));
        return MessageDigest.isEqual(esperado, calculado); // comparação em tempo constante
    }

    private static byte[] derivar(String senha, byte[] salt, int iteracoes) {
        PBEKeySpec especificacao = new PBEKeySpec(senha.toCharArray(), salt, iteracoes, TAMANHO_CHAVE);
        try {
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(especificacao).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(ALGORITMO + " não disponível", e);
        } finally {
            especificacao.clearPassword();
        }
    }
}
