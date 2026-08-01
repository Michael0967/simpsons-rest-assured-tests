package com.simpsons.filters;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Reintenta peticiones ante errores transitorios (429/5xx) y fallos de conexión.
 * Combate la flakiness de APIs externas sin ocultar errores reales:
 * tras agotar los intentos, la última respuesta (o excepción) se propaga tal cual.
 */
public class RetryFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RetryFilter.class);
    private static final Set<Integer> RETRYABLE_STATUS = Set.of(429, 500, 502, 503, 504);

    private final int maxRetries;
    private final int backoffMs;

    public RetryFilter(int maxRetries, int backoffMs) {
        this.maxRetries = maxRetries;
        this.backoffMs = backoffMs;
    }

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext ctx) {
        Response response = null;
        int attempt = 0;
        while (true) {
            try {
                response = ctx.next(requestSpec, responseSpec);
                if (response != null && RETRYABLE_STATUS.contains(response.getStatusCode()) && attempt < maxRetries) {
                    attempt++;
                    sleepAndLog(requestSpec, response.getStatusCode(), attempt);
                    continue;
                }
                return response;
            } catch (RuntimeException e) {
                if (attempt >= maxRetries) {
                    throw e;
                }
                attempt++;
                log.warn("{} {} falló por excepción: {} — reintentando ({}/{})",
                        requestSpec.getMethod(), requestSpec.getURI(), e.getMessage(), attempt, maxRetries);
                sleep(backoffMs * (long) Math.pow(2, attempt - 1));
            }
        }
    }

    private void sleepAndLog(FilterableRequestSpecification requestSpec, int status, int attempt) {
        long delay = backoffMs * (long) Math.pow(2, attempt - 1);
        log.warn("{} {} respondió {} — reintentando ({}/{}) en {} ms",
                requestSpec.getMethod(), requestSpec.getURI(), status, attempt, maxRetries, delay);
        sleep(delay);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrumpido durante backoff de reintento", e);
        }
    }
}
