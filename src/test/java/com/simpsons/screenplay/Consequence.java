package com.simpsons.screenplay;

/**
 * A verifiable expectation about the outcome of an {@link Actor}'s actions.
 * Consequence failures are reported as assertion errors.
 */
@FunctionalInterface
public interface Consequence {

    void verifyAs(Actor actor);
}
