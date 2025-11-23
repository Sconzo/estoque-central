package com.estoquecentral.shared.validator;

/**
 * CPF (Cadastro de Pessoas Físicas) validator for Brazilian tax IDs.
 *
 * Validates CPF format and check digits according to Brazilian government rules.
 *
 * Story 4.1: Customer Management - AC4 (CPF validation)
 */
public class CpfValidator {

    private static final int CPF_LENGTH = 11;

    /**
     * Validates a CPF string.
     *
     * @param cpf the CPF to validate (with or without formatting)
     * @return true if CPF is valid, false otherwise
     */
    public static boolean isValid(String cpf) {
        if (cpf == null || cpf.isEmpty()) {
            return false;
        }

        // Remove formatting characters
        cpf = cpf.replaceAll("[^0-9]", "");

        // Check length
        if (cpf.length() != CPF_LENGTH) {
            return false;
        }

        // Check for known invalid CPFs (all digits the same)
        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        // Validate first check digit
        int[] multiplicadores1 = {10, 9, 8, 7, 6, 5, 4, 3, 2};
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * multiplicadores1[i];
        }
        int digito1 = (soma % 11 < 2) ? 0 : 11 - (soma % 11);

        if (Character.getNumericValue(cpf.charAt(9)) != digito1) {
            return false;
        }

        // Validate second check digit
        int[] multiplicadores2 = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * multiplicadores2[i];
        }
        int digito2 = (soma % 11 < 2) ? 0 : 11 - (soma % 11);

        return Character.getNumericValue(cpf.charAt(10)) == digito2;
    }

    /**
     * Formats a CPF string to the standard format: 000.000.000-00
     *
     * @param cpf the CPF to format (digits only)
     * @return formatted CPF string
     */
    public static String format(String cpf) {
        if (cpf == null || cpf.isEmpty()) {
            return cpf;
        }

        cpf = cpf.replaceAll("[^0-9]", "");

        if (cpf.length() != CPF_LENGTH) {
            return cpf;
        }

        return String.format("%s.%s.%s-%s",
            cpf.substring(0, 3),
            cpf.substring(3, 6),
            cpf.substring(6, 9),
            cpf.substring(9, 11)
        );
    }

    /**
     * Removes formatting from a CPF string (returns digits only).
     *
     * @param cpf the CPF to clean
     * @return CPF with digits only
     */
    public static String cleanFormat(String cpf) {
        if (cpf == null) {
            return null;
        }
        return cpf.replaceAll("[^0-9]", "");
    }
}
