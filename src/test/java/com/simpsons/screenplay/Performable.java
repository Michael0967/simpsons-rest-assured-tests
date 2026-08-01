package com.simpsons.screenplay;

/**
 * Anything an {@link Actor} can do, such as a {@link Task} or an
 * {@link Interaction}.
 */
@FunctionalInterface
public interface Performable {

    void performAs(Actor actor);
}
