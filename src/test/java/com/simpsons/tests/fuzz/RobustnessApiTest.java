package com.simpsons.tests.fuzz;

import com.simpsons.BaseApiTest;
import com.simpsons.core.ApiConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas de robustez y fuzzing: la API nunca debe caer (5xx) ante
 * entradas aleatorias, IDs/páginas extremas, headers malformados,
 * content negotiation no soportada, rutas raras y unicode.
 * Correr: mvn test -Pfuzz
 */
@Tag("fuzz")
class RobustnessApiTest extends BaseApiTest {

    @Test
    @DisplayName("IDs aleatorios nunca producen errores de servidor (5xx)")
    void randomNumericIdsNeverFail() {
        Random random = new Random(42);
        for (int i = 0; i < fuzzIterations(); i++) {
            int id = random.nextInt(5_000) + 1;
            int status = client.get("/characters/" + id).statusCode();
            assertThat(status).as("GET /characters/%d", id).isIn(200, 404);
        }
    }

    @Test
    @DisplayName("Cadenas aleatorias en el path se rechazan con 4xx, nunca 5xx")
    void randomStringsNeverFail() {
        Random random = new Random(42);
        String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_+.,!~()[]{};:@";

        for (int i = 0; i < fuzzIterations(); i++) {
            int length = 1 + random.nextInt(10);
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < length; j++) {
                sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
            }
            int status = client.get("/characters/" + sb).statusCode();
            assertThat(status).as("GET /characters/%s", sb).isLessThan(500);
        }
    }

    @Test
    @DisplayName("Páginas extremas o inválidas no rompen el listado")
    void extremePagesNeverFail() {
        for (String page : List.of("-1", "0", "1", "999999", "2147483647",
                "9223372036854775807", "abc", "1.5", "")) {
            int status = client.get("/characters?page=" + page).statusCode();
            assertThat(status).as("page=%s", page).isLessThan(500);
        }
    }

    @Test
    @DisplayName("Negociación de contenido: formatos no soportados no rompen la API")
    void contentNegotiationHandled() {
        for (String accept : List.of("application/xml", "text/html",
                "application/vnd.api+json", "text/csv", "image/png")) {
            int status = client.getWithHeaders("/characters/1", Map.of("Accept", accept)).statusCode();
            assertThat(status).as("Accept=%s", accept).isLessThan(500);
        }

        client.getWithHeaders("/characters/1", Map.of("Accept", "application/json"))
                .then().statusCode(200);
    }

    @Test
    @DisplayName("Headers malformados o extremos se toleran sin 5xx")
    void malformedHeadersTolerated() {
        Map<String, String> cases = Map.of(
                "Accept-Language", "a;b;c;d;e;;;;;;;;;;;;;;;;;;;;;;;",
                "X-Forwarded-For", "1.2.3.4, 5.6.7.8, ;;;;",
                "User-Agent", "Mozilla/5.0 (" + "x".repeat(2000) + ")",
                "Accept-Encoding", "gzip, deflate, br, " + "zz".repeat(100)
        );

        for (Map.Entry<String, String> entry : cases.entrySet()) {
            int status = client.getWithHeaders("/characters/1", Map.of(entry.getKey(), entry.getValue()))
                    .statusCode();
            assertThat(status).as("header %s malformado", entry.getKey()).isLessThan(500);
        }
    }

    @Test
    @DisplayName("Rutas con slashes dobles, trailing slash y encoding no rompen")
    void unusualPathsHandled() {
        for (String path : List.of("/characters//1", "/characters/1/", "/characters/%20",
                "/characters/%2e%2e", "/characters/1%2F2", "//characters/1", "/characters/1?page=1")) {
            int status = client.get(path).statusCode();
            assertThat(status).as("path=%s", path).isLessThan(500);
        }
    }

    @Test
    @DisplayName("Caracteres unicode en el path se manejan sin 5xx")
    void unicodePathsHandled() {
        for (String path : List.of("/characters/ñoño", "/characters/日本",
                "/characters/üñíçödé", "/characters/αβγ")) {
            int status = client.get(path).statusCode();
            assertThat(status).as("path=%s", path).isLessThan(500);
        }
    }

    private static int fuzzIterations() {
        return ApiConfig.intValue("fuzz.iterations", 10);
    }
}
