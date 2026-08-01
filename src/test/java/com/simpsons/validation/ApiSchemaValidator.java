package com.simpsons.validation;

import io.restassured.response.ValidatableResponse;
import io.restassured.response.Response;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

/**
 * Validación de contratos: comprueba que la respuesta respeta su JSON Schema
 * (src/test/resources/schemas). Actúa como la red de seguridad estructural
 * del proyecto.
 */
public final class ApiSchemaValidator {

    private static final String SCHEMA_BASE = "schemas/";

    private ApiSchemaValidator() {
    }

    public static ValidatableResponse validate(Response response, String schemaFile) {
        return response.then().body(matchesJsonSchemaInClasspath(SCHEMA_BASE + schemaFile));
    }
}
