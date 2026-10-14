package com.finsight.common.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Enterprise utility for Authenticated Encryption with Associated Data (AEAD)
 * utilizing the ChaCha20-Poly1305 algorithm (RFC 8439).
 * <p>
 * ChaCha20-Poly1305 provides high performance, constant-time software encryption
 * and tamper-evident authentication tags.
 */
public final class ChaCha20Poly1305Util {

    private static final String ALGORITHM = "ChaCha20-Poly1305/None/NoPadding";
    private static final String KEY_ALGORITHM = "ChaCha20";
    private static final int NONCE_LENGTH_BYTES = 12; // 96-bit nonce
    private static final int KEY_LENGTH_BYTES = 32;   // 256-bit key

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private ChaCha20Poly1305Util() {
        // Utility class
    }

    /**
     * Generates a random 256-bit SecretKey for ChaCha20-Poly1305.
     */
    public static SecretKey generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(KEY_ALGORITHM);
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            byte[] keyBytes = new byte[KEY_LENGTH_BYTES];
            SECURE_RANDOM.nextBytes(keyBytes);
            return new SecretKeySpec(keyBytes, KEY_ALGORITHM);
        }
    }

    /**
     * Converts a 32-byte (256-bit) raw key or Base64-encoded key into a SecretKey.
     */
    public static SecretKey keyFromBase64(String base64Key) {
        byte[] decoded = Base64.getDecoder().decode(base64Key);
        if (decoded.length != KEY_LENGTH_BYTES) {
            throw new IllegalArgumentException("ChaCha20 key must be exactly 32 bytes (256 bits)");
        }
        return new SecretKeySpec(decoded, KEY_ALGORITHM);
    }

    /**
     * Encrypts plaintext string using ChaCha20-Poly1305 with a random 96-bit nonce.
     *
     * @param plaintext String to encrypt
     * @param key       256-bit SecretKey
     * @return Base64-encoded string containing [12-byte Nonce + Ciphertext + 16-byte Poly1305 Tag]
     */
    public static String encrypt(String plaintext, SecretKey key) {
        return encrypt(plaintext, key, null);
    }

    /**
     * Encrypts plaintext string with Associated Authenticated Data (AAD).
     */
    public static String encrypt(String plaintext, SecretKey key, byte[] associatedData) {
        if (plaintext == null) {
            return null;
        }

        try {
            byte[] nonce = new byte[NONCE_LENGTH_BYTES];
            SECURE_RANDOM.nextBytes(nonce);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(nonce);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

            if (associatedData != null && associatedData.length > 0) {
                cipher.updateAAD(associatedData);
            }

            byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
            byte[] ciphertextWithTag = cipher.doFinal(plaintextBytes);

            ByteBuffer byteBuffer = ByteBuffer.allocate(NONCE_LENGTH_BYTES + ciphertextWithTag.length);
            byteBuffer.put(nonce);
            byteBuffer.put(ciphertextWithTag);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encrypt using ChaCha20-Poly1305: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypts Base64-encoded ciphertext using ChaCha20-Poly1305.
     *
     * @param encryptedBase64 Base64 string containing [Nonce + Ciphertext + Tag]
     * @param key             256-bit SecretKey
     * @return Original decrypted plaintext String
     */
    public static String decrypt(String encryptedBase64, SecretKey key) {
        return decrypt(encryptedBase64, key, null);
    }

    /**
     * Decrypts Base64-encoded ciphertext with Associated Authenticated Data (AAD).
     */
    public static String decrypt(String encryptedBase64, SecretKey key, byte[] associatedData) {
        if (encryptedBase64 == null) {
            return null;
        }

        try {
            byte[] combined = Base64.getDecoder().decode(encryptedBase64);
            if (combined.length < NONCE_LENGTH_BYTES) {
                throw new IllegalArgumentException("Invalid ciphertext payload: insufficient length for nonce");
            }

            ByteBuffer byteBuffer = ByteBuffer.wrap(combined);
            byte[] nonce = new byte[NONCE_LENGTH_BYTES];
            byteBuffer.get(nonce);

            byte[] ciphertextWithTag = new byte[byteBuffer.remaining()];
            byteBuffer.get(ciphertextWithTag);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(nonce);
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);

            if (associatedData != null && associatedData.length > 0) {
                cipher.updateAAD(associatedData);
            }

            byte[] decrypted = cipher.doFinal(ciphertextWithTag);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decrypt ChaCha20-Poly1305 ciphertext: " + e.getMessage(), e);
        }
    }
}
