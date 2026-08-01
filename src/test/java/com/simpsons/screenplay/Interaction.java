package com.simpsons.screenplay;

/**
 * A single, low-level action performed by an {@link Actor}, such as a single
 * HTTP request. Unlike a {@link Task}, an interaction is not business-meaningful
 * on its own.
 */
@FunctionalInterface
public interface Interaction extends Performable {
}
