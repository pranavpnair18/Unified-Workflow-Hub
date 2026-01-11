package com.workflowhub.integrations_backend.security;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

/**
 * Simple AES-GCM encryption service for demonstration.
 *
 * IMPORTANT:
 * - In production use a KMS (AWS KMS / GCP KMS / HashiVault) and rotate keys.
 * - The env var INTEGRATIONS_ENC_KEY must be a base64-encoded 32-byte key (256-bit).
 * - This implementation prefixes the random IV to the ciphertext (IV|CIPHERTEXT) encoded in Base64.
 */
@Service
public class EncryptionService {

    private static final String ALGO = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // bits
    private static final int IV_LENGTH = 12; // bytes (recommended for GCM)
    private final SecretKeySpec keySpec;
    private final SecureRandom secureRandom = new SecureRandom();

    public EncryptionService() {
        String b64 = System.getenv("INTEGRATIONS_ENC_KEY");
        if (b64 == null || b64.isBlank()) {
            throw new IllegalStateException("INTEGRATIONS_ENC_KEY env var not set. Please set a base64-encoded 32-byte key.");
        }
        byte[] keyBytes = Base64.getDecoder().decode(b64);
        if (keyBytes.length != 32) {
            throw new IllegalStateException("INTEGRATIONS_ENC_KEY must decode to 32 bytes (256-bit).");
        }
        this.keySpec = new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String plaintext) {
        if (plaintext == null) return null;
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGO);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);
            byte[] cipherBytes = cipher.doFinal(plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // Store as: base64( iv || cipherBytes )
            byte[] out = new byte[iv.length + cipherBytes.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(cipherBytes, 0, out, iv.length, cipherBytes.length);
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception ex) {
            throw new RuntimeException("Encryption failed", ex);
        }
    }

    public String decrypt(String b64Cipher) {
        if (b64Cipher == null) return null;
        try {
            byte[] all = Base64.getDecoder().decode(b64Cipher);
            if (all.length < IV_LENGTH) throw new IllegalArgumentException("Ciphertext too short");
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(all, 0, iv, 0, IV_LENGTH);
            byte[] cipherBytes = new byte[all.length - IV_LENGTH];
            System.arraycopy(all, IV_LENGTH, cipherBytes, 0, cipherBytes.length);

            Cipher cipher = Cipher.getInstance(ALGO);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);
            byte[] plain = cipher.doFinal(cipherBytes);
            return new String(plain, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new RuntimeException("Decryption failed", ex);
        }
    }
}
