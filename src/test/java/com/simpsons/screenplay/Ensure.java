package com.simpsons.screenplay;

import com.simpsons.screenplay.questions.TheResponse;
import io.restassured.response.Response;

/**
 * Entry point for building readable consequences, e.g.
 * <pre>
 * actor.should(
 *     thatTheStatusCode().isEqualTo(200),
 *     that(TheResponse.bodyField("name")).isEqualTo("Homer Simpson")
 * );
 * </pre>
 */
public final class Ensure {

    private Ensure() {
    }

    public static <T> ValueAssertion<T> that(Question<T> question) {
        return new ValueAssertion<>("", actor -> question.answerAs(actor));
    }

    public static <T> ValueAssertion<T> thatValue(T value) {
        return new ValueAssertion<>("", actor -> value);
    }

    public static <T> Consequence that(Question<T> question, ThrowingConsumer<T> check) {
        return actor -> {
            try {
                check.accept(question.answerAs(actor));
            } catch (Exception e) {
                throw new AssertionError("Answer did not satisfy the check", e);
            }
        };
    }

    public static ValueAssertion<Integer> thatTheStatusCode() {
        return that(TheResponse.statusCode());
    }

    public static ValueAssertion<Response> thatTheResponse() {
        return that(TheResponse.status());
    }
}
