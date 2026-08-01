package com.simpsons.screenplay;

/**
 * A consumer that is allowed to throw checked exceptions, so callbacks can be
 * written without try/catch noise.
 *
 * @param <T> the type of the accepted value
 */
@FunctionalInterface
public interface ThrowingConsumer<T> {

    void accept(T value) throws Exception;
}
