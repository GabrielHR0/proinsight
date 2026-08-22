package com.prosup.proinsight.infrastructure.persistence.document.composite;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class PersistedComponentWriteConverter implements Converter<PersistedComponent, Document> {

    private final ObjectMapper objectMapper;

    public PersistedComponentWriteConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Document convert(PersistedComponent source) {
        try {
            String json = objectMapper.writeValueAsString(source);
            return Document.parse(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert PersistedComponent to Document", e);
        }
    }
}
