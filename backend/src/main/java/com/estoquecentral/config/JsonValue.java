package com.estoquecentral.config;

/**
 * Wrapper type for PostgreSQL JSONB columns in Spring Data JDBC entities.
 * Use this instead of plain String for columns mapped to JSONB.
 */
public record JsonValue(String value) {

    public static JsonValue of(String json) {
        return json != null ? new JsonValue(json) : null;
    }

    @Override
    public String toString() {
        return value;
    }
}
