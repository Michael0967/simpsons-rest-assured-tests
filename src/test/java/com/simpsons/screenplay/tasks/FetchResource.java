package com.simpsons.screenplay.tasks;

import com.simpsons.screenplay.Actor;
import com.simpsons.screenplay.Task;
import com.simpsons.screenplay.abilities.ApiAbility;

/**
 * Fetches an arbitrary resource, optionally with path params, and stores the
 * response in the actor's {@link ApiAbility}. Used for error, security and
 * fuzz scenarios that exercise arbitrary paths.
 */
public class FetchResource implements Task {

    private final String resource;
    private Object[] pathParams = new Object[0];

    private FetchResource(String resource) {
        this.resource = resource;
    }

    public static FetchResource named(String resource) {
        return new FetchResource(resource);
    }

    public FetchResource withPathParams(Object... params) {
        this.pathParams = params;
        return this;
    }

    @Override
    public void performAs(Actor actor) {
        actor.using(ApiAbility.class).perform(client -> client.get(resource, pathParams));
    }
}
