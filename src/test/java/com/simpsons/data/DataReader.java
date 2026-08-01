package com.simpsons.data;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Reads test data sets from external JSON files (src/test/resources/data),
 * decoupling the data from the test logic.
 */
public final class DataReader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private DataReader() {
    }

    public static <T> List<T> readList(String resourcePath, Class<T> type) {
        try (InputStream in = DataReader.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalStateException("Resource not found: " + resourcePath);
            }
            return MAPPER.readValue(in,
                    MAPPER.getTypeFactory().constructCollectionType(List.class, type));
        } catch (IOException e) {
            throw new IllegalStateException("Error reading test data: " + resourcePath, e);
        }
    }
}
