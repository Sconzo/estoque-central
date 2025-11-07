package com.estoquecentral.purchasing.domain;

public enum TaxRegime {
    SIMPLES_NACIONAL,    // Simplified tax regime (Brazil)
    LUCRO_PRESUMIDO,     // Presumed profit
    LUCRO_REAL,          // Real profit
    MEI,                 // Microempreendedor Individual
    OUTROS               // Others
}
