package com.estoquecentral.shared.security;

import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

/**
 * JPA Attribute Converter for CNPJ encryption/decryption.
 * Automatically encrypts CNPJ values when persisting to database
 * and decrypts when reading from database.
 *
 * Story 4.1: Customer Management - NFR14 Compliance
 */
@Component
@WritingConverter
@ReadingConverter
public class CnpjEncryptionConverter {

    private final CryptoService cryptoService;

    public CnpjEncryptionConverter(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    public String convertToDatabaseColumn(String cnpj) {
        try {
            return cryptoService.encrypt(cnpj);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting CNPJ", e);
        }
    }

    public String convertToEntityAttribute(String encryptedCnpj) {
        try {
            return cryptoService.decrypt(encryptedCnpj);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting CNPJ", e);
        }
    }
}
