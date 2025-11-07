package com.estoquecentral.reporting.application;

import com.estoquecentral.reporting.adapter.in.dto.*;
import com.estoquecentral.reporting.adapter.out.SalesReportRepository;
import com.estoquecentral.reporting.adapter.out.SalesReportRepository.SalesReportPeriodDTO;
import com.estoquecentral.reporting.adapter.out.SalesReportRepository.SalesTotalsDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Sales Report Service
 * Business logic for sales reporting
 */
@Service
@Transactional(readOnly = true)
public class SalesReportService {

    private final SalesReportRepository repository;

    public SalesReportService(SalesReportRepository repository) {
        this.repository = repository;
    }

    /**
     * Get sales by date and channel
     */
    public List<SalesByDateChannelDTO> getSalesByDateAndChannel(SalesFilterDTO filter) {
        return repository.getSalesByDateAndChannel(filter);
    }

    /**
     * Get sales by channel summary
     */
    public List<SalesByChannelSummaryDTO> getSalesByChannelSummary() {
        return repository.getSalesByChannelSummary();
    }

    /**
     * Get sales by period
     */
    public List<SalesByPeriodDTO> getSalesByPeriod(SalesFilterDTO filter) {
        return repository.getSalesByPeriod(filter);
    }

    /**
     * Get sales trend (last 30 days)
     */
    public List<SalesTrendDTO> getSalesTrend30Days() {
        return repository.getSalesTrend30Days();
    }

    /**
     * Get sales report grouped by period (uses function)
     */
    public List<SalesReportPeriodDTO> getSalesReportByPeriod(SalesFilterDTO filter) {
        return repository.getSalesReportByPeriod(filter);
    }

    /**
     * Get sales totals
     */
    public SalesTotalsDTO getSalesTotals(SalesFilterDTO filter) {
        return repository.getSalesTotals(filter);
    }

    /**
     * Get complete sales report with all data
     */
    public Map<String, Object> getCompleteSalesReport(SalesFilterDTO filter) {
        List<SalesByDateChannelDTO> salesByDate = repository.getSalesByDateAndChannel(filter);
        List<SalesByChannelSummaryDTO> channelSummary = repository.getSalesByChannelSummary();
        SalesTotalsDTO totals = repository.getSalesTotals(filter);

        return Map.of(
                "salesByDate", salesByDate,
                "channelSummary", channelSummary,
                "totals", totals,
                "filter", filter
        );
    }

    /**
     * Get sales grouped by channel
     */
    public Map<String, List<SalesByDateChannelDTO>> getSalesGroupedByChannel(SalesFilterDTO filter) {
        List<SalesByDateChannelDTO> sales = repository.getSalesByDateAndChannel(filter);
        return sales.stream()
                .collect(Collectors.groupingBy(SalesByDateChannelDTO::salesChannel));
    }

    /**
     * Get chart data for sales by channel
     */
    public Map<String, Object> getChartDataByChannel(SalesFilterDTO filter) {
        List<SalesByChannelSummaryDTO> summary = repository.getSalesByChannelSummary();

        List<String> labels = summary.stream()
                .map(SalesByChannelSummaryDTO::getChannelDisplayName)
                .collect(Collectors.toList());

        List<Object> salesData = summary.stream()
                .map(SalesByChannelSummaryDTO::totalSales)
                .collect(Collectors.toList());

        List<Object> orderData = summary.stream()
                .map(SalesByChannelSummaryDTO::totalOrders)
                .collect(Collectors.toList());

        return Map.of(
                "labels", labels,
                "datasets", List.of(
                        Map.of(
                                "label", "Vendas (R$)",
                                "data", salesData
                        ),
                        Map.of(
                                "label", "Pedidos",
                                "data", orderData
                        )
                )
        );
    }

    /**
     * Get chart data for sales trend
     */
    public Map<String, Object> getChartDataTrend() {
        List<SalesTrendDTO> trend = repository.getSalesTrend30Days();

        List<String> labels = trend.stream()
                .map(t -> t.saleDate().toString())
                .collect(Collectors.toList());

        List<Object> salesData = trend.stream()
                .map(SalesTrendDTO::totalSales)
                .collect(Collectors.toList());

        List<Object> movingAvgData = trend.stream()
                .map(SalesTrendDTO::movingAvg7Days)
                .collect(Collectors.toList());

        return Map.of(
                "labels", labels,
                "datasets", List.of(
                        Map.of(
                                "label", "Vendas Diárias",
                                "data", salesData,
                                "type", "bar"
                        ),
                        Map.of(
                                "label", "Média Móvel 7 dias",
                                "data", movingAvgData,
                                "type", "line"
                        )
                )
        );
    }

    /**
     * Export sales by date and channel to CSV
     */
    public byte[] exportSalesByDateChannelToCSV(SalesFilterDTO filter) {
        List<SalesByDateChannelDTO> sales = repository.getSalesByDateAndChannel(filter);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, true, StandardCharsets.UTF_8);

        // CSV Header
        writer.println(String.join(";",
                "Data",
                "Canal",
                "Pedidos",
                "Clientes Únicos",
                "Total Itens",
                "Subtotal",
                "Desconto",
                "Frete",
                "Total Vendas",
                "Ticket Médio",
                "Ticket Mínimo",
                "Ticket Máximo"
        ));

        // CSV Rows
        for (SalesByDateChannelDTO sale : sales) {
            writer.println(String.join(";",
                    escapeCSV(sale.saleDate().toString()),
                    escapeCSV(sale.getChannelDisplayName()),
                    escapeCSV(sale.orderCount().toString()),
                    escapeCSV(sale.uniqueCustomers().toString()),
                    escapeCSV(sale.totalItems().toString()),
                    escapeCSV(sale.totalSubtotal().toString()),
                    escapeCSV(sale.totalDiscount().toString()),
                    escapeCSV(sale.totalShipping().toString()),
                    escapeCSV(sale.totalSales().toString()),
                    escapeCSV(sale.averageTicket().toString()),
                    escapeCSV(sale.minTicket().toString()),
                    escapeCSV(sale.maxTicket().toString())
            ));
        }

        writer.flush();
        return outputStream.toByteArray();
    }

    /**
     * Export channel summary to CSV
     */
    public byte[] exportChannelSummaryToCSV() {
        List<SalesByChannelSummaryDTO> summary = repository.getSalesByChannelSummary();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, true, StandardCharsets.UTF_8);

        // CSV Header
        writer.println(String.join(";",
                "Canal",
                "Total Pedidos",
                "Clientes Únicos",
                "Pedidos por Cliente",
                "Total Itens",
                "Itens por Pedido",
                "Total Vendas",
                "Ticket Médio",
                "% Desconto",
                "Pedidos Entregues",
                "Pedidos Pagos",
                "Taxa Pagamento %"
        ));

        // CSV Rows
        for (SalesByChannelSummaryDTO channel : summary) {
            writer.println(String.join(";",
                    escapeCSV(channel.getChannelDisplayName()),
                    escapeCSV(channel.totalOrders().toString()),
                    escapeCSV(channel.uniqueCustomers().toString()),
                    escapeCSV(channel.ordersPerCustomer().toString()),
                    escapeCSV(channel.totalItems().toString()),
                    escapeCSV(channel.averageItemsPerOrder().toString()),
                    escapeCSV(channel.totalSales().toString()),
                    escapeCSV(channel.averageTicket().toString()),
                    escapeCSV(channel.discountPercentage().toString()),
                    escapeCSV(channel.deliveredOrders().toString()),
                    escapeCSV(channel.paidOrders().toString()),
                    escapeCSV(String.format("%.2f", channel.getPaymentRate()))
            ));
        }

        writer.flush();
        return outputStream.toByteArray();
    }

    /**
     * Export sales trend to CSV
     */
    public byte[] exportSalesTrendToCSV() {
        List<SalesTrendDTO> trend = repository.getSalesTrend30Days();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, true, StandardCharsets.UTF_8);

        // CSV Header
        writer.println(String.join(";",
                "Data",
                "Pedidos",
                "Clientes Únicos",
                "Total Vendas",
                "Ticket Médio",
                "Média Móvel 7 dias",
                "Tendência"
        ));

        // CSV Rows
        for (SalesTrendDTO day : trend) {
            writer.println(String.join(";",
                    escapeCSV(day.saleDate().toString()),
                    escapeCSV(day.orderCount().toString()),
                    escapeCSV(day.uniqueCustomers().toString()),
                    escapeCSV(day.totalSales().toString()),
                    escapeCSV(day.averageTicket().toString()),
                    escapeCSV(day.movingAvg7Days().toString()),
                    escapeCSV(day.getTrendIndicator())
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
        value = value.replace(";", ",");
        value = value.replace("\n", " ").replace("\r", " ");
        return value;
    }
}
