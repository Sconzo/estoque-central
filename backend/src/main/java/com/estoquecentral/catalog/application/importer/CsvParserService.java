package com.estoquecentral.catalog.application.importer;

import com.estoquecentral.catalog.adapter.in.dto.ProductCsvRow;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * CsvParserService - Parses and validates product CSV files
 *
 * <p>CSV Format (header row required):
 * name,sku,barcode,description,category,price,cost,unit,controlsInventory
 *
 * <p>Example:
 * <pre>
 * Notebook Dell,NOTE-001,7891234567890,Notebook 15pol,Eletrônicos,3499.90,2500.00,UN,true
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
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        return parseAndValidate(content);
    }

    public List<ProductCsvRow> parseAndValidate(String csvContent) throws IOException {
        List<ProductCsvRow> rows = new ArrayList<>();

        try (Reader reader = new StringReader(csvContent);
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

        // name,sku,barcode,description,category,price,cost,unit,controlsInventory
        row.setName(getField(fields, 0));
        row.setSku(getField(fields, 1));
        row.setBarcode(getField(fields, 2));
        row.setDescription(getField(fields, 3));
        row.setCategory(getField(fields, 4));

        // Parse numeric fields
        String priceStr = getField(fields, 5);
        if (priceStr != null && !priceStr.isEmpty()) {
            try {
                row.setPrice(new BigDecimal(priceStr));
            } catch (NumberFormatException e) {
                row.addError("Preço inválido: " + priceStr);
            }
        }

        String costStr = getField(fields, 6);
        if (costStr != null && !costStr.isEmpty()) {
            try {
                row.setCost(new BigDecimal(costStr));
            } catch (NumberFormatException e) {
                row.addError("Custo inválido: " + costStr);
            }
        }

        row.setUnit(getField(fields, 7));

        // Parse boolean
        String controlsInvStr = getField(fields, 8);
        if (controlsInvStr != null && !controlsInvStr.isEmpty()) {
            row.setControlsInventory(Boolean.parseBoolean(controlsInvStr));
        }

        return row;
    }

    /**
     * Validates a ProductCsvRow
     */
    private void validateRow(ProductCsvRow row) {
        if (isEmpty(row.getName())) {
            row.addError("Nome é obrigatório");
        }

        if (isEmpty(row.getSku())) {
            row.addError("SKU é obrigatório");
        }

        if (isEmpty(row.getCategory())) {
            row.addError("Categoria é obrigatória");
        }

        if (row.getPrice() == null) {
            row.addError("Preço é obrigatório");
        } else if (row.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            row.addError("Preço não pode ser negativo");
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
