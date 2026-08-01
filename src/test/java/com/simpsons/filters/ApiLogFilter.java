package com.simpsons.filters;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cross-cutting logging: one summarized line per request with its duration.
 * Only 5xx are logged as ERROR; 4xx (expected in error/security/fuzz tests)
 * stay at INFO with their body at DEBUG so predictable error responses do not
 * flood the log.
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
                log.error("{} {} did not return an HTTP response", requestSpec.getMethod(), requestSpec.getURI());
                return null;
            }
            int status = response.getStatusCode();
            if (status >= 500) {
                log.error("{} {} -> {} ({} ms)", requestSpec.getMethod(), requestSpec.getURI(),
                        status, elapsedMs(start));
                log.error("Error response -> {}", response.body().asString());
            } else if (status >= 400) {
                log.info("{} {} -> {} ({} ms)", requestSpec.getMethod(), requestSpec.getURI(),
                        status, elapsedMs(start));
                log.debug("4xx response -> {}", response.body().asString());
            } else {
                log.info("{} {} -> {} ({} ms)", requestSpec.getMethod(), requestSpec.getURI(),
                        status, elapsedMs(start));
            }
            return response;
        } catch (RuntimeException e) {
            log.error("{} {} failed after exhausting retries ({})", requestSpec.getMethod(),
                    requestSpec.getURI(), e.getMessage());
            throw e;
        }
    }

    private long elapsedMs(long start) {
        return (System.nanoTime() - start) / 1_000_000;
    }
}
