package com.finsight.common.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChaCha20Poly1305UtilTest {

    @Test
    @DisplayName("Should successfully encrypt and decrypt string using ChaCha20-Poly1305")
    void shouldEncryptAndDecrypt() {
        SecretKey key = ChaCha20Poly1305Util.generateKey();
        String originalText = "Account-No-4920-1928-3847; Balance=$5,250,000.00";

        String encrypted = ChaCha20Poly1305Util.encrypt(originalText, key);
        assertThat(encrypted).isNotNull();
        assertThat(encrypted).isNotEqualTo(originalText);

        String decrypted = ChaCha20Poly1305Util.decrypt(encrypted, key);
        assertThat(decrypted).isEqualTo(originalText);
    }

    @Test
    @DisplayName("Should encrypt and decrypt with Associated Authenticated Data (AAD)")
    void shouldEncryptAndDecryptWithAAD() {
        SecretKey key = ChaCha20Poly1305Util.generateKey();
        String sensitivePayload = "Confidential Financial Audit Note";
        byte[] aad = "tenant-id-4401".getBytes(StandardCharsets.UTF_8);

        String encrypted = ChaCha20Poly1305Util.encrypt(sensitivePayload, key, aad);
        String decrypted = ChaCha20Poly1305Util.decrypt(encrypted, key, aad);

        assertThat(decrypted).isEqualTo(sensitivePayload);

        // Tampering with AAD must fail authentication
        byte[] invalidAad = "tenant-id-9999".getBytes(StandardCharsets.UTF_8);
        assertThatThrownBy(() -> ChaCha20Poly1305Util.decrypt(encrypted, key, invalidAad))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Should import SecretKey from Base64")
    void shouldImportKeyFromBase64() {
        SecretKey key = ChaCha20Poly1305Util.generateKey();
        String base64 = Base64.getEncoder().encodeToString(key.getEncoded());

        SecretKey importedKey = ChaCha20Poly1305Util.keyFromBase64(base64);
        assertThat(importedKey.getEncoded()).isEqualTo(key.getEncoded());
    }
}
