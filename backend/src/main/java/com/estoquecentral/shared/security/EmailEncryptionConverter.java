package com.estoquecentral.shared.security;

import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

/**
 * JPA Attribute Converter for Email encryption/decryption.
 * Automatically encrypts email values when persisting to database
 * and decrypts when reading from database.
 *
 * Story 4.1: Customer Management - NFR14 Compliance
 */
@Component
@WritingConverter
@ReadingConverter
public class EmailEncryptionConverter {

    private final CryptoService cryptoService;

    public EmailEncryptionConverter(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    public String convertToDatabaseColumn(String email) {
        try {
            return cryptoService.encrypt(email);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting email", e);
        }
    }

    public String convertToEntityAttribute(String encryptedEmail) {
        try {
            return cryptoService.decrypt(encryptedEmail);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting email", e);
        }
    }
}
