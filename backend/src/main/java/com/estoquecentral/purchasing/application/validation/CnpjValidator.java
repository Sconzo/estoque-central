package com.estoquecentral.purchasing.application.validation;

/**
 * CnpjValidator - Validates Brazilian CNPJ (Cadastro Nacional da Pessoa Jurídica)
 * Story 3.1: Supplier Management
 *
 * CNPJ format: 00.000.000/0000-00 (14 digits)
 */
public class CnpjValidator {

    /**
     * Validates CNPJ with check digits
     */
    public static boolean isValid(String cnpj) {
        if (cnpj == null || cnpj.trim().isEmpty()) {
            return false;
        }

        // Remove formatação (pontos, barras, hífens)
        String cnpjClean = cnpj.replaceAll("[^0-9]", "");

        // CNPJ deve ter exatamente 14 dígitos
        if (cnpjClean.length() != 14) {
            return false;
        }

        // Verifica CNPJs conhecidos como inválidos (todos os dígitos iguais)
        if (cnpjClean.matches("(\\d)\\1{13}")) {
            return false;
        }

        try {
            // Calcula primeiro dígito verificador
            int[] multiplicadores1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            int soma = 0;
            for (int i = 0; i < 12; i++) {
                soma += Character.getNumericValue(cnpjClean.charAt(i)) * multiplicadores1[i];
            }
            int digito1 = (soma % 11 < 2) ? 0 : 11 - (soma % 11);

            if (Character.getNumericValue(cnpjClean.charAt(12)) != digito1) {
                return false;
            }

            // Calcula segundo dígito verificador
            int[] multiplicadores2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            soma = 0;
            for (int i = 0; i < 13; i++) {
                soma += Character.getNumericValue(cnpjClean.charAt(i)) * multiplicadores2[i];
            }
            int digito2 = (soma % 11 < 2) ? 0 : 11 - (soma % 11);

            return Character.getNumericValue(cnpjClean.charAt(13)) == digito2;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Formats CNPJ to standard format: 00.000.000/0000-00
     */
    public static String format(String cnpj) {
        if (cnpj == null) {
            return null;
        }

        String cnpjClean = cnpj.replaceAll("[^0-9]", "");

        if (cnpjClean.length() != 14) {
            return cnpj; // Return original if invalid length
        }

        return String.format("%s.%s.%s/%s-%s",
                cnpjClean.substring(0, 2),
                cnpjClean.substring(2, 5),
                cnpjClean.substring(5, 8),
                cnpjClean.substring(8, 12),
                cnpjClean.substring(12, 14));
    }

    /**
     * Removes formatting from CNPJ (returns only digits)
     */
    public static String cleanFormat(String cnpj) {
        if (cnpj == null) {
            return null;
        }
        return cnpj.replaceAll("[^0-9]", "");
    }
}
