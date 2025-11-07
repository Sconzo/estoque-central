package com.estoquecentral.reporting.adapter.in.web;

import com.estoquecentral.reporting.adapter.in.dto.*;
import com.estoquecentral.reporting.adapter.out.SalesReportRepository.SalesReportPeriodDTO;
import com.estoquecentral.reporting.adapter.out.SalesReportRepository.SalesTotalsDTO;
import com.estoquecentral.reporting.application.SalesReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Sales Report REST Controller
 * Endpoints for sales reporting by period and channel
 */
@RestController
@RequestMapping("/api/reports/sales")
public class SalesReportController {

    private final SalesReportService service;

    public SalesReportController(SalesReportService service) {
        this.service = service;
    }

    /**
     * GET /api/reports/sales/by-date-channel
     * Get sales by date and channel with filters
     *
     * Query params:
     * - startDate: Start date (YYYY-MM-DD)
     * - endDate: End date (YYYY-MM-DD)
     * - salesChannel: STORE, ONLINE, MARKETPLACE, PHONE, WHATSAPP
     * - groupBy: day, week, month (default: day)
     *
     * Response:
     * [
     *   {
     *     "saleDate": "2025-11-07",
     *     "salesChannel": "STORE",
     *     "orderCount": 25,
     *     "uniqueCustomers": 18,
     *     "totalSales": 8500.00,
     *     "averageTicket": 340.00
     *   }
     * ]
     */
    @GetMapping("/by-date-channel")
    public ResponseEntity<List<SalesByDateChannelDTO>> getSalesByDateAndChannel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String salesChannel,
            @RequestParam(defaultValue = "day") String groupBy
    ) {
        SalesFilterDTO filter = new SalesFilterDTO(startDate, endDate, salesChannel, groupBy);
        List<SalesByDateChannelDTO> sales = service.getSalesByDateAndChannel(filter);
        return ResponseEntity.ok(sales);
    }

    /**
     * GET /api/reports/sales/by-channel-summary
     * Get complete sales statistics by channel
     *
     * Response:
     * [
     *   {
     *     "salesChannel": "STORE",
     *     "totalOrders": 450,
     *     "uniqueCustomers": 280,
     *     "ordersPerCustomer": 1.61,
     *     "totalSales": 125000.00,
     *     "averageTicket": 277.78,
     *     "discountPercentage": 8.5,
     *     "deliveredOrders": 420,
     *     "paidOrders": 440
     *   }
     * ]
     */
    @GetMapping("/by-channel-summary")
    public ResponseEntity<List<SalesByChannelSummaryDTO>> getSalesByChannelSummary() {
        List<SalesByChannelSummaryDTO> summary = service.getSalesByChannelSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * GET /api/reports/sales/by-period
     * Get sales aggregated by period
     *
     * Response includes: day, week, month, year aggregations
     * [
     *   {
     *     "saleDate": "2025-11-07",
     *     "saleYear": 2025,
     *     "saleMonth": 11,
     *     "yearMonth": "2025-11",
     *     "orderCount": 45,
     *     "totalSales": 12500.00
     *   }
     * ]
     */
    @GetMapping("/by-period")
    public ResponseEntity<List<SalesByPeriodDTO>> getSalesByPeriod(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String salesChannel,
            @RequestParam(defaultValue = "day") String groupBy
    ) {
        SalesFilterDTO filter = new SalesFilterDTO(startDate, endDate, salesChannel, groupBy);
        List<SalesByPeriodDTO> sales = service.getSalesByPeriod(filter);
        return ResponseEntity.ok(sales);
    }

    /**
     * GET /api/reports/sales/by-period-grouped
     * Get sales report grouped by period using SQL function
     *
     * Response:
     * [
     *   {
     *     "periodKey": "2025-11-07",
     *     "salesChannel": "STORE",
     *     "orderCount": 25,
     *     "totalSales": 8500.00,
     *     "averageTicket": 340.00
     *   }
     * ]
     */
    @GetMapping("/by-period-grouped")
    public ResponseEntity<List<SalesReportPeriodDTO>> getSalesReportByPeriod(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String salesChannel,
            @RequestParam(defaultValue = "day") String groupBy
    ) {
        SalesFilterDTO filter = new SalesFilterDTO(startDate, endDate, salesChannel, groupBy);
        List<SalesReportPeriodDTO> report = service.getSalesReportByPeriod(filter);
        return ResponseEntity.ok(report);
    }

    /**
     * GET /api/reports/sales/trend
     * Get sales trend (last 30 days) with moving average
     *
     * Response:
     * [
     *   {
     *     "saleDate": "2025-11-07",
     *     "orderCount": 45,
     *     "totalSales": 12500.00,
     *     "movingAvg7Days": 11800.00,
     *     "trendIndicator": "UP"
     *   }
     * ]
     */
    @GetMapping("/trend")
    public ResponseEntity<List<SalesTrendDTO>> getSalesTrend() {
        List<SalesTrendDTO> trend = service.getSalesTrend30Days();
        return ResponseEntity.ok(trend);
    }

    /**
     * GET /api/reports/sales/totals
     * Get sales totals with filters
     *
     * Response:
     * {
     *   "totalOrders": 450,
     *   "uniqueCustomers": 280,
     *   "totalItems": 1250,
     *   "totalSales": 125000.00,
     *   "averageTicket": 277.78,
     *   "totalDiscount": 8500.00
     * }
     */
    @GetMapping("/totals")
    public ResponseEntity<SalesTotalsDTO> getSalesTotals(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String salesChannel,
            @RequestParam(defaultValue = "day") String groupBy
    ) {
        SalesFilterDTO filter = new SalesFilterDTO(startDate, endDate, salesChannel, groupBy);
        SalesTotalsDTO totals = service.getSalesTotals(filter);
        return ResponseEntity.ok(totals);
    }

    /**
     * GET /api/reports/sales/complete
     * Get complete sales report with all data
     *
     * Response:
     * {
     *   "salesByDate": [ ... ],
     *   "channelSummary": [ ... ],
     *   "totals": { ... },
     *   "filter": { ... }
     * }
     */
    @GetMapping("/complete")
    public ResponseEntity<Map<String, Object>> getCompleteSalesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String salesChannel,
            @RequestParam(defaultValue = "day") String groupBy
    ) {
        SalesFilterDTO filter = new SalesFilterDTO(startDate, endDate, salesChannel, groupBy);
        Map<String, Object> report = service.getCompleteSalesReport(filter);
        return ResponseEntity.ok(report);
    }

    /**
     * GET /api/reports/sales/grouped-by-channel
     * Get sales grouped by channel
     *
     * Response:
     * {
     *   "STORE": [ ... ],
     *   "ONLINE": [ ... ],
     *   "MARKETPLACE": [ ... ]
     * }
     */
    @GetMapping("/grouped-by-channel")
    public ResponseEntity<Map<String, List<SalesByDateChannelDTO>>> getSalesGroupedByChannel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String salesChannel,
            @RequestParam(defaultValue = "day") String groupBy
    ) {
        SalesFilterDTO filter = new SalesFilterDTO(startDate, endDate, salesChannel, groupBy);
        Map<String, List<SalesByDateChannelDTO>> grouped = service.getSalesGroupedByChannel(filter);
        return ResponseEntity.ok(grouped);
    }

    /**
     * GET /api/reports/sales/chart/by-channel
     * Get chart data for sales by channel
     *
     * Response: Chart.js compatible format
     * {
     *   "labels": ["Loja Física", "Loja Online", "Marketplace"],
     *   "datasets": [
     *     {
     *       "label": "Vendas (R$)",
     *       "data": [125000, 85000, 45000]
     *     },
     *     {
     *       "label": "Pedidos",
     *       "data": [450, 320, 180]
     *     }
     *   ]
     * }
     */
    @GetMapping("/chart/by-channel")
    public ResponseEntity<Map<String, Object>> getChartDataByChannel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String salesChannel,
            @RequestParam(defaultValue = "day") String groupBy
    ) {
        SalesFilterDTO filter = new SalesFilterDTO(startDate, endDate, salesChannel, groupBy);
        Map<String, Object> chartData = service.getChartDataByChannel(filter);
        return ResponseEntity.ok(chartData);
    }

    /**
     * GET /api/reports/sales/chart/trend
     * Get chart data for sales trend (last 30 days)
     *
     * Response: Chart.js compatible format
     * {
     *   "labels": ["2025-10-08", "2025-10-09", ...],
     *   "datasets": [
     *     {
     *       "label": "Vendas Diárias",
     *       "data": [12500, 11800, ...],
     *       "type": "bar"
     *     },
     *     {
     *       "label": "Média Móvel 7 dias",
     *       "data": [11800, 11900, ...],
     *       "type": "line"
     *     }
     *   ]
     * }
     */
    @GetMapping("/chart/trend")
    public ResponseEntity<Map<String, Object>> getChartDataTrend() {
        Map<String, Object> chartData = service.getChartDataTrend();
        return ResponseEntity.ok(chartData);
    }

    /**
     * GET /api/reports/sales/export/by-date-channel/csv
     * Export sales by date and channel to CSV
     *
     * Response: CSV file download
     */
    @GetMapping("/export/by-date-channel/csv")
    public ResponseEntity<byte[]> exportSalesByDateChannelToCSV(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String salesChannel,
            @RequestParam(defaultValue = "day") String groupBy
    ) {
        SalesFilterDTO filter = new SalesFilterDTO(startDate, endDate, salesChannel, groupBy);
        byte[] csvContent = service.exportSalesByDateChannelToCSV(filter);

        String filename = String.format(
                "vendas-data-canal-%s.csv",
                LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
                .body(csvContent);
    }

    /**
     * GET /api/reports/sales/export/channel-summary/csv
     * Export channel summary to CSV
     *
     * Response: CSV file download
     */
    @GetMapping("/export/channel-summary/csv")
    public ResponseEntity<byte[]> exportChannelSummaryToCSV() {
        byte[] csvContent = service.exportChannelSummaryToCSV();

        String filename = String.format(
                "resumo-vendas-canal-%s.csv",
                LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
                .body(csvContent);
    }

    /**
     * GET /api/reports/sales/export/trend/csv
     * Export sales trend to CSV
     *
     * Response: CSV file download
     */
    @GetMapping("/export/trend/csv")
    public ResponseEntity<byte[]> exportSalesTrendToCSV() {
        byte[] csvContent = service.exportSalesTrendToCSV();

        String filename = String.format(
                "tendencia-vendas-%s.csv",
                LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
                .body(csvContent);
    }
}
