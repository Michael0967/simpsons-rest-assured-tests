package com.simpsons.core;

import com.simpsons.filters.ApiLogFilter;
import com.simpsons.filters.RetryFilter;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/**
 * Low-level HTTP client: builds the base RequestSpecification (URL, headers,
 * timeouts and cross-cutting filters) used by SimpsonsApiClient.
 */
public class RestClient {

    private final RequestSpecification spec;

    public RestClient() {
        this.spec = new RequestSpecBuilder()
                .setBaseUri(ApiConfig.baseUri())
                .setBasePath(ApiConfig.basePath())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .setConfig(RestAssuredConfig.config()
                        .httpClient(HttpClientConfig.httpClientConfig()
                                .setParam("http.connection.timeout", ApiConfig.connectionTimeoutMs())
                                .setParam("http.socket.timeout", ApiConfig.readTimeoutMs())))
                .addFilter(new ApiLogFilter())
                .addFilter(new RetryFilter(ApiConfig.maxRetries(), ApiConfig.retryBackoffMs()))
                .addFilter(new AllureRestAssured())
                .build();
    }

    public RequestSpecification spec() {
        return spec;
    }
}
