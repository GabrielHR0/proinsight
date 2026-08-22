package com.prosup.proinsight.infrastructure.persistence.document.composite;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class PersistedComponentReadConverter implements Converter<Document, PersistedComponent> {

    private final ObjectMapper objectMapper;

    public PersistedComponentReadConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public PersistedComponent convert(Document source) {
        try {
            return objectMapper.readValue(source.toJson(), PersistedComponent.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Document to PersistedComponent", e);
        }
    }
}
