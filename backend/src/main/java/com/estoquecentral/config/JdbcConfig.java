package com.estoquecentral.config;

import org.postgresql.util.PGobject;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;

import java.sql.SQLException;
import java.util.List;

/**
 * Spring Data JDBC configuration with custom converters for PostgreSQL JSONB support.
 */
@Configuration
public class JdbcConfig extends AbstractJdbcConfiguration {

    @Override
    protected List<?> userConverters() {
        return List.of(new JsonValueToJsonbConverter(), new PGobjectToJsonValueConverter());
    }

    @WritingConverter
    static class JsonValueToJsonbConverter implements Converter<JsonValue, PGobject> {
        @Override
        public PGobject convert(JsonValue source) {
            PGobject pgObject = new PGobject();
            pgObject.setType("jsonb");
            try {
                pgObject.setValue(source.value());
            } catch (SQLException e) {
                throw new RuntimeException("Failed to convert JsonValue to JSONB", e);
            }
            return pgObject;
        }
    }

    @ReadingConverter
    static class PGobjectToJsonValueConverter implements Converter<PGobject, JsonValue> {
        @Override
        public JsonValue convert(PGobject source) {
            return new JsonValue(source.getValue());
        }
    }
}
