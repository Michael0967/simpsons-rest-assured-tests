package com.simpsons.tests.smoke;

import com.simpsons.BaseApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Verificación rápida de que la API está viva y responde correctamente.
 * Es la primera capa que se ejecuta en CI (mvn test -Psmoke).
 */
@Tag("smoke")
class SmokeApiTest extends BaseApiTest {

    @Test
    @DisplayName("Los tres recursos responden 200")
    void coreEndpointsAreReachable() {
        client.getCharacter(1).then().statusCode(200);
        client.getEpisode(1).then().statusCode(200);
        client.getLocation(1).then().statusCode(200);
        client.getCharacters(1).then().statusCode(200);
    }

    @Test
    @DisplayName("Las respuestas tienen formato JSON")
    void responsesAreJson() {
        client.getCharacter(1).then().contentType("application/json");
        client.getCharacters(1).then().contentType("application/json");
    }

    @Test
    @Tag("performance")
    @DisplayName("El detalle de personaje responde en menos de 5s")
    void characterDetailRespondsFastEnough() {
        client.getCharacter(1).then()
                .statusCode(200)
                .body("name", notNullValue())
                .time(lessThan(5_000L));
    }
}
