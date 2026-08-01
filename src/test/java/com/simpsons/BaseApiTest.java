package com.simpsons;

import com.simpsons.screenplay.Actor;
import com.simpsons.screenplay.abilities.ApiAbility;

/**
 * Base class for every suite. Each test method gets its own Screenplay
 * {@link Actor} wired to the Simpsons API. The actor is an instance field (not
 * static) so tests stay isolated under surefire parallel execution.
 */
public abstract class BaseApiTest {

    protected final Actor actor = Actor.named("QA Engineer")
            .whoCan(ApiAbility.callingTheSimpsonsApi());
}
