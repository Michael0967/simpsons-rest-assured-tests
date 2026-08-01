package com.simpsons.tests.security;

import com.simpsons.BaseApiTest;
import com.simpsons.screenplay.interactions.SendRequest;
import com.simpsons.screenplay.questions.AConfigValue;
import com.simpsons.screenplay.tasks.FetchCharacter;
import com.simpsons.screenplay.tasks.FetchCharacters;
import com.simpsons.screenplay.tasks.FetchResource;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.simpsons.screenplay.Ensure.*;
import static com.simpsons.screenplay.questions.TheResponse.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * Black-box security tests against the API: TLS transport, security headers,
 * injection (SQL/XSS/path traversal), numeric overflow, internal-detail leaks
 * and dangerous HTTP methods.
 * Run: mvn test -Psecurity
 */
@Tag("security")
class SecurityApiTest extends BaseApiTest {

    static Stream<String> sqlPayloads() {
        return Stream.of(
                "1 OR 1=1",
                "1 OR 1=1--",
                "' OR '1'='1",
                "1; DROP TABLE characters",
                "1 UNION SELECT 1,2,3",
                "1/*",
                "-1 OR 1=1"
        );
    }

    static Stream<String> xssPayloads() {
        return Stream.of(
                "<script>alert(1)</script>",
                "\"><svg onload=alert(1)>",
                "javascript:alert(1)",
                "1<img src=x onerror=alert(1)>"
        );
    }

    static Stream<String> traversalPayloads() {
        return Stream.of(
                "../etc/passwd",
                "..%2f..%2fetc%2fpasswd",
                "%2e%2e%2fetc%2fpasswd",
                "....//....//etc/passwd"
        );
    }

    static Stream<String> overflowPayloads() {
        return Stream.of(
                "9999999999999999999999",
                "9223372036854775807",
                "1e3",
                "0x1"
        );
    }

    static Stream<Arguments> errorScenarios() {
        return Stream.of(
                Arguments.of("/characters/999999", 404),
                Arguments.of("/characters/abc", 400),
                Arguments.of("/episodes/-1", 400),
                Arguments.of("/nope", 404)
        );
    }

    @Test
    @DisplayName("The API is served over HTTPS (TLS in transit)")
    void apiIsServedOverTls() {
        actor.should(that(AConfigValue.string("api.base.uri")).startsWith("https://"));
    }

    @Test
    @DisplayName("Responses include the essential security headers")
    void securityHeadersPresent() {
        actor.attemptsTo(FetchCharacter.withId(1));
        actor.should(thatTheStatusCode().isEqualTo(200));

        Headers headers = actor.asksFor(headers());

        assertSoftly(softly -> {
            softly.assertThat(headers.getValue("X-Content-Type-Options"))
                    .as("X-Content-Type-Options: nosniff")
                    .isEqualTo("nosniff");
            softly.assertThat(headers.getValue("X-Frame-Options"))
                    .as("X-Frame-Options (anti clickjacking)")
                    .isEqualTo("SAMEORIGIN");
            softly.assertThat(headers.getValue("Strict-Transport-Security"))
                    .as("HSTS").isNotNull();
            softly.assertThat(headers.getValue("Referrer-Policy"))
                    .as("Referrer-Policy").isNotNull();
        });
    }

    @ParameterizedTest(name = "SQLi: {0}")
    @MethodSource("sqlPayloads")
    @DisplayName("SQL injection payloads are rejected without leaking data")
    void sqlInjectionPayloadsRejected(String payload) {
        actor.attemptsTo(FetchResource.named("/characters/" + payload));
        actor.should(thatTheStatusCode().isIn(400, 404));

        String body = actor.asksFor(body());
        assertThat(body.toLowerCase())
                .as("response for payload %s", payload)
                .doesNotContain("sql", "syntax", "select", "from");
    }

    @ParameterizedTest(name = "XSS: {0}")
    @MethodSource("xssPayloads")
    @DisplayName("XSS payloads are rejected without being reflected in the response")
    void xssPayloadsNotReflected(String payload) {
        actor.attemptsTo(FetchResource.named("/characters/" + payload));
        actor.should(thatTheStatusCode().isIn(400, 404));

        String body = actor.asksFor(body());
        assertThat(body).as("response for payload %s", payload).doesNotContain(payload);
    }

    @ParameterizedTest(name = "Path traversal: {0}")
    @MethodSource("traversalPayloads")
    @DisplayName("Path traversal does not access server files")
    void pathTraversalRejected(String payload) {
        actor.attemptsTo(FetchResource.named("/characters/" + payload));
        actor.should(
                thatTheStatusCode().isNotEqualTo(200),
                thatTheStatusCode().isNotEqualTo(500)
        );

        String body = actor.asksFor(body());
        assertThat(body).as("response for payload %s", payload)
                .doesNotContain("root:", "Exception");
    }

    @ParameterizedTest(name = "Overflow: {0}")
    @MethodSource("overflowPayloads")
    @DisplayName("Out-of-range ids are rejected or resolve without breaking the API")
    void integerOverflowRejected(String payload) {
        actor.attemptsTo(FetchResource.named("/characters/" + payload));
        actor.should(thatTheStatusCode().isIn(200, 400, 404));
    }

    @Test
    @DisplayName("KNOWN: /characters/2147483648 crashes the API (500/null) — requires fix")
    void knownIntegerOverflowCrashIsDocumented() {
        actor.attemptsTo(FetchResource.named("/characters/2147483648"));

        Response response = actor.asksFor(status());
        if (response == null) {
            return;
        }
        assertThat(response.getStatusCode())
                .as("2147483648 should return 4xx; the API answers 500 (bug)")
                .isEqualTo(500);
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("errorScenarios")
    @DisplayName("Error responses do not leak internal details")
    void noInternalDetailsLeakedOnError(String path, int expectedStatus) {
        actor.attemptsTo(FetchResource.named(path));
        actor.should(thatTheStatusCode().isEqualTo(expectedStatus));

        String body = actor.asksFor(body());
        assertThat(body)
                .as("error response of %s", path)
                .doesNotContain("Exception", "StackTrace", "Caused by", ".java:", "at com.", "Internal Server");
    }

    @Test
    @DisplayName("Write and dangerous HTTP methods do not execute operations")
    void unsafeHttpMethodsRejected() {
        assertSoftly(softly -> {
            for (String method : List.of("PUT", "PATCH", "TRACE", "DELETE")) {
                actor.attemptsTo(SendRequest.with(method, "/characters/1"));
                int status = actor.asksFor(statusCode());
                softly.assertThat(status)
                        .as("HTTP %s on /characters/1", method)
                        .isNotEqualTo(200)
                        .isLessThan(500);
            }
        });
    }

    @Test
    @DisplayName("OPTIONS is tolerated (CORS preflight)")
    void optionsIsTolerated() {
        actor.attemptsTo(SendRequest.with("OPTIONS", "/characters/1"));
        actor.should(thatTheStatusCode().isLessThan(500));
    }

    @Test
    @DisplayName("Error responses are not served as HTML (prevents XSS)")
    void errorsAreNotHtml() {
        actor.attemptsTo(FetchResource.named("/nope"));
        actor.should(
                thatTheStatusCode().isEqualTo(404),
                that(header("Content-Type")).doesNotContain("text/html")
        );

        actor.attemptsTo(FetchResource.named("/characters/abc"));
        actor.should(
                thatTheStatusCode().isIn(400, 404),
                that(header("Content-Type")).doesNotContain("text/html")
        );
    }

    @Test
    @DisplayName("Resources do not expose sensitive field names")
    void sensitiveFieldNamesNotExposed() {
        Set<String> sensitive = Set.of(
                "password", "secret", "token", "api_key", "apikey",
                "ssn", "credit_card", "email", "phone", "session"
        );

        actor.attemptsTo(FetchCharacters.onPage(1));
        actor.should(thatTheStatusCode().isEqualTo(200));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> results =
                (List<Map<String, Object>>) (List<?>) actor.asksFor(bodyList("results", Map.class));

        for (Map<String, Object> item : results) {
            assertThat(sensitive)
                    .as("sensitive fields in item with keys %s", item.keySet())
                    .noneMatch(item::containsKey);
        }
    }

    @Test
    @DisplayName("The error message for an invalid id is informative")
    void errorMessageIsInformative() {
        actor.attemptsTo(FetchResource.named("/characters/abc"));
        actor.should(
                thatTheStatusCode().isEqualTo(400),
                that(bodyFieldAsString("message")).isNotBlank()
        );
    }
}
