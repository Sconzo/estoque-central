package com.estoquecentral.purchasing.domain;

public enum BarcodeType {
    EAN13,       // EAN-13 (European Article Number)
    EAN8,        // EAN-8
    UPC,         // Universal Product Code
    CODE128,     // Code 128
    CODE39,      // Code 39
    QR,          // QR Code
    DATAMATRIX,  // Data Matrix
    CUSTOM       // Custom/Internal barcode
}
