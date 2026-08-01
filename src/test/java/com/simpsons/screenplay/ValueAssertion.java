package com.simpsons.screenplay;

import org.assertj.core.api.ObjectAssert;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;

import java.util.Arrays;

/**
 * Fluent builder of {@link Consequence}s backed by AssertJ. Every method
 * returns a {@link Consequence} ready to be passed to
 * {@link Actor#should(Consequence...)}.
 *
 * @param <T> the type of the value being verified
 */
public final class ValueAssertion<T> {

    private final String description;
    private final ThrowingFunction<Actor, T> resolver;

    ValueAssertion(String description, ThrowingFunction<Actor, T> resolver) {
        this.description = description;
        this.resolver = resolver;
    }

    public Consequence isEqualTo(Object expected) {
        return actor -> described(assertObject(resolve(actor))).isEqualTo(expected);
    }

    public Consequence isNotEqualTo(Object unexpected) {
        return actor -> described(assertObject(resolve(actor))).isNotEqualTo(unexpected);
    }

    public Consequence isNull() {
        return actor -> described(assertObject(resolve(actor))).isNull();
    }

    public Consequence isNotNull() {
        return actor -> described(assertObject(resolve(actor))).isNotNull();
    }

    public Consequence isTrue() {
        return actor -> Assertions.assertThat(asBoolean(resolve(actor))).isTrue();
    }

    public Consequence isFalse() {
        return actor -> Assertions.assertThat(asBoolean(resolve(actor))).isFalse();
    }

    public Consequence isIn(Object... values) {
        return actor -> described(assertObject(resolve(actor))).isIn(values);
    }

    public Consequence matches(Matcher<? super T> matcher) {
        return actor -> {
            T actual = resolve(actor);
            if (description.isBlank()) {
                MatcherAssert.assertThat(actual, matcher);
            } else {
                MatcherAssert.assertThat(description, actual, matcher);
            }
        };
    }

    public Consequence satisfies(ThrowingConsumer<T> check) {
        return actor -> {
            try {
                check.accept(resolve(actor));
            } catch (Exception e) {
                throw new AssertionError(
                        description.isBlank() ? "Answer did not satisfy the check" : description, e);
            }
        };
    }

    public Consequence contains(Object... values) {
        return actor -> assertContains(resolve(actor), values);
    }

    public Consequence doesNotContain(Object... values) {
        return actor -> assertDoesNotContain(resolve(actor), values);
    }

    public Consequence hasSize(int size) {
        return actor -> assertHasSize(resolve(actor), size);
    }

    public Consequence isLessThan(long bound) {
        return actor -> Assertions.assertThat(asLong(resolve(actor))).isLessThan(bound);
    }

    public Consequence isLessThanOrEqualTo(long bound) {
        return actor -> Assertions.assertThat(asLong(resolve(actor))).isLessThanOrEqualTo(bound);
    }

    public Consequence isGreaterThan(long bound) {
        return actor -> Assertions.assertThat(asLong(resolve(actor))).isGreaterThan(bound);
    }

    public Consequence startsWith(String prefix) {
        return actor -> Assertions.assertThat(asString(resolve(actor))).startsWith(prefix);
    }

    public Consequence isNotBlank() {
        return actor -> Assertions.assertThat(asString(resolve(actor))).isNotBlank();
    }

    public Consequence isNotEmpty() {
        return actor -> Assertions.assertThat(asString(resolve(actor))).isNotEmpty();
    }

    private T resolve(Actor actor) {
        try {
            return resolver.apply(actor);
        } catch (Exception e) {
            throw new ScreenplayException("Could not resolve " + description, e);
        }
    }

    private ObjectAssert<Object> described(ObjectAssert<Object> assertion) {
        return description.isBlank() ? assertion : assertion.as(description);
    }

    private ObjectAssert<Object> assertObject(Object actual) {
        return Assertions.assertThat((Object) actual);
    }

    private static void assertContains(Object actual, Object... values) {
        if (actual instanceof Iterable<?> iterable) {
            Assertions.assertThat((Iterable) iterable).contains(values);
        } else if (actual instanceof Object[] array) {
            Assertions.assertThat(array).contains(values);
        } else if (actual instanceof CharSequence text) {
            Assertions.assertThat(text.toString())
                    .contains(Arrays.stream(values).map(String::valueOf).toArray(String[]::new));
        } else {
            throw new IllegalArgumentException(
                    "contains() expects a collection, array or string, got: " + actual);
        }
    }

    private static void assertDoesNotContain(Object actual, Object... values) {
        if (actual == null) {
            return;
        }
        if (actual instanceof Iterable<?> iterable) {
            Assertions.assertThat((Iterable) iterable).doesNotContain(values);
        } else if (actual instanceof Object[] array) {
            Assertions.assertThat(array).doesNotContain(values);
        } else if (actual instanceof CharSequence text) {
            Assertions.assertThat(text.toString())
                    .doesNotContain(Arrays.stream(values).map(String::valueOf).toArray(String[]::new));
        } else {
            throw new IllegalArgumentException(
                    "doesNotContain() expects a collection, array or string, got: " + actual);
        }
    }

    private static void assertHasSize(Object actual, int size) {
        if (actual instanceof Iterable<?> iterable) {
            Assertions.assertThat((Iterable<?>) iterable).hasSize(size);
        } else if (actual instanceof Object[] array) {
            Assertions.assertThat(array).hasSize(size);
        } else if (actual instanceof CharSequence text) {
            Assertions.assertThat(text.toString()).hasSize(size);
        } else {
            throw new IllegalArgumentException(
                    "hasSize() expects a collection, array or string, got: " + actual);
        }
    }

    private static long asLong(Object actual) {
        if (actual instanceof Number number) {
            return number.longValue();
        }
        if (actual instanceof CharSequence text) {
            return Long.parseLong(text.toString());
        }
        throw new IllegalArgumentException("Expected a numeric value, got: " + actual);
    }

    private static boolean asBoolean(Object actual) {
        if (actual instanceof Boolean bool) {
            return bool;
        }
        throw new IllegalArgumentException("Expected a boolean, got: " + actual);
    }

    private static String asString(Object actual) {
        return actual == null ? null : actual.toString();
    }
}
