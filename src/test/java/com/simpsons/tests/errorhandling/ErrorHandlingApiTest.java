package com.simpsons.tests.errorhandling;

import com.simpsons.BaseApiTest;
import com.simpsons.validation.ApiSchemaValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Comportamiento ante errores: IDs inexistentes, formatos inválidos,
 * métodos no soportados y rutas desconocidas.
 */
@Tag("regression")
class ErrorHandlingApiTest extends BaseApiTest {

    static Stream<Arguments> resources() {
        return Stream.of(
                Arguments.of("/characters", "Character not found"),
                Arguments.of("/episodes", "Episode not found"),
                Arguments.of("/locations", "Location not found")
        );
    }

    static Stream<Arguments> invalidIdFormats() {
        return Stream.of(
                Arguments.of("abc"),
                Arguments.of("-1"),
                Arguments.of("1.5")
        );
    }

    @ParameterizedTest(name = "{0} -> 404 ({1})")
    @MethodSource("resources")
    @DisplayName("Un ID inexistente devuelve 404 con estructura de error")
    void unknownIdReturnsStructured404(String resource, String expectedMessage) {
        client.get(resource + "/999999")
                .then()
                .statusCode(404)
                .body("error", equalTo("Not Found"))
                .body("statusCode", equalTo(404))
                .body("message", equalTo(expectedMessage));

        ApiSchemaValidator.validate(client.get(resource + "/999999"), "error.json")
                .statusCode(404);
    }

    @ParameterizedTest(name = "/characters/{0} -> 400")
    @MethodSource("invalidIdFormats")
    @DisplayName("Un ID no numérico devuelve 400")
    void nonNumericIdReturns400(String invalidId) {
        client.get("/characters/" + invalidId)
                .then()
                .statusCode(400)
                .body("error", equalTo("Bad Request"))
                .body("statusCode", equalTo(400))
                .body("message", equalTo("Validation failed (numeric string is expected)"));

        ApiSchemaValidator.validate(client.get("/characters/" + invalidId), "error.json")
                .statusCode(400);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("resources")
    @DisplayName("Un ID inexistente deserializa correctamente en POJO de error")
    void errorResponseDeserializesIntoPojo(String resource, String expectedMessage) {
        var error = client.get(resource + "/999999").as(com.simpsons.models.ErrorResponse.class);

        assertThat(error.getError()).isEqualTo("Not Found");
        assertThat(error.getStatusCode()).isEqualTo(404);
        assertThat(error.getMessage()).isEqualTo(expectedMessage);
    }

    @ParameterizedTest(name = "POST {0}")
    @MethodSource("resourcePaths")
    @DisplayName("Métodos no soportados devuelven 404")
    void unsupportedMethodsReturn404(String resource) {
        client.post(resource).then().statusCode(404);
        client.delete(resource, 1).then().statusCode(404);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("unknownRoutes")
    @DisplayName("Rutas desconocidas devuelven 404")
    void unknownRoutesReturn404(String route) {
        client.get(route).then().statusCode(404);
    }

    static Stream<Arguments> resourcePaths() {
        return Stream.of(
                Arguments.of("/characters"),
                Arguments.of("/episodes"),
                Arguments.of("/locations")
        );
    }

    static Stream<Arguments> unknownRoutes() {
        return Stream.of(
                Arguments.of("/nope"),
                Arguments.of("/shorts"),
                Arguments.of("/characters/1/friends"),
                Arguments.of("/character/1")
        );
    }
}
