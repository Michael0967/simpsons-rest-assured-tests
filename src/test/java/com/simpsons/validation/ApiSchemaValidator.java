package com.simpsons.validation;

import io.restassured.response.ValidatableResponse;
import io.restassured.response.Response;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

/**
 * Contract validation: checks that a response respects its JSON Schema
 * (src/test/resources/schemas). Acts as the project's structural safety net.
 */
public final class ApiSchemaValidator {

    private static final String SCHEMA_BASE = "schemas/";

    private ApiSchemaValidator() {
    }

    public static ValidatableResponse validate(Response response, String schemaFile) {
        return response.then().body(matchesJsonSchemaInClasspath(SCHEMA_BASE + schemaFile));
    }
}
