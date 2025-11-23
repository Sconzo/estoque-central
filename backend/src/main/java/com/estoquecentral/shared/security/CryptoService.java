package com.estoquecentral.shared.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service for AES-256-GCM encryption and decryption of sensitive data.
 * Used for encrypting CPF, CNPJ, and email fields in the database.
 *
 * Story 4.1: Customer Management - NFR14 Compliance (AES-256 Encryption)
 */
@Component
public class CryptoService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;

    @Value("${encryption.key:}")
    private String encryptionKey;

    /**
     * Encrypts plain text using AES-256-GCM.
     *
     * @param plainText the text to encrypt
     * @return encrypted text as Base64 string (IV + ciphertext), or null if input is null
     * @throws RuntimeException if encryption fails
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return null;
        }

        try {
            // Validate encryption key
            if (encryptionKey == null || encryptionKey.isEmpty()) {
                throw new IllegalStateException("Encryption key not configured. Set 'encryption.key' property.");
            }

            SecretKeySpec keySpec = new SecretKeySpec(
                Base64.getDecoder().decode(encryptionKey),
                "AES"
            );

            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom.getInstanceStrong().nextBytes(iv);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);

            // Encrypt
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Concatenate IV + cipherText
            byte[] encrypted = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, encrypted, 0, iv.length);
            System.arraycopy(cipherText, 0, encrypted, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }

    /**
     * Decrypts encrypted text using AES-256-GCM.
     *
     * @param encryptedText the Base64 encoded encrypted text (IV + ciphertext)
     * @return decrypted plain text, or null if input is null
     * @throws RuntimeException if decryption fails
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return null;
        }

        try {
            // Validate encryption key
            if (encryptionKey == null || encryptionKey.isEmpty()) {
                throw new IllegalStateException("Encryption key not configured. Set 'encryption.key' property.");
            }

            byte[] encrypted = Base64.getDecoder().decode(encryptedText);

            SecretKeySpec keySpec = new SecretKeySpec(
                Base64.getDecoder().decode(encryptionKey),
                "AES"
            );

            // Extract IV and cipherText
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(encrypted, 0, iv, 0, iv.length);
            byte[] cipherText = new byte[encrypted.length - iv.length];
            System.arraycopy(encrypted, iv.length, cipherText, 0, cipherText.length);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);

            // Decrypt
            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt data", e);
        }
    }
}
