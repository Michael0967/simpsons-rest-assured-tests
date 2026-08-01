package com.simpsons.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Gestión de configuración centralizada.
 *
 * Precedencia de valores (de mayor a menor):
 *  1. Propiedad de sistema   (-Dapi.base.uri=...)
 *  2. Variable de entorno    (API_BASE_URI)
 *  3. Valor por defecto      (src/test/resources/config.properties)
 */
public final class ApiConfig {

    private static final Properties props = new Properties();

    static {
        try (InputStream in = ApiConfig.class.getResourceAsStream("/config.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer config.properties", e);
        }
    }

    private ApiConfig() {
    }

    public static String baseUri() {
        return value("api.base.uri");
    }

    public static String basePath() {
        return value("api.base.path");
    }

    public static int connectionTimeoutMs() {
        return intValue("api.timeout.connect.ms", 10_000);
    }

    public static int readTimeoutMs() {
        return intValue("api.timeout.read.ms", 15_000);
    }

    public static int maxRetries() {
        return intValue("api.retry.max.attempts", 2);
    }

    public static int retryBackoffMs() {
        return intValue("api.retry.backoff.ms", 500);
    }

    public static String value(String key) {
        String systemProp = System.getProperty(key);
        if (systemProp != null && !systemProp.isBlank()) {
            return systemProp;
        }
        String envVar = System.getenv(key.toUpperCase().replace('.', '_'));
        if (envVar != null && !envVar.isBlank()) {
            return envVar;
        }
        return props.getProperty(key);
    }

    public static int intValue(String key, int defaultValue) {
        try {
            return Integer.parseInt(value(key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
