package com.simpsons.tests.errorhandling;

import com.simpsons.BaseApiTest;
import com.simpsons.models.ErrorResponse;
import com.simpsons.screenplay.interactions.Delete;
import com.simpsons.screenplay.interactions.Post;
import com.simpsons.screenplay.tasks.FetchResource;
import com.simpsons.validation.ApiSchemaValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.simpsons.screenplay.Ensure.*;
import static com.simpsons.screenplay.questions.TheResponse.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Behaviour under errors: unknown ids, invalid formats, unsupported methods
 * and unknown routes.
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
    @DisplayName("An unknown id returns 404 with an error structure")
    void unknownIdReturnsStructured404(String resource, String expectedMessage) {
        actor.attemptsTo(FetchResource.named(resource + "/999999"));

        actor.should(
                thatTheStatusCode().isEqualTo(404),
                that(bodyFieldAsString("error")).isEqualTo("Not Found"),
                that(bodyField("statusCode")).isEqualTo(404),
                that(bodyFieldAsString("message")).isEqualTo(expectedMessage)
        );

        ApiSchemaValidator.validate(actor.asksFor(status()), "error.json").statusCode(404);
    }

    @ParameterizedTest(name = "/characters/{0} -> 400")
    @MethodSource("invalidIdFormats")
    @DisplayName("A non-numeric id returns 400")
    void nonNumericIdReturns400(String invalidId) {
        actor.attemptsTo(FetchResource.named("/characters/" + invalidId));

        actor.should(
                thatTheStatusCode().isEqualTo(400),
                that(bodyFieldAsString("error")).isEqualTo("Bad Request"),
                that(bodyField("statusCode")).isEqualTo(400),
                that(bodyFieldAsString("message")).isEqualTo("Validation failed (numeric string is expected)")
        );

        ApiSchemaValidator.validate(actor.asksFor(status()), "error.json").statusCode(400);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("resources")
    @DisplayName("An unknown id deserializes into an error POJO")
    void errorResponseDeserializesIntoPojo(String resource, String expectedMessage) {
        actor.attemptsTo(FetchResource.named(resource + "/999999"));

        ErrorResponse error = actor.asksFor(as(ErrorResponse.class));

        assertThat(error.getError()).isEqualTo("Not Found");
        assertThat(error.getStatusCode()).isEqualTo(404);
        assertThat(error.getMessage()).isEqualTo(expectedMessage);
    }

    @ParameterizedTest(name = "POST {0}")
    @MethodSource("resourcePaths")
    @DisplayName("Unsupported methods return 404")
    void unsupportedMethodsReturn404(String resource) {
        actor.attemptsTo(Post.to(resource));
        actor.should(thatTheStatusCode().isEqualTo(404));

        actor.attemptsTo(Delete.resource(resource, 1));
        actor.should(thatTheStatusCode().isEqualTo(404));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("unknownRoutes")
    @DisplayName("Unknown routes return 404")
    void unknownRoutesReturn404(String route) {
        actor.attemptsTo(FetchResource.named(route));
        actor.should(thatTheStatusCode().isEqualTo(404));
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
