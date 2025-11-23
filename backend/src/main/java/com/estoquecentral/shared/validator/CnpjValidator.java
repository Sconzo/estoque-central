package com.estoquecentral.shared.validator;

/**
 * CNPJ (Cadastro Nacional da Pessoa Jurídica) validator for Brazilian company tax IDs.
 *
 * Validates CNPJ format and check digits according to Brazilian government rules.
 *
 * Story 4.1: Customer Management - AC4 (CNPJ validation)
 */
public class CnpjValidator {

    private static final int CNPJ_LENGTH = 14;

    /**
     * Validates a CNPJ string.
     *
     * @param cnpj the CNPJ to validate (with or without formatting)
     * @return true if CNPJ is valid, false otherwise
     */
    public static boolean isValid(String cnpj) {
        if (cnpj == null || cnpj.isEmpty()) {
            return false;
        }

        // Remove formatting characters
        cnpj = cnpj.replaceAll("[^0-9]", "");

        // Check length
        if (cnpj.length() != CNPJ_LENGTH) {
            return false;
        }

        // Check for known invalid CNPJs (all digits the same)
        if (cnpj.matches("(\\d)\\1{13}")) {
            return false;
        }

        // Validate first check digit
        int[] multiplicadores1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int soma = 0;
        for (int i = 0; i < 12; i++) {
            soma += Character.getNumericValue(cnpj.charAt(i)) * multiplicadores1[i];
        }
        int digito1 = (soma % 11 < 2) ? 0 : 11 - (soma % 11);

        if (Character.getNumericValue(cnpj.charAt(12)) != digito1) {
            return false;
        }

        // Validate second check digit
        int[] multiplicadores2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        soma = 0;
        for (int i = 0; i < 13; i++) {
            soma += Character.getNumericValue(cnpj.charAt(i)) * multiplicadores2[i];
        }
        int digito2 = (soma % 11 < 2) ? 0 : 11 - (soma % 11);

        return Character.getNumericValue(cnpj.charAt(13)) == digito2;
    }

    /**
     * Formats a CNPJ string to the standard format: 00.000.000/0000-00
     *
     * @param cnpj the CNPJ to format (digits only)
     * @return formatted CNPJ string
     */
    public static String format(String cnpj) {
        if (cnpj == null || cnpj.isEmpty()) {
            return cnpj;
        }

        cnpj = cnpj.replaceAll("[^0-9]", "");

        if (cnpj.length() != CNPJ_LENGTH) {
            return cnpj;
        }

        return String.format("%s.%s.%s/%s-%s",
            cnpj.substring(0, 2),
            cnpj.substring(2, 5),
            cnpj.substring(5, 8),
            cnpj.substring(8, 12),
            cnpj.substring(12, 14)
        );
    }

    /**
     * Removes formatting from a CNPJ string (returns digits only).
     *
     * @param cnpj the CNPJ to clean
     * @return CNPJ with digits only
     */
    public static String cleanFormat(String cnpj) {
        if (cnpj == null) {
            return null;
        }
        return cnpj.replaceAll("[^0-9]", "");
    }
}
