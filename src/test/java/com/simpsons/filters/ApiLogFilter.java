package com.simpsons.filters;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logging transversal: una línea resumida por petición con su duración.
 * Solo los 5xx se registran como ERROR; los 4xx (esperados en pruebas de
 * error/seguridad/fuzz) quedan en INFO y su cuerpo en DEBUG para no inundar
 * el log con respuestas de error previsibles.
 */
public class ApiLogFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(ApiLogFilter.class);

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext ctx) {
        long start = System.nanoTime();
        try {
            Response response = ctx.next(requestSpec, responseSpec);
            if (response == null) {
                log.error("{} {} no devolvió respuesta HTTP", requestSpec.getMethod(), requestSpec.getURI());
                return null;
            }
            int status = response.getStatusCode();
            if (status >= 500) {
                log.error("{} {} -> {} ({} ms)", requestSpec.getMethod(), requestSpec.getURI(),
                        status, elapsedMs(start));
                log.error("Respuesta de error -> {}", response.body().asString());
            } else if (status >= 400) {
                log.info("{} {} -> {} ({} ms)", requestSpec.getMethod(), requestSpec.getURI(),
                        status, elapsedMs(start));
                log.debug("Respuesta 4xx -> {}", response.body().asString());
            } else {
                log.info("{} {} -> {} ({} ms)", requestSpec.getMethod(), requestSpec.getURI(),
                        status, elapsedMs(start));
            }
            return response;
        } catch (RuntimeException e) {
            log.error("{} {} falló tras agotar reintentos ({})", requestSpec.getMethod(),
                    requestSpec.getURI(), e.getMessage());
            throw e;
        }
    }

    private long elapsedMs(long start) {
        return (System.nanoTime() - start) / 1_000_000;
    }
}
