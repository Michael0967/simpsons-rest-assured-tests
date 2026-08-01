package com.simpsons.screenplay;

/**
 * A function that is allowed to throw checked exceptions, used internally for
 * lazy resolution of values and questions.
 *
 * @param <T> the input type
 * @param <R> the result type
 */
@FunctionalInterface
public interface ThrowingFunction<T, R> {

    R apply(T value) throws Exception;
}
