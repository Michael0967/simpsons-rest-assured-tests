package com.simpsons.data;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Lee conjuntos de test data desde archivos JSON externos
 * (src/test/resources/data), desacoplando los datos de la lógica de los tests.
 */
public final class DataReader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private DataReader() {
    }

    public static <T> List<T> readList(String resourcePath, Class<T> type) {
        try (InputStream in = DataReader.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalStateException("No se encontró el recurso: " + resourcePath);
            }
            return MAPPER.readValue(in,
                    MAPPER.getTypeFactory().constructCollectionType(List.class, type));
        } catch (IOException e) {
            throw new IllegalStateException("Error leyendo test data: " + resourcePath, e);
        }
    }
}
