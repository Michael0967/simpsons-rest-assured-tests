package com.simpsons.screenplay;

/**
 * A high-level, business-meaningful unit of work performed by an
 * {@link Actor}, e.g. "fetch the character with id 1". Tasks usually wrap one
 * or more {@link Interaction}s.
 */
@FunctionalInterface
public interface Task extends Performable {
}
