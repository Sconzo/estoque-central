package com.estoquecentral.marketplace.domain;

import com.estoquecentral.shared.security.CryptoService;
import org.springframework.stereotype.Component;

/**
 * Converter for encrypting/decrypting strings in database using AES-256-GCM
 * Story 5.1: Mercado Livre OAuth2 Authentication - AC5 (Token Encryption)
 *
 * Note: Spring Data JDBC doesn't support @Convert annotation like JPA.
 * This converter can be used manually in the service layer.
 */
@Component
public class EncryptedStringConverter {

    private final CryptoService cryptoService;

    public EncryptedStringConverter(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    public String encrypt(String plainText) {
        return cryptoService.encrypt(plainText);
    }

    public String decrypt(String encryptedText) {
        return cryptoService.decrypt(encryptedText);
    }
}
