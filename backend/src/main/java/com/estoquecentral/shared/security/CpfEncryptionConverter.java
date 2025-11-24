package com.estoquecentral.shared.security;

import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

/**
 * JPA Attribute Converter for CPF encryption/decryption.
 * Automatically encrypts CPF values when persisting to database
 * and decrypts when reading from database.
 *
 * Story 4.1: Customer Management - NFR14 Compliance
 */
@Component
@WritingConverter
@ReadingConverter
public class CpfEncryptionConverter {

    private final CryptoService cryptoService;

    public CpfEncryptionConverter(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    // Note: For Spring Data JDBC, we need different approach than JPA's @Converter
    // This will be used as a custom converter registered in the configuration

    public String convertToDatabaseColumn(String cpf) {
        try {
            return cryptoService.encrypt(cpf);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting CPF", e);
        }
    }

    public String convertToEntityAttribute(String encryptedCpf) {
        try {
            return cryptoService.decrypt(encryptedCpf);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting CPF", e);
        }
    }
}
