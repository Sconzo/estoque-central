package com.estoquecentral.sales.application;

/**
 * NFCe Response DTO from middleware
 * Story 4.3: NFCe Emission and Stock Decrease
 */
public class NfceResponse {
    private String key;  // NFCe access key (44 digits)
    private String xml;  // NFCe XML
    private String status; // Status from SEFAZ
    private String protocol; // Authorization protocol

    public NfceResponse() {
    }

    public NfceResponse(String key, String xml, String status, String protocol) {
        this.key = key;
        this.xml = xml;
        this.status = status;
        this.protocol = protocol;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
