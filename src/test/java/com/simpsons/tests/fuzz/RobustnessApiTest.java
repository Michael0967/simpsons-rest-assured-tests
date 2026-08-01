package com.simpsons.tests.fuzz;

import com.simpsons.BaseApiTest;
import com.simpsons.core.ApiConfig;
import com.simpsons.screenplay.interactions.Get;
import com.simpsons.screenplay.tasks.FetchResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.simpsons.screenplay.Ensure.thatTheStatusCode;
import static com.simpsons.screenplay.questions.TheResponse.statusCode;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Robustness and fuzzing: the API must never crash (5xx) on random inputs,
 * extreme ids/pages, malformed headers, unsupported content negotiation,
 * unusual paths and unicode.
 * Run: mvn test -Pfuzz
 */
@Tag("fuzz")
class RobustnessApiTest extends BaseApiTest {

    @Test
    @DisplayName("Random ids never produce server errors (5xx)")
    void randomNumericIdsNeverFail() {
        Random random = new Random(42);
        for (int i = 0; i < fuzzIterations(); i++) {
            int id = random.nextInt(5_000) + 1;
            actor.attemptsTo(FetchResource.named("/characters/" + id));
            int status = actor.asksFor(statusCode());
            assertThat(status).as("GET /characters/%d", id).isIn(200, 404);
        }
    }

    @Test
    @DisplayName("Random strings in the path are rejected with 4xx, never 5xx")
    void randomStringsNeverFail() {
        Random random = new Random(42);
        String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_+.,!~()[]{};:@";

        for (int i = 0; i < fuzzIterations(); i++) {
            int length = 1 + random.nextInt(10);
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < length; j++) {
                sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
            }
            actor.attemptsTo(FetchResource.named("/characters/" + sb));
            int status = actor.asksFor(statusCode());
            assertThat(status).as("GET /characters/%s", sb).isLessThan(500);
        }
    }

    @Test
    @DisplayName("Extreme or invalid pages do not break the listing")
    void extremePagesNeverFail() {
        for (String page : List.of("-1", "0", "1", "999999", "2147483647",
                "9223372036854775807", "abc", "1.5", "")) {
            actor.attemptsTo(FetchResource.named("/characters?page=" + page));
            int status = actor.asksFor(statusCode());
            assertThat(status).as("page=%s", page).isLessThan(500);
        }
    }

    @Test
    @DisplayName("Content negotiation: unsupported formats do not break the API")
    void contentNegotiationHandled() {
        for (String accept : List.of("application/xml", "text/html",
                "application/vnd.api+json", "text/csv", "image/png")) {
            actor.attemptsTo(Get.resource("/characters/1").withHeaders(Map.of("Accept", accept)));
            int status = actor.asksFor(statusCode());
            assertThat(status).as("Accept=%s", accept).isLessThan(500);
        }

        actor.attemptsTo(Get.resource("/characters/1").withHeaders(Map.of("Accept", "application/json")));
        actor.should(thatTheStatusCode().isEqualTo(200));
    }

    @Test
    @DisplayName("Malformed or extreme headers are tolerated without 5xx")
    void malformedHeadersTolerated() {
        Map<String, String> cases = Map.of(
                "Accept-Language", "a;b;c;d;e;;;;;;;;;;;;;;;;;;;;;;;",
                "X-Forwarded-For", "1.2.3.4, 5.6.7.8, ;;;;",
                "User-Agent", "Mozilla/5.0 (" + "x".repeat(2000) + ")",
                "Accept-Encoding", "gzip, deflate, br, " + "zz".repeat(100)
        );

        for (Map.Entry<String, String> entry : cases.entrySet()) {
            actor.attemptsTo(Get.resource("/characters/1").withHeaders(Map.of(entry.getKey(), entry.getValue())));
            int status = actor.asksFor(statusCode());
            assertThat(status).as("malformed header %s", entry.getKey()).isLessThan(500);
        }
    }

    @Test
    @DisplayName("Double slashes, trailing slash and encoding do not break")
    void unusualPathsHandled() {
        for (String path : List.of("/characters//1", "/characters/1/", "/characters/%20",
                "/characters/%2e%2e", "/characters/1%2F2", "//characters/1", "/characters/1?page=1")) {
            actor.attemptsTo(FetchResource.named(path));
            int status = actor.asksFor(statusCode());
            assertThat(status).as("path=%s", path).isLessThan(500);
        }
    }

    @Test
    @DisplayName("Unicode characters in the path are handled without 5xx")
    void unicodePathsHandled() {
        for (String path : List.of("/characters/ñoño", "/characters/日本",
                "/characters/üñíçödé", "/characters/αβγ")) {
            actor.attemptsTo(FetchResource.named(path));
            int status = actor.asksFor(statusCode());
            assertThat(status).as("path=%s", path).isLessThan(500);
        }
    }

    private static int fuzzIterations() {
        return ApiConfig.intValue("fuzz.iterations", 10);
    }
}
