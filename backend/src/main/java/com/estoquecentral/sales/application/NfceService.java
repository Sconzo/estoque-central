package com.estoquecentral.sales.application;

import com.estoquecentral.sales.domain.Sale;
import com.estoquecentral.sales.domain.SaleItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

/**
 * NFCe Service - Integration with NFCe middleware (Focus NFe / NFe.io)
 * Story 4.3: NFCe Emission and Stock Decrease
 * 
 * STUB IMPLEMENTATION: This is a mock implementation for development.
 * Replace with actual middleware integration in production.
 */
@Service
public class NfceService {
    private static final Logger logger = LoggerFactory.getLogger(NfceService.class);
    private static final Random random = new Random();

    /**
     * Emit NFCe for a sale
     * 
     * @param sale Sale to emit NFCe for
     * @param saleItems Items of the sale
     * @return NFCe response with key and XML
     * @throws NfceEmissionException if emission fails
     */
    public NfceResponse emitNfce(Sale sale, List<SaleItem> saleItems) throws NfceEmissionException {
        logger.info("Emitting NFCe for sale: {}", sale.getSaleNumber());

        try {
            // Simulate middleware call (timeout 10s in production)
            // In production, this would make REST call to Focus NFe / NFe.io
            
            // Generate mock NFCe key (44 digits)
            String nfceKey = generateMockNfceKey();
            
            // Generate mock XML
            String nfceXml = generateMockXml(sale, saleItems, nfceKey);
            
            logger.info("NFCe emitted successfully. Key: {}", nfceKey);
            
            return new NfceResponse(nfceKey, nfceXml, "AUTHORIZED", "123456789");
            
        } catch (Exception e) {
            logger.error("Failed to emit NFCe for sale: {}", sale.getSaleNumber(), e);
            throw new NfceEmissionException("Failed to emit NFCe: " + e.getMessage(), e);
        }
    }

    /**
     * Cancel NFCe
     * 
     * @param nfceKey NFCe key to cancel
     * @param reason Cancellation reason
     * @throws NfceEmissionException if cancellation fails
     */
    public void cancelNfce(String nfceKey, String reason) throws NfceEmissionException {
        logger.info("Cancelling NFCe: {}", nfceKey);
        
        try {
            // In production: call middleware to cancel NFCe
            logger.info("NFCe cancelled successfully");
        } catch (Exception e) {
            logger.error("Failed to cancel NFCe: {}", nfceKey, e);
            throw new NfceEmissionException("Failed to cancel NFCe: " + e.getMessage(), e);
        }
    }

    // Mock helpers (remove in production)
    
    private String generateMockNfceKey() {
        // NFCe key format: UF(2) + AAMM(4) + CNPJ(14) + MOD(2) + SERIE(3) + NNN(9) + tpEmis(1) + cNF(8) + DV(1)
        // Total: 44 digits
        StringBuilder key = new StringBuilder();
        key.append("35"); // UF: São Paulo
        key.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMM")));
        key.append(String.format("%014d", random.nextLong() & Long.MAX_VALUE).substring(0, 14)); // CNPJ
        key.append("65"); // Model: NFCe
        key.append("001"); // Serie
        key.append(String.format("%09d", random.nextInt(999999999))); // Number
        key.append("1"); // Emission type
        key.append(String.format("%08d", random.nextInt(99999999))); // Random code
        key.append(String.valueOf(random.nextInt(10))); // Check digit
        
        return key.toString();
    }

    private String generateMockXml(Sale sale, List<SaleItem> saleItems, String nfceKey) {
        // In production: build proper XML according to SEFAZ layout 4.0
        return String.format(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<nfeProc versao=\"4.00\">\n" +
            "  <NFe>\n" +
            "    <infNFe Id=\"NFe%s\">\n" +
            "      <ide>\n" +
            "        <cUF>35</cUF>\n" +
            "        <mod>65</mod>\n" +
            "        <serie>1</serie>\n" +
            "        <nNF>%s</nNF>\n" +
            "      </ide>\n" +
            "      <total>\n" +
            "        <vNF>%.2f</vNF>\n" +
            "      </total>\n" +
            "    </infNFe>\n" +
            "  </NFe>\n" +
            "</nfeProc>",
            nfceKey,
            sale.getSaleNumber(),
            sale.getTotalAmount()
        );
    }
}
