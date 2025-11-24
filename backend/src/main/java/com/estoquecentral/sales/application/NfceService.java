package com.estoquecentral.sales.application;

import com.estoquecentral.sales.domain.Sale;
import com.estoquecentral.sales.domain.SaleItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * NfceService - Integration with NFCe middleware (Focus NFe / NFe.io)
 * Story 4.3: NFCe Emission and Stock Decrease
 */
@Service
public class NfceService {

    private final RestTemplate restTemplate;
    private final String nfceApiUrl;
    private final String nfceApiToken;
    private final boolean nfceEnabled;

    public NfceService(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${nfce.api.url:http://localhost:9090/nfce}") String nfceApiUrl,
            @Value("${nfce.api.token:demo-token}") String nfceApiToken,
            @Value("${nfce.enabled:false}") boolean nfceEnabled) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
        this.nfceApiUrl = nfceApiUrl;
        this.nfceApiToken = nfceApiToken;
        this.nfceEnabled = nfceEnabled;
    }

    /**
     * Emit NFCe for sale
     * Calls external middleware (Focus NFe or NFe.io)
     * Timeout: 10 seconds
     */
    public NfceResponse emitNfce(Sale sale, List<SaleItem> items) {
        if (!nfceEnabled) {
            // Mock response for development/testing
            return new NfceResponse(
                    "35251112345678901234550010001234561001234567",
                    generateMockXml(sale, items),
                    "EMITTED"
            );
        }

        try {
            // Build NFCe request payload
            Map<String, Object> payload = Map.of(
                    "sale_id", sale.getId().toString(),
                    "sale_number", sale.getSaleNumber(),
                    "total_amount", sale.getTotalAmount().toString(),
                    "payment_method", sale.getPaymentMethod().name(),
                    "items", items.stream().map(item -> Map.of(
                            "product_id", item.getProductId().toString(),
                            "quantity", item.getQuantity().toString(),
                            "unit_price", item.getUnitPrice().toString(),
                            "total_price", item.getTotalPrice().toString()
                    )).toList()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + nfceApiToken);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    nfceApiUrl + "/emit",
                    request,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                return new NfceResponse(
                        (String) body.get("nfce_key"),
                        (String) body.get("xml"),
                        "EMITTED"
                );
            } else {
                throw new NfceException("NFCe emission failed: " + response.getStatusCode());
            }

        } catch (Exception e) {
            throw new NfceException("Failed to emit NFCe: " + e.getMessage(), e);
        }
    }

    /**
     * Generate mock XML for development (when nfce.enabled=false)
     */
    private String generateMockXml(Sale sale, List<SaleItem> items) {
        return String.format("""
                <?xml version="1.0" encoding="UTF-8"?>
                <nfeProc xmlns="http://www.portalfiscal.inf.br/nfe">
                    <NFe>
                        <infNFe>
                            <ide>
                                <cNF>%s</cNF>
                                <natOp>Venda</natOp>
                                <mod>65</mod>
                                <serie>1</serie>
                                <nNF>%s</nNF>
                            </ide>
                            <total>
                                <ICMSTot>
                                    <vNF>%s</vNF>
                                </ICMSTot>
                            </total>
                        </infNFe>
                    </NFe>
                </nfeProc>
                """,
                sale.getId().toString().substring(0, 8),
                sale.getSaleNumber(),
                sale.getTotalAmount().toString()
        );
    }

    /**
     * NFCe response from middleware
     */
    public record NfceResponse(String nfceKey, String xml, String status) {}

    /**
     * Exception thrown when NFCe emission fails
     */
    public static class NfceException extends RuntimeException {
        public NfceException(String message) {
            super(message);
        }

        public NfceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
