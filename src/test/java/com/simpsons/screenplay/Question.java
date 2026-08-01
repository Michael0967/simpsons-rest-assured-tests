package com.simpsons.screenplay;

/**
 * A question an {@link Actor} can ask, producing an answer of type
 * {@code T}. Answers are usually consumed by {@link Consequence}s.
 *
 * @param <T> the type of the answer
 */
@FunctionalInterface
public interface Question<T> {

    T answerAs(Actor actor);
}
