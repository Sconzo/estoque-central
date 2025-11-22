package com.estoquecentral.purchasing.application.validation;

/**
 * CpfValidator - Validates Brazilian CPF (Cadastro de Pessoas Físicas)
 * Story 3.1: Supplier Management
 *
 * CPF format: 000.000.000-00 (11 digits)
 */
public class CpfValidator {

    /**
     * Validates CPF with check digits
     */
    public static boolean isValid(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return false;
        }

        // Remove formatação (pontos, hífens)
        String cpfClean = cpf.replaceAll("[^0-9]", "");

        // CPF deve ter exatamente 11 dígitos
        if (cpfClean.length() != 11) {
            return false;
        }

        // Verifica CPFs conhecidos como inválidos (todos os dígitos iguais)
        if (cpfClean.matches("(\\d)\\1{10}")) {
            return false;
        }

        try {
            // Calcula primeiro dígito verificador
            int soma = 0;
            for (int i = 0; i < 9; i++) {
                soma += Character.getNumericValue(cpfClean.charAt(i)) * (10 - i);
            }
            int digito1 = 11 - (soma % 11);
            digito1 = (digito1 >= 10) ? 0 : digito1;

            if (Character.getNumericValue(cpfClean.charAt(9)) != digito1) {
                return false;
            }

            // Calcula segundo dígito verificador
            soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += Character.getNumericValue(cpfClean.charAt(i)) * (11 - i);
            }
            int digito2 = 11 - (soma % 11);
            digito2 = (digito2 >= 10) ? 0 : digito2;

            return Character.getNumericValue(cpfClean.charAt(10)) == digito2;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Formats CPF to standard format: 000.000.000-00
     */
    public static String format(String cpf) {
        if (cpf == null) {
            return null;
        }

        String cpfClean = cpf.replaceAll("[^0-9]", "");

        if (cpfClean.length() != 11) {
            return cpf; // Return original if invalid length
        }

        return String.format("%s.%s.%s-%s",
                cpfClean.substring(0, 3),
                cpfClean.substring(3, 6),
                cpfClean.substring(6, 9),
                cpfClean.substring(9, 11));
    }

    /**
     * Removes formatting from CPF (returns only digits)
     */
    public static String cleanFormat(String cpf) {
        if (cpf == null) {
            return null;
        }
        return cpf.replaceAll("[^0-9]", "");
    }
}
