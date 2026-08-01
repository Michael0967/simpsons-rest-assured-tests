package com.simpsons.tests.contract;

import com.simpsons.BaseApiTest;
import com.simpsons.validation.ApiSchemaValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Validación de contratos estructurales contra JSON Schema.
 * Correr: mvn test -Pcontract
 */
@Tag("contract")
class ContractApiTest extends BaseApiTest {

    @Test
    @DisplayName("El detalle de personaje cumple su schema")
    void characterDetailMatchesContract() {
        ApiSchemaValidator.validate(client.getCharacter(1), "character-detail.json").statusCode(200);
    }

    @Test
    @DisplayName("Los items del listado de personajes cumplen su schema")
    void characterListItemMatchesContract() throws Exception {
        Object item = client.getCharacters(1).then().extract().jsonPath().get("results[0]");
        String itemJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(item);

        assertThat(itemJson,
                matchesJsonSchemaInClasspath("schemas/character-list-item.json"));
    }

    @Test
    @DisplayName("El episodio cumple su schema")
    void episodeMatchesContract() {
        ApiSchemaValidator.validate(client.getEpisode(1), "episode.json").statusCode(200);
    }

    @Test
    @DisplayName("La ubicación cumple su schema")
    void locationMatchesContract() {
        ApiSchemaValidator.validate(client.getLocation(1), "location.json").statusCode(200);
    }

    @Test
    @DisplayName("El envoltorio de paginación cumple su schema")
    void pagedResponseMatchesContract() {
        client.getCharacters(1)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/paged-response.json"));
    }

    @Test
    @DisplayName("Las respuestas de error cumplen su schema")
    void errorResponseMatchesContract() {
        ApiSchemaValidator.validate(client.getCharacter(999_999), "error.json").statusCode(404);
    }
}
