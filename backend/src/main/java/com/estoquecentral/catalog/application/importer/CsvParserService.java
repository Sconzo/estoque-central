package com.estoquecentral.catalog.application.importer;

import com.estoquecentral.catalog.adapter.in.dto.ProductCsvRow;
import com.estoquecentral.catalog.domain.BomType;
import com.estoquecentral.catalog.domain.ProductStatus;
import com.estoquecentral.catalog.domain.ProductType;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * CsvParserService - Parses and validates product CSV files
 *
 * <p>CSV Format (header row required):
 * type,name,sku,barcode,description,categoryId,price,cost,unit,controlsInventory,status,bomType
 *
 * <p>Example:
 * <pre>
 * SIMPLE,Produto Teste,SKU001,7891234567890,Descrição,uuid-category,99.90,50.00,UN,true,ACTIVE,
 * COMPOSITE,Kit Teste,KIT001,,,uuid-category,199.90,100.00,UN,false,ACTIVE,VIRTUAL
 * </pre>
 */
@Service
public class CsvParserService {

    /**
     * Parses CSV file and validates each row
     *
     * @param file uploaded CSV file
     * @return list of parsed rows with validation results
     * @throws IOException if file cannot be read
     */
    public List<ProductCsvRow> parseAndValidate(MultipartFile file) throws IOException {
        List<ProductCsvRow> rows = new ArrayList<>();

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReader(reader)) {

            List<String[]> allRows = csvReader.readAll();

            if (allRows.isEmpty()) {
                return rows; // Empty file
            }

            // Skip header row (index 0)
            for (int i = 1; i < allRows.size(); i++) {
                String[] fields = allRows.get(i);
                ProductCsvRow row = parseRow(i + 1, fields); // Row number is 1-based
                validateRow(row);
                rows.add(row);
            }

        } catch (CsvException e) {
            throw new IOException("Failed to parse CSV: " + e.getMessage(), e);
        }

        return rows;
    }

    /**
     * Parses a single CSV row into ProductCsvRow
     */
    private ProductCsvRow parseRow(int rowNumber, String[] fields) {
        ProductCsvRow row = new ProductCsvRow(rowNumber);

        // Safe get with bounds checking
        row.setType(getField(fields, 0));
        row.setName(getField(fields, 1));
        row.setSku(getField(fields, 2));
        row.setBarcode(getField(fields, 3));
        row.setDescription(getField(fields, 4));
        row.setCategoryId(getField(fields, 5));

        // Parse numeric fields
        String priceStr = getField(fields, 6);
        if (priceStr != null && !priceStr.isEmpty()) {
            try {
                row.setPrice(new BigDecimal(priceStr));
            } catch (NumberFormatException e) {
                row.addError("Preço inválido: " + priceStr);
            }
        }

        String costStr = getField(fields, 7);
        if (costStr != null && !costStr.isEmpty()) {
            try {
                row.setCost(new BigDecimal(costStr));
            } catch (NumberFormatException e) {
                row.addError("Custo inválido: " + costStr);
            }
        }

        row.setUnit(getField(fields, 8));

        // Parse boolean
        String controlsInvStr = getField(fields, 9);
        if (controlsInvStr != null && !controlsInvStr.isEmpty()) {
            row.setControlsInventory(Boolean.parseBoolean(controlsInvStr));
        }

        row.setStatus(getField(fields, 10));
        row.setBomType(getField(fields, 11));

        return row;
    }

    /**
     * Validates a ProductCsvRow
     */
    private void validateRow(ProductCsvRow row) {
        // Required fields
        if (isEmpty(row.getType())) {
            row.addError("Tipo é obrigatório");
        } else {
            // Validate type enum
            try {
                ProductType.valueOf(row.getType());
            } catch (IllegalArgumentException e) {
                row.addError("Tipo inválido: " + row.getType() + " (valores válidos: SIMPLE, VARIANT_PARENT, VARIANT, COMPOSITE)");
            }
        }

        if (isEmpty(row.getName())) {
            row.addError("Nome é obrigatório");
        }

        if (isEmpty(row.getSku())) {
            row.addError("SKU é obrigatório");
        }

        if (isEmpty(row.getCategoryId())) {
            row.addError("ID da categoria é obrigatório");
        } else {
            // Validate UUID format
            try {
                UUID.fromString(row.getCategoryId());
            } catch (IllegalArgumentException e) {
                row.addError("ID da categoria inválido (deve ser UUID)");
            }
        }

        if (row.getPrice() == null) {
            row.addError("Preço é obrigatório");
        } else if (row.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            row.addError("Preço não pode ser negativo");
        }

        // Validate status enum (if provided)
        if (!isEmpty(row.getStatus())) {
            try {
                ProductStatus.valueOf(row.getStatus());
            } catch (IllegalArgumentException e) {
                row.addError("Status inválido: " + row.getStatus() + " (valores válidos: ACTIVE, INACTIVE, DISCONTINUED)");
            }
        }

        // COMPOSITE products must have bomType
        if ("COMPOSITE".equals(row.getType())) {
            if (isEmpty(row.getBomType())) {
                row.addError("Produtos COMPOSITE devem ter bomType (VIRTUAL ou PHYSICAL)");
            } else {
                try {
                    BomType.valueOf(row.getBomType());
                } catch (IllegalArgumentException e) {
                    row.addError("bomType inválido: " + row.getBomType() + " (valores válidos: VIRTUAL, PHYSICAL)");
                }
            }
        } else {
            // Non-COMPOSITE products should not have bomType
            if (!isEmpty(row.getBomType())) {
                row.addError("Apenas produtos COMPOSITE podem ter bomType");
            }
        }
    }

    /**
     * Safely gets field from array with bounds checking
     */
    private String getField(String[] fields, int index) {
        if (index < fields.length) {
            String value = fields[index].trim();
            return value.isEmpty() ? null : value;
        }
        return null;
    }

    /**
     * Checks if string is null or empty
     */
    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
