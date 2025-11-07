package com.estoquecentral.reporting.application;

import com.estoquecentral.reporting.adapter.in.dto.*;
import com.estoquecentral.reporting.adapter.out.InventoryMovementReportRepository;
import com.estoquecentral.reporting.adapter.out.InventoryMovementReportRepository.MovementTotalsDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Inventory Movement Report Service
 * Business logic for movement reporting
 */
@Service
@Transactional(readOnly = true)
public class InventoryMovementReportService {

    private final InventoryMovementReportRepository repository;

    public InventoryMovementReportService(InventoryMovementReportRepository repository) {
        this.repository = repository;
    }

    /**
     * Get detailed movements with filters
     */
    public List<InventoryMovementDetailDTO> getMovements(InventoryMovementFilterDTO filter) {
        return repository.getMovements(filter);
    }

    /**
     * Get detailed movements with default filter (last 30 days)
     */
    public List<InventoryMovementDetailDTO> getMovements() {
        InventoryMovementFilterDTO defaultFilter = new InventoryMovementFilterDTO(
                LocalDate.now().minusDays(30),
                LocalDate.now(),
                null,
                null,
                null,
                null,
                1000
        );
        return repository.getMovements(defaultFilter);
    }

    /**
     * Get recent movements (last 30 days, max 1000)
     */
    public List<InventoryMovementDetailDTO> getRecentMovements() {
        return repository.getRecentMovements();
    }

    /**
     * Get movement summary by type
     */
    public List<InventoryMovementSummaryByTypeDTO> getSummaryByType() {
        return repository.getSummaryByType();
    }

    /**
     * Get movement summary by product
     */
    public List<InventoryMovementSummaryByProductDTO> getSummaryByProduct() {
        return repository.getSummaryByProduct();
    }

    /**
     * Get movement summary by product with filters
     */
    public List<InventoryMovementSummaryByProductDTO> getSummaryByProduct(
            UUID productId,
            String categoryName,
            Integer limit
    ) {
        return repository.getSummaryByProduct(productId, categoryName, limit);
    }

    /**
     * Count movements with filters
     */
    public long countMovements(InventoryMovementFilterDTO filter) {
        return repository.countMovements(filter);
    }

    /**
     * Get movement totals with filters
     */
    public MovementTotalsDTO getMovementTotals(InventoryMovementFilterDTO filter) {
        return repository.getMovementTotals(filter);
    }

    /**
     * Get complete report with movements and totals
     */
    public Map<String, Object> getCompleteReport(InventoryMovementFilterDTO filter) {
        List<InventoryMovementDetailDTO> movements = repository.getMovements(filter);
        MovementTotalsDTO totals = repository.getMovementTotals(filter);
        long count = repository.countMovements(filter);

        return Map.of(
                "movements", movements,
                "totals", totals,
                "count", count,
                "filter", filter,
                "hasMoreResults", count > filter.limit()
        );
    }

    /**
     * Export movements to CSV format
     */
    public byte[] exportMovementsToCSV(InventoryMovementFilterDTO filter) {
        List<InventoryMovementDetailDTO> movements = repository.getMovements(filter);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, true, StandardCharsets.UTF_8);

        // CSV Header
        writer.println(String.join(";",
                "Data",
                "Tipo",
                "Direção",
                "SKU",
                "Produto",
                "Categoria",
                "Localização",
                "Quantidade",
                "Custo Unitário",
                "Valor Total",
                "Estoque Atual",
                "Referência",
                "Observações"
        ));

        // CSV Rows
        for (InventoryMovementDetailDTO movement : movements) {
            writer.println(String.join(";",
                    escapeCSV(movement.movementDate().toString()),
                    escapeCSV(movement.getMovementTypeDisplay()),
                    escapeCSV(movement.getMovementDirectionDisplay()),
                    escapeCSV(movement.sku()),
                    escapeCSV(movement.productName()),
                    escapeCSV(movement.categoryName()),
                    escapeCSV(movement.locationName()),
                    escapeCSV(movement.quantity().toString()),
                    escapeCSV(movement.unitCost().toString()),
                    escapeCSV(movement.totalValue().toString()),
                    escapeCSV(movement.currentStock().toString()),
                    escapeCSV(movement.referenceType() != null ? movement.referenceType() : ""),
                    escapeCSV(movement.notes() != null ? movement.notes() : "")
            ));
        }

        writer.flush();
        return outputStream.toByteArray();
    }

    /**
     * Export summary by product to CSV
     */
    public byte[] exportSummaryByProductToCSV() {
        List<InventoryMovementSummaryByProductDTO> summary = repository.getSummaryByProduct();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, true, StandardCharsets.UTF_8);

        // CSV Header
        writer.println(String.join(";",
                "SKU",
                "Produto",
                "Categoria",
                "Total Movimentos",
                "Movimentos Entrada",
                "Quantidade Entrada",
                "Movimentos Saída",
                "Quantidade Saída",
                "Saldo Líquido",
                "Valor Movimentado",
                "Estoque Atual",
                "Taxa Giro"
        ));

        // CSV Rows
        for (InventoryMovementSummaryByProductDTO product : summary) {
            writer.println(String.join(";",
                    escapeCSV(product.sku()),
                    escapeCSV(product.productName()),
                    escapeCSV(product.categoryName()),
                    escapeCSV(product.totalMovements().toString()),
                    escapeCSV(product.inMovementsCount().toString()),
                    escapeCSV(product.totalQuantityIn().toString()),
                    escapeCSV(product.outMovementsCount().toString()),
                    escapeCSV(product.totalQuantityOut().toString()),
                    escapeCSV(product.netQuantityChange().toString()),
                    escapeCSV(product.totalValueMoved().toString()),
                    escapeCSV(product.currentStock().toString()),
                    escapeCSV(String.format("%.2f", product.getTurnoverRatio()))
            ));
        }

        writer.flush();
        return outputStream.toByteArray();
    }

    /**
     * Export summary by type to CSV
     */
    public byte[] exportSummaryByTypeToCSV() {
        List<InventoryMovementSummaryByTypeDTO> summary = repository.getSummaryByType();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, true, StandardCharsets.UTF_8);

        // CSV Header
        writer.println(String.join(";",
                "Tipo",
                "Direção",
                "Quantidade Movimentos",
                "Quantidade Total",
                "Valor Total",
                "Custo Médio Unitário",
                "Valor Médio por Movimento",
                "Primeira Movimentação",
                "Última Movimentação"
        ));

        // CSV Rows
        for (InventoryMovementSummaryByTypeDTO type : summary) {
            writer.println(String.join(";",
                    escapeCSV(type.getMovementTypeDisplay()),
                    escapeCSV(type.movementDirection()),
                    escapeCSV(type.movementCount().toString()),
                    escapeCSV(type.totalQuantity().toString()),
                    escapeCSV(type.totalValue().toString()),
                    escapeCSV(type.averageUnitCost().toString()),
                    escapeCSV(type.getAverageValuePerMovement().toString()),
                    escapeCSV(type.firstMovementDate() != null ? type.firstMovementDate().toString() : ""),
                    escapeCSV(type.lastMovementDate() != null ? type.lastMovementDate().toString() : "")
            ));
        }

        writer.flush();
        return outputStream.toByteArray();
    }

    /**
     * Escape CSV special characters
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        // Replace semicolon with comma to avoid breaking CSV
        value = value.replace(";", ",");
        // Remove newlines
        value = value.replace("\n", " ").replace("\r", " ");
        return value;
    }
}
